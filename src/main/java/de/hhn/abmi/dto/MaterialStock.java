package de.hhn.abmi.dto;

import java.math.BigDecimal;

/** Our own type, so the services do not depend on the SAP format. */
public record MaterialStock(
        String material,
        String name,
        BigDecimal quantity,
        String unit
) {
}
