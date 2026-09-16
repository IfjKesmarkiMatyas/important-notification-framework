# Plan of attack — üzleti kivitelezés

Cél: **ne az egész terméket egyszerre** építeni, hanem olyan sorrendben haladni, hogy minden lépés után van mit mutatni, van mit elutasítani, és a következő lépés üzleti kockázata csökken.

Technikai sprintterv ez **nem**. A motorok sorrendje **lezárt**.

**Állapot (2026-09-16):** a négy fázis kódja bent van. D9 (két forrás → egy világesemény) és az AI FIRE-felülírás szándékosan később. Indítás: [README.md](../README.md). Tesztállomány: [station/](../station/).

---

## Lezárt kivitelezési sorrend

1. **Kézbesítőmotor** — email + Slack chatbot, kézzel adott megbízással is tesztelhető.
2. **UI** — meghívás, JSON kit (érdekeltség + preferencia), admin.
3. **Gyűjtőmotor** — 15 perces cron, Telex + további POC-források.
4. **Döntésmotor** — native **vagy** AI **magyarázat**, **váltósetting**; FIRE a matcheré; nincs árnyéküzemmód.

A D0 szabálykönyv (mindhárom eseménycsalád, zárt típusok) a kézbesítés előtt / mellett lezárható papíron: nélküle a JSON kit mezői elúsznak.

---

## Irányelv

1. **Először a kézbesítés.** Ha a levél és a chatbot nem visz ki üzenetet, a többi motor üres ígéret.
2. **Aztán a UI**, hogy legyen kinek és hová küldeni (meghívott user, JSON kit).
3. **Aztán a gyűjtés**, 15 perces cronnal, folyamatosan.
4. **Végül a döntés** köti össze a normalizált eseményt a megbízással. FIRE a matcher; a magyarázat native vagy AI, váltóval.
5. **A második csatorna (Slack chatbot) a bővíthetőség tesztje**, és a kézbesítő *első* fázisában már bent van, nem utólag.
6. **Több user, élő szándék, JSON kit.** Új usernél default másolat, utána szerkesztés.
7. **MCP-paritás minden szeleten**, admin jogú gépfelhasználóval.
8. **Nincs árnyék, nincs hírszintű admin-felülbírálat, nincs napi digest.**

---

## Fázisok

### Fázis 0 — Szerződés (papír)

**Deliverable:** D0  
**Kimenet:** szótár; V1 típusok = **breaking + piac + katasztrófa**; zárt típusok + szűrők; JSON kit default váz; nyelvenkénti szabályfile váz (hu + en).

**Döntési kapu:** a 12 példakártya egyezik. A kit mezői ezekből jönnek.

---

### Fázis 1 — Kézbesítőmotor

| Lépés | Deliverable | Üzleti kérdés |
|---|---|---|
| 1.1 | D4 Email | Kézzel adott megbízásból megérkezik a levél? |
| 1.2 | D5 Slack chatbot | Ugyanaz a megbízásmodell chatbotként kimegy? |
| 1.3 | D11 a kézbesítésre | Tesztküldés MCP-n (admin gépuser) is megy? |

**Döntési kapu:** két tesztcímzett (A email, B Slack chatbot) megkapja a **bedobott** megbízást. A kézbesítő nem dönt és nem scrape-el. Ami már bent van a deliveryben, **kimegy**. Az `INVITE` sablon renderelhető; éles meghívás a 2. fázis.

---

### Fázis 2 — UI

| Lépés | Deliverable | Üzleti kérdés |
|---|---|---|
| 2.1 | D1 meghívás | Nyilvános signup nélkül be tudunk venni usert? |
| 2.2 | D1 JSON kit | Default másolat → user/admin szerkeszti, bővíti? |
| 2.3 | D1 élő írás | Mentés után a kit a következő döntés bemenete? (döntés még mock) |
| 2.4 | D6 | A user **bármit** beállíthat a csatornáira a kitben? |
| 2.5 | D7/D8 váz | Van admin felület a userekre és a későbbi nyomvonal helyére? |
| 2.6 | D11 a UI-ra | Meghívás + kit írás MCP-n, admin gépuserrel? |

**Döntési kapu:** seed admin belép. Meghív két usert a People képernyőn; a **delivery** viszi az `INVITE` emailt. Elfogadás után default kit másolat, a két kit eltér. Failed meghívó látszik, újra küldhető. Nincs digest, nincs `/register`. Terv: [10-delivery-ui-users/](10-delivery-ui-users/).

---

### Fázis 3 — Gyűjtőmotor

