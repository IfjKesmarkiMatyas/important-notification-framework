# Meghívás — UI → userkezelés → delivery email

Ez a fázis 2 **fő története**. A meghívó levél a kézbesítőmotor `INVITE` megbízása.

---

## Boldog út

```
Admin a People képernyőn
  → email (+ opcionális név, nyelv hu|en)
  → Userkezelés: USER + INVITED + token
  → DeliveryJob purpose=INVITE, channel=email
  → Notif email kimegy a meghívottnak
  → Link: https://…/invite/{token}
  → Meghívott: név + jelszó
  → ACTIVE, default kit másolat
  → belép a saját kitjére
```

Nincs `/register`. Aki token nélkül jön, a loginra kerül.

---

## UI kötelességek

**Admin — People**

- Email mező, nyelv (hu/en, default hu), *Meghívás*.
- Lista: cím, állapot (`INVITED` / `ACTIVE` / …), utolsó meghívó job státusza (`sent` / `failed`).
- `INVITED`: *Újra küldés* (új token + új delivery job), *Visszavonás*.
- `ACTIVE`: *Megnyitás* (kit), *Inaktiválás*.
- Ha a delivery `failed`, a sor **piros**, ok olvasható — az admin nem „elküldtem a böngészőből”, hanem a motor státuszát látja.

**Meghívott — `/invite/:token`**

- Érvényes token: rövid űrlap (megjelenő név, jelszó). Nincs szerepválasztó.
- Lejárt / hamis / felhasznált: egy mondat + *nincs nyilvános signup*, menjen az admintól új meghívót kérni.
- Siker: be van lépve, saját kit (default másolat).

**Mindenki más**

- Login (email + jelszó). Seed admin ezzel lép be először.

---

## Delivery tartalom (meghívó)

- Feladó: `Notif <noreply@…>`
- Tárgy (hu): `Meghívó a Notifra`
- Törzs: egy mondat, ki hívta meg (ha van admin név), gomb a linkkel, lejárat.
- Nem riasztás, nem Telex-hírcímlap.

Ugyanez a job-modell, mint a tesztlevélnek; más sablonkulcs.

---

## Hibák

| Helyzet | Viselkedés |
|---|---|
| Dupla meghívás ugyanarra az *aktív* emailre | Nem második user; az admin ezt látja |
| Dupla meghívás `INVITED` emailre | Újra-küldés szemantikája (új token + job) |
| Delivery `failed` | User `INVITED` marad; admin újra küldhet |
| Token lejárt, de a user még INVITED | Újra-küldés |
| Kit-átírás, miközben a meghívó kimegy | A meghívó **kimegy**; a kit az elfogadáskor másolódik, nem a küldéskor |

---

## Teszt (ember + MCP)

1. Seed admin belép.
2. Meghív egy címet a UI-n.
3. A postaládában Notif-levél, linkkel.
4. A linken jelszó → saját kit, nem az adminé.
5. Második user más kitet állít.
6. MCP `invite_user` ugyanazt az utat hozza (levél kimegy).
7. *Újra küldés* után a régi link halott, az új él.
