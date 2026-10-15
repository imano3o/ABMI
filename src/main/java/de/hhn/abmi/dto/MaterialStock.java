package de.hhn.abmi.dto;

import java.math.BigDecimal;

public record MaterialStock(
        String material,
        BigDecimal quantity,
        String unit

) {
}
