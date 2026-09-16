$ErrorActionPreference = "Stop"
Set-Location (Split-Path -Parent $PSScriptRoot)
mvn -f backend/pom.xml test
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
