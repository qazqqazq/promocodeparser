@echo off
chcp 65001 >nul
title PromoHub - tunnel
echo ============================================================
echo   PromoHub: запуск сервера и туннеля
echo ============================================================
echo.
echo 1) Открывается отдельное окно с сервером (не закрывай его).
echo 2) Здесь появится публичный адрес https://...trycloudflare.com
echo 3) Вставь этот адрес в приложении: Профиль -^> Настройки сервера.
echo.

REM Сервер в отдельном окне
start "PromoHub Server" cmd /k powershell -ExecutionPolicy Bypass -File "C:\Users\Public\promohub\backend\run-local.ps1"

echo Жду запуск сервера (8 секунд)...
timeout /t 8 /nobreak >nul

echo.
echo ====== АДРЕС ДЛЯ ПРИЛОЖЕНИЯ НИЖЕ (строка trycloudflare.com) ======
echo.
"C:\Users\Public\promohub\cloudflared.exe" tunnel --url http://localhost:8000
