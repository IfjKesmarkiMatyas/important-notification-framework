# Notification Framework — üzleti dokumentáció

Ez a mappa a termék **üzleti** szerződését tartalmazza: deliverable-ök, sorrend, tesztelhető szeletek, motorhatárok.

A kód **megvan**. Indítás, station, teszt: a gyökér [README.md](../README.md). A doksik szándékosan nem API-katalógusok; ahol a viselkedés a kódban pontosabb (pl. AI csak magyarázat), a képességdoksi a ténnyel együtt él.

## Állapot (2026-09-16)

| Fázis | Terv | Kód |
|---|---|---|
| 0 szerződés | D0, szótár, zárt típusok | [02-glossary.md](02-glossary.md), default kit + rules.hu/en |
| 1 kézbesítő | email + Slack chatbot | `delivery` modul, Mailpit / Slack adapter |
| 2 UI + userek | meghívás, kit, admin | Angular 20 Notif Ink |
| 3 gyűjtő | 15 p cron, Telex/BBC/USGS/CoinGecko/Frankfurter | `scrape` modul |
| 4 döntés | matcher FIRE; magyarázat native **vagy** AI | native F₁ = 1.00 a 65 mintán; AI csak `{reason}`; D9 később |

Tesztállomány userekkel és scrapelt eseményekkel: [station/](../station/). Golden: [11-decision-fixtures/](11-decision-fixtures/).

**Kézikönyv (Cinege):** [Notif admin felületek és Tower döntési nyomvonal](Notif-admin-feluletek-es-Tower-dontesi-nyomvonal-attekintese/KEZIKONYV.hu.html) — 20 lépés, képernyőképekkel.

## Lezárt üzleti döntések (2026-09-16)

- Több user, JSON **kit** (érdekeltség + preferencia + `rules.hu` / `rules.en`); új usernél **default másolat**.
- Meghívásos felvétel; admin élőben ír; ami a deliveryben van, **kimegy**.
- Magyarázat **native vagy AI**, **váltósetting** — **nincs árnyéküzemmód**. FIRE mindig a determinisztikus matcher; az AI nem írja felül.
- Kivitelezés: **kézbesítő → UI → gyűjtő → döntés** (ez lefutott).
- Mindhárom eseménycsalád; zárt típusok; Slack **chatbot**; 15 perc cron; user bármit beállíthat a csatornára; nincs digest.
- MCP = **admin jogú gépfelhasználó**.
- Források: Telex kötelező + USGS + CoinGecko — [09-v1-sources-and-branding.md](09-v1-sources-and-branding.md).

Részletek: [07-questions-and-recommendations.md](07-questions-and-recommendations.md).  
AI-irányítás (verdict + review): [12-ai-review.md](12-ai-review.md).

## Hogyan olvasd

| Sorrend | Dokumentum | Mit ad |
|---|---|---|
| 1 | [01-vision-and-scope.md](01-vision-and-scope.md) | Mi a termék, kinek, mi van bent / kint az első körben |
| 2 | [02-glossary.md](02-glossary.md) | Közös nyelv, hogy a szeletek ugyanazt jelentsék |
| 3 | [03-business-deliverables.md](03-business-deliverables.md) | Mit adunk át üzletileg |
| 4 | [04-plan-of-attack.md](04-plan-of-attack.md) | Üzleti kivitelezési sorrend, függőségek, döntési kapuk |
| 5 | [05-testable-slices.md](05-testable-slices.md) | Elkülöníthető, egyenként tesztelhető szeletek |
| 6 | [06-capabilities/](06-capabilities/) | Motorok és felületek üzleti viselkedése |
| 7 | [07-questions-and-recommendations.md](07-questions-and-recommendations.md) | Döntési log (a kérdések lezárva) |
| 8 | [08-success-criteria.md](08-success-criteria.md) | Mikor tekintünk egy szeletet / a V1-et késznek |
| 9 | [09-v1-sources-and-branding.md](09-v1-sources-and-branding.md) | Telex, USGS, CoinGecko, feladó |
| 10 | [10-delivery-ui-users/](10-delivery-ui-users/) | Fázis 1–2: delivery, meghívás emailben, Angular UI |
| 11 | [11-decision-fixtures/](11-decision-fixtures/) | Gyűjtött korpusz → döntés-létra, golden truth, pontossági napló |
| 12 | [12-ai-review.md](12-ai-review.md) | Prompt verdict + AI review log (mit utasítottunk el) |
| — | [Kézikönyv (Cinege)](Notif-admin-feluletek-es-Tower-dontesi-nyomvonal-attekintese/KEZIKONYV.hu.html) | Admin UI + Tower nyomvonal, 20 lépés |
| — | [transcript-user-prompts.md](transcript-user-prompts.md) | Csak a user promptok, időrendben |
| — | [transcript.md](transcript.md) | Teljes Cursor export (válaszokkal) |

## Rövid kép

A rendszer figyeli a világot, **normalizált eseményeket** készít belőle, **userenként** eldönti, mi fontos, majd **emailen és Slack chatboton** értesít.  
Kivitelezés: **kézbesítő → UI → gyűjtő → döntés**. MCP: **admin jogú gépuser**.

Három motor + irányítópult + gép-kapu:

1. **Gyűjtőmotor** — információt szed le változó forrásokból, közös alakra hozza.
2. **Döntésmotor** — mi fontos, kinek, milyen csatornán. FIRE = matcher; magyarázat **native vagy AI**, váltóval (nem egyszerre).
3. **Kézbesítőmotor** — email + Slack chatbot.
4. **Admin felület** — motorok, nyomvonal (system / inbox / story), userek, JSON kit, váltó.
5. **MCP** — admin jogú gépfelhasználó.

## Szándékos korlát (V1)

- Nincs vendor- vagy hosting-döntés a doksiban.
- D9 összevonás, AI FIRE-felülírás, árnyék, hírszintű admin-stop: kint.
- A last-write-wins a kit egyidejű szerkesztésére maradt nyitva.
