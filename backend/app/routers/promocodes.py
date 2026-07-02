from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import Optional, List

from ..database import get_db
from ..models import User
from ..schemas import (
    PromoCodeCreate, PromoCodeUpdate,
    PromoCodeResponse, FavoriteResponse, HistoryResponse,
    VoteRequest, VoteResponse
)
from .. import crud, auth
from ..parser import run_parser

router = APIRouter(prefix="/api/promocodes", tags=["Промокоды"])


@router.get("/", response_model=List[PromoCodeResponse])
def list_promocodes(
    skip: int = Query(0, ge=0),
    limit: int = Query(50, ge=1, le=100),
    service: Optional[str] = None,
    search: Optional[str] = None,
    db: Session = Depends(get_db),
    current_user: User = Depends(auth.get_current_user)
):
    return crud.get_promocodes(
        db, skip=skip, limit=limit,
        service=service, search=search,
        user_id=current_user.id
    )


@router.post("/refresh")
def refresh_from_sources(
    current_user: User = Depends(auth.get_current_user)
):
    """Ручной запуск парсера промокодов из Telegram-каналов."""
    stats = run_parser()
    return {"detail": "Парсер выполнен", **stats}


@router.get("/{promocode_id}", response_model=PromoCodeResponse)
def get_promocode(
    promocode_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(auth.get_current_user)
):
    pc = crud.get_promocode(db, promocode_id, user_id=current_user.id)
    if not pc:
        raise HTTPException(status_code=404, detail="Промокод не найден")
    crud.add_history(db, current_user.id, promocode_id)
    return pc


@router.post("/", response_model=PromoCodeResponse)
def create_promocode(
    promocode: PromoCodeCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(auth.get_current_user)
):
    return crud.create_promocode(db, promocode)


@router.put("/{promocode_id}", response_model=PromoCodeResponse)
def update_promocode(
    promocode_id: int,
    promocode: PromoCodeUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(auth.get_current_user)
):
    updated = crud.update_promocode(db, promocode_id, promocode)
    if not updated:
        raise HTTPException(status_code=404, detail="Промокод не найден")
    return updated


@router.delete("/{promocode_id}")
def delete_promocode(
    promocode_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(auth.get_current_user)
):
    if not crud.delete_promocode(db, promocode_id):
        raise HTTPException(status_code=404, detail="Промокод не найден")
    return {"detail": "Промокод удалён"}


@router.post("/{promocode_id}/vote", response_model=VoteResponse)
def vote_promocode(
    promocode_id: int,
    vote: VoteRequest,
    db: Session = Depends(get_db),
    current_user: User = Depends(auth.get_current_user)
):
    result = crud.vote_promocode(
        db, current_user.id, promocode_id, vote.is_working
    )
    if result is None:
        raise HTTPException(status_code=404, detail="Промокод не найден")
    works_count, not_working_count, removed = result
    detail = (
        "Промокод скрыт: его отметили нерабочим несколько пользователей"
        if removed else "Голос учтён"
    )
    return VoteResponse(
        detail=detail,
        works_count=works_count,
        not_working_count=not_working_count,
        removed=removed,
    )


@router.post("/{promocode_id}/favorite")
def toggle_favorite(
    promocode_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(auth.get_current_user)
):
    from sqlalchemy import and_
    from ..models import Favorite

    fav = db.query(Favorite).filter(
        and_(
            Favorite.user_id == current_user.id,
            Favorite.promocode_id == promocode_id
        )
    ).first()

    if fav:
        crud.remove_favorite(db, current_user.id, promocode_id)
        return {"detail": "Удалено из избранного", "is_favorited": False}
    else:
        crud.add_favorite(db, current_user.id, promocode_id)
        return {"detail": "Добавлено в избранное", "is_favorited": True}


@router.get("/favorites/list", response_model=List[FavoriteResponse])
def list_favorites(
    db: Session = Depends(get_db),
    current_user: User = Depends(auth.get_current_user)
):
    return crud.get_favorites(db, current_user.id)


@router.get("/history/list", response_model=List[HistoryResponse])
def list_history(
    db: Session = Depends(get_db),
    current_user: User = Depends(auth.get_current_user)
):
    return crud.get_history(db, current_user.id)


@router.delete("/history/clear")
def clear_history(
    db: Session = Depends(get_db),
    current_user: User = Depends(auth.get_current_user)
):
    crud.clear_history(db, current_user.id)
    return {"detail": "История очищена"}
