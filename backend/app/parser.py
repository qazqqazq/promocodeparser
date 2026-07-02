# -*- coding: utf-8 -*-
"""
Парсер промокодов сервисов доставки еды/продуктов из публичных Telegram-каналов.

Читает веб-версию каналов (t.me/s/<channel>) — без токенов и Telegram API.
Из сообщений извлекает промокоды, определяет сервис доставки, скидку и заголовок,
отфильтровывает рекламный мусор и платные «промокоды»-разводы, дедуплицирует и
сохраняет новые коды в БД.
"""
import re
import logging
from datetime import datetime, timedelta
from typing import List, Dict, Optional

import requests

logger = logging.getLogger(__name__)

# Публичные каналы-агрегаторы с реальными бесплатными промокодами
TELEGRAM_CHANNELS = [
    # Только ЖИВЫЕ каналы (постят регулярно). Мёртвые дают протухшие коды —
    # их убрали. Добавляй сюда только активные (проверяй дату последнего поста).
    "https://t.me/s/skidki",
    "https://t.me/s/halyavshiki",
    "https://t.me/s/promokod_samokat_dostavka",
    "https://t.me/s/yandeks_market1",
]

# Сервисы доставки еды/продуктов. Ключи — стеммы в нижнем регистре (ё заменяется на е).
# Порядок важен: более специфичные варианты раньше.
FOOD_SERVICE_KEYWORDS = {
    "Яндекс Лавка": ["яндекс лавка"],
    "Яндекс Еда": ["яндекс еда", "яндекс.еда", "яндекс-еда", "yandex eda"],
    "Самокат": ["самокат", "samokat"],
    "Delivery Club": ["delivery club", "деливери клаб", "деливери"],
    "ВкусВилл": ["вкусвилл", "вкус вилл", "vkusvill"],
    "СберМаркет": ["сбермаркет", "сбер маркет", "sbermarket"],
    "Купер": ["купер"],
    "Пятёрочка Доставка": ["пятерочк"],
    "KFC": ["kfc", "кфс"],
    "Папа Джонс": ["папа джонс", "papa john", "пападжонс"],
    "Domino's Pizza": ["domino", "доминос"],
    "FARFOR": ["farfor", "фарфор"],
}

# Признаки мусора: реклама-разводы, платные коды, вакансии, ставки
SPAM_MARKERS = [
    "стоимость промокода", "цена промокода", "промокод платный", "писать в лс",
    "куплю аккаунт", "продажа аккаунт", "продажа аккаунтов", "букмекер", "казино",
    "1win", "1вин", "ставк", "реферал", "курьер", "подработ", "ваканси",
    "self-destruct", "линк продается", "channel created",
]

DISCOUNT_HINT = re.compile(r'скидк|кэшбэк|кешбэк|%|₽|руб|бесплат|подар|дней|мес', re.I)
DISCOUNT_RE = re.compile(r'(\d[\d\s]*\s*%|\d[\d\s]*\s*₽|\d[\d\s]*\s*руб)', re.I)
LABEL_RE = re.compile(
    r'(?:промокод\w*|промо-код|купон\w*|\bкод\b|coupon)\s*[:\-–—>]*\s*([A-Za-z0-9][A-Za-z0-9\-]{3,24})',
    re.I,
)
LINE_CODE_RE = re.compile(r'^([A-Za-z0-9][A-Za-z0-9\-]{4,24})\s*[-–—:]\s*(.+)$')
STRIP_CHARS = " \t•—–->*#✔️❗️❕🔥🎟➡🪼🔘🔵🔴🟢🟡👉🎁💚⚡️🤑💸💰🏷️"


