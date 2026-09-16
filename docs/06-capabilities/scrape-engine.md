# Gyűjtőmotor

## Üzleti felelősség

A világból **15 percenként** (cron), folyamatosan — éjjel is — információt hoz, megőrzi az eredetet, és **normalizált eseményt** ad.  
V1-ben mindhárom család él: breaking, piac, katasztrófa. POC: most bármely forrás rátehető; a kezdő készlet könnyen integrálható, kulcs nélküli API/RSS, **Telex kötelező**.

Részletes lista: [09-v1-sources-and-branding.md](../09-v1-sources-and-branding.md).

## Amit tudnia kell (V1)

- **15 perces cron**, nincs éjszakai leállás. A user azt kapja, ami az utolsó ciklusban megjelent; helyi időzóna nem téma.
- Cserélhető csatlakozók: RSS (Telex, BBC) + JSON API (USGS, CoinGecko).
- Nyers felvétel megőrzése (DB, most bármeddig).
- Normalizálás: típus (`breaking` / `market` / `disaster`), idő, hely ha van, rövid leírás, forrás, link.
- Forrás hallgatása: cron lefutott, de üres/hiba → admin állapot.

## Kezdő források

| Család | Forrás | Miért |
|---|---|---|
| Breaking hu | Telex RSS | Kötelező |
| Breaking en | BBC World RSS | Az angol szabályfile-nak legyen jele |
| Katasztrófa | USGS GeoJSON | Küszöbös, kulcs nélküli JSON |
| Piac | CoinGecko Simple Price | Kulcs nélküli JSON, 15 perc alatt van mozgás |

## Amit szándékosan nem csinál

- Nem pontozza, hogy fontos-e.
- Nem választ csatornát.
- Nem küld.
- Nem áll le éjjel „mert más a time zone”.

## Szerződés

| Irány | Szerződés |
|---|---|
| Világ → gyűjtő | RSS / JSON nyers. |
| Gyűjtő → döntés | Csak normalizált esemény + eredet. |
| Gyűjtő → admin | Nyers + normalizált + utolsó cron forrásonként. |
| Gyűjtő → MCP | Ugyanez admin gépuserrel. |

## Tesztelhetőség egyedül

A döntésmotor **még nem kell** (3. fázis a 4. előtt). Az adminon / MCP-n látszik a felvétel.

1. 15 perc után van Telex-eredetű normalizált breaking.
2. USGS → disaster ugyanabban az alakban.
3. CoinGecko → market ugyanabban az alakban.
4. Szándékos forrásleállás → „hallgat” az adminon.

Pass: a terméktulajdonos felolvassa a három alakot, és azt mondja: ezen a döntés tud dolgozni.
