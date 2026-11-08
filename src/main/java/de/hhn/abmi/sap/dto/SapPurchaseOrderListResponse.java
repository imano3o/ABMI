package de.hhn.abmi.sap.dto;

import java.util.List;

/** OData v2 wrapper around a list of purchase orders: {@code {"d":{"results":[...]}}}. */
public record SapPurchaseOrderListResponse(
        Results d
) {
    public record Results(List<SapPurchaseOrder> results) {
    }
}
