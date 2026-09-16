# Felhasználók, érdekeltség, preferencia — JSON kit

## Üzleti felelősség

Több, egymástól független felhasználó van. Mindegyiknek saját **JSON kitje**: érdekeltség (mire) + preferencia (hogyan) + nyelvenkénti szabályfile (hu / en).  
A user a **sajátját** szerkeszti. Az admin **meghív**, és **menet közben** átírhatja. Új usernél a **default kit** másolódik le, utána lehet bővíteni.

A döntésmotor a **pillanatnyi** kitfile-okat olvassa.

## A készlet (file-ok)

Bővíthető, szerkeszthető JSON. Nem kód, nem titkos séma — leíró adat.

| File | Szerep |
|---|---|
| `default-kit.json` | Sablon: preferencia + zárt típusú érdekeltségek |
| `default-rules.hu.json` | Native szabályok magyarul |
| `default-rules.en.json` | Native szabályok angolul |
| user másolata ugyanebből | Meghívás után: a default **lemásolódik**, a user/admin ezt szerkeszti |

A mezőkészlet bővíthető (új szűrő, új csatornaflag), anélkül hogy a három motort újra kellene mesélni. A zárt típusok V1-ben: `breaking`, `market`, `disaster`.

**Példa (üzleti váz, nem implementáció):**

```json
{
  "version": 1,
  "preferences": {
    "channels": { "email": true, "slack": true },
    "email": "user@example.com",
    "slack": { "mode": "chatbot" }
  },
  "interests": [
    { "type": "disaster", "kind": "earthquake", "minMagnitude": 6 },
    { "type": "market", "instrument": "bitcoin", "movePercent": 5 },
    { "type": "breaking", "topics": ["belfold"] }
  ]
}
```

A `rules.hu.json` / `rules.en.json` ugyanennél a usernél mondja meg native módban, melyik interest milyen szintet kap. AI módban a váltó a döntésmotort állítja; a kit ettől még a „kinek / milyen csatornán” forrása.

## Meghívásos felvétel

- Nincs nyilvános regisztráció.
- Meghívás → default kit + default rules.hu + default rules.en **másolat**.
- A meghívott a másolatot szerkeszti (csatornát ki/be, küszöb, téma).
- Meghívás nélkül nincs értesítés.
- A meghívó **levelet a kézbesítőmotor** küldi. Terv: [10-delivery-ui-users/invite-flow.md](../10-delivery-ui-users/invite-flow.md).

## Amit a usernek tudnia kell (V1)

- Belépés meghívás után.
- Saját kit: érdekeltség hozzáad / módosít / szüneteltet / töröl / bővít.
- Preferencia: email, Slack chatbot, bármilyen kombináció (kritikusan is kikapcsolhatja az egyiket).
- Nincs digest-kapcsoló.
- Csak a saját kitjét látja.
- hu / en szabályfile a saját másolatán szerkeszthető.

## Amit az adminnak tudnia kell (V1)

- Meghívás, visszavonás, inaktiválás.
- Bármely user kitjének és szabályfile-jainak olvasása / írása.
- Default sablon szerkesztése (a *következő* új user másolata ezt kapja; a meglévőkét nem írja felül magától).
- Native / AI **váltó** a döntésmotoron (rendszer-setting, nem user-kit).
- Nincs hírszintű stop gomb.

## Élő frissítés

| Állapot | Hatás |
|---|---|
| Még nincs döntés | Az új kit számít |
| A megbízás **már a deliveryben van** | **Kimegy**, nem hívjuk vissza |
| Szüneteltetés | Új megbízás nem születik; a deliveryben lévő lefut |

## Amit szándékosan nem ez a képesség

- Nem dönt (döntésmotor).
- Nem küld (kézbesítő).
- Nem scrape-el.
- Nem napi összefoglaló.

## Szerződés

| Irány | Mit ad / mit vár |
|---|---|
| → Döntés | Aktuális kit + `rules.hu` / `rules.en` + native/AI váltó állása |
| → Kézbesítő | Az adott user célpontjai a kitből (email, chatbot) |
| → Admin / UI | Lista, meghívás, JSON szerkesztés, default másolás |
| → MCP | Admin gépuser: meghívás, kit írás/olvasás |

## Tesztelhetőség

1. Meghívás → a kit a default másolata, nem üres.
2. A és B másolata szétválik szerkesztés után.
3. Admin átírja A `channels.email` flagjét; A látja; ami már deliveryben volt, kimegy.
4. Ugyanez MCP-n, admin gépuserrel.
