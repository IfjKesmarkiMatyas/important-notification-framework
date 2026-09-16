# Üzleti deliverable-ök

Egy deliverable **átadható üzleti eredmény**, nem sprintfeladat és nem technikai komponens.  
Akkor kész, ha egy nem-fejlesztő is meg tudja mondani: *ezt kipróbáltam, ezt kaptam*.

A kapcsolódó szeletek: [05-testable-slices.md](05-testable-slices.md).

---

## D0 — Közös nyelv és fontossági szabálykönyv

**Mit adunk át:** írásos megállapodás arról, mik a világesemény-típusok, mit jelent a fontosság, mikor melyik csatorna jár.

**Miért kell először:** nélküle a gyűjtés, a döntés és a kézbesítés három különböző terméket épít.

**Üzleti kész:** a szótár és a V1 eseménytípusok (pl. breaking news, piac, katasztrófa) le vannak zárva; van 8–12 példa „ez megy / ez nem megy”.

---

## D1 — Userek, meghívás, érdekeltség, preferencia

**Mit adunk át:** több user, mindegyik saját érdekeltséggel és preferenciával; meghívásos belépés; a user sajátja írható; az admin **élőben** írhatja.

**Üzleti kész:**

- Admin meghív egy usert; meghívás nélkül nincs belépés és nincs értesítés.
- A meghívott a **default JSON kit + rules.hu + rules.en másolatát** kapja, majd szerkeszti, bővíti.
- Két user listája nem keveredik.
- Admin menet közben átírja; a következő döntés az újat használja. Ami már deliveryben van, **kimegy**.
- Csatornák: a user **bármit** beállíthat a kitben (email / Slack chatbot / mindkettő / egyik sem).
- Ugyanez MCP-n, admin gépuserrel.

**Nem kell:** nyilvános signup, digest, hírszintű stop.

---

## D2 — Gyűjtés normalizált eseménnyé

**Mit adunk át:** a világból jövő anyag belép, megmarad az eredete, és **összehasonlítható alakra** kerül.

**Üzleti kész:**

- Legalább **két különböző forrástípus** (RSS + JSON API). Kezdő készlet: Telex, USGS, CoinGecko — [09-v1-sources-and-branding.md](09-v1-sources-and-branding.md).
- **15 perces cron**, folyamatos.
- Mindhárom család: breaking, piac, katasztrófa.
- A nyers felvétel megvan, DB-ben most bármeddig.

**Nem kell:** forrásjogi szűrés (POC).

---

## D3 — Döntés magyarázattal (szabályalapon)

**Mit adunk át:** a rendszer a normalizált esemény + riasztási szándék alapján dönt, és **elmondja, miért**.

**Üzleti kész:**

- Igen/nem helyett fontossági szint.
- Címzett(ek) **userenként** és javasolt csatorna a *annak a usernek* a preferenciája szerint.
- Ugyanaz az esemény A-nak mehet, B-nek nem, mindkettő magyarázattal.
- Olvasható magyarázat („miért kritikus”, „miért nem ment ki *ennek a usernek*”).
- Ugyanarra a bemenetre + ugyanarra a user-állapotra **ismételhető** eredmény.
- A döntés a **pillanatnyi** kitet olvassa.
- Native: user `rules.hu` / `rules.en` adja a **szintet** is. FIRE/NO a matcheré mindkét módban.
- AI: csak ha a váltó `ai` — akkor a **magyarázat** a modellé, a FIRE/NO nem.
- Nincs árnyék, nincs hírszintű stop.

**Nem kell:** a két agy párhuzamos futtatása.

---

## D4 — Email kézbesítés

**Mit adunk át:** a döntésből **tényleges email** lesz, státusszal.

**Üzleti kész:**

- A tesztfelhasználó megkapja a levelet.
- Látszik a tárgy/törzs üzleti tartalma (mi történt, miért fontos).
- Sikertelen küldés látszik, nem vész el csendben.

---

## D5 — Slack chatbot (a bővíthetőség bizonyítéka)

**Mit adunk át:** ugyanaz a döntés **második csatornán**, Slack **chatbotként**, nem webhook-kivételként.

**Üzleti kész:**

- A Notif bot üzenete megérkezik.
- Az adminon a csatorna „Slack”, nem különtermék.
- Feladó/identitás: a bot saját neve, nem Telex.

---

## D6 — Csatorna = a user kitje

**Mit adunk át:** a user **bármit** beállíthat. Nincs rendszer-kényszer (kritikus ≠ kötelező mindkét csatorna). Nincs napi digest.

