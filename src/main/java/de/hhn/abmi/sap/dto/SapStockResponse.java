package de.hhn.abmi.sap.dto;

/** OData v2 wraps every response in { "d": ... }. */
public record SapStockResponse(
        SapStockResults d
) {
}
