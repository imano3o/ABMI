package de.hhn.abmi.dto.api;

import java.math.BigDecimal;

/** Ein Medikament in der Liste von GET /api/medications (für das Frontend). */
public record MedicationResponse(
        String material,
        String name,
        BigDecimal stock,
        String unit,
        BigDecimal minimumStock,
        StockStatus status
) {
}
