# Fázis 1–2 terv: kézbesítő, userkezelés, UI

Ez a csomag a **első két motoros fázis** terve. Még nincs implementáció.

A meghívás **nem** külön SMTP. Az admin a UI-n meghív → a userkezelés tokent gyárt → a **kézbesítőmotor** küldi az emailt. Ugyanaz a motor viszi később a riasztást is.

| Dokumentum | Mit ad |
|---|---|
| [delivery-engine.md](delivery-engine.md) | Csatorna-interface, megbízás, email + Slack chatbot, státusz |
| [user-management.md](user-management.md) | Szerepek, állapotgép, JSON kit másolat, első admin |
| [invite-flow.md](invite-flow.md) | Meghívás UI → delivery email → elfogadás |
| [ui-design.md](ui-design.md) | Angular képernyők, **Notif Ink** téma |
| [acceptance.md](acceptance.md) | Egyenként tesztelhető szeletek ehhez a két fázishoz |

**Stack (adottság, aktuális stabil):** Java 25, Spring Boot 4.1.x, PostgreSQL, Angular 20, MCP admin gépuser.

**Sorrend:** delivery (email először, Slack adapter a kapun) → UI + userkezelés (meghívás a deliveryn) → később scrape / döntés.
