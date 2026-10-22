package de.hhn.abmi.dto.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ReorderResponse(
        String purchaseOrder,
        String material,
        String name,
        BigDecimal quantity,
        String unit,
        String supplier,
        LocalDate orderedAt,
        LocalDate deliveryDate
) {
}
