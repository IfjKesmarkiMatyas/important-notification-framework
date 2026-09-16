# Döntésmotor

## Üzleti felelősség

A normalizált esemény és a user **pillanatnyi JSON kitje** alapján eldönti:

1. **Fontos-e** (szint) **ennek a usernek**,
2. **Kinek** (A igen, B nem — normál eset),
3. **Milyen csatornán** — **pontosan ahogy a user a kitben kérte** (bármit csinálhat, nincs rendszer-kényszer),
4. **Miért** (magyarázat userenként),
5. **egy** aggyal: **native vagy AI**, a **váltósetting** szerint.

A kimenet: döntés + kézbesítési megbízás(ok). A küldést a kézbesítő végzi. Ami a deliverybe került, kimegy.

## Egy agy egy időben (nincs árnyék)

| Mód | Mikor fut | Küld-e |
|---|---|---|
| **Native** | A váltó `native` | Igen, a user `rules.hu` / `rules.en` file-ja szerint |
| **AI** | A váltó `ai` | FIRE a matcher szerint; a modell csak `{reason}`-t ír. Kulcs nélkül fail-closed. |
| Árnyék | **Nincs** | — |
| Hírszintű admin stop | **Nincs** | — |

A váltó **rendszer-setting** (admin + MCP). A következő döntés a beállított aggyal megy; a másik **nem** fut mellé, nem hasonlítgatjuk élőben.

A native szabály **userenként és nyelvenként** file: `rules.hu.json`, `rules.en.json` (default másolat, utána szerkeszthető). Magyar eseménynél / magyar kimenetnél a hu, angolnál az en. Az AI **nem** dönt FIRE/NO-t.

## Amit tudnia kell (V1)

- Mindhárom család: breaking, piac, katasztrófa.
- Zárt típus + szűrő a kitből (hely, küszöb, instrumentum, téma).
- Userenkénti illesztés; szüneteltetett / meg nem hívott: nincs megbízás.
- Csatorna = a kit `preferences.channels` (email és/vagy Slack chatbot, tetszőleges).
- Magyarázat; native-nál ismételhető ugyanarra a kit+esemény párra.
- Dedup: ugyanaz a történést, userenként egyszer (`user_id, source_id, external_id`); D9 világesemény-összevonás később.
- AI: csak magyarázat, FIRE a matcherből.

## Amit szándékosan nem csinál

- Nem olvas nyers HTML-t.
- Nem hív email/Slack API-t.
- Nem árnyékol.
- Nem vár admin „engedélyezd ezt a hírt” gombra.
- Nem digestel.

## Szerződés

| Irány | Szerződés |
|---|---|
| Gyűjtő → döntés | Normalizált esemény + eredet. |
| Kit → döntés | Érdekeltség, preferencia, `rules.hu` / `rules.en`, aktív-e. |
| Admin → döntés | Native/AI váltó. |
| Döntés → kézbesítő | Megbízás: user, csatorna a kit szerint, tartalom, fontosság. |
| Döntés → admin | Döntés userenként, magyarázat, **melyik agy** (a váltó állása), döntéskori kit-pillanat. |
| Döntés → MCP | Váltó, döntés, bedobott teszt — admin gépuser. |

## Tesztelhetőség egyedül

Bedobott normalizált eseményekkel, élő scrape nélkül. A kézbesítő már létezik (1. fázis), ide megbízás megy.

| # | Esemény | Kit | Elvárt |
|---|---|---|---|
| 1 | Földrengés 6.4, USGS | A: disaster ≥6, email+slack | A: kimegy mindkettőre |
| 2 | ugyanaz | B: csak market | B: nem megy |
| 3 | Földrengés 4.1 | A: ≥6 | A: nem |
| 4 | Bitcoin +8% | B: market 5%, csak slack | B: csak chatbot |
| 5 | Telex breaking, A szünet | inaktív kit | nincs új megbízás |
| 6 | Váltó `ai` | ugyanaz mint 1 | FIRE a matcher szerint; a magyarázat AI-tól; native nem fut *árnyékként* mellé |

Pass: 1–5 native-on; 6 a váltót bizonyítja. Nincs „mindkét agy listája” képernyő.
