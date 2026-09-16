# Admin — irányítópult, nyomvonal, userek

## Üzleti felelősség

Az adminnak **négy** kérdése van, mind kód nélkül:

1. **Mi van ezzel az egy hízzel?** Honnan jött, mit döntöttünk, kinek ment ki, megérkezett-e?
2. **Mi van a rendszerrel?** Melyik motor dolgozik, melyik hallgat, melyik csatorna bukik?
3. **Kik a userek, mire figyelnek?** Meghívás, érdekeltség, preferencia.
4. **Át tudom írni most?** Menet közben, a következő döntésre érvényesen.

Ez control tower **és** élő user-kezelés. Nem enterprise IAM-szvit, de nem is csak olvasható log.

A funkciók MCP-n ugyanúgy kellenek: [mcp-access.md](mcp-access.md).

## Nyomvonalnézet (D7) — egy történet, több címzett

Egy keresés / kiválasztás után az admin **időrendben** látja:

| Lépés | Amit látnia kell |
|---|---|
| Eredet | Forrás neve, idő, nyers bizonyíték vagy annak kivonata, link |
| Normalizálás | Típus, hely, idő, rövid leírás — ugyanaz, amit a döntés kapott |
| Világesemény (D9-től) | Melyik felvételek tartoznak össze |
| Döntés **usereként** | Szint, magyarázat, melyik agy (a **váltó** állása); A kapta / B nem, és miért |
| Megbízások | Userenként és csatornánként: kinek, mikor |
| Kézbesítés | Elküldve / sikertelen / várakozik, hiba oka |
| Negatív út | **Miért nem ment ki** (ennek a usernek) ugyanolyan elsőosztályú |
| Szándék pillanata | Melyik érdekeltség / preferencia volt érvényben a döntéskor (az élő átírás miatt) |

Pass: egy breaking hírre az admin 2 percen belül végigmeséli a láncot, **két user eltérő kimenetével** együtt.

## Motorállapot (D8) — a rendszer

| Motor | Kérdés az adminon |
|---|---|
| Gyűjtő | Mikor volt az utolsó sikeres felvétel forrásonként? Melyik forrás hallgat? |
| Döntés | Van-e esemény döntés nélkül? Van-e ismétlődő bukás? |
| Kézbesítő | Csatornánként: kiment / elakadt / hibaarány az utolsó időszakban |

Pass: egy szándékos Slack-hibánál az admin azt mondja „kézbesítés / Slack”, nem azt, hogy „valami elromlott”.

## Userek és élő preferencia (D1)

| Művelet | Üzleti kész |
|---|---|
| Meghívás | Új user; default kit **másolat**; amíg nincs kit, nem kap hírt |
| Lista | Aktív-e, kit, csatornák |
| Élő írás | Admin átírja a kitet / rules.hu / rules.en; a következő döntés ez |
| Inaktiválás | Nem megy új megbízás; a deliveryben lévő kimegy |
| Ki változtatott | User vs. admin, időpont |
| Native / AI váltó | Rendszer-setting; a következő döntés **magyarázata** ezt használja. FIRE mindig a matcheré. |

Az admin **ír** a kitre és a váltóra. Nincs hírszintű stop.

## Amit az admin V1-ben még nem kell, hogy tudjon

- Teljes enterprise jogosultságmátrix, SSO-katalógus, audit-export hatóságnak.
- Szabályok vizuális programozása.
- AI tanítóstúdió, árnyék-összehasonlító nézet.
- Hírszintű moderáció.
- Pénzügyi riport, számlázás.
- Nyilvános meghívó-link bárkinek, self-service signup.

## Szerződés

| Irány | Szerződés |
|---|---|
| ← motorok | Felvétel, esemény, döntés, megbízás, státusz — nyomvonalhoz |
| ↔ userek | Meghívás, érdekeltség, preferencia, élő írás |
| ↔ MCP | Ugyanazok a műveletek toolként |

Ha az élő preferencia-íráshoz külön, titkos mező kell, amit a döntés nem lát, a határ elromlott.

## Tesztelhetőség

- S7 rögzített lánccal, majd élő aranyúttal — **két userrel**.
- S8: szándékos hiba → a helyes motor piros.
- S1a / S1c: meghívás és élő átírás az adminon **és** MCP-n.

A nyomvonal, az egészség és a user-írás **külön** UAT-olható.
