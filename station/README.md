# Tesztállomány

A `station.json` a repóban van: **öt persona** (Ada, Béla, Cora, Dénes, Elena) + **13 unique scrapelt esemény** a [golden selected.json](../docs/11-decision-fixtures/selected.json)-ból (D5 duplikátum nélkül). Élő Telex/USGS hívás nélkül tölthető.

Jelszó minden personánál: `stationstation`.

| Email | Kit | Állapot |
|---|---|---|
| `ada@notif.local` | default: disaster ≥6, bitcoin ≥5%, `belfold`, email | ACTIVE |
| `bela@notif.local` | csak bitcoin ≥5%, Slack `U_BELA` | ACTIVE |
| `cora@notif.local` | earthquake ≥7, email | ACTIVE |
| `denes@notif.local` | mint Ada | **INACTIVE** |
| `elena@notif.local` | breaking `world`, locale en, email | ACTIVE |

## Betöltés

API fusson (`native` váltó). Admin JWT-vel:

```powershell
..\scripts\load-station.ps1
```

Vagy: `POST /api/admin/station/load`, MCP `load_station`. Újrafuttatás idempotens: `(sourceId, externalId)` és `(user, sourceId, externalId)` unique — második körben nincs új FIRE-levél.

`?decide=false` csak usereket és eseményeket ír, döntés nélkül.

## Mit ellenőrizz

Más gépen, seed után, kód nélkül:

1. People: öt persona, Dénes inaktív.
2. Tower inbox: 13 esemény (usgs/telex/bbc/coingecko/frankfurter).
3. Peru 6.7 (D2): Ada FIRE, Béla/Cora/Dénes NO.
4. Ada belép a saját kitjével; Dénes nem lép be.
5. `mvn -f backend/pom.xml test` zöld a hálózat nélkül.
