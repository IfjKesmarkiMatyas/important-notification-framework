$ErrorActionPreference = "Stop"
Set-Location (Split-Path -Parent $PSScriptRoot)
docker compose up -d
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Write-Host "Postgres :5432  Mailpit SMTP :1025  UI http://localhost:8025"
