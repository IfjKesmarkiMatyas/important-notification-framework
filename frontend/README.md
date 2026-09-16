# Notif Ink (frontend)

Angular 20 admin/user UI. Proxy: `/api` → `http://localhost:8080`.

```powershell
npm install
npm start
```

[http://localhost:4200/login](http://localhost:4200/login) — admin `admin@notif.local` / `adminadmin`. Station personák: lásd a gyökér [README](../README.md).

| Út | Ki |
|---|---|
| `/login`, `/invite/:token` | vendég |
| `/app/kit` | user: saját érdekeltség + csatornák |
| `/admin/people`, `/admin/delivery`, `/admin/defaults`, `/admin/tower` | admin |

A Tower három oszlop: rendszer (források, native/AI, Golden F₁), inbox (normalizált események), story (egy hír nyomvonala).

i18n: hu / en. Téma: Notif Ink (`#0C0E12`, lime `#C8F54A`).
