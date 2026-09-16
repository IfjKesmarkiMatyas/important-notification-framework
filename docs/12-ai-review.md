# AI-irányítás: verdict + review log

A nyers promptok: [transcript-user-prompts.md](transcript-user-prompts.md) (32 tétel). A teljes export: [transcript.md](transcript.md).

Ez a lap **nem** chatlog. Az értékelőnek két kérdést válaszol:

1. Mit kértél, mit adott a modell, mit tartottunk meg.
2. Melyik feltételezést utasítottuk el, melyik kódot írtuk újra.

A termékdöntések külön: [07-questions-and-recommendations.md](07-questions-and-recommendations.md). Itt a **bírálat**.

Verdict: **accept** / **reject** / **rewrite**. A 32-ből a kurzusváltások vannak a táblában. A rutin (commit, tesztfuttatás, UI-pixel) alul egy sor.

---

## Verdict — a 32 prompt indexe

| # | Mit kértél | Mit adott az AI | Verdict | Hol |
|---|---|---|---|---|
| 1 | Brief + „még ne implementálj”; deliverable, terv, szeletek | Üzleti szerződés, kód nélkül | **accept** | [01](01-vision-and-scope.md)–[08](08-success-criteria.md) |
| 2 | README a `docs`-ba | `docs/README.md` | **accept** | [docs/README.md](README.md) |
| 3 | Több user, élő kit, MCP, meghívás | D1 / D11 a szerződésbe | **accept** | [D1](03-business-deliverables.md), [S1a](05-testable-slices.md) |
| 4 | Vedd ki az árnyékot; JSON kit; sorrend: kézbesítő → UI → gyűjtő → döntés; 17 lezárt kérdés | Az **1. körös tervben még árnyék** volt (AI második agy, éles küldés nélkül) | **reject** (árnyék); a többi **accept** | döntés 5, [D10](03-business-deliverables.md) |
| 6 | Tervezd a deliveryt, UI-t, userkezelést | Fázis 1–2 terv | **accept** | [10-delivery-ui-users](10-delivery-ui-users/) |
| 7 | Ne doksit, implementációs tervet | Terv a kód előtt, külön a 6-ostól | **accept** | fázis 1–2 plan |
| 8 | Adj Pushover csatornát | Harmadik adapter | **accept** mint bővíthetőség; **nem** V1-ígéret (éles marad email + Slack) | `DeliveryChannelType.pushover` |
| 11 | Űrlap-varázsló, kártyás érdekeltség, slider csatorna | Kit UI, nem nyers JSON-szerkesztő | **accept** | Angular kit wizard |
| 15 | Tervezd a gyűjtőt (források, UI, MCP) | Fázis 3 terv | **accept** | [06 scrape](06-capabilities/scrape-engine.md) |
| 19 | Scrape korpusz → izolálj → golden truth a döntéshez | 13 esemény × 5 user = 65 minta, 7 FIRE / 58 NO | **accept** | [11-decision-fixtures](11-decision-fixtures/) |
| 20 | DTO / entity `common`-ba; modulok csak üzleti logika | Modulhatár | **accept** | `backend/common` |
| 22 | Pontossági napló, F-értékek | `accuracy.md` séma + későbbi mért sor | **accept** | [accuracy.md](11-decision-fixtures/accuracy.md) |
| 24–26 | Mérhető, újratesztelhető native+AI motor | Első javaslat: az AI **dönthet** FIRE/NO-t (felülírhatja a küszöböt) | **rewrite** | döntés 23, [S11](05-testable-slices.md) |
| 27–28 | Implementáld a döntést; tökéletesítés + értékelés a tesztállományon | Matcher FIRE, AI `{reason}`, native F₁ = 1.00 | **accept** a rewrite után | `InterestMatcher`, `AiExplainer` |
| 29 | Tower: system / inbox / story | Háromoszlopos nyomvonal | **accept** | [admin-control-tower](06-capabilities/admin-control-tower.md) |
| 32 | README tények + betölthető tesztállomány | How to start, station script | **accept** | [README.md](../README.md), [station/](../station/) |

