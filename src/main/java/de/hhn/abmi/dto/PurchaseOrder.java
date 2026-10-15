package de.hhn.abmi.dto;

import java.math.BigDecimal;

public record PurchaseOrder(
        String purchaseOrder,
        String material,
        String supplier,
        BigDecimal orderQuantity
) {
}
