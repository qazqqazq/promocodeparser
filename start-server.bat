@echo off
chcp 65001 >nul
title PromoHub server (постоянный адрес ngrok)
echo ============================================================
echo   PromoHub: сервер + ПОСТОЯННЫЙ адрес ngrok
echo ============================================================
echo.
echo Постоянный адрес (вставляется в приложение ОДИН раз):
echo   https://usual-splashing-slug.ngrok-free.dev
echo.
echo Открывается окно сервера — не закрывай его.
echo Это окно (туннель) тоже держи открытым во время демо.
echo.

REM Сервер в отдельном окне
start "PromoHub Server" cmd /k powershell -ExecutionPolicy Bypass -File "C:\Users\Public\promohub\backend\run-local.ps1"

echo Жду запуск сервера (8 секунд)...
timeout /t 8 /nobreak >nul

echo.
echo ====== Туннель на постоянный адрес запускается ======
"C:\Users\Public\promohub\ngrok.exe" http --url=https://usual-splashing-slug.ngrok-free.dev 8000
