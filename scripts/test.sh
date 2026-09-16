#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")/.."
mvn -f backend/pom.xml test
