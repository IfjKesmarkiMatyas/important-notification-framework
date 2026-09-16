$ErrorActionPreference = "Stop"
$base = if ($env:NOTIF_API_URL) { $env:NOTIF_API_URL } else { "http://localhost:8080" }
$email = if ($env:NOTIF_ADMIN_EMAIL) { $env:NOTIF_ADMIN_EMAIL } else { "admin@notif.local" }
$password = if ($env:NOTIF_ADMIN_PASSWORD) { $env:NOTIF_ADMIN_PASSWORD } else { "adminadmin" }
$body = @{ email = $email; password = $password } | ConvertTo-Json
$login = Invoke-RestMethod -Method Post -Uri "$base/api/auth/login" -ContentType "application/json; charset=utf-8" -Body $body
$headers = @{ Authorization = "Bearer $($login.token)" }
$result = Invoke-RestMethod -Method Post -Uri "$base/api/admin/station/load" -Headers $headers
$result | ConvertTo-Json -Depth 6
