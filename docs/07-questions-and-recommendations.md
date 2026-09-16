# Nyitott kérdések, javaslatok, döntési log

A lenti kérdések **lezárva** vannak, hacsak külön nem jelöljük. A többi doksi ehhez igazodik.

---

## Döntési log

| ID | Döntés | Dátum | Hatás |
|---|---|---|---|
| 1 | Több user; userenként saját érdekeltség és preferencia | 2026-09-16 | S1, S1b |
| 2 | Élőben frissíthetők adminról | 2026-09-16 | S1c |
| 3 | Meghívásos felvétel; a meghívott a sajátját is írja | 2026-09-16 | S1a |
| 4 | Minden funkció MCP-n hívható | 2026-09-16 | D11 |
| 5 | Nincs árnyéküzemmód. Native **vagy** AI, **váltósetting** | 2026-09-16 | D10, S11 |
| 6 | Érdekeltség + preferencia = bővíthető **JSON kit**. Új usernél **default másolat**, utána szerkesztés | 2026-09-16 | D1, kit |
| 7 | Kivitelezés: **kézbesítő → UI → gyűjtő → döntés** | 2026-09-16 | 04-es terv |
| 3b | V1 eseménycsalád: **mindhárom** (breaking, piac, katasztrófa) | 2026-09-16 | D0, D2 |
| 4b | Zárt típusok + szűrők (a javaslat elfogadva) | 2026-09-16 | S1, kit |
| 5b | Slack = **chatbot** | 2026-09-16 | D5 |
| 6b | Gyűjtés: **15 perces cron**, folyamatos | 2026-09-16 | D2, időrúd |
| 21 | Ami már a deliveryben van, **menjen ki** | 2026-09-16 | S1c, D4/D5 |
| 22 | MCP = **admin jogú gépfelhasználó** | 2026-09-16 | D11 |
| 7b | Csatornapolicy: a **user bármit** csinálhat | 2026-09-16 | D6 |
| 8 | Nincs napi összefoglaló | 2026-09-16 | D6 |
| 9 | Dedup: ugyanaz a történést, userenként egyszer (javaslat elfogadva) | 2026-09-16 | D9 |
| 10 | Forrásjog: **POC**, most bármely forrás mehet | 2026-09-16 | D2 |
| 11 | Téves/kihagyott riasztás kockázata: **most nem foglalkozunk vele** | 2026-09-16 | — |
| 12 | Hírszintű admin-felülbírálat: **nem kell** | 2026-09-16 | admin |
| 13 | Nyelv: **magyar + angol**, userenként **nyelvenkénti szabályfile** | 2026-09-16 | D3, kit |
| 14 | Folyamatos cron, nincs éjszakai leállás; helyi időzóna **nem** téma | 2026-09-16 | D2 |
| 15 | Admin mindent lát/ír, user csak a sajátját | 2026-09-16 | D1 |
| 16 | Megőrzés: most **bármeddig DB-ben** | 2026-09-16 | D7 |
| 17 | User / forrás / riasztás: **bármennyi** lehessen | 2026-09-16 | — |
| 18–20 | Források és feladó: lásd [09-v1-sources-and-branding.md](09-v1-sources-and-branding.md) | 2026-09-16 | D2, D4, D5 |

Még nyitott, nem blokkol: user és admin **egyszerre** írja ugyanazt a kitmezőt → last-write-wins + ki nyert.

---

## Lezárt válaszok röviden

| # | Döntés |
|---|---|
| 3 | Mindhárom eseménycsalád. |
| 4 | Zárt típus + szűrők. |
| 5 | Slack chatbot. |
| 6 | 15 perc cron a scrapingre. |
| 21 | Deliverybe került megbízás kimegy. |
| 22 | MCP: admin jogú gépuser. |
| 7 | User bármilyen csatornakombinációt beállíthat, kritikusan is. |
| 8 | Nincs napi digest. |
| 9 | Ugyanaz a történést + szűk időablak; fejlemény külön; userenként egyszer. |
| 10 | POC: bármit lehet húzni. |
| 11 | Most nem foglalkozunk a kockázatviselővel. |
| 12 | Nincs hírszintű admin stop/engedély. |
| 13 | hu + en szabályfile userenként. |
| 14 | Cron hozza az infót; a user tudni fogja, hogy most jött. |
| 15 | Admin minden, user saját. |
| 16 | Nincs törlési ablak most. |
| 17 | Nincs mesterséges limit. |

---

## Amit ebből szándékosan **nem** csinálunk

- Árnyéküzemmód.
- Napi összefoglaló.
- Hírenkénti admin-kontroll.
- Időzóna-motor.
- Forrásjogi szűrés a POC-ban.
- Felelősségi / disclaimer-szelet most.
