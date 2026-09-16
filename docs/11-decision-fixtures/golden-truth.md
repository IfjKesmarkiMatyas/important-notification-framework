# Golden truth — native döntés a kiválasztott eseményekre

Minden sor: ha ez a normalizált esemény a döntésmotorba kerül **most**, a kit pillanatával. Nincs delivery-visszahívás; ez csak a megbízás születik-e.

Jelölés: **FIRE** = van megbízás (csatorna + native szint); **NO** = nincs.

---

## 1. Egyszerű, egyértelmű

### D1 — gyenge quake (4.0, Chignik, Alaska)

`aka2026shusro` · mag **4.0**

| User | Golden | Miért |
|---|---|---|
| Ada | **NO** | 4.0 &lt; 6 |
| Béla | **NO** | nem market |
| Cora | **NO** | 4.0 &lt; 7 |
| Dénes | **NO** | szünet |
| Elena | **NO** | nem breaking |

### D2 — erős quake (6.7, Tambo, Peru)

`us6000tm81` · mag **6.7**

| User | Golden | Miért |
|---|---|---|
| Ada | **FIRE** email, szint `critical` | 6.7 ≥ 6, kind earthquake |
| Béla | **NO** | nincs disaster interest |
| Cora | **NO** | 6.7 &lt; 7 — lásd D5 |
| Dénes | **NO** | szünet, hiába passzolna a küszöb |
| Elena | **NO** | nincs disaster interest |

### M3 — Ethereum +7.2% (rossz instrumentum)

`ethereum@14d-span` · **7.2%**, instrument `ethereum` (14 napos scrapelt min→max)

| User | Golden | Miért |
|---|---|---|
| Ada | **NO** | kit `bitcoin`, nem ethereum |
| Béla | **NO** | ugyanaz |
| Cora / Dénes / Elena | **NO** | nincs market / szünet |

Ha valaki kitjében `ethereum` + 5% lenne: **FIRE** `high`. Ez a „majdnem, de nem a te instrumentumod” lépcső.

### B1 — Telex belföld

Terror Háza / Tarr Zoltán · topics `["belfold"]`

| User | Golden | Miért |
|---|---|---|
| Ada | **FIRE** email, szint `medium` | témaillesztés |
| Béla | **NO** | csak market |
| Elena | **NO** | `belfold` ∩ `{world}` üres |
| Dénes | **NO** | szünet |

---

## 2. Közepes: küszöb és téma

### D3 — 5.9, Timor Leste (majdnem 6)

`us7000tgl2` · mag **5.9**

| User | Golden |
|---|---|
| Ada | **NO** (5.9 &lt; 6) |
| Cora | **NO** |
| többiek | **NO** |

### D4 — pontosan 6.0, Szomália

`us7000tcf3` · mag **6.0**

| User | Golden | Megjegyzés |
|---|---|---|
| Ada | **FIRE** email `critical` | szerződés: **≥ 6** |
| Cora | **NO** | 6 &lt; 7 |

Ha a terméktulajdonos a „6.0 feletti” szöveget szó szerint akarja: Ada itt **NO**. Ezt S0-n le kell zárni.

### M1 — Bitcoin +4.7% (12 órás minta)

`bitcoin@2026-09-03T16:00:00Z` · **4.7%**

| User | Golden |
|---|---|
| Ada | **NO** (4.7 &lt; 5) |
| Béla | **NO** |

Valós CoinGecko-chart, nem kitalált szám. A default 5% alatt marad.

### M2 — Bitcoin +7.6% (14 napos scrapelt min→max)

`bitcoin@14d-span` · **7.6%** · `previousUsd` 75610 → 81321.7

| User | Golden | Csatorna |
|---|---|---|
| Ada | **FIRE** `high` | **csak email** (slack ki) |
| Béla | **FIRE** `high` | **csak slack** |
| Dénes | **NO** | szünet |

Ugyanaz az esemény, két user, két csatorna — S1b.

### B2 — Telex külföld (nincs belföld)

EP / Oroszország hibrid · topics `["kulfold"]`

| User | Golden |
|---|---|
| Ada | **NO** |
| Elena | **NO** |

### B3 — BBC world+business, magyar default kit

Bond yields · topics `["world","business"]` · locale `en`

| User | Golden |
|---|---|
| Ada | **NO** | `world` nem `belfold` |
| Elena | **FIRE** email `medium` | `world` illeszkedik; `rules.en` |

---

## 3. Nehezebb

### D5 — a korpusz legerősebb quakeja Cora ≥7 kitjén

Ugyanaz, mint D2: **6.7 Peru**. A hónapban **nincs 7+**.

| User | Golden |
|---|---|
| Cora | **NO** | a „nagyon fontos világesemény” attól még nem az ő küszöbe |
| Ada | **FIRE** (ugyanaz mint D2) |

### B4 — BBC science, `world` is rajta van

Himalája · topics `["world","science"]`

| User | Golden | Miért nehéz |
|---|---|---|
| Elena | **FIRE** | a BBC csatlakozó mindig rárakja a `world`-öt, nem csak a `science`-t |
| Ada | **NO** | |

Ha a döntés csak a „fő” témát nézné, Elena **NO** lenne. Golden: **halmaz-metszet**, nem első téma.

### B5 — Telex PR-cikk (x)

SPAR zenei kihívás · topics `["pr_cikk"]`

| User | Golden |
|---|---|
| Ada | **NO** | nem belföld, fizetett tartalom |
| Elena | **NO** |

A scrape **nem** szűr PR-t; a döntés a témánál eldobja, ha a kit zárt listás.

### M4 — EUR/HUF 0%

Frankfurter napi zárás, `movePercent: 0`, instrument `eurhuf`

| User | Golden |
|---|---|
| minden fenti | **NO** | nincs `eurhuf` interest, és 0% &lt; 5 |

---

## 4. Keresztvágások (minden eseményre)

| Szabály | Golden |
|---|---|
| Dénes (szünet) | **mindig NO**, még D2/M2/B1-re is |
| Meg nem hívott user | nincs kit → **NO** (nincs a táblában) |
| Ami már deliveryben van | kimegy; ez a tábla a **döntés előtti** pillanat |
| D9 Telex+USGS ugyanaz a quake | ebben a korpuszban **nincs pár**; ha lenne: két eredet, **egy** világesemény, userenként **egy** ping |
| Native vs AI | most native; AI-váltónál ugyanaz a FIRE/NO a kit küszöbén, a magyarázat szövege más, **árnyék nincs** |

---

## Pass a Fázis 4-hez

A döntésmotor akkor pass, ha Ada/Béla/Cora/Dénes/Elena kimenete **bitre** a fenti FIRE/NO + csatorna. Vita csak D4 (≥ vs feletti). A 825 USGS-ből a többi 4.x ugyanúgy NO, mint D1 — nem kell mindet felsorolni.