**Rutin, nincs külön bírálat:** 5, 9–10, 12–14, 16–18, 21, 23, 25, 30–31 (commit/push, unit teszt, branding, gombméret, MCP smoke, backend indítás, GPT kulcs bekötés).

A nyers szövegek: [transcript-user-prompts.md](transcript-user-prompts.md).

---

## Review log — amit elutasítottunk / újraírtunk

Hat tétel. A tábla *melyik prompt*; ez a *miért*. A kód és a kézikönyv a bizonyíték.

### R1 — AI mint FIRE-agy → rewrite (24–28)

**Feltételezés, amit a modell hozott:** a döntésmotor két agy, és AI módban a modell mondja meg, kimegy-e.

**Miért utasítottuk el:** a golden F₁ szétesik egyetlen promptcserén (Ada B1, Elena B4). A brief „something important” nem modell-osztályozó.

**Ami készült:** `InterestMatcher` adja a FIRE/NO-t mindkét váltóálláson. `AiExplainer` csak `{reason}`-t ír, a `Verdict` már megvan. Kulcs nélkül fail-closed (`IllegalStateException`, nyomvonal `error`, nincs ALERT).

**Doksi:** döntés 23, [D10](03-business-deliverables.md), [S11](05-testable-slices.md) Fail = „az AI felülírja a FIRE-t” — ez *nem* a jelenlegi viselkedés.

### R2 — Árnyéküzemmód → reject (4)

**Feltételezés az 1. körös tervben:** a szabály küld, az AI árnyékban összevethető.

**Miért utasítottuk el:** a 4-es prompt explicit. Élőben egy mód indokol és küld; a másik nem fut mellé.

**Ami készült:** egy `native` | `ai` setting. Nincs árnyéklista a Toweren.

**Doksi:** döntés 5.

### R3 — „Majd az AI megoldja a fontosságot” → reject (S0)

**Shortcut, amit elkaptunk:** vitás kártyánál a modellre bízni, hogy breaking-e.

**Ami készült:** zárt típus + kit küszöb; native F₁ = **1.00** a 65 mintán, 0 tévesztés. Az AI magyarázata **nincs** az F₁-ben.

**Doksi:** [golden-truth.md](11-decision-fixtures/golden-truth.md), [accuracy.md](11-decision-fixtures/accuracy.md).

### R4 — Pushover mint 3. csatorna → accept, V1-ígéret kint (8)

**Amit a brief kér:** „flexible enough that we can add more channels later.”

**Amit nem kért:** harmadik *éles* csatorna a V1 demóban.

**Ami készült:** `DeliveryChannelType.pushover` adapter. Token nélkül a job `failed` — őszinte, nem hamis siker. Éles út: email + Slack chatbot.

### R5 — D9 világesemény-összevonás → később (19, station)

**Shortcut, amit nem engedünk „kész”-nek:** két forrás ugyanarról = egy riasztás.

**Ami van:** userenkénti dedup `(user_id, source_id, external_id)`. CoinGecko `instrument@now` minden cronban új `external_id` — zaj, nem D9.

**Doksi:** [D9](03-business-deliverables.md) szándékosan később.

### R6 — A Toweren a modell-szöveg a reason, nem a FIRE

**Amit egy értékelő a képen félreolvashat:** AI-váltó be, magyar modell-bekezdés a kártyán → „az AI döntött.”

**Ami a nyomvonalon van:**

- Bitcoin 0% → mindenki **NO**, „No matching interest”. [kézikönyv](Notif-admin-feluletek-es-Tower-dontesi-nyomvonal-attekintese/KEZIKONYV.hu.html) `080016-00035-ui_action-after.png`
- Telex belföld → Ada **FIRE**, Béla **NO**. `111764-00073-ui_action-after.png`
- A magyar indoklás az **explainer**; a FIRE a kit `belfold` / küszöb illesztése.

Ez R1 látható bizonyítéka, nem külön döntés.

---

## Mit nem tartalmaz ez a lap

- A 32 prompt teljes szövegét (az a [transcript-user-prompts.md](transcript-user-prompts.md)).
- Termékdöntési logot (az a [07](07-questions-and-recommendations.md)).
- A későbbi értékelő-szál nyers exportját — ez a lap abból a szálból *született*, a 32-es építő exportban nincs benne.
