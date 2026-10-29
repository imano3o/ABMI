# ABMI – MediFlow

Backend und einfaches Frontend für die Medikamentenverwaltung auf Krankenhausstationen.
Pflegekräfte sehen den Bestand ihrer Station und buchen Verbräuche direkt in SAP.

## Was die App macht

- Zeigt pro Station alle Medikamente mit Bestand, Mindestbestand und Ampel-Status (`OK`, `LOW`, `CRITICAL`)
- Bucht Entnahmen als Materialbeleg (Bewegungsart 201) auf die Kostenstelle der Station in SAP
- Prüft vorher, ob die Menge gültig ist und genug Bestand da ist

Der **aktuelle Bestand** kommt immer aus SAP. In der eigenen Datenbank (H2) stehen nur die
**Stammdaten**: Krankenhaus, Stationen und Mindestbestände.

## Aufbau

```
Frontend (static/)  →  Controller  →  Service  →  SapClient  →  SAP-Mock (HTTP, OData v2)
                                         ↓
                                  Repository → H2
```

| Paket | Aufgabe |
|---|---|
| `controller` | REST-Endpunkte, nur Weiterleitung an die Services |
| `service` | Fachlogik: Bestand prüfen, Status berechnen, Verbrauch buchen |
| `model` | JPA-Entities und Repositories für die Stammdaten |
| `sap` | `SapClient` und `HttpSapClient`: übersetzen zwischen SAP-Format und unserem Format |
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

Fehler kommen immer im Format `{ "status": 400, "message": "…" }`.
Beispielaufrufe für IntelliJ stehen in `test.http`.

## Tests

```bash
./mvnw test
```
