from sqlalchemy.orm import Session
from sqlalchemy import func
from datetime import datetime, timedelta

from . import models, schemas, auth

# Сколько разных пользователей должны отметить промокод как нерабочий,
# чтобы он был автоматически скрыт у всех.
NOT_WORKING_THRESHOLD = 2


# User CRUD
def get_user(db: Session, user_id: int):
    return db.query(models.User).filter(models.User.id == user_id).first()


def get_user_by_username(db: Session, username: str):
    return db.query(models.User).filter(models.User.username == username).first()


def get_user_by_email(db: Session, email: str):
    return db.query(models.User).filter(models.User.email == email).first()


def create_user(db: Session, user: schemas.UserCreate):
    hashed_password = auth.get_password_hash(user.password)
    db_user = models.User(
        username=user.username,
        email=user.email,
        hashed_password=hashed_password
    )
    db.add(db_user)
    db.commit()
    db.refresh(db_user)
    return db_user


# PromoCode CRUD
def _attach_vote_stats(db: Session, promocodes, user_id: int = None):
    """Проставляет works_count / not_working_count / user_vote для списка промокодов."""
    if not promocodes:
        return promocodes

    ids = [pc.id for pc in promocodes]

    # Считаем голоса одним запросом с группировкой
    rows = (
        db.query(
            models.PromoVote.promocode_id,
            models.PromoVote.is_working,
            func.count(models.PromoVote.id),
        )
        .filter(models.PromoVote.promocode_id.in_(ids))
        .group_by(models.PromoVote.promocode_id, models.PromoVote.is_working)
        .all()
    )
    works = {}
    not_working = {}
    for promo_id, is_working, count in rows:
        if is_working:
            works[promo_id] = count
        else:
            not_working[promo_id] = count

    user_votes = {}
    if user_id:
        for v in (
            db.query(models.PromoVote)
            .filter(
                models.PromoVote.user_id == user_id,
                models.PromoVote.promocode_id.in_(ids),
            )
            .all()
        ):
            user_votes[v.promocode_id] = v.is_working

    for pc in promocodes:
        pc.works_count = works.get(pc.id, 0)
        pc.not_working_count = not_working.get(pc.id, 0)
        pc.user_vote = user_votes.get(pc.id)

    return promocodes


def vote_promocode(db: Session, user_id: int, promocode_id: int, is_working: bool):
    """Сохраняет голос пользователя и скрывает код, если набралось
    NOT_WORKING_THRESHOLD голосов «не работает» от разных пользователей.

    Возвращает (works_count, not_working_count, removed) или None, если код не найден.
    """
    pc = db.query(models.PromoCode).filter(models.PromoCode.id == promocode_id).first()
    if not pc:
        return None

    vote = (
        db.query(models.PromoVote)
        .filter(
            models.PromoVote.user_id == user_id,
            models.PromoVote.promocode_id == promocode_id,
        )
        .first()
    )
    if vote:
        vote.is_working = is_working
    else:
        vote = models.PromoVote(
            user_id=user_id,
            promocode_id=promocode_id,
            is_working=is_working,
        )
        db.add(vote)
    db.commit()

    works_count = (
        db.query(func.count(models.PromoVote.id))
        .filter(
            models.PromoVote.promocode_id == promocode_id,
            models.PromoVote.is_working.is_(True),
        )
        .scalar()
    )
    not_working_count = (
        db.query(func.count(models.PromoVote.id))
        .filter(
            models.PromoVote.promocode_id == promocode_id,
            models.PromoVote.is_working.is_(False),
        )
        .scalar()
    )

    removed = False
    if not_working_count >= NOT_WORKING_THRESHOLD and pc.is_active:
        pc.is_active = False
        db.commit()
        removed = True

    return works_count, not_working_count, removed