class TelegramParser:
    def __init__(self):
        self.session = requests.Session()
        self.session.headers.update({
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                          "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120 Safari/537.36"
        })

    def fetch_channel_messages(self, channel_url: str) -> List[str]:
        try:
            response = self.session.get(channel_url, timeout=15)
            response.raise_for_status()
            return self._extract_messages(response.text)
        except Exception as e:
            logger.error(f"Ошибка при загрузке канала {channel_url}: {e}")
            return []

    @staticmethod
    def _extract_messages(html: str) -> List[str]:
        messages = []
        for m in re.findall(r'tgme_widget_message_text[^>]*>(.*?)</div>', html, re.DOTALL):
            t = re.sub(r"<br\s*/?>", "\n", m)
            t = re.sub(r"<[^>]+>", "", t)
            t = (t.replace("&amp;", "&").replace("&#33;", "!")
                  .replace("&quot;", '"').replace("&#39;", "'"))
            t = t.strip()
            if t:
                messages.append(t)
        return messages

    @staticmethod
    def detect_service(low: str) -> Optional[str]:
        for service, keywords in FOOD_SERVICE_KEYWORDS.items():
            if any(k in low for k in keywords):
                return service
        return None

    @staticmethod
    def _looks_like_code(tok: str) -> bool:
        if not (4 <= len(tok) <= 25):
            return False
        if not re.search(r'[A-Za-z]', tok):
            return False
        if tok.lower() in ("first", "shop", "promo", "sale", "http", "https"):
            return False
        return True

    @staticmethod
    def _clean(line: str) -> str:
        return re.sub(r'\s+', ' ', line.strip(STRIP_CHARS)).strip()

    def _message_title(self, text: str) -> Optional[str]:
        # предпочитаем содержательную строку со скидкой
        for l in text.splitlines():
            c = self._clean(l)
            if len(c) >= 15 and DISCOUNT_HINT.search(c):
                return c[:120]
        for l in text.splitlines():
            c = self._clean(l)
            if len(c) >= 15:
                return c[:120]
        return None

    def _find_code(self, line: str) -> Optional[str]:
        m = LABEL_RE.search(line)
        if m and self._looks_like_code(m.group(1)):
            return m.group(1)
        m2 = LINE_CODE_RE.match(line.strip(STRIP_CHARS))
        if m2 and self._looks_like_code(m2.group(1)) and DISCOUNT_HINT.search(m2.group(2)):
            return m2.group(1)
        return None

    def parse_message(self, text: str, source_url: str) -> List[Dict]:
        low = text.lower().replace("ё", "е")
        if any(s in low for s in SPAM_MARKERS):
            return []
        service = self.detect_service(low)
        if not service:
            return []

        d = DISCOUNT_RE.search(text)
        msg_discount = re.sub(r'\s+', '', d.group(1)) if d else None
        msg_title = self._message_title(text)

        results, seen = [], set()
        for raw in text.splitlines():
            line = raw.strip()
            if not line:
                continue
            code = self._find_code(line)
            if not code or code.lower() in seen:
                continue
            seen.add(code.lower())

            dl = DISCOUNT_RE.search(line)
            discount = re.sub(r'\s+', '', dl.group(1)) if dl else msg_discount

            desc = self._clean(re.sub(re.escape(code), '', line))
            desc = re.sub(r'^(промокод\w*|промо-код|купон\w*|код|coupon)[:\s]*', '', desc, flags=re.I)
            desc = desc.strip(STRIP_CHARS)
            title = desc if len(desc) >= 12 else (msg_title or f"Промокод {service}")
            if title:
                title = title[0].upper() + title[1:]

            results.append({
                "code": code,
                "title": title[:120],
                "description": text[:500],
                "service": service,
                "discount": discount,
                "expires_at": datetime.utcnow() + timedelta(days=30),
                "source_url": source_url,
            })
        return results

    def parse_all_channels(self) -> List[Dict]:
        all_promos = []
        for channel_url in TELEGRAM_CHANNELS:
            source_url = channel_url.replace("/s/", "/")
            for message in self.fetch_channel_messages(channel_url):
                all_promos.extend(self.parse_message(message, source_url))
        return all_promos


def run_parser() -> Dict[str, int]:
    """Запускает парсинг и сохраняет новые промокоды. Возвращает статистику."""
    from .database import SessionLocal
    from .models import PromoCode

    parser = TelegramParser()
    promos = parser.parse_all_channels()

    db = SessionLocal()
    added = 0
    try:
        existing_codes = {c for (c,) in db.query(PromoCode.code).all()}
        for promo_data in promos:
            code = promo_data["code"]
            if code in existing_codes:
                continue
            existing_codes.add(code)
            db.add(PromoCode(**promo_data))
            added += 1
        db.commit()
    finally:
        db.close()

    logger.info(f"Парсер: найдено {len(promos)}, добавлено новых {added}")
    return {"found": len(promos), "added": added}
