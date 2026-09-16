#!/usr/bin/env sh
set -eu
base="${NOTIF_API_URL:-http://localhost:8080}"
email="${NOTIF_ADMIN_EMAIL:-admin@notif.local}"
password="${NOTIF_ADMIN_PASSWORD:-adminadmin}"
token="$(curl -sS -X POST "$base/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$email\",\"password\":\"$password\"}" \
  | python -c "import json,sys; print(json.load(sys.stdin)['token'])")"
curl -sS -X POST "$base/api/admin/station/load" \
  -H "Authorization: Bearer $token"
echo
