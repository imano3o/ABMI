# MediFlow – REST-API (Frontend ↔ ABMI-Backend)

Basis-URL: `http://localhost:8080/api`

Grundsatz: **Das Frontend kennt keine SAP-Begriffe.** Es schickt nur, was eine Pflegekraft eingibt.
SAP-spezifische Werte (Werk, Lagerort, Kostenstelle, Bewegungsart, Lieferant …) ergänzt das Backend
aus Konfiguration (`application.yaml`) und Stammdaten (`MinimumStock`).

## Konventionen

- JSON in **camelCase**
- Datum als ISO-String: `"2026-10-16"`
- Mengen als **Zahl** (nicht als String wie bei SAP)
- `material` (z.B. `MED-IBU400`) ist überall der Schlüssel; die Nummern stimmen mit dem SAP-Mock überein

---

## 1. Medikamentenliste

`GET /api/medications`

**Response `200`**

```json
[
  {
    "material": "MED-ENOX40",
    "name": "Enoxaparin 40 mg Fertigspritze",
    "stock": 6,
    "unit": "PC",
    "minimumStock": 20,
    "status": "CRITICAL",
    "suggestedOrderQuantity": 34,
    "openOrder": null
  },
  {
    "material": "MED-METAM500",
    "name": "Metamizol 500 mg Tabletten",
    "stock": 18,
    "unit": "PC",
    "minimumStock": 30,
    "status": "LOW",
    "suggestedOrderQuantity": 42,
    "openOrder": {
      "purchaseOrder": "4500000001",
      "quantity": 42,
      "orderedAt": "2026-10-16"
    }
  },
  {
    "material": "MED-IBU400",
    "name": "Ibuprofen 400 mg Tabletten",
    "stock": 240,
    "unit": "PC",
    "minimumStock": 50,
    "status": "OK",
    "suggestedOrderQuantity": 0,
    "openOrder": null
  }
]
```

| Feld | Bedeutung |
|---|---|
| `stock` | aktueller Bestand aus SAP (Material Stock) |
| `minimumStock` | Mindestbestand aus den KIS-Stammdaten |
| `status` | `OK`, `LOW` (unter Mindestbestand) oder `CRITICAL` (unter der Hälfte des Mindestbestands oder 0) – berechnet das Backend |
| `suggestedOrderQuantity` | Mengenvorschlag für die Nachbestellung – berechnet das Backend |
| `openOrder` | offene Bestellung oder `null`; ist sie gesetzt, zeigt das Frontend „bestellt am …“ statt des Buttons |

---

## 2. Verbrauchen → SAP Material Document

`POST /api/medications/{material}/consume`

**Request**

```json
{ "quantity": 2, "note": "Zi. 12, Fr. Müller" }
```

| Feld | Pflicht | Regel |
|---|---|---|
| `quantity` | ja | ganze Zahl > 0, nicht mehr als der aktuelle Bestand |
| `note` | nein | Freitext (Patient, Zimmer …); wird als `MaterialDocumentHeaderText` an SAP übergeben (max. 25 Zeichen) |

**Response `200`**

```json
{
  "materialDocument": "4900000007",
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
| `Material` | Pfad |
| `QuantityInEntryUnit`, `EntryUnit` | `quantity` aus dem Request, Einheit aus dem Bestand |
| `MaterialDocumentHeaderText` | `note` (gekürzt) |
| `GoodsMovementCode` = `03`, `GoodsMovementType` = `201` | fest im Code (Verbrauch an Kostenstelle) |
| `Plant`, `StorageLocation`, `CostCenter` | `application.yaml` |
| `PostingDate` | heutiges Datum |

---

## 3. Nachbestellen → SAP Purchase Order

`POST /api/medications/{material}/reorder`

**Request**

```json
{ "quantity": 34 }
```

| Feld | Pflicht | Regel |
|---|---|---|
| `quantity` | ja | > 0; das Frontend füllt `suggestedOrderQuantity` vor |

**Response `201`**

```json
{
  "purchaseOrder": "4500000002",
  "material": "MED-ENOX40",
  "name": "Enoxaparin 40 mg Fertigspritze",
  "quantity": 34,
  "unit": "PC",
  "supplier": "17300001",
  "orderedAt": "2026-10-16",
  "deliveryDate": "2026-10-19"
}
```

**Mapping auf SAP** (`POST /API_PURCHASEORDER_PROCESS_SRV/A_PurchaseOrder`)

| SAP-Feld | Quelle |
|---|---|
| `Material`, `OrderQuantity`, `PurchaseOrderQuantityUnit` | Pfad, Request, Bestand |
| `Plant`, `StorageLocation` | `application.yaml` (Lieferort) |
| `PurchaseOrderType` = `NB` | fest (Normalbestellung) |
| `CompanyCode`, `PurchasingOrganization`, `PurchasingGroup` | `application.yaml` (z.B. `1710` / `1710` / `001`) |
| `Supplier` | `application.yaml` (Standard-Lieferant) oder Spalte in `MinimumStock` |
| `ScheduleLineDeliveryDate` | heute + 3 Tage |

> Die genauen SAP-Feldnamen der Purchase Order werden noch mit der Sandbox abgeglichen.

---

## 4. Fehlerformat (alle Endpunkte)

```json
{ "status": 400, "message": "Nicht genug Bestand: 6 PC vorhanden, 10 angefordert." }
```

| Status | Wann |
|---|---|
| `400` | ungültige Menge, zu wenig Bestand |
| `404` | unbekannte Materialnummer |
| `409` | es gibt bereits eine offene Bestellung |
| `502` | SAP (Mock) nicht erreichbar oder SAP meldet einen Fehler |

Das Frontend zeigt `message` direkt an; die Texte formuliert das Backend auf Deutsch.

---

## 5. Optional (Meilenstein 4)

| Endpunkt | Zweck |
|---|---|
| `POST /api/orders/{purchaseOrder}/goods-receipt` | Wareneingang buchen (Material Document, Bewegungsart 101); danach `openOrder = null`, Bestand steigt |
| `GET /api/medications/{material}/consumptions` | Verbrauchshistorie: `[{ "materialDocument", "quantity", "note", "postingDate" }]` |
