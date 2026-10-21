package de.hhn.abmi.dto;

import java.math.BigDecimal;

public record MaterialStock(
        String material,
        String name,
        BigDecimal quantity,
        String unit

) {
}
