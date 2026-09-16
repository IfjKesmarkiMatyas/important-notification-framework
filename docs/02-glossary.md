# Közös nyelv

Ha ezek a szavak mást jelentenek a beszélgetésben, a szeletek nem tesztelhetők egymástól függetlenül.  
Ez üzleti szótár, nem technikai séma.

## Alapszereplők

| Fogalom | Jelentés |
|---|---|
| **Felhasználó** | Meghívott személy, saját érdekeltséggel és preferenciával; értesítést kap. Több van belőle, a beállításaik nem keverednek. |
| **Admin** | Meghív, élőben írja bármely user beállításait, látja a motorokat és a nyomvonalat. |
| **Meghívás** | Az egyetlen mód, ahogy user bekerül. Nincs nyilvános regisztráció. |
| **JSON kit** | A user érdekeltsége + preferenciája egy bővíthető JSON-ban. Új usernél a default **másolódik**. |
| **Szabályfile** | Native magyarázathoz / szinthez userenként `rules.hu.json` és `rules.en.json` (szintén default másolat). |
| **Érdekeltség** | *Mire* figyeljen (zárt típus: breaking / piac / katasztrófa + szűrők). |
| **Preferencia** | *Hogyan* értesüljön (email, Slack chatbot, tetszőleges kombináció). |
| **Élő frissítés** | A következő döntés az új kitet használja. Ami már deliveryben van, kimegy. |
| **Váltó** | Rendszer-setting: a **magyarázat** native *vagy* AI. FIRE mindig a matcheré. Nincs árnyék. |
| **MCP-elérés** | Admin jogú gépfelhasználó hívja ugyanazokat a funkciókat. |
| **Csatorna** | Az értesítés *hová* megy: email, Slack, később bármi más. A csatorna cserélhető képesség, nem a termék magja. |
| **Forrás** | Honnan jön az információ: híroldal, piaci feed, katasztrófa-közlemény, API, HTML oldal, stb. |

## A lánc objektumai

| Fogalom | Jelentés | Miért kell |
|---|---|---|
| **Riasztási szándék** | A user JSON kitje (érdekeltség + preferencia + szabályfile-ok). | E nélkül a döntésnek nincs „kinek”. |
| **Nyers felvétel** | Amit a gyűjtőmotor *ahogy van* behúzott (oldal, feed-elem, API válasz). | Az eredet bizonyítéka. |
| **Normalizált esemény** | A nyers felvétel közös üzleti alakja: mi történt, hol, mikor, milyen típus, milyen forrásból. | A döntésmotor csak ezt hasonlítja össze. |
| **Világesemény** | Ugyanaz a valós történést jelentő dolog, akkor is, ha több forrás ír róla. | Deduplikáció, „ne pingelj kétszer ugyanarról”. |
| **Döntés** | A motor válasza **userenként**: fontos-e, milyen csatornán, milyen súllyal, **miért**. Ugyanaz az esemény A-nak mehet, B-nek nem. | A termék szíve. |
| **Kézbesítési megbízás** | Konkrét utasítás: ezt a döntést ennek a felhasználónak ezen a csatornán küldd ki. | A kézbesítés ettől válik tesztelhetővé. |
| **Értesítés** | Ami tényleg megérkezett (vagy meghiúsult) a csatornán. | A felhasználó ezt éli meg. |
| **Nyomvonal** | Egy történet lánca: nyers felvétel → normalizált esemény → döntés(ek) → megbízás(ok) → értesítés(ek). | Az admin ígérete. |

## Fontosság és zaj

| Fogalom | Jelentés |
|---|---|
| **Fontosság** | Nem bináris „igen/nem” a célállapotban, hanem **szint**: pl. kritikus / magas / közepes / alacsony. A csatornaválasztás ehhez igazodik. |
| **Zaj** | Olyan értesítés, amit a felhasználó nem kért, vagy túl gyakran kap ugyanarról. A V1 ellensége. |
| **Kihagyás** | Olyan esemény, amit a felhasználó elvárt volna, de nem kapott. A másik ellenség. |
| **Magyarázat** | Emberi nyelven: *miért* lett fontos / miért nem ment ki / miért Slack és nem email. |

## Motorok

| Fogalom | Üzleti felelősség | Nem felelőssége |
|---|---|---|
| **Gyűjtőmotor** | Forrásból információt hozni, eredetet megőrizni, **normalizált eseményt** adni. | Eldönteni, hogy fontos-e. Kiküldeni. |
| **Döntésmotor** | Fontosság, címzett, csatorna, magyarázat. FIRE a matcheré; a magyarázat native sablon **vagy** AI, váltóval. | Magát a weblapot olvasni. A Slack/email API-t hívni. A FIRE felülírása modellel. |
| **Kézbesítőmotor** | Megbízást csatornára vinni, státuszt visszajelezni (elküldve / sikertelen / késleltetve). | Újradönteni a fontosságot. |

A motorok **szerződéses határa** a tesztelhetőség feltétele: egy motor hibája ne tegye értelmezhetetlenné a másikat.

Minden motor- és felületfunkció **MCP-n** is a fenti határokat tartja: az MCP nem negyedik motor.

## Döntésmódok

| Fogalom | Jelentés |
|---|---|
| **Szabályalapú (native) magyarázat** | FIRE után a szint + sablon-indok a user `rules.hu` / `rules.en` file-jából. |
| **AI magyarázat** | Ugyanaz a FIRE/NO; a `{reason}` a modellé. Csak ha a váltó `ai`. A modell **nem** dönt kimenetelt. |
| **Matcher** | Kit küszöb / téma / állapot → FIRE vagy NO. Mindkét váltóálláson ez a kapu. |
| **Váltó** | Egy időben egy magyarázat-mód. Árnyék **nincs**. Hírszintű felülbírálat **nincs**. |

## Admin

| Fogalom | Jelentés |
|---|---|
| **Irányítópult** | A motorok üzleti egészsége: van-e friss felvétel, várakozik-e döntés, akad-e a kézbesítés. |
| **Nyomvonalnézet** | Egy konkrét hír/esemény teljes útja, **címzettenként**. |
| **Userkezelés** | Meghívás, lista, érdekeltség/preferencia élő írása. |
| **Forrásbizalom** | Üzleti jelzés, hogy egy forrás mennyire megbízható. Befolyásolhatja a fontosságot. |

## Szelet

**Szelet** = olyan üzleti képesség, amit:

- el lehet határolni a többitől,
- **önmagában** be lehet mutatni,
- van elfogadási története („ezt láttam, ez kész”),
- nem kell hozzá a teljes termék.

A szeletek listája: [05-testable-slices.md](05-testable-slices.md).
