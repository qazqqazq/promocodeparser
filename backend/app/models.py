from sqlalchemy import (
    Column, Integer, String, Text, Boolean,
    DateTime, ForeignKey, Float, Enum, UniqueConstraint
)
from sqlalchemy.orm import relationship
from sqlalchemy.sql import func
import enum

from .database import Base


class ServiceName(str, enum.Enum):
    SAMOKAT = "Самокат"
    YANDEX_EDA = "Яндекс Еда"
    DELIVERY_CLUB = "Delivery Club"
    VKUSVILL = "ВкусВилл"


class User(Base):
    __tablename__ = "users"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(50), unique=True, nullable=False)
    email = Column(String(100), unique=True, nullable=False)
    hashed_password = Column(String(255), nullable=False)
    avatar_url = Column(String(500), nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    favorites = relationship("Favorite", back_populates="user")
    history = relationship("History", back_populates="user")


class PromoCode(Base):
    __tablename__ = "promocodes"

    id = Column(Integer, primary_key=True, index=True)
    code = Column(String(50), nullable=False)
    title = Column(String(200), nullable=False)
    description = Column(Text, nullable=True)
    service = Column(String(50), nullable=False)
    discount = Column(String(50), nullable=True)
    expires_at = Column(DateTime(timezone=True), nullable=True)
    is_active = Column(Boolean, default=True)
    rating = Column(Float, default=0.0)
    usage_count = Column(Integer, default=0)
    source_url = Column(String(500), nullable=True)
    created_at = Column(DateTime(timezone=True), server_default=func.now())
    updated_at = Column(DateTime(timezone=True), server_default=func.now(), onupdate=func.now())

    favorites = relationship("Favorite", back_populates="promocode")
    history = relationship("History", back_populates="promocode")
    votes = relationship("PromoVote", back_populates="promocode", cascade="all, delete-orphan")


class PromoVote(Base):
    """Голос пользователя за промокод: работает ли он.

    Один пользователь может оставить только один голос на промокод
    (последний голос перезаписывает предыдущий). Когда два разных
    пользователя отмечают код как нерабочий, он скрывается у всех.
    """
    __tablename__ = "promo_votes"
    __table_args__ = (
        UniqueConstraint("user_id", "promocode_id", name="uq_user_promocode_vote"),
    )

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    promocode_id = Column(Integer, ForeignKey("promocodes.id"), nullable=False)
    is_working = Column(Boolean, nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    promocode = relationship("PromoCode", back_populates="votes")


class Favorite(Base):
    __tablename__ = "favorites"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    promocode_id = Column(Integer, ForeignKey("promocodes.id"), nullable=False)
    created_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", back_populates="favorites")
    promocode = relationship("PromoCode", back_populates="favorites")


class History(Base):
    __tablename__ = "history"

    id = Column(Integer, primary_key=True, index=True)
    user_id = Column(Integer, ForeignKey("users.id"), nullable=False)
    promocode_id = Column(Integer, ForeignKey("promocodes.id"), nullable=False)
    viewed_at = Column(DateTime(timezone=True), server_default=func.now())

    user = relationship("User", back_populates="history")
    promocode = relationship("PromoCode", back_populates="history")
