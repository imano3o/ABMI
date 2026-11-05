package de.hhn.abmi.sap.dto;

import java.util.List;

/** OData wrapper around a list of purchase order items (field {@code results}). */
public record SapPurchaseOrderItems(
        List<SapPurchaseOrderItem> results
) {
}
