# MCP-elérés

## Üzleti felelősség

**Minden funkció**, amit valaha elkészítünk — user, admin, gyűjtő, döntés, kézbesítés, későbbi csatorna, native/AI váltó —, **MCP-n hívható**.  
Az MCP-t egy **admin jogú gépfelhasználó** hívja. Nem user-szerep, nem árnyék-API.

Ha egy képesség csak képernyőn létezik, a szelet nincs kész.

## Mit jelent üzletileg

| Ember a UI-n | Gép MCP-n |
|---|---|
| Admin meghív egy usert | Ugyanaz a meghívás toolból |
| User beállítja az érdekeltségét | Ugyanaz a beállítás toolból |
| Admin menet közben átír egy preferenciát | Ugyanaz az élő frissítés toolból |
| Valaki megnézi egy hír nyomvonalát | Ugyanaz a nyomvonal lekérdezhető |
| Döntés / küldés / gyűjtés indítása vagy lekérdezése | Ugyanaz a motorfunkció hívható |

A gép **admin jogú**. A human UI továbbra is: user = saját kit, admin = minden. Az MCP nem a meghívott user nevében, korlátozottan hív — hanem admin gépuserként.

## Állandó kész-szabály (minden szeletre)

Egy szelet pass-jához a UI-sztori **és** az MCP-sztori kell:

1. A funkció megvan a szánt szereplő felületén.
2. Ugyanaz a funkció MCP-n is meghívható, ugyanazzal az üzleti eredménnyel.
3. Amit a human user UI-n nem tehet meg a *másik* kitjén, azt a human user UI továbbra sem. Az MCP admin gépuser ezt **igen** (admin jog).

Ez a V1 utáni funkciókra is érvényes. Nincs „majd később kiteszszük MCP-re”.

## Amit szándékosan nem jelent

- Nem jelenti, hogy a V1-ben nyilvános, internetes MCP-piactér van.
- Nem jelenti, hogy az MCP megkerülheti a döntés- vagy kézbesítőhatárt (pl. HTML-t dobjon a döntésnek).
- Nem helyettesíti az admin képernyőt: embereknek UI, gépeknek MCP, **közös viselkedés**.

## Tesztelhetőség

Minden szelet kártyájához tartozik egy mondat: „ezt MCP-n is megcsináltam”.  
Az első bizonyíték a user/preferencia szeleteken a legegyszerűbb (meghívás, érdekeltség írása, olvasása), de a gyűjtés, döntés, küldés, nyomvonal ugyanerre a rúdra megy.

Minimális MCP UAT a vékony úthoz:

| # | Tool-szerű művelet | Pass |
|---|---|---|
| 1 | User meghívása | A meghívott beléphet / beállíthat |
| 2 | Érdekeltség + preferencia írása (user) | A döntés ezt használja |
| 3 | Ugyanannak az admin általi élő módosítása | A következő esemény az újat követi |
| 4 | Nyomvonal lekérése egy hírre | Ugyanaz a lánc, mint az admin képernyőn |

## Szerződés

Az MCP a **képességek** kapuja, nem egy negyedik motor. A három motor és az admin/user funkciók maradnak a forrás; az MCP csak hívja őket.
