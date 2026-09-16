# Tesztelhető szeletek

Egy szelet akkor jó, ha **külön átadható, külön elutasítható**, és van olyan ember, aki kód nélkül megmondja: pass / fail.

Minden szelet tartalmazza:

- melyik deliverable-t viszi,
- ki teszteli,
- a történetet,
- a pass/fail kritériumot,
- mit *nem* kell még tudnia,
- a függőséget.

Részletes viselkedés a motoroknál: [06-capabilities/](06-capabilities/).

**Állandó rúd minden szeleten (D11):** a történet UI-n (ahol van UI) **és** MCP-n is pass. Az MCP-t **admin jogú gépfelhasználó** hívja.

---

## S0 — Szabálykönyv és példatár

| | |
|---|---|
| Deliverable | D0 |
| Tesztelő | Terméktulajdonos + 1-2 leendő felhasználó / admin |
| Függőség | Nincs |
| Nem kell | Futó rendszer |

**Történet:** kapunk 12 hírkártyát (breaking, piac, katasztrófa, irreleváns zaj). Közösen rászabjuk a szintet és a csatornát.

**Pass:** a kártyák 80%+ ugyanoda esnek; a vitásak fel vannak jegyezve szabálynak, nem „majd az AI megoldja”.

**Fail:** „attól függ” minden kártyánál, és nincs leírt kivétel.

---

## S1 — Saját érdekeltség és preferencia

| | |
|---|---|
| Deliverable | D1 |
| Tesztelő | Felhasználó |
| Függőség | S0 (legalább a V1 témakörök), S1a (meghívott user) |
| Nem kell | Élő hír, küldés |

**Történet:** a meghívott a default kit másolatán létrehoz egy érdekeltséget: „6.0 feletti földrengés”, csatorna: Slack chatbot és email. Elmenti, szünetelteti. A másik user kitjét **nem** látja.

**Pass:** amit beírt, azt kapja vissza; szüneteltetés után a rendszer *üzletileg* nem ígér küldést neki; MCP-n ugyanaz.

**Fail:** csak egy szabad szövegmező van; vagy minden user ugyanazt a listát látja.

**Tesztelhető izoláltan:** igen, mock döntéssel / küldés nélkül.

---

## S1a — Meghívásos felvétel

| | |
|---|---|
| Deliverable | D1 |
| Tesztelő | Admin + meghívott |
| Függőség | — (S0 nélkül is mutatható) |
| Nem kell | Élő hír |

**Történet:** admin meghív egy új usert. A kit a **default másolata**, nem üres. Meghívás előtt nincs belépés. Elfogadás után a másolat szerkeszthető. MCP-n admin gépuserrel is meghívható.

**Pass:** nincs nyilvános „regisztrálok” út; a meghívott a sajátját állítja.

**Fail:** bárki beregisztrál, vagy a meghívott csak az admin által kitöltött listát kapja, saját írás nélkül.

---

## S1b — Két user, két kimenet

| | |
|---|---|
| Deliverable | D1 + D3 |
| Tesztelő | Terméktulajdonos |
| Függőség | S1, S3 (S3 lehet bedobott esemény) |
| Nem kell | Slack, AI |

**Történet:** A Japán-katasztrófára iratkozott, B piacra. Ugyanaz a földrengés-esemény: A kap (vagy kapna) jelzést, B nem. Az adminon mindkét út látszik.

**Pass:** nincs keresztküldés; a magyarázat userenként szól.

**Fail:** broadcast mindenkinek, vagy B megkapja A érdekeltségét.

---

## S1c — Admin élő frissítés

| | |
|---|---|
| Deliverable | D1 |
| Tesztelő | Admin + user |
| Függőség | S1 |
| Nem kell | Új motor |

**Történet:** A érdekeltsége katasztrófa. Admin menet közben átírja a kitet. A **következő** esemény az új szerint dől el. Ha egy megbízás **már deliveryben van**, az **kimegy**. MCP-n ugyanaz.

**Pass:** nincs újraindítás; a bent lévő küldés lefut; a következő döntés az új kit.

**Fail:** a döntés a régit használja újraindításig, vagy a deliveryben lévő megbízást eldobják.

---

## S2 — Egy forrás → olvasható normalizált esemény

| | |
|---|---|
| Deliverable | D2 (első típus) |
| Tesztelő | Admin + terméktulajdonos |
| Függőség | S0 |
| Nem kell | Döntés, user, csatorna |

**Történet:** a kiválasztott első forrásból bejön egy valós (vagy rögzített élő) elem. Látjuk a nyers eredetet és a normalizált „mi / hol / mikor / típus” alakot.

**Pass:** két ember ugyanazt olvassa ki a normalizált eseményből; az eredet megvan.