def get_promocodes(
    db: Session,
    skip: int = 0,
    limit: int = 50,
    service: str = None,
    search: str = None,
    user_id: int = None
):
    query = db.query(models.PromoCode).filter(models.PromoCode.is_active == True)

    if service:
        query = query.filter(models.PromoCode.service == service)

    if search:
        query = query.filter(
            models.PromoCode.title.ilike(f"%{search}%") |
            models.PromoCode.code.ilike(f"%{search}%") |
            models.PromoCode.description.ilike(f"%{search}%")
        )

    query = query.order_by(models.PromoCode.created_at.desc())

    if skip:
        query = query.offset(skip)
    if limit:
        query = query.limit(limit)

    promocodes = query.all()

    if user_id:
        favorite_ids = {
            f.promocode_id for f in
            db.query(models.Favorite.promocode_id)
            .filter(models.Favorite.user_id == user_id)
            .all()
        }
        for pc in promocodes:
            pc.is_favorited = pc.id in favorite_ids

    _attach_vote_stats(db, promocodes, user_id)

    return promocodes


def get_promocode(db: Session, promocode_id: int, user_id: int = None):
    pc = db.query(models.PromoCode).filter(models.PromoCode.id == promocode_id).first()
    if pc and user_id:
        fav = db.query(models.Favorite).filter(
            models.Favorite.user_id == user_id,
            models.Favorite.promocode_id == promocode_id
        ).first()
        pc.is_favorited = fav is not None
    if pc:
        _attach_vote_stats(db, [pc], user_id)
    return pc


def create_promocode(db: Session, promocode: schemas.PromoCodeCreate):
    db_pc = models.PromoCode(**promocode.dict())
    db.add(db_pc)
    db.commit()
    db.refresh(db_pc)
    return db_pc


def update_promocode(db: Session, promocode_id: int, promocode: schemas.PromoCodeUpdate):
    db_pc = db.query(models.PromoCode).filter(models.PromoCode.id == promocode_id).first()
    if not db_pc:
        return None
    update_data = promocode.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_pc, key, value)
    db.commit()
    db.refresh(db_pc)
    return db_pc


def delete_promocode(db: Session, promocode_id: int):
    db_pc = db.query(models.PromoCode).filter(models.PromoCode.id == promocode_id).first()
    if db_pc:
        db.delete(db_pc)
        db.commit()
        return True
    return False


def hide_old_promocodes(db: Session):
    threshold = datetime.utcnow() - timedelta(days=30)
    db.query(models.PromoCode).filter(
        models.PromoCode.is_active == True,
        models.PromoCode.created_at < threshold
    ).update({"is_active": False})
    db.commit()


# Favorite CRUD
def add_favorite(db: Session, user_id: int, promocode_id: int):
    existing = db.query(models.Favorite).filter(
        models.Favorite.user_id == user_id,
        models.Favorite.promocode_id == promocode_id
    ).first()
    if existing:
        return existing

    fav = models.Favorite(user_id=user_id, promocode_id=promocode_id)
    db.add(fav)
    db.commit()
    db.refresh(fav)
    return fav


def remove_favorite(db: Session, user_id: int, promocode_id: int):
    fav = db.query(models.Favorite).filter(
        models.Favorite.user_id == user_id,
        models.Favorite.promocode_id == promocode_id
    ).first()
    if fav:
        db.delete(fav)
        db.commit()
        return True
    return False


def get_favorites(db: Session, user_id: int):
    return db.query(models.Favorite).filter(
        models.Favorite.user_id == user_id
    ).order_by(models.Favorite.created_at.desc()).all()


# History CRUD
def add_history(db: Session, user_id: int, promocode_id: int):
    existing = db.query(models.History).filter(
        models.History.user_id == user_id,
        models.History.promocode_id == promocode_id
    ).first()
    if existing:
        existing.viewed_at = datetime.utcnow()
        db.commit()
        db.refresh(existing)
        return existing

    hist = models.History(user_id=user_id, promocode_id=promocode_id)
    db.add(hist)
    db.commit()
    db.refresh(hist)
    return hist


def get_history(db: Session, user_id: int):
    return db.query(models.History).filter(
        models.History.user_id == user_id
    ).order_by(models.History.viewed_at.desc()).limit(50).all()


def clear_history(db: Session, user_id: int):
    db.query(models.History).filter(
        models.History.user_id == user_id
    ).delete()
    db.commit()
