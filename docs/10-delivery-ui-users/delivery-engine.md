# Kézbesítőmotor — terv

A motor **megbízást** visz csatornára. Nem dönt, nem scrape-el, nem választ címzettet.  
V1 csatornák: **email** és **Slack chatbot**. A meghívás **csak email**.

Üzleti szerződés: [06-capabilities/delivery-engine.md](../06-capabilities/delivery-engine.md).

---

## Határ

```
Bármely hívó (teszt, userkezelés, később döntés, MCP)
        │  DeliveryJob
        ▼
   Kézbesítőmotor
        │
        ├─► EmailChannel
        └─► SlackChatbotChannel
```

Új csatorna = új adapter a kapun. A hívó nem tud SMTP-t vagy Slack API-t.

---

## Megbízás (DeliveryJob)

Egy sor, ami vagy kimegy, vagy hibával megáll. Ami **létrejött**, azt a motor **elviszi** — kitváltás nem hívja vissza.

| Mező (üzleti) | Jelentés |
|---|---|
| azonosító | Nyomvonalhoz |
| cél (`purpose`) | `TEST` / `INVITE` / `ALERT` |
| csatorna | `email` / `slack` |
| címzett | Email cím vagy Slack user-azonosító — a hívó adja |
| tartalom | Tárgy + törzs, vagy sablonkulcs + adatok |
| állapot | `queued` → `sending` → `sent` / `failed` |
| hiba oka | Embernek szóló, pl. „érvénytelen cím” |
| idők | Sorba került, elküldve, utolsó próbálkozás |

**Sablonok** (ugyanaz a motor, más szöveg):

| Cél | Sablon | Mikor |
|---|---|---|
| `TEST` | Rövid „Notif él” | Fázis 1 kapu |
| `INVITE` | Meghívó + elfogadó link | Fázis 2, userkezelés kéri |
| `ALERT` | Esemény + magyarázat + forráslink | Fázis 4, döntés kéri |

A meghívó **nem** más pipeline. `purpose=INVITE`, `channel=email`.

---

## Csatorna-interface

Minden adapter ugyanazt ígéri:

1. Átveszi a megbízást.
2. A csatorna nyelvén elküldi.
3. Visszaadja: siker vagy üzleti hiba (nem nyers SDK-exception a UI-ra).

**Email (Notif):** feladó `Notif <noreply@…>`, HTML + szöveges alternatíva. A meghívóban egy gomb: *Elfogadom a meghívást*.

**Slack chatbot:** a bot saját nevén (Notif) beszél. Meghívásra **nem** használjuk. Fázis 1-ben az adapter létezzen (tesztüzenet), hogy a kapu ne email-specifikus legyen.

---

## Viselkedési szabályok

- **Kimegy, ami bent van.** Nincs admin-stop a jobon (hírszintű kontroll nincs).
- **Nincs digest-sor.**
- **Sikertelen** látszik; nem csend.
- **Újrapróbálás:** átmeneti hibára kevés, korlátos retry; végtelen ping tilos.
- **POC tárolás:** a job a Postgresben él (outbox). Nincs külön üzenetsor-kötelezettség V1-ben.
- **Nyelvek:** a meghívó hu/en a meghívott preferred locale-ja, default hu.

---

## Backend-váz (nem kód)

Saját Spring-modul / package: `delivery`. A userkezelés és a későbbi döntés **ezt** hívja, SMTP-t nem.

MCP (admin gépuser): tesztküldés, job státusz, lista. A meghívás MCP-n a **user** tool, ami belül a deliveryt hívja — nem kell a gépnek külön SMTP tool, ha van `invite_user`.

---

## Fázis 1 kész (UI nélkül)

1. Bedobott `TEST` email megérkezik.
2. Hibás cím → `failed`, ok olvasható (API/MCP).
3. Bedobott Slack teszt a chatboton megjön (vagy a csatorna `failed` okkal, ha a bot még nincs bekötve — akkor ez explicit blokkoló a Slack-szeleten, az email-kapu ettől még zöld).
4. `INVITE` sablon renderelhető dummy linkkel (éles token a fázis 2-ben).

A meghívás **éles** útja a fázis 2 kapuja.
