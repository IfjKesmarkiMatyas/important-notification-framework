# Döntésmotor pontosság

Ide kerül, amit a döntésmotor a [golden truth](golden-truth.md) ellen **tényleg** produkál: találat, melléfogás, F-érték, fejlemény.

## Mit mérünk

Egy minta = **egy normalizált esemény × egy user** (Ada, Béla, Cora, Dénes, Elena).

Pozitív osztály: **FIRE** (születik megbízás, a golden csatornával).  
Negatív: **NO**.

D5 nem külön minta: ugyanaz a Peru 6.7, mint D2. A létra elmeséli Corát; a F-szám a 13 egyedi eseményen megy.

| | Motor FIRE | Motor NO |
|---|---|---|
| Golden FIRE | **TP** | **FN** (kihagyta) |
| Golden NO | **FP** (zaj) | **TN** |

$$
P = \frac{TP}{TP+FP} \qquad R = \frac{TP}{TP+FN} \qquad F_1 = \frac{2PR}{P+R}
$$

Ha a nevező 0: a cellába `—`. Csatorna-hiba (Ada slacket kap email helyett) = **FP + FN** ugyanarra a mintára, nem „majdnem TP”.

A native motoron a cél **F₁ = 1.00** a lenti 65 mintán (kivéve ha D4 S0-n „feletti”-re dől — akkor Ada D4 NO, FIRE=6).

## Golden nevező (2026-09-16 korpusz, ≥ szabály)

13 esemény × 5 user = **65**. FIRE = **7**, NO = **58**.

| User | FIRE (melyik) | NO |
|---|---|---:|
| Ada | D2, D4, M2, B1 | 9 |
| Béla | M2 | 12 |
| Cora | — | 13 |
| Dénes | — (szünet) | 13 |
| Elena | B3, B4 | 11 |
| **Σ** | **7** | **58** |

Családonként a FIRE: disaster 2 (Ada D2, Ada D4), market 2 (Ada M2, Béla M2), breaking 3 (Ada B1, Elena B3, Elena B4).

## Mért futások

Új motor / prompt / küszöb / S0-döntés = új sor. A nyers tévesztés a [tévesztésnaplóba](#tévesztésnapló) kerül.

| Dátum | Agy | n | TP | FP | FN | TN | P | R | F₁ | Megjegyzés |
|---|---|---:|---:|---:|---:|---:|---:|---:|---:|---|
| 2026-09-16 | native | 65 | 7 | 0 | 0 | 58 | 1.00 | 1.00 | 1.00 | `GoldenTruthTest`, élő GPT nélkül; policy `gte` + `topicMode=any` |
| 2026-09-16 | — | 65 | — | — | — | — | — | — | — | Döntésmotor nincs; baseline a golden nevező |

Családonként (ha van motor, ugyanabból a futásból):

| Család | Események | n (×5 user) | Golden FIRE | F₁ (mért) |
|---|---|---:|---:|---|
| disaster | D1 D2 D3 D4 | 20 | 2 | 1.00 |
| market | M1 M2 M3 M4 | 20 | 2 | 1.00 |
| breaking | B1 B2 B3 B4 B5 | 25 | 3 | 1.00 |


## Fejlemények

| Dátum | Mi változott | Hatás a mérőre |
|---|---|---|
| 2026-09-16 | Golden létra + 65-ös nevező rögzítve (≥ 6, halmaz-téma, Dénes mindig NO) | Innét számolunk |
| 2026-09-16 | Native matcher + `decision-policy.json` (`gte`, `topicMode=any`) | F₁ = 1.00, 0 mismatch |
| | S0: D4 ≥ vs „6.0 feletti” | Ha feletti: Ada D4 NO, FIRE 7→6, n marad 65 |

## Tévesztésnapló

Futásonként a nem-egyező `(esemény, user)` sorok.

| Dátum | Eset | User | Golden | Motor | Típus |
|---|---|---|---|---|---|
| 2026-09-16 native | — | — | — | — | nincs tévesztés |
