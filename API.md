# MediFlow – REST-API (Frontend ↔ ABMI-Backend)

Basis-URL: `http://localhost:8080/api`

Grundsatz: **Das Frontend kennt keine SAP-Begriffe.** Es schickt nur, was eine Pflegekraft eingibt.
SAP-spezifische Werte (Werk, Lagerort, Kostenstelle, Bewegungsart, Lieferant …) ergänzt das Backend
aus den Stammdaten (`Station`, `MinimumStock`) und der Konfiguration (`application.yaml`).

## Konventionen

- JSON in **camelCase**
- Datum als ISO-String: `"2026-10-16"`
- Mengen als **Zahl** (nicht als String wie bei SAP)
- `material` (z.B. `MED-ENOX40`) ist überall der Schlüssel; die Nummern stimmen mit dem SAP-Mock überein
- Alle stationsbezogenen Endpunkte brauchen den Parameter `?station=ST01|ST02|ST03`
- Alle Endpunkte antworten bei Erfolg mit `200`

## Übersicht

| Methode | Pfad | Zweck | SAP-API |
|---|---|---|---|
| `GET` | `/api/hospital` | Name des Krankenhauses | – |
| `GET` | `/api/stations` | Alle Stationen | – |
| `GET` | `/api/medications?station=…` | Medikamente mit Bestand und Ampel | Material Stock |
| `GET` | `/api/medications/{material}?station=…` | Mindestbestand eines Medikaments | – |
| `POST` | `/api/medications/{material}/consume?station=…` | Verbrauch buchen | Material Document (201) |
| `GET` | `/api/orders/proposals?station=…` | Bestellvorschläge anzeigen | Material Stock, Purchase Order |
| `POST` | `/api/orders?station=…` | Alle Vorschläge bestellen | Purchase Order |
| `POST` | `/api/orders/{material}?station=…` | Ein Medikament mit eigener Menge bestellen | Purchase Order |
| `GET` | `/api/orders?station=…` | Bestellungen der Station | Purchase Order |
| `POST` | `/api/orders/{purchaseOrder}/goods-receipt?station=…` | Wareneingang buchen | Material Document (101) |

---

## 1. Stammdaten

`GET /api/hospital` → `{ "name": "…" }`

`GET /api/stations` → `[{ "code": "ST01", "name": "Station 1 – Chirurgie" }, …]`

---

## 2. Medikamentenliste

`GET /api/medications?station=ST03`

**Response**

```json
[
  {
    "material": "MED-ENOX40",
    "name": "Enoxaparin 40 mg Fertigspritze",
    "stock": 6,
    "unit": "PC",
    "minimumStock": 10,
    "status": "LOW"
  },
  {
    "material": "MED-ONDAN4",
    "name": "Ondansetron 4 mg Ampullen",
    "stock": 3,
    "unit": "PC",
    "minimumStock": 10,
    "status": "CRITICAL"
  }
]
```

| Feld | Bedeutung |
|---|---|
| `name` | Anzeigename aus unserer Datenbank (SAP liefert im Bestand nur die Materialnummer) |
| `stock` | aktueller Bestand aus SAP (Material Stock) |
| `minimumStock` | Mindestbestand aus unserer Datenbank, `0` wenn keiner gepflegt ist |
| `status` | `OK`, `LOW` (unter Mindestbestand) oder `CRITICAL` (leer oder unter der Hälfte des Mindestbestands), berechnet das Backend |

`GET /api/medications/{material}?station=…` liefert nur den gespeicherten Mindestbestand:
`{ "station": "ST03", "material": "MED-ENOX40", "minimumQuantity": 10 }`

---

## 3. Verbrauchen → SAP Material Document (Bewegungsart 201)

`POST /api/medications/{material}/consume?station=ST03`

**Request**

```json
{ "quantity": 2, "note": "Zi. 12, Fr. Müller" }
```

| Feld | Pflicht | Regel |
|---|---|---|
| `quantity` | ja | > 0, nicht mehr als der aktuelle Bestand |
| `note` | nein | Freitext (Patient, Zimmer …), wird als `MaterialDocumentHeaderText` an SAP übergeben und auf 25 Zeichen gekürzt |

**Response**

```json
{
  "materialDocument": "4900000001",
  "materialDocumentYear": "2026",
  "material": "MED-ENOX40",
  "quantity": 2,
  "unit": "PC",
  "postingDate": "2026-10-16",
  "remainingStock": 4,
  "status": "CRITICAL"
}
```

**Mapping auf SAP** (`POST /API_MATERIAL_DOCUMENT_SRV/A_MaterialDocumentHeader`)

| SAP-Feld | Quelle |
|---|---|
| `Material`, `QuantityInEntryUnit` | Pfad und Request |
| `MaterialDocumentHeaderText` | `note` (gekürzt) |
| `GoodsMovementCode` = `03`, `GoodsMovementType` = `201` | fest im Code (Verbrauch an Kostenstelle) |
| `Plant`, `StorageLocation`, `CostCenter` | Stammdaten der Station |
| `EntryUnit` | leer, SAP nimmt die Basiseinheit |

---

## 4. Nachbestellen

### Wie die Bestellmenge entsteht

Ein Vorschlag entsteht nur für Medikamente, die **unter dem Mindestbestand** liegen (Status `LOW` oder `CRITICAL`).
Bestellt wird dann **bis zum Zielbestand**, abzüglich dessen, was schon bestellt, aber noch nicht geliefert ist,
aufgerundet auf ganze Packungen:

