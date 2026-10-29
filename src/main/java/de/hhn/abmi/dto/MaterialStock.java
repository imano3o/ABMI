package de.hhn.abmi.dto;

import java.math.BigDecimal;

/** Stock of one material in our own format, so the services do not depend on SAP. */
public record MaterialStock(
        String material,
        String name,
        BigDecimal quantity,
        String unit
) {
}
