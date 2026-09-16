# Képességek — üzleti viselkedés

Ezek a dokumentumok azt írják le, **mit csinál** egy motor vagy felület, kivel szerződik, és **hogyan tesztelhető** egyedül.  
Nem tartalmaznak osztályokat, topicneveket, táblákat.

| Képesség | Dokumentum | Üzleti felelősség egy mondatban |
|---|---|---|
| Userek, kit | [user-alerts.md](user-alerts.md) | Meghívás, default JSON másolat, élő szerkesztés, hu/en szabályfile. |
| Gyűjtőmotor | [scrape-engine.md](scrape-engine.md) | 15 p cron, Telex + USGS + CoinGecko, normalizált esemény. |
| Döntésmotor | [decision-engine.md](decision-engine.md) | Userenként; native **vagy** AI, váltó; nincs árnyék. |
| Kézbesítőmotor | [delivery-engine.md](delivery-engine.md) | Email + Slack chatbot; ami bent van, kimegy. |
| Admin | [admin-control-tower.md](admin-control-tower.md) | Nyomvonal, motorok, meghívás, kit, váltó. |
| MCP | [mcp-access.md](mcp-access.md) | Admin jogú gépuser, minden funkció. |

A határok szándékosak: ha egy képesség átnyúl a másik felelősségébe, a szelet nem tesztelhető külön. Minden képesség MCP-kapuja a saját doksijában is szerződés.