| Lépés | Deliverable | Üzleti kérdés |
|---|---|---|
| 3.1 | D2 Telex + 15 p cron | 15 percenként bejön a Telex, normalizált breaking? |
| 3.2 | D2 USGS (API) | Katasztrófa strukturált API-ból, ugyanaz a normalizált alak? |
| 3.3 | D2 CoinGecko (API) | Piac JSON API-ból, ugyanaz az alak? |
| 3.4 | D8 gyűjtőállapot | Látszik, mikor volt utolsó cron, melyik forrás hallgat? |
| 3.5 | D11 a gyűjtésre | Forrásállapot / utolsó felvétel MCP-n? |

**Döntési kapu:** mindhárom eseménycsalád élő (vagy cronnal frissen vett) normalizált eseményt ad. A cron éjjel is megy. Még **nem** kell döntés: az admin látja a felvételeket.

---

### Fázis 4 — Döntésmotor

| Lépés | Deliverable | Üzleti kérdés |
|---|---|---|
| 4.1 | D3 native | Userenkénti szabály (hu/en file a kit mellett) magyarázattal megbízást ad a kézbesítőnek? |
| 4.2 | D6 kit = policy | A user csatornaválasztása megy, nincs rendszer-kényszer? |
| 4.3 | D7 teljes nyomvonal | Eredet → döntés → chatbot/email, userenként? |
| 4.4 | D9 | Két forrás, egy világesemény, userenként egyszer? |
| 4.5 | D10 váltó | Native ↔ AI **egy** setting: ugyanaz a FIRE, más magyarázat; a másik mód **nem** fut mellé? |
| 4.6 | D11 a döntésre | Váltó + döntés MCP-n? |

**Döntési kapu (első teljes demo):**  
cron hoz egy Telex/USGS/piaci jelet → matcher A-nak FIRE, B-nek NO → email és/vagy chatbot a kit szerint → admin nyomvonal. Váltó AI-ra: a **következő** esemény **ugyanazzal a FIRE/NO-val** megy, a magyarázat szövege más; nem árnyék, a modell nem dönt.

---

## Mit csinálunk szándékosan később / nem

- Harmadik éles csatorna.
- Nyilvános signup, digest, hírszintű admin-stop.
- Árnyéküzemmód — **kivéve**, nem „később”: **nem használjuk**.
- AI FIRE-felülírás — később, külön flag (a modell most nem dönt kimenetelt).
- Jogi/SLA, forrásjogi szigor — POC, most bármely forrás mehet.
- Téves/kihagyott riasztás felelőssége — most nem foglalkozunk vele.

---

## Függőségi kép (üzleti)

```
D0 szabálykönyv + default JSON kit + default rules.hu / rules.en
    │
    ▼
D4 email + D5 Slack chatbot     ← 1. KÉZBESÍTŐ (bedobott megbízás)
    │
    ▼
D1 meghívás + JSON kit UI       ← 2. UI
    │
    ▼
D2 gyűjtés (15 p cron)          ← 3. GYŰJTŐ  Telex, USGS, CoinGecko
    │
    ▼
D3 döntés (matcher FIRE; magyarázat native | AI)   ← 4. DÖNTÉS
    │
    ├─► D6 = a user kitje
    ├─► D7 nyomvonal + D8 állapot
    └─► D9 dedup

D10 = magyarázat-váltó, ugyanaz a FIRE, nem árnyék
D11 MCP (admin gépuser) ── minden fázis kapuja
```

---

## Ajánlott demo-ritmus

1. **Fázis 1:** „bedobtam egy megbízást → levél + Slack chatbot megjött”.
2. **Fázis 2:** „meghívtam kettőt → default kit másolat → A és B mást állított → admin átírta A-t”.
3. **Fázis 3:** „15 perc múlva van friss Telex / földrengés / árfolyam az adminon, normalizálva”.
4. **Fázis 4:** „A kapott, B nem; chatbot/email a kit szerint; váltó AI-ra a FIRE ugyanaz, a reason más”.

Ha egy demo nem mesélhető el így, a fázis nincs kész.

---

## Párhuzamosítás

A **lezárt sorrend** a tanulás sorrendje. Előkészület mehet előre (Slack app, email feladó, Telex RSS próbaolvasás), de a fázis kapuja nem ugorható.

Nem párhuzamosítható a kapuk kárára:

- Döntés a működő kézbesítés és a kit UI előtt.
- Gyűjtés éles usernek döntés nélkül (az adminnak a 3. fázisban **szabad** mutatni a felvételeket).
- AI native mellett árnyékként.
- „Majd MCP-zzük, ha a UI kész.”
