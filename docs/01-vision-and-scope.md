# Vízió és hatókör

## A probléma üzleti nyelven

Az emberek és a csapatok **lemaradnak** arról, ami számukra fontos a világban: breaking news, piaci mozgás, természeti katasztrófa.  
A hírfolyam túlzott, a releváns jel későn vagy egyáltalán nem érkezik, és ha megérkezik, gyakran **rossz csatornán** (email, amikor Slack kellene, vagy fordítva).

A termék ezt a rést zárja: a felhasználó megmondja, **mire figyeljen a rendszer**, a rendszer figyeli a világot, **eldönti**, mi számít, és **oda küldi** az értesítést, ahol a felhasználó tényleg meglátja.

## Egy mondatos ígéret

> Meghívunk, beállítod (vagy az admin élőben beállítja), mire vagy kíváncsi. Ha a világban történik valami, ami *neked* fontos, időben értesültsz — emailen, Slacken, később más csatornán is —, az admin mindig meg tudja mondani, *honnan jött, miért ment ki, kinek*, és ugyanez MCP-n is meghívható.

## Kiknek szól (lezárt)

**Meghívásos, többfelhasználós kör** — nincs nyilvános regisztráció. Minden user saját érdekeltséggel és preferenciával. A nyilvános fogyasztói növekedés és a tenant-os B2B self-service **kint** marad a V1-ből.

| Szerep | Mit vár |
|---|---|
| Meghívott felhasználó | Saját érdekeltség és preferencia, releváns jel, kevés más user zaja |
| Admin | Meghívás, élő átírás, nyomvonal userenként, motorállapot |
| Gép (MCP) | Admin jogú gépfelhasználó; minden funkció toolból |

## Mi van bent az első üzleti körben (V1)

- **Több user**, userenként JSON **kit** (érdekeltség + preferencia + hu/en szabályfile); új usernél default másolat.
- **Meghívásos** felvétel; a meghívott a másolatot szerkeszti.
- Az admin ugyanezt **menet közben** frissíti. Ami már a kézbesítőben van, **kimegy**.
- Döntés: **native vagy AI**, egy váltó — nincs árnyék.
- **Minden funkció MCP-n** hívható, **admin jogú gépuserrel**.
- Gyűjtés **15 perces cronnal**, folyamatosan; források: Telex + USGS + CoinGecko.
- A rendszer **legalább néhány valós világforrásból** hoz be információt.
- Minden bejövő anyag **közös, összehasonlítható alakra** kerül.
- A rendszer **userenként dönt** fontosságról és kézbesítésről, és **megindokolja**.
- Értesítés **emailen** és **Slack chatboton**. A user a kitben bármilyen csatornakombinációt kérhet; nincs digest.
- Új csatorna később **a mag újraírása nélkül** hozzáadható (V1-ben bizonyíték: a második csatorna).
- Az admin **végigkövet egy hírt** eredettől döntésen át kiküldésig, **címzettenként**.
- Az admin látja a **három motor** üzleti állapotát (fut-e, mit csinál, hol akad el).

## Mi van szándékosan kint a V1-ből

Ezek nem elvetett ötletek, hanem **későbbi szeletek**, hogy az első átadás tesztelhető maradjon:

- Tetszőleges számú éles csatorna (SMS, push, Teams, webhook) — a *képesség* bent van, a *többi csatorna* kint.
- Nyilvános self-service regisztráció, fizetés, csomagok (a meghívásos saját-beállítás **bent** van).
- Árnyéküzemmód (nem használjuk).
- Napi összefoglaló, hírszintű admin-felülbírálat.
- Közösségi média teljes lefedése, dark web, zárt adatvendorok.
- Jogi/compliance tanúsítvány, 24/7 SLA-vállalás.
- Szerkesztői CMS, cikkírás, hírlevél-készítés.
- Felhasználói közösség, megosztott watchlist marketplace.

## Nem-célok (hogy ne csússzon szét)

- Nem híroldal és nem „még egy RSS-olvasó”.
- Nem általános scrape-platform tetszőleges webes adatokra.
- Nem általános workflow-motor (nincs tetszőleges üzleti folyamat-szerkesztő az első körben).
- Nem helyettesíti a katasztrófavédelmi vagy tőzsdei hivatalos riasztórendszereket. A termék **jelzést** ad, nem hatósági forrás.

## Érték, amit mérni kell (irány)

A részletes kész-kritériumok: [08-success-criteria.md](08-success-criteria.md). Üzletileg négy dolog számít:

1. **Relevancia** — a kiküldött riasztás *annak a usernek* az érdekeltségéhez illeszkedik, nem egy közös listához.
2. **Időbeliség** — a fontos jel nem késik annyit, hogy értéktelen legyen.
3. **Zajszint** — a felhasználó nem fullad bele a közepes hírekbe.
4. **Magyarázhatóság** — egy riasztásra megmondható: honnan, miért, kinek, melyik csatornán.

## Rögzített működési kép (a három motor)

```
Világ / források
        │
        ▼
  Gyűjtőmotor     →  nyers anyag + normalizált esemény
        │
        ▼
  Döntésmotor     →  fontos-e, kinek (userenként), milyen csatornán, miért
        │
        ▼
  Kézbesítőmotor  →  email / Slack / későbbi csatornák
        │
        ▼
  Felhasználók (saját érdekeltség + preferencia)

        └── Admin: nyomvonal + motorállapot + meghívás + élő preferencia
        └── MCP: minden fenti funkció gépből
```

Ez a kép a szerződés a szeletek között: egy szelet **egy szakaszt** visz átadható minőségig, nem az egész láncot egyszerre.
