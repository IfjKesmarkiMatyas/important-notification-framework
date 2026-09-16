# Notification Framework

Meghívásos, többfelhasználós riasztórendszer. A gyűjtő normalizált eseményt készít (Telex, BBC, USGS, CoinGecko, Frankfurter), a döntésmotor **usereként** eldönti, hogy FIRE vagy NO, a kézbesítő emailen és Slack chatboton viszi ki. FIRE a matcheré; magyarázat **native vagy AI**, egy váltó — nincs árnyék. A modell csak `{reason}`-t ír. MCP = admin jogú gépuser.

**Állapot (2026-09-16):** Fázis 1–4 bent van (kézbesítő, Angular UI, gyűjtő, native döntés F₁ = 1.00 a 65 mintán). Szándékosan később: D9 világesemény-összevonás, AI FIRE-felülírás, árnyék, Pushover éles.

Üzleti szerződés: [docs/README.md](docs/README.md). Tesztállomány: [station/](station/). Prompt verdict + AI review: [docs/12-ai-review.md](docs/12-ai-review.md).

**Kézikönyv (Cinege):** [Notif admin felületek és Tower döntési nyomvonal](docs/Notif-admin-feluletek-es-Tower-dontesi-nyomvonal-attekintese/KEZIKONYV.hu.html) — 20 lépéses UI-útmutató (kit, People, Delivery, Defaults, Tower). Nyisd meg a HTML-t a böngészőben.

## Stack

| Réteg | Ami fut |
|---|---|
| Backend | Java 25, Spring Boot 4.1.1, Maven, PostgreSQL 16, Flyway |
| Frontend | Angular 20, Notif Ink, `http://localhost:4200` → proxy `/api` → `:8080` |
| Mellék | Mailpit (`:1025` SMTP, `:8025` UI), opcionális Slack / Pushover / `GPT_API_KEY` |

## How to start

Előfeltétel: **Java 25**, **Maven**, **Node 20+**, **Docker**. Élő Telex/GPT **nem** kell a belépéshez.

1. Infó a gyökérben:

   ```powershell
   copy .env.example .env
   .\scripts\start-infra.ps1
   ```

   Linux/macOS: `cp .env.example .env && ./scripts/start-infra.sh`

   A compose a Postgres-t **5432**-re, a Mailpitet **1025/8025**-re teszi. Ha a 5432 foglalt, a `.env` `NOTIF_DB_URL` portját igazítsd.

2. API:

   ```powershell
   mvn -f backend/pom.xml -pl api -am spring-boot:run
   ```

   Első induláskor Flyway + seed admin: `admin@notif.local` / `adminadmin` (vagy a `.env` `NOTIF_ADMIN_*`).

3. UI (másik terminál):

   ```powershell
   cd frontend
   npm install
   npm start
   ```

   Böngésző: [http://localhost:4200/login](http://localhost:4200/login).

4. Tesztállomány (Ada / Béla / Cora / Dénes / Elena + 13 scrapelt golden esemény):

   ```powershell
   .\scripts\load-station.ps1
   ```

   Native módban első betöltéskor **7 FIRE** (Ada: D2, D4, M2, B1; Béla: M2; Elena: B3, B4). Dénes INACTIVE, Cora küszöbe ≥7 — ezen a korpuszon nem lő. A Tower `/admin/tower` inboxában látod az eseményeket és a nyomvonalat.

Persona belépés: `ada@notif.local` … `elena@notif.local`, jelszó **`stationstation`**. Dénes inaktív, nem lép be.

Mailpit: Ada FIRE levelei a [http://localhost:8025](http://localhost:8025) alatt. Béla Slackje `U_BELA` — token nélkül a job `failed`, ez várható.

## Teszt

Élő hálózat nélkül (CI is ezt futtatja; nincs élő Telex, nincs élő GPT):

```powershell
.\scripts\test.ps1
```

Ugyanez: `mvn -f backend/pom.xml test`. A native motor F₁ = 1.00 a [golden](docs/11-decision-fixtures/golden-truth.md) 65 mintáján (`GoldenTruthTest`). A station JSON szerződését a `StationCatalogTest` őrzi.

AI-váltóhoz `.env` → `GPT_API_KEY`. Kulcs nélkül a váltó `ai` **fail-closed** (error, nincs FIRE-felülírás).

## MCP

`POST /mcp`, header a `.env` `NOTIF_MCP_TOKEN`. Toolok: meghívás, kit, scrape, döntés, `score_golden`, `load_station`.

## Alapértelmezések

| | |
|---|---|
| Admin | `admin@notif.local` / `adminadmin` |
| Station userek | `*@notif.local` / `stationstation` |
| JWT / MCP token | `.env.example` placeholder — élesben cseréld |
| Döntés | `native`, küszöb **≥**, téma `any` |

A `.env` **nem** megy a gitbe.
