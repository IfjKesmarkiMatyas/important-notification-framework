# Fázis 1–2: kézbesítő, userkezelés, UI

Ez a csomag az **első két motoros fázis** terve. **Implementálva** (2026-09-16): meghívásos user, JSON kit, delivery jobok, Notif Ink Angular, Mailpit. A 3–4. fázis (gyűjtő, döntés, Tower) is bent van; a gyökér [README](../../README.md) az indítás.

A meghívás **nem** külön SMTP. Az admin a UI-n meghív → a userkezelés tokent gyárt → a **kézbesítőmotor** küldi az emailt. Ugyanaz a motor viszi a riasztást is.

| Dokumentum | Mit ad |
|---|---|
| [delivery-engine.md](delivery-engine.md) | Csatorna-interface, megbízás, email + Slack chatbot, státusz |
| [user-management.md](user-management.md) | Szerepek, állapotgép, JSON kit másolat, első admin |
| [invite-flow.md](invite-flow.md) | Meghívás UI → delivery email → elfogadás |
| [ui-design.md](ui-design.md) | Angular képernyők, **Notif Ink** téma |
| [acceptance.md](acceptance.md) | Egyenként tesztelhető szeletek ehhez a két fázishoz |

**Stack (adottság, aktuális stabil):** Java 25, Spring Boot 4.1.x, PostgreSQL, Angular 20, MCP admin gépuser.

**Sorrend:** delivery (email először, Slack adapter a kapun) → UI + userkezelés (meghívás a deliveryn) → scrape / döntés. Ez a sorrend lefutott.
