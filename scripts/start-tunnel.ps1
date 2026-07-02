# Starts the backend in Docker and opens a public Cloudflare tunnel to http://localhost:8000.
# Usage (from the repo root):  powershell -ExecutionPolicy Bypass -File scripts\start-tunnel.ps1
#
# NOTE: This script uses Docker. If you do NOT have Docker, run the backend with
#       backend\run-local.ps1 instead, then run cloudflared manually (see docs/server_setup.md).
#
# Steps:
#   1) docker compose up -d (backend + DB);
#   2) wait until http://localhost:8000/health is ready;
#   3) run cloudflared and print the public https URL to paste into the app.

$ErrorActionPreference = "Stop"
$repoRoot = Split-Path -Parent $PSScriptRoot
Set-Location $repoRoot

Write-Host "==> Starting backend (docker compose up -d)..." -ForegroundColor Cyan
docker compose up -d --build

Write-Host "==> Waiting for http://localhost:8000/health ..." -ForegroundColor Cyan
$ready = $false
for ($i = 0; $i -lt 30; $i++) {
    try {
        $r = Invoke-RestMethod -Uri "http://localhost:8000/health" -TimeoutSec 2
        if ($r.status -eq "ok") { $ready = $true; break }
    } catch { Start-Sleep -Seconds 2 }
}
if (-not $ready) {
    Write-Host "Server did not respond. Check 'docker compose logs backend'." -ForegroundColor Red
    exit 1
}
Write-Host "Server is ready." -ForegroundColor Green

# Find cloudflared in PATH or next to the script
$cf = (Get-Command cloudflared -ErrorAction SilentlyContinue)?.Source
if (-not $cf) {
    $local = Join-Path $repoRoot "cloudflared.exe"
    if (Test-Path $local) { $cf = $local }
}
if (-not $cf) {
    Write-Host "cloudflared not found." -ForegroundColor Yellow
    Write-Host "Download cloudflared-windows-amd64.exe from:" -ForegroundColor Yellow
    Write-Host "  https://github.com/cloudflare/cloudflared/releases" -ForegroundColor Yellow
    Write-Host "rename it to cloudflared.exe, put it in the repo root, and run this script again." -ForegroundColor Yellow
    exit 1
}

Write-Host "==> Starting tunnel. Copy the *.trycloudflare.com URL and paste it into the app (Profile / Login)." -ForegroundColor Cyan
& $cf tunnel --url http://localhost:8000
