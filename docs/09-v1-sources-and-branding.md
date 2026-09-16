# V1 források és feladó (18 / 19 / 20)

POC, egyszerű integráció, **ingyenes public API / RSS**, ahol lehet. A Telex **kötelező**.

A gyűjtő 15 perces cronja ezeket hívja. Mindhárom eseménycsalád élő forrást kap.

---

## 18 — Piac: mit figyelünk?

**Elsődleges: CoinGecko Simple Price** — kulcs nélkül hívható JSON, 15 perc alatt van értelme (mozgás látszik).

- Példa: Bitcoin / Ethereum ár USD-ben és HUF-ban.
- A JSON kitben zárt típus: `market` + instrumentum + küszöb (pl. 5% elmozdulás az utolsó ismert árhoz képest).
- Nincs tőzsdei SLA, nincs BÉT-integráció a POC-ban (az fizetős / nehéz).

**Másodlagos, ha kell hivatalosabb FX:** [Frankfurter](https://www.frankfurter.app) (EKB napi árfolyam, kulcs nélkül). Napi zárás, 15 perces cronon ritkán mozdul — demóra gyengébb, „euró/forint” szabályhoz jó kiegészítő.

**Nem V1:** fizetős stock API, Yahoo nemhivatalos hack, MNB SOAP.

---

## 19 — Breaking: ki a forrás, mi van ellentmondáskor?

**Kötelező: Telex RSS** — `https://telex.hu/rss` (legfrissebb; tematikus RSS a rovatokból szintén publikus).

- Ez a magyar breaking **narratív** forrása.
- A gyűjtő RSS-t normalizál breaking eseménnyé (cím, idő, link, kivonat).

**Angol breaking (hogy a `rules.en` ne üresjárat legyen):** BBC World RSS — `https://feeds.bbci.co.uk/news/world/rss.xml`, kulcs nélkül.

**Ellentmondás (POC-szabály):**

| Vita | Ki nyer |
|---|---|
| Magyar breaking szövege | Telex |
| Földrengés **száma** (magnitúdó, hely) | USGS, ne a hír címe |
| Piac **száma** | CoinGecko |
| Ugyanaz a földrengés Telexen + USGS-en | **Egy** világesemény (D9), két eredet a nyomvonalon, nem két ping |

Nincs szerkesztői etalon-ember. Nincs admin „ez a hír ne menjen”.

---

## Katasztrófa (a 3. család forrása)

**USGS Earthquake GeoJSON** — kulcs nélkül, JSON.

- Cron-barát összefoglaló pl. napi / órás GeoJSON feed, vagy query `format=geojson`.
- A kit zárt típusa: `disaster` / `earthquake` + min. magnitúdó + opcionális régió.

Magyar katasztrófa-hírt a Telex breaking ág is behozza; a **küszöbös** döntés az USGS számra támaszkodik.

---

## Forrástábla (V1 POC)

| Család | Forrás | Típus | Kulcs | Miért ez |
|---|---|---|---|---|
| Breaking (hu) | Telex RSS | RSS | nem | Kötelező, egyszerű, magyar |
| Breaking (en) | BBC World RSS | RSS | nem | Az angol szabályfile-nak legyen jele |
| Katasztrófa | USGS GeoJSON | JSON API | nem | Küszöb, hely, idő kész mező |
| Piac | CoinGecko Simple Price | JSON API | nem (demo) | 15 perc alatt van mozgás |
| Piac (opcionális FX) | Frankfurter | JSON API | nem | Hivatalos EKB, napi |

A POC alatt **bármi más** is rátehető a gyűjtő interface-re; ez a **kezdő, könnyen integrálható** készlet.

---

## 20 — Feladó / branding

Ne a Telex vagy az USGS nevében küldjünk.

| Csatorna | Candidate |
|---|---|
| Slack | A **chatbot** saját bot-neve, pl. **Notif** |
| Email | Ugyanaz a megjelenő név: `Notif <noreply@…>` |
| Törzs | A forrás **hivatkozásként** benne van (Telex-link, USGS-link), nem hamisítjuk a feladót |

POC: egyetlen identitás, két csatorna. Spam-mappa a kézbesítő failje, nem branding-vita.
