# Döntés-fixturek a gyűjtött korpuszból

A native döntésmotor a [golden truth](golden-truth.md) 65 mintáján F₁ = 1.00. Ez a mappa a **gyűjtőből** származó élő normalizált eseményekből rak össze egy létrát: egyszerű egyértelmű találat/melléfogás → küszöb → rossz instrumentum → témaillesztés → zaj → szüneteltetett user.

A nyers feed **nem** megy a döntésnek. Csak a `selected.json` alak.

## Értékelés (tökéletesítés)

1. `GoldenTruthTest` — élő GPT nélkül, FIRE/NO + csatorna + szint a 13×5 mintán.
2. MCP `score_golden` vagy Tower **Golden F₁** — ugyanaz a native korpusz, családonkénti F₁.
3. Téves `(esemény, user)` → [accuracy.md](accuracy.md) tévesztésnapló, majd policy/matcher/kit.
4. FIRE-t a modell **nem** írja felül. AI csak magyarázat; élő GPT nem CI.

Szándékosan később: D9 összevonás, AI FIRE-felülírás, árnyék.

## Korpusz (2026-09-16)

| Család | Darab | Forrás |
|---|---:|---|
| disaster | 825 | USGS `2.5_day` + `2.5_week` + `4.5_month` + `significant_month` |
| breaking | 165 | Telex RSS (fő + belföld/külföld/gazdaság, duplikátum nélkül 52) + BBC world/business/science |
| market | 77 | CoinGecko simple price + 14 napos `market_chart` mintavétel + 1 Frankfurter EUR/HUF |
| **összesen** | **1067** | |

Magnitúdó-eloszlás: 225 db &lt;4, 449 db 4–5, 143 db 5–6, 6 db 6–6.5, 2 db 6.5–7, **0 db 7+**.  
Bitcoin 12 órás minták max elmozdulása **4.7%**; a 14 napos min→max (scrapelt árakból) **7.6%**.

Részletek: [corpus-summary.json](corpus-summary.json). A kiválasztott 13 esemény: [selected.json](selected.json). Golden truth: [golden-truth.md](golden-truth.md). Pontosság (F₁, futások): [accuracy.md](accuracy.md).

## Kit-szereplők (native)

A default sablon + két szűkítés. Csatorna = `preferences.channels`, szint = `default-rules.*.json`.

| ID | Kit | Csatorna | Állapot |
|---|---|---|---|
| **Ada** | default: disaster ≥6, bitcoin ≥5%, breaking `belfold` | csak email | ACTIVE |
| **Béla** | csak `market` / bitcoin / 5% | csak slack | ACTIVE |
| **Cora** | csak disaster earthquake **≥7** | email | ACTIVE |
| **Dénes** | mint Ada | email | **szüneteltetett / INACTIVE** |
| **Elena** | breaking `topics: ["world"]` | email | ACTIVE, locale en |

Küszöb: a [döntésmotor](../06-capabilities/decision-engine.md) táblája **≥**-t használ (`disaster ≥6`). A user-sztori „6.0 **feletti**” szigorúbb lenne — a **6.0 pontos** eset (D4) ezért S0-vita, a golden itt a ≥ szabály.

## Ami szándékosan nincs a létrában

- D9 világesemény-összevonás: ebben a korpuszban nincs Telex-cím, ami ugyanarra az USGS id-re utalna. Ha jön, **két `normalized_events` sor, egy döntés, egy ping**.
- 7.0+ földrengés: a hónap USGS 4.5+ feedjében nem volt; Cora ezért ezen a korpuszon **soha nem lő**.
- AI-agy: a váltó `native`; árnyék nincs.
