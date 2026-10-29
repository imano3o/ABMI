package de.hhn.abmi.sap.dto;

/** OData v2 wraps every response in an object with a d field. */
public record SapStockResponse(
        SapStockResults d
) {
}