**Fail:** csak nyers HTML/XML van, vagy a normalizálás minden alkalommal más mezőket jelent.

**Tesztelhető izoláltan:** igen. A gyűjtőmotor önálló átadása.

---

## S3 — Szabálydöntés magyarázattal

| | |
|---|---|
| Deliverable | D3 |
| Tesztelő | Terméktulajdonos |
| Függőség | S0, S1, S2 (S2 lehet rögzített mintaesemény is) |
| Nem kell | Valódi email/Slack |

**Történet:** bekötünk 5 ismert eseményt két usernél (3 kellene, hogy menjen A-nak, 2 nem / B-nek más). A motor szintenként, **userenként** címzettet, csatornajavaslatot és *miért*-et ad.

**Pass:** a bank a szabálykönyv szerint viselkedik userenként; a magyarázat embernek szól; MCP-n a döntés lekérdezhető.

**Fail:** igen/nem pipa magyarázat nélkül; minden user ugyanazt kapja; ugyanarra a bemenetre + user-állapotra hol így, hol úgy dönt.

**Tesztelhető izoláltan:** igen, ha a normalizált eseményt „bedobjuk” külső gyűjtés nélkül.

---

## S4 — Email megérkezik

| | |
|---|---|
| Deliverable | D4 |
| Tesztelő | Felhasználó (tesztfiók) |
| Függőség | S3 (vagy kézzel adott kézbesítési megbízás) |
| Nem kell | Slack, AI, több forrás |

**Történet:** egy kritikus döntés után a tesztfiókban ott a levél, érthető tárggyal és indoklással.

**Pass:** a levél megjön ésszerű időn belül; hibás címnél a státusz „sikertelen”, nem csend.

**Fail:** „a motor elküldte”, de a postaládában nincs semmi, és ezt senki nem látja.

**Tesztelhető izoláltan:** részben. A kézbesítőmotor kaphat *kész megbízást* döntés nélkül — ez szándékos, így a csatorna külön is minősíthető.

---

## S5 — Slack chatbot megérkezik

| | |
|---|---|
| Deliverable | D5 |
| Tesztelő | Felhasználó (tesztcsatorna) |
| Függőség | Ugyanaz a megbízás-modell, mint S4 |
| Nem kell | Új döntéslogika |

**Történet:** ugyanaz a kritikus esemény a Slack **chatbot** üzeneteként megjelenik (Notif bot).

**Pass:** az üzenet ott van; az adminon ez *Slack-kézbesítés*, nem különtermék.

**Fail:** a döntésmotor Slackre van írva, vagy az email sablonja van bemásolva Slackre érthetetlenül.

**Tesztelhető izoláltan:** igen, kész megbízással, email nélkül is.

---

## S6 — A user kitje a csatornapolicy

| | |
|---|---|
| Deliverable | D6 |
| Tesztelő | User |
| Függőség | S4 és S5 |
| Nem kell | Digest, rendszer-kényszer |

**Történet:** A a kitben kikapcsolja az emailt, csak chatbotot kér. Kritikus esemény → csak chatbot. B csak emailt kér → csak email.

**Pass:** a rendszer nem kényszerít második csatornát; nincs digest.

**Fail:** „kritikus = mindkettő”, vagy napi összefoglaló.

---

## S7 — Egy hír teljes nyomvonala az adminon

| | |
|---|---|
| Deliverable | D7 |
| Tesztelő | Admin |
| Függőség | Legalább S2–S4; ideális S5–S6 |
| Nem kell | AI, dedup |

**Történet:** az admin kap egy azonosítót vagy keres egy címet: „Tokió, 6.4”. Végigmegy: forrás → normalizálás → döntés+indok **userenként** → megbízás → email/Slack státusz. MCP-n lekérdezve ugyanaz a lánc.

**Pass:** egyetlen szakadás sincs a láncban; a „miért nem ment ki *ennek a usernek*” is látszik; a döntéskori szándék látszik (élő átírás után is értelmezhető).

**Fail:** külön logok három helyen, az admin nem tudja összekötni.

**Tesztelhető izoláltan:** a nézet tesztelhető rögzített lánccal is (még élő motorok nélkül) — majd élő adattal újratesztelve.

---

## S8 — Motorok egészsége az adminon

| | |
|---|---|
| Deliverable | D8 |
| Tesztelő | Admin / ops |
| Függőség | S2, S3, S4 legalább mintaként |
| Nem kell | Szép dashboard, ha a kérdésre válaszol |

**Történet:** szándékosan megállítunk / elrontunk egy forrást vagy egy csatornát. Az admin **üzleti nyelven** látja: „ez a forrás 3 órája hallgat”, „Slack küldés bukik”.

