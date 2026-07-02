from pydantic import BaseModel, EmailStr
from typing import Optional, List
from datetime import datetime


# User schemas
class UserCreate(BaseModel):
    username: str
    email: str
    password: str


class UserLogin(BaseModel):
    username: str
    password: str


class UserResponse(BaseModel):
    id: int
    username: str
    email: str
    avatar_url: Optional[str] = None
    created_at: datetime

    class Config:
        from_attributes = True


class Token(BaseModel):
    access_token: str
    token_type: str


# PromoCode schemas
class PromoCodeCreate(BaseModel):
    code: str
    title: str
    description: Optional[str] = None
    service: str
    discount: Optional[str] = None
    expires_at: Optional[datetime] = None
    source_url: Optional[str] = None


class PromoCodeUpdate(BaseModel):
    code: Optional[str] = None
    title: Optional[str] = None
    description: Optional[str] = None
    service: Optional[str] = None
    discount: Optional[str] = None
    expires_at: Optional[datetime] = None
    is_active: Optional[bool] = None


class PromoCodeResponse(BaseModel):
    id: int
    code: str
    title: str
    description: Optional[str] = None
    service: str
    discount: Optional[str] = None
    expires_at: Optional[datetime] = None
    is_active: bool
    rating: float
    usage_count: int
    source_url: Optional[str] = None
    created_at: datetime
    updated_at: datetime
    is_favorited: bool = False
    works_count: int = 0
    not_working_count: int = 0
    # Голос текущего пользователя: True — работает, False — не работает, None — не голосовал
    user_vote: Optional[bool] = None

    class Config:
        from_attributes = True


class VoteRequest(BaseModel):
    is_working: bool


class VoteResponse(BaseModel):
    detail: str
    works_count: int
    not_working_count: int
    # True, если после этого голоса промокод был скрыт у всех
    removed: bool


# Favorite schemas
class FavoriteResponse(BaseModel):
    id: int
    promocode_id: int
    created_at: datetime

    class Config:
        from_attributes = True


# History schemas
class HistoryResponse(BaseModel):
    id: int
    promocode_id: int
    viewed_at: datetime
    promocode: Optional[PromoCodeResponse] = None

    class Config:
        from_attributes = True
