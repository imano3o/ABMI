package de.hhn.abmi.sap.dto;

/** OData v2 wrapper around the stock list (field {@code d}). */
public record SapStockResponse(
        SapStockResults d
) {
}
