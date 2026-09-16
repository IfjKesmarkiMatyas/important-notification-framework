# Userkezelés — terv

Meghívásos kör. Nincs nyilvános regisztráció. A human UI: user = saját, admin = minden. Az MCP külön **admin jogú gépuser**.

Üzleti szerződés: [06-capabilities/user-alerts.md](../06-capabilities/user-alerts.md).

---

## Szerepek

| Szerep | Hol él | Mit tehet |
|---|---|---|
| `USER` | Meghívott ember | Saját kit, saját preferencia, saját profil |
| `ADMIN` | Ember | Meghív, visszavon, inaktivál, bármely kit, default sablon, native/AI váltó |
| `MCP_ADMIN` | Gép | Admin jog toolokból; nem jelentkezik a Angularba |

A meghívott **nem** lesz automatikusan admin.

---

## User állapot

```
(nincs) --invite--> INVITED --accept--> ACTIVE
                         |                |
                      expire/revoke    deactivate
                         |                |
                         v                v
                      REVOKED         INACTIVE
```

- `INVITED`: van token, **még nincs** kit-másolat, **nem** kap riasztást.
- `ACTIVE`: belépett, default kit **másolva**, riasztást kaphat a kit szerint.
- `INACTIVE`: nem megy új megbízás; ami deliveryben van, kimegy.
- `REVOKED`: lejárt / visszavont meghívó, link halott.

---

## Első admin (tyúk-tojás)

Meghívásos termékben kell egy **seed admin**: konfigurációból (email + jelszó / hash) induló `ACTIVE` `ADMIN`, kit-másolattal.  
Ő hívja a többieket a UI-ról. Nincs „első, aki nyitva hagyott /register-t”.

---

## JSON kit

Meghívás **elfogadásakor** (nem a kiküldéskor):

1. `default-kit.json` → a user saját JSONB másolata
2. `default-rules.hu.json` / `default-rules.en.json` → saját másolatok
3. A `preferences.email` a meghívott címre áll, ha üres volt a defaultban

A default sablont az admin szerkeszti; **csak a következő** új user másolata változik.

Tárolás POC: Postgres JSONB a userhez, nem külön git-file élesben. A „file” a **alak** és a bővíthetőség ígérete.

Last-write-wins, ha user és admin egyszerre írja ugyanazt a mezőt; a nyomvonalon (később) ki nyert.

---

## Meghívó token

- Egy userhez egyszerre egy élő token.
- Újra-küldés: új token, a régi halott, új `INVITE` job a deliveryben.
- Lejárat: 7 nap (POC, állítható).
- A DB-ben **hash**, a nyers token csak a levélben / linkben.
- A link az Angularra megy: `/invite/:token`.

---

## Mit nem a userkezelés csinál

- Nem küld emailt közvetlenül — **delivery jobot** kér (`INVITE`).
- Nem dönt hírről.
- Nem Slackezik meghívót.

---

## MCP

Admin gépuser toolok (fázis 2): `invite_user`, `list_users`, `get_user`, `update_kit`, `deactivate_user`, `resend_invite`.  
A `invite_user` belül: user sor + token + delivery `INVITE` email.