**Pass:** a hiba helye (gyűjtés / döntés / kézbesítés) kitalálható az irányítópultról, debug nélkül.

**Fail:** csak „rendszer OK” / „rendszer nem OK”.

---

## S9 — Második forrástípus (interface-ígéret)

| | |
|---|---|
| Deliverable | D2 bővítés |
| Tesztelő | Terméktulajdonos |
| Függőség | S2 (az első típus már ad normalizált eseményt) |
| Nem kell | Új döntésmotor |

**Történet:** egy HTML (vagy más, az elsőtől eltérő) forrásból **ugyanolyan** normalizált esemény születik.

**Pass:** a döntésmotor nem tudja (és nem is kell tudnia), hogy feed volt-e vagy scrape; ugyanúgy dönt.

**Fail:** a második típus külön, csonka mezőkészletet ad, a döntés csak az első típusra megy.

---

## S10 — Két forrás, egy világesemény

| | |
|---|---|
| Deliverable | D9 |
| Tesztelő | Felhasználó + admin |
| Függőség | S9, S3, S4 |
| Nem kell | AI |

**Történet:** ugyanaz a katasztrófa két forrásból. A user egy (vagy indokolt update) pinget kap. Az admin mindkét eredetet látja egy esemény alatt.

**Pass:** nincs dupla riasztás azonos történéssre; a második forrás nem vész el a nyomvonalon.

**Fail:** két email ugyanarról, vagy a második forrás „eltűnik”, mert összecsúsztattuk.

---

## S11 — Native / AI váltó

| | |
|---|---|
| Deliverable | D10 |
| Tesztelő | Admin |
| Függőség | S3 |
| Nem kell | Árnyék, párhuzamos agy |

**Történet:** a **FIRE/NO mindig a matcheré** (kit küszöb / téma). A váltó csak a **magyarázatot** állítja. `native` — sablon-indok a `rules.hu` / `rules.en` szerint. `ai` — ugyanaz a FIRE/NO, a `{reason}` a modelltől; kulcs nélkül fail-closed (nincs küldés, nyomvonal `error`). A másik mód **nem** fut mellé, nincs árnyéklista.

**Pass:** ugyanaz az esemény + kit mindkét álláson **ugyanazt** a FIRE/NO-t adja; a nyomvonalon látszik a váltó (`native` / `ai`); `ai` + hiányzó kulcs → `error`, nincs ALERT.

**Fail:** az AI felülírja a FIRE-t vagy a NO-t; mindkét agy kimenete élőben kimegy (árnyék); `ai` módban a modell dönt, a matcher nem fut.

---

## Szeletmátrix — ki mit érez

| Szelet | User érzi | Admin érzi | Motor, ami „főszereplő” | MCP |
|---|---|---|---|---|
| S0 | még nem | még nem | — | nem kell |
| S1 | **igen** (saját) | látja / írhatja | — | **igen** |
| S1a | meghívott | **igen** | — | **igen** |
| S1b | **igen** (A vs B) | **igen** | Döntés | **igen** |
| S1c | látja az újat | **igen** | — | **igen** |
| S2 | nem | **igen** | Gyűjtő | **igen** |
| S3 | közvetve | **igen** | Döntés | **igen** |
| S4 | **igen** | státusz | Kézbesítő | **igen** |
| S5 | **igen** | státusz | Kézbesítő | **igen** |
| S6 | **igen** | **igen** | Döntés + kézbesítő | **igen** |
| S7 | nem | **igen** | mind + admin | **igen** |
| S8 | nem | **igen** | mind + admin | **igen** |
| S9 | nem | **igen** | Gyűjtő | **igen** |
| S10 | **igen** | **igen** | Döntés (+ gyűjtő) | **igen** |
| S11 | nem | **igen** | Döntés | **igen** |

---

## Mini-UAT csomag (V1)

Ha csak egyszer tesztelünk „kész”-re, ez a 8 történet elég a V1 csomagra (D0–D8 + D10 + D11; D9 később):

1. Szabálykönyv kártyák (S0).
2. Meghívás, saját beállítás, két user elválik (S1a, S1, S1b).
3. Admin élő átírás, következő esemény az új szerint (S1c).
4. Élő/rögzített forrás → normalizált esemény (S2).
5. Kritikus esemény → A a kitje szerint email és/vagy chatbot, B nem (S3–S6, S1b).
6. Ami deliveryben volt átírás közben, az kiment (S1c).
7. Elrontott forrás vagy Slack → melyik motor (S8).
8. Meghívás, kit, nyomvonal, tesztküldés **MCP-n**, admin gépuserrel (D11).

A D9 a 4. fázisban **szándékosan később**. A D10 / S11 váltó **bent van**: ugyanaz a FIRE, más magyarázat.
