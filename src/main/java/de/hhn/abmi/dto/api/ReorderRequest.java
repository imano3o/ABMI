package de.hhn.abmi.dto.api;

import java.math.BigDecimal;

public record ReorderRequest(
        BigDecimal quantity
) {
}
