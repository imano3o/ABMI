package de.hhn.abmi.sap.dto;

/** Äußerste Ebene einer OData-v2-Antwort: { "d": { "results": [...] } } */
public record SapStockResponse(
        SapStockResults d
) {
}
