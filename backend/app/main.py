from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from apscheduler.schedulers.background import BackgroundScheduler

from .database import engine, SessionLocal, Base
from .routers import auth, promocodes
from .crud import hide_old_promocodes
from .seed import seed_data
from .parser import run_parser

app = FastAPI(
    title="PromoHub API",
    description="API для приложения с промокодами",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(auth.router)
app.include_router(promocodes.router)

scheduler = BackgroundScheduler()


def scheduled_hide_old():
    db = SessionLocal()
    try:
        hide_old_promocodes(db)
    finally:
        db.close()


@app.on_event("startup")
def startup():
    Base.metadata.create_all(bind=engine)
    db = SessionLocal()
    try:
        seed_data(db)
    finally:
        db.close()
    scheduler.add_job(scheduled_hide_old, "interval", hours=24)
    scheduler.add_job(run_parser, "interval", hours=6)
    scheduler.start()


@app.on_event("shutdown")
def shutdown():
    scheduler.shutdown()


@app.get("/")
def root():
    return {"message": "PromoHub API работает"}


@app.get("/health")
def health_check():
    return {"status": "ok"}
