package de.hhn.abmi.sap.dto;

/** OData v2 wrapper around the created purchase order (field {@code d}). */
public record SapPurchaseOrderResponse(
        SapPurchaseOrder d
) {
}
