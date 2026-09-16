# Notification Framework — üzleti tervezés (1. kör)

Ez a mappa a termék **üzleti** tervezését tartalmazza.  
Ebben a körben **nincs implementáció**: cél a deliverable-ök, a kivitelezési sorrend és az egyenként átadható / tesztelhető szeletek rögzítése.

A technikai irány (Java / Spring backend, PostgreSQL, Angular frontend, motorokra bontott backend) **adottság**, de a dokumentumok szándékosan nem kód-, API- vagy sématervek.

## Lezárt üzleti döntések (2026-09-16)

- Több user, JSON **kit** (érdekeltség + preferencia + `rules.hu` / `rules.en`); új usernél **default másolat**.
- Meghívásos felvétel; admin élőben ír; ami a deliveryben van, **kimegy**.
- Native **vagy** AI, **váltósetting** — **nincs árnyéküzemmód**.
- Kivitelezés: **kézbesítő → UI → gyűjtő → döntés**.
- Mindhárom eseménycsalád; zárt típusok; Slack **chatbot**; 15 perc cron; user bármit beállíthat a csatornára; nincs digest.
- MCP = **admin jogú gépfelhasználó**.
- Források: Telex kötelező + USGS + CoinGecko — [09-v1-sources-and-branding.md](09-v1-sources-and-branding.md).

Részletek: [07-questions-and-recommendations.md](07-questions-and-recommendations.md).

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

## Rövid kép

A rendszer figyeli a világot, **normalizált eseményeket** készít belőle, **userenként** eldönti, mi fontos, majd **emailen és Slack chatboton** értesít.  
Kivitelezés: **kézbesítő → UI → gyűjtő → döntés**. MCP: **admin jogú gépuser**.

Három motor + irányítópult + gép-kapu:

1. **Gyűjtőmotor** — információt szed le változó forrásokból, közös alakra hozza.
2. **Döntésmotor** — mi fontos, kinek, milyen csatornán; **native vagy AI**, váltóval (nem egyszerre).
3. **Kézbesítőmotor** — email + Slack chatbot (ezt építjük először).
4. **Admin felület** — motorok, nyomvonal, userek, JSON kit, váltó.
5. **MCP** — admin jogú gépfelhasználó.

## Szándékos korlát ebben a körben

- Nincs kód, séma, endpoint-lista, sprintbontás story pointtal.
- Nincs vendor- vagy hosting-döntés.
- A kérdések lezárva; egyedül a last-write-wins a kit egyidejű szerkesztésére maradt nyitva.
