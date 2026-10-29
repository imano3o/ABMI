package de.hhn.abmi.dto;

import java.math.BigDecimal;

/** Bestand eines Materials, wie unsere App ihn intern nutzt (unabhängig vom SAP-Format). */
public record MaterialStock(
        String material,
        String name,
        BigDecimal quantity,
        String unit
) {
}
