# UI — Angular, Notif Ink

Letisztult, sötét, kevés elem: nem „admin template”, nem default indigo Material.

**Angular 20**, standalone komponensek, szigorú TypeScript. i18n: **hu + en**.

---

## Téma: Notif Ink

Egy vászon, egy kiemelés, meleg tipográfia. A riasztás a tartalom, nem a chrome.

| Token | Érték | Szerep |
|---|---|---|
| Háttér | `#0C0E12` | Oldal |
| Felület | `#14181F` | Panel, mező |
| Vonal | `rgba(244,241,234,0.08)` | 1px szegély |
| Szöveg | `#F4F1EA` | Címek, body |
| Tompa | `#8A909A` | Segéd, meta |
| Akcentus | `#C8F54A` | Egyetlen savanyú lime: gomb, fókusz, aktív sor |
| Veszély | `#FF6B4A` | Failed job, inaktiválás |
| Sugár | `12px` | Gomb, mező, panel |
| Típus | Geist / Inter / `ui-sans-serif` | 14–15px body, 28–36px cím, kevés súly |

**Elvek**

- Sok levegő, egyoszlopos űrlapok, széles táblázat csak a People / Delivery listán.
- Nincs árnyék-halom, nincs színes kártyaerdő, nincs hamburger tíz menüponttal.
- Fókuszgyűrű az akcentusból. Hover: felület 4%-kal világosabb, nem új szín.
- Mozgás: 150ms, csak opacity/translate, semmi bounce.
- Email (meghívó) **ugyanazt** a világot idézi: sötét fej, lime gomb, Notif szó — ne Outlook-kék CTA.

Angular Material 3 **csak** a primitívhez (form field, snackbar), **teljes custom paletta** a fenti tokenekkel. Nem M2 indigo, nem sűrű denseness.

---

## Információs architektúra

Két shell, egy login.

```
/login
/invite/:token

USER
  /app/kit            saját érdekeltség + csatornák
  /app/rules          rules.hu / rules.en (később is ide fér)

ADMIN  (a user shell + )
  /admin/people       meghívás, lista, újra küldés
  /admin/people/:id   kit másé
  /admin/delivery     jobok (invite/test/alert), státusz
  /admin/defaults     default kit sablon
  /admin/tower        gyűjtő + döntés: system / inbox / story, native/AI váltó, Golden F₁
```

Kevesebb menü, mint a fenti lista: **People**, **Delivery**, **Defaults**, **Tower**, **Saját kit**.

---

## Képernyők (fázis 2)

### Login
Középre zárt keskeny panel. Email, jelszó, lime *Belépés*. Nincs „Regisztráció”.

### Invite accept
Ugyanaz a panel. Token érvényes: név, jelszó, *Csatlakozom*. Érvénytelen: egy mondat, semmi űrlap.

### People (admin) — a meghívás otthona
- Fejléc: *People* + email mező + nyelv + lime *Invite*.
- Táblázat: email, szerep, állapot, utolsó meghívó (`sent` / `failed` / `queued`), idő.
- Failed: veszély szín + az ok egy sorban.
- Sor menü: Resend / Revoke / Open kit / Deactivate — csak ami az állapotra érvényes.

### Saját kit
Három zárt típus kártya helyett **egy lista + hozzáadás**: breaking / market / disaster szűrőkkel. Csatorna: két kapcsoló (email, Slack chatbot), bármilyen kombináció. Mentés azonnal él a következő döntésre (amikor lesz döntés).

### Delivery (admin)
A motor tükre: purpose, csatorna, címzett, állapot. Tesztküldés gomb (email) ide, nem külön „SMTP debug” oldal.

---

## Viselkedés

- Human user nem látja a `/admin/*` routot.
- Üres lista: egy mondat + a meghívó mező, nem illusztrációs clipart.
- Minden írható dolog MCP-n is megvan; a UI nem titkos mezőket ment.

---

## Fázis 2-ben szándékosan váz (azóta bent)

- Nyomvonal, scrape egészség, native/AI váltó: a Tower **system / inbox / story** oszlopai.
- Slack chatbot bekötés a user kitben mezőként (slack user id), a meghívás ettől még email.
