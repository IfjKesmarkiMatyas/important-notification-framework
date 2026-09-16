# Fázis 1–2 — tesztelhető szeletek

A nagy lista [05-testable-slices.md](../05-testable-slices.md) marad a terméké. Itt csak az, ami **most** épül.

---

## D-A — Teszt email a deliveryn

**Pass:** bedobott `TEST` job → Notif-levél a postaládában. Hibás cím → `failed`, ok olvasható. MCP tesztküldés ugyanaz.

**Fail:** a userkezelés vagy a UI SMTP-zik.

---

## D-B — Slack chatbot teszt (ugyanaz a kapu)

**Pass:** `TEST` + `slack` kimegy a Notif boton, vagy a szelet explicit piros, ha a bot nincs bekötve — de az email-kapu ettől zöld maradhat.

**Fail:** a döntés/Slack-specifikus ág a delivery magjában.

---

## U-A — Seed admin + login

**Pass:** a seed admin belép az Angular loginon. Nincs `/register`.

**Fail:** nyilvános signup.

---

## U-B — Meghívás a UI-ról, levél a deliveryn

**Pass:** People → Invite → `INVITE` job `sent` → a meghívott postaládájában link. A link `/invite/:token`.

**Fail:** a UI saját mailer; vagy a levél kimegy, de nincs job a Delivery listán.

---

## U-C — Elfogadás, default kit másolat

**Pass:** jelszó után `ACTIVE`, a kit a default másolata, `preferences.email` a meghívott cím. A második meghívott kitje külön példány.

**Fail:** közös kit; vagy a másolás már a levélküldéskor megtörténik, és a soha el nem fogadott userre is riasztást ígér.

---

## U-D — Újra küldés és failed job

**Pass:** failed meghívó látszik a People soron; Resend új tokent és új jobot csinál; a régi link halott.

**Fail:** a failed eltűnik; a régi token élve marad.

---

## U-E — Jogok

**Pass:** user nem hív meg másokat, nem látja a másik kitjét. Admin látja / írja. MCP admin gépuser meghív és kitet ír.

**Fail:** a meghívott MCP nélkül is admin, vagy a gépuser a login-képernyőn jelenik meg.

---

## U-F — Notif Ink

**Pass:** sötét vászon, egy lime akcentus, nincs default Material lila, a meghívó email vizuálisan ugyanahhoz a márkához tartozik.

**Fail:** stock theme, vagy a levél egy másik terméknek tűnik.

---

## Kapuk

**Fázis 1 zöld:** D-A (D-B Slack, ha a bot kész).  
**Fázis 2 zöld:** U-A … U-E. U-F a 2-es demó része, nem utólagos „szépítés”.
