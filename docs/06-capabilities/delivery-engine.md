# Kézbesítőmotor

## Üzleti felelősség

A megbízást **elvinné a csatornára**, és visszamondja, mi lett vele.  
V1: **email** és **Slack chatbot**. A kézbesítő a **első** elkészülő motor: döntés és scrape nélkül, bedobott megbízással tesztelhető.

Ami **már bent van** a deliveryben, **kimegy**. Preferencia-váltás nem hívja vissza.

## Amit tudnia kell (V1)

- Email: `Notif <noreply@…>` feladóval, tárgy/törzs a megbízásból; hibás cím nem csend.
- Slack **chatbot**: a bot saját nevén (Notif) üzen a usernek; nem webhook-kivétel, nem Telex-nek álcázva.
- Státusz: várakozik / elküldve / sikertelen.
- A user kitjéből jön a célpont; A és B nem cserélődik.
- Nincs digest-sor.

## Amit szándékosan nem csinál

- Nem pontozza újra a hírt.
- Nem választ címzetteket a kit helyett.
- Nem scrape-el.
- Nem vár „engedélyezd ezt a hírt” adminra.
- Nem állítja le a már átvett megbízást, ha a kit közben változott.

## Szerződés

| Irány | Szerződés |
|---|---|
| Döntés (vagy teszt) → kézbesítő | Megbízás: user, csatorna, tartalom. |
| Kézbesítő → email / Slack chatbot | Csatorna nyelve. |
| Kézbesítő → admin | Státusz, hiba oka. |
| MCP | Tesztküldés, státusz — admin gépuser. |

## Tesztelhetőség egyedül (1. fázis)

| Teszt | Pass |
|---|---|
| Email boldog út | levél a postaládában, feladó Notif |
| Email bukás | státusz sikertelen |
| Slack chatbot boldog út | a bot üzenete megjön |
| Slack bukás | státusz sikertelen, a megbízás ettől döntésnek marad „jó” |
| Kit változás küldés közben | a bent lévő megbízás **kimegy** |

A meghívás **ezen a motoron** megy ki (`purpose=INVITE`). Részletes terv: [10-delivery-ui-users/](../10-delivery-ui-users/).

Két élő csatorna, ugyanaz a megbízásmodell. Harmadik később. A chatbot nem külön termék az adminon.
