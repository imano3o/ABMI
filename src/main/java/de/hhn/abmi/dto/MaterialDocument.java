package de.hhn.abmi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Ein gebuchter Materialbeleg, wie unsere App ihn intern nutzt (unabhängig vom SAP-Format). */
public record MaterialDocument(
        String materialDocument,
        LocalDate postingDate,
        String goodsMovementType,
        String material,
        BigDecimal quantity
) {
}
