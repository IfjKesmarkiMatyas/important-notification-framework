#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
docker compose up -d
echo "Postgres :5432  Mailpit SMTP :1025  UI http://localhost:8025"
