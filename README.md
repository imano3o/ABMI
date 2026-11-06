# ABMI – MediFlow

Backend und einfaches Frontend für die Medikamentenverwaltung auf Krankenhausstationen.
Pflegekräfte sehen den Bestand ihrer Station, buchen Verbräuche direkt in SAP und bestellen nach,
wenn ein Medikament unter den Mindestbestand fällt.

## Was die App macht

- Zeigt pro Station alle Medikamente mit Bestand, Mindestbestand und Ampel-Status (`OK`, `LOW`, `CRITICAL`)
- Bucht Entnahmen als Materialbeleg (Bewegungsart 201) auf die Kostenstelle der Station in SAP
- Prüft vorher, ob die Menge gültig ist und genug Bestand da ist
- Schlägt für alles unter dem Mindestbestand eine Bestellmenge vor: bis zum Zielbestand auffüllen,
  minus offene Bestellungen, aufgerundet auf ganze Packungen (keine Doppelbestellung)
- Legt Bestellungen in SAP an (Purchase Order), wahlweise mit dem Vorschlag oder einer eigenen Menge
- Bucht den Wareneingang einer Bestellung (Bewegungsart 101); danach steigt der Bestand in SAP

Der **aktuelle Bestand** und die **Bestellungen** kommen immer aus SAP. In der eigenen Datenbank (H2) stehen nur die
**Stammdaten**: Krankenhaus, Stationen sowie pro Station und Medikament Mindestbestand, Zielbestand und Packungsgröße.
Lieferant, Buchungskreis und Einkaufsorganisation für Bestellungen stehen unter `sap.purchasing` in `application.yaml`.

## Aufbau

```
Frontend (static/)  →  Controller  →  Service  →  SapClient  →  SAP-Mock (HTTP, OData v2)
                                         ↓
                                  Repository → H2
```

| Paket | Aufgabe |
|---|---|
| `controller` | REST-Endpunkte, nur Weiterleitung an die Services |
| `service` | Fachlogik: Bestand prüfen, Status berechnen, Verbrauch buchen, Bestellmenge berechnen und prüfen |
| `model` | JPA-Entities und Repositories für die Stammdaten |
| `sap` | `SapClient` und `HttpSapClient`: übersetzen zwischen SAP-Format und unserem Format; `PurchasingSettings` für die Einkaufsdaten |
| `sap.dto` | Datenklassen im SAP-Format |
| `dto` / `dto.api` | Unsere eigenen Datenklassen und die Antworten an das Frontend |
| `exception` | `ApiException` und einheitliche Fehlerantworten |

## Voraussetzungen

- **JDK 25** (Spring Boot 4.1)
- **SAP-Mock** läuft auf `http://localhost:8081` (einstellbar über `sap.base-url` in `application.yaml`)

## Starten

```bash
./mvnw spring-boot:run
```

Danach:

- Frontend: <http://localhost:8080>
- API: <http://localhost:8080/api/medications?station=ST01>

Die Datenbank liegt in `data/` und wird bei jedem Start neu aus `src/main/resources/data.sql` befüllt.

## Endpunkte

| Methode | Pfad | Zweck |
|---|---|---|
| `GET` | `/api/hospital` | Name des Krankenhauses |
| `GET` | `/api/stations` | Alle Stationen |
| `GET` | `/api/medications?station=…` | Medikamente einer Station mit Status |
| `GET` | `/api/medications/{material}?station=…` | Mindestbestand eines Medikaments |
| `POST` | `/api/medications/{material}/consume?station=…` | Verbrauch buchen, Body: `{ "quantity": 2, "note": "…" }` |
| `GET` | `/api/orders/proposals?station=…` | Bestellvorschläge anzeigen (bestellt nichts) |
| `POST` | `/api/orders?station=…` | Alle Vorschläge bestellen, eine SAP-Bestellung pro Medikament |
| `POST` | `/api/orders/{material}?station=…` | Ein Medikament mit eigener Menge bestellen, Body: `{ "quantity": 40 }` |
| `GET` | `/api/orders?station=…` | Bestellungen der Station, offene zuerst |
| `POST` | `/api/orders/{purchaseOrder}/goods-receipt?station=…` | Wareneingang buchen, Bestand steigt |

Fehler kommen immer im Format `{ "status": 400, "message": "…" }`.
Details zu Feldern, Regeln und dem Mapping auf SAP stehen in [`API.md`](API.md),
Beispielaufrufe für IntelliJ in `test.http`.

## Demo-Ablauf Bestellung

1. Station `ST03` wählen: Enoxaparin, Metamizol, NaCl und Ondansetron liegen unter dem Mindestbestand.
2. Unter „Bestellvorschläge“ steht die Rechnung, z.B. Enoxaparin: Zielbestand 30 − Bestand 6 = 24 → 30 (10er-Packungen).
3. „Alle Vorschläge bestellen“ legt in SAP eine Bestellung pro Medikament an (Nummern ab `4500000001`).
   Danach zeigen die Vorschläge „bereits bestellt“, ein zweiter Klick bestellt nichts mehr.
4. Unter „Bestellungen“ bei einer offenen Bestellung „Wareneingang buchen“: Der Bestand steigt, die Ampel springt um.

Für einen neuen Durchlauf den SAP-Mock neu starten, dann gelten wieder die Startbestände und es gibt keine Bestellungen.

## Tests

```bash
./mvnw test
```

`OrderQuantityTest` prüft die Formel für die Bestellmenge, `OrderQuantityCheckTest` die Regeln für eine selbst
eingegebene Menge. Beide laufen ohne Spring und ohne SAP.
