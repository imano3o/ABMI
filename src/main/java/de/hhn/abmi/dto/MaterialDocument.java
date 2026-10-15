package de.hhn.abmi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record MaterialDocument(
        String materialDocument,
        LocalDate postingDate,
        String goodsMovementType,
        String material,
        BigDecimal quantity
) {
}
