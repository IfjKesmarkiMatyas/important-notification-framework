# Kész-kritériumok

Két szint: **egy szelet kész**, és **a V1 kész**.  
Technikai lefedettség (tesztek száma, uptime) ide nem tartozik — csak az, amit üzleti átvételkor nézünk.

A szelet-szintű pass/fail: [05-testable-slices.md](05-testable-slices.md). Itt a **összegző** rúd.

---

## Egy szelet akkor kész, ha

1. Elmesélhető a 15 perces történet a szelet kártyájáról.
2. A tesztelő szerep (user / admin / tulajdonos) **kód nélkül** pass-t vagy failt mond.
3. A motorhatár tart: a szelet nem kéri, hogy egy másik motor „csak gyorsan” belenyúljon.
4. A negatív út is látszik, ha a szeletnek van negatív útja (nem ment ki, csatorna bukott, forrás hallgat).
5. A következő szelet bemenete létezik (pl. S3 után van megbízás, amit az S4 megfoghat).
6. **MCP-paritás:** ugyanaz a funkció az **admin jogú gépuserrel** is.

Ha a 2-es pontban a tulajdonos „majdnem jó, de…” — a szelet nincs kész. A „majdnem” maradjon a következő szeletben, ne a pass pecsétjén.

---

## V1 akkor kész (D0–D10 + D11), ha ez a nyolc történet mind igaz

| # | Történet | Szeletek |
|---|---|---|
| 1 | Van lezárt szabálykönyv, a példakártyák egyeznek | S0 |
| 2 | Meghívásos felvétel; a meghívott a **saját** érdekeltségét és preferenciáját állítja | S1a, S1 |
| 3 | Két user: ugyanaz az esemény A-nak megy, B-nek nem | S1b |
| 4 | Admin élőben átír; a következő esemény az újat követi | S1c |
| 5 | Legalább egy forrásból olvasható normalizált esemény eredettel | S2 |
| 6 | Kritikus, szándékhoz illő esemény a **kit szerinti** csatornákon megérkezik (email és/vagy chatbot) | S3–S6 |
| 7 | Admin userenként látja az okot; ami deliveryben volt, kiment | S6–S7, S1c |
| 8 | Motorhiba helye + MCP admin gépuserrel | S8, D11 |

**Plus:** Slack = chatbot. MCP = admin gépuser. Nincs árnyék, nincs digest.

Nem V1-feltétel:

- Harmadik csatorna
- Nyilvános signup
- Hírszintű admin-stop

---

## Négy minőségi rúd (a demo mellett)

Ezeket a V1 demón **szám nélkül is** érezni kell. Később mérővé tehetők.

| Rúd | Jó | Rossz |
|---|---|---|
| Relevancia | Amit *én* kaptam, arra iratkoztam; B más listán van | Mindenki ugyanazt kapja |
| Idő | A kritikus még cselekvési időn belül | Másnap tudom meg a breakinget |
| Zaj | A csendes nap csendes | Közepes hírek ugyanott, ahol a katasztrófa |
| Magyarázat | Az admin 2 perc alatt végigmondja | „A motor úgy döntött” |

A V1 **nem** kész, ha a négy csatorna megy, de a magyarázat nincs — a brief admin-ígérete akkor üres.

---

## Explicit nem-kész jelek

- Van gyűjtés, de a döntés HTML-t parse-ol.
- Van döntés, de a Slackre külön ág van írva a döntésben.
- Van küldés, de az admin három képernyőn keresgél időbélyegekkel.
- Az AI és a native **egyszerre** fut (árnyék).
- A funkció csak UI-n van meg, MCP „később”.
- Az admin átírja a kitet, a döntés újraindításig a régit használja.
- A deliveryben lévő megbízást visszavonják kitváltáskor.
- Két user keresztbe kapja egymás híreit.

Ezek a plan of attack **szándékos sorrendjét** törik. Ha mégis így történik, a dokumentumot kell frissíteni, ne a pecsétet.