```
Bestellmenge = aufrunden_auf_Packungen(Zielbestand − Bestand − offen bestellt)
```

Ist das Ergebnis ≤ 0, ist der Vorschlag `0`: Es ist bereits genug unterwegs, eine Doppelbestellung wird verhindert.
Zielbestand und Packungsgröße stehen pro Station und Medikament in `MinimumStock`
(Zielbestand leer → doppelter Mindestbestand, Packungsgröße leer → 1).

Beispiel Enoxaparin auf ST03: 30 − 6 − 0 = 24, in 10er-Packungen → **30**.

### 4.1 Vorschläge anzeigen

`GET /api/orders/proposals?station=ST03`

Bestellt nichts. Sortiert nach Dringlichkeit (`CRITICAL` zuerst), Medikamente mit Status `OK` fehlen.

```json
[
  {
    "material": "MED-ONDAN4",
    "name": "Ondansetron 4 mg Ampullen",
    "stock": 3,
    "unit": "PC",
    "minimumStock": 10,
    "targetStock": 20,
    "openOrderQuantity": 0,
    "packageSize": 5,
    "proposedQuantity": 20,
    "status": "CRITICAL"
  },
  {
    "material": "MED-ENOX40",
    "name": "Enoxaparin 40 mg Fertigspritze",
    "stock": 6,
    "unit": "PC",
    "minimumStock": 10,
    "targetStock": 30,
    "openOrderQuantity": 0,
    "packageSize": 10,
    "proposedQuantity": 30,
    "status": "LOW"
  }
]
```

Alle Eingangswerte der Formel sind enthalten, damit das Frontend die Rechnung zeigen kann.
`proposedQuantity: 0` heißt: bereits bestellt.

### 4.2 Alle Vorschläge bestellen

`POST /api/orders?station=ST03` (ohne Body)

Legt in SAP **eine Bestellung pro Medikament** an, für jeden Vorschlag mit `proposedQuantity > 0`.
Ein zweiter Aufruf liefert `[]`, weil dann alles schon offen bestellt ist.

```json
[
  {
    "purchaseOrder": "4500000001",
    "material": "MED-ONDAN4",
    "quantity": 20,
    "unit": "PC",
    "orderDate": "2026-10-16",
    "delivered": false
  }
]
```

### 4.3 Ein Medikament mit eigener Menge bestellen

`POST /api/orders/{material}?station=ST03`

```json
{ "quantity": 40 }
```

| Regel | Fehler |
|---|---|
| `quantity` > 0 | `400` |
| Vielfaches der Packungsgröße | `400` |
| höchstens doppelter Zielbestand (fängt Tippfehler wie 300 statt 30 ab) | `400` |
| für Station und Medikament ist ein Mindestbestand gepflegt | `404` |

Antwort: eine Bestellung im Format von 4.2.

### 4.4 Bestellungen der Station

`GET /api/orders?station=ST03`

Liste im Format von 4.2, offene Bestellungen zuerst, danach die neuesten zuerst.

**Mapping auf SAP** (`POST /API_PURCHASEORDER_PROCESS_SRV/A_PurchaseOrder`)

| SAP-Feld | Quelle |
|---|---|
| `Material`, `OrderQuantity` | Vorschlag bzw. Request |
| `PurchaseOrderQuantityUnit` | Einheit aus dem SAP-Bestand |
| `Plant`, `StorageLocation` | Stammdaten der Station (Lieferort) |
| `PurchaseOrderType` = `NB` | fest (Normalbestellung) |
| `Supplier`, `CompanyCode`, `PurchasingOrganization`, `PurchasingGroup` | `sap.purchasing` in `application.yaml` (`17300001` / `1710` / `1710` / `001`) |
| `PurchaseOrder`, `PurchaseOrderItem`, `PurchaseOrderDate` | vergibt SAP |

Lieferant und Buchungskreis kommen bewusst nicht vom Frontend: Die Pflegekraft entscheidet, **dass** bestellt wird,
nicht bei wem.

---

## 5. Wareneingang → SAP Material Document (Bewegungsart 101)

`POST /api/orders/{purchaseOrder}/goods-receipt?station=ST03` (ohne Body)

Bucht die Lieferung der ganzen Bestellmenge. SAP erhöht den Bestand und markiert die Bestellung als geliefert.

```json
{
  "materialDocument": "4900000002",
  "purchaseOrder": "4500000001",
  "material": "MED-ONDAN4",
  "quantity": 20,
  "unit": "PC",
  "postingDate": "2026-10-16"
}
```

| Fall | Fehler |
|---|---|
| Bestellung gehört nicht zu dieser Station | `404` |
| Bestellung ist schon geliefert | `400` |

**Mapping auf SAP**: dieselbe API wie der Verbrauch, aber `GoodsMovementCode` = `01`,
`GoodsMovementType` = `101`, ohne Kostenstelle und mit `PurchaseOrder` / `PurchaseOrderItem`.

---

## 6. Fehlerformat (alle Endpunkte)

```json
{ "status": 400, "message": "Order quantity must be a multiple of the package size 10." }
```

| Status | Wann |
|---|---|
| `400` | ungültige Menge, zu wenig Bestand, Bestellung schon geliefert, Parameter `station` fehlt, Body kein gültiges JSON |
| `404` | unbekannte Station, unbekanntes Material, kein Mindestbestand gepflegt, Bestellung gehört nicht zur Station |
| `502` | SAP (Mock) nicht erreichbar oder SAP meldet einen Fehler |

Das Frontend zeigt `message` direkt an. Beispielaufrufe für alle Fälle stehen in `test.http`.