**Üzleti kész:** A kikapcsolja az emailt, csak chatbotot kér — kritikusan is csak chatbot megy. B fordítva. A nyomvonalon látszik.

---

## D7 — Admin nyomvonal (control tower, 1. ígéret)

**Mit adunk át:** egy konkrét hírről az admin megmondja a teljes utat.

**Üzleti kész, egy képernyőn / folyamatban:**

1. Honnan jött (forrás, idő, nyers bizonyíték).
2. Mivé normalizálódott.
3. Milyen döntés született és miért.
4. Kinek (melyik usernek), melyik csatornán ment ki (vagy miért nem).
5. Melyik érdekeltség / preferencia volt érvényben a döntéskor.
6. Megérkezett-e / elakadt-e.

Ez a V1 **megkülönböztető** deliverable-je. Enélkül a motorok fekete dobozok.

---

## D8 — Admin motorállapot (control tower, 2. ígéret)

**Mit adunk át:** nem csak egy hír útja, hanem a **rendszer egészsége** üzleti nyelven.

**Üzleti kész:**

- Gyűjtő: mikor volt utolsó sikeres felvétel, melyik forrás hallgat.
- Döntés: van-e várakozó / elakadt döntés.
- Kézbesítés: kimenő sor, hibaarány csatornánként.

---

## D9 — Több forrás, egy világesemény (zajcsökkentés)

**Mit adunk át:** ha három újság írja ugyanazt a földrengést, a felhasználó **egyszer** kap jelzést (vagy tudatosan „fejleményt”), nem háromszor.

**Üzleti kész:** bemutatható példa: két nyers felvétel → egy világesemény → egy (vagy indokoltan kettő: breaking + update) értesítés.

---

## D10 — Native / AI váltó

**Mit adunk át:** egy setting, két **magyarázat**-mód. A FIRE/NO **mindig** a matcheré. Egyszerre **egy** mód indokol és küld.

**Üzleti kész:**

- Váltó `native`: matcher + sablon-indok (`rules.hu` / `rules.en`); a modell nem fut.
- Váltó `ai`: **ugyanaz** a matcher-FIRE; a `{reason}` a modellé. Kulcs nélkül fail-closed.
- A nyomvonalon látszik a váltó állása (`engine=native|ai`).
- Nincs árnyéklista, nincs AI-FIRE-felülírás (az később, külön flag).

---

## D11 — MCP-paritás (állandó, nem „majd később”)

**Mit adunk át:** minden elkészült funkció gépből is hívható, ugyanazzal a joggal és eredménnyel, mint a UI-n.

**Üzleti kész egy funkcióra:** van UI-sztori **és** MCP-sztori. Az MCP-t **admin jogú gépfelhasználó** hívja.

Ez nem V1 utáni deliverable. A D1–D10 (és minden későbbi) **csak akkor kész**, ha a D11 rájuk is igaz.

---

## Deliverable-térkép a motorokra

| Deliverable | Gyűjtő | Döntés | Kézbesítés | Felhasználói felület | Admin | MCP |
|---|---|---|---|---|---|---|
| D0 Szabálykönyv | bemenet | bemenet | bemenet | — | — | — |
| D1 User / szándék | — | használja | — | **saját** | **meghív + élő írás** | **igen** |
| D2 Gyűjtés | **fő** | — | — | — | olvassa | **igen** |
| D3 Döntés | — | **fő** | — | — | olvassa | **igen** |
| D4 Email | — | — | **fő** | kapja | olvassa | **igen** |
| D5 Slack chatbot | — | — | **fő** | kapja | olvassa | **igen** |
| D6 Kit = policy | — | dönt | végrehajt | **bármit** | látja | **igen** |
| D7 Nyomvonal | adat | adat | adat | — | **fő** | **igen** |
| D8 Motorállapot | adat | adat | adat | — | **fő** | **igen** |
| D9 Dedup | részt vesz | **fő** | kevesebb zaj | érzi | látja | **igen** |
| D10 Native/AI váltó | — | **fő** (magyarázat) | küld a matcher FIRE után | — | váltó | **igen** |
| D11 MCP-paritás | kapu | kapu | kapu | kapu | kapu | **admin gépuser** |

---

## V1 csomag vs. később

**V1 üzleti csomag:** D0–D10 + D11, a lezárt sorrendben (kézbesítő → UI → gyűjtő → döntés).

**Nem V1:** újabb éles csatornák, nyilvános signup, digest, árnyék, hírszintű admin-stop.
