# Run the backend WITHOUT Docker: Python + SQLite. Handy when Docker is not installed.
# Run (from anywhere):
#   powershell -ExecutionPolicy Bypass -File C:\Users\<you>\Documents\parser\backend\run-local.ps1
# Server: http://localhost:8000  (Swagger: http://localhost:8000/docs)
# Database is the file backend\promohub.db. To re-seed promo codes, delete that file and run again.

$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot

# 1) venv
if (-not (Test-Path ".venv")) {
    Write-Host "==> Creating virtual environment..." -ForegroundColor Cyan
    python -m venv .venv
}

# 2) dependencies (fast if already installed)
Write-Host "==> Checking dependencies..." -ForegroundColor Cyan
.\.venv\Scripts\python.exe -m pip install -q --disable-pip-version-check -r requirements-local.txt

# 3) run on SQLite
$env:DATABASE_URL = "sqlite:///./promohub.db"
Write-Host "==> Server: http://localhost:8000  (Swagger: http://localhost:8000/docs)" -ForegroundColor Green
Write-Host "    Stop with Ctrl+C. Keep this window open during the demo." -ForegroundColor Yellow
.\.venv\Scripts\python.exe -m uvicorn app.main:app --host 0.0.0.0 --port 8000
