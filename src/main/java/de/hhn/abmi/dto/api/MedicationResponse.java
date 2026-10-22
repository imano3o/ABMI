package de.hhn.abmi.dto.api;

import java.math.BigDecimal;

/*
Für das Frontend
 */
public record MedicationResponse(
        String material,
        String name,
        BigDecimal stock,
        String unit,
        BigDecimal minimumStock,
        StockStatus status,
        BigDecimal suggestedOrderQuantity,
        OpenOrderResponse openOrder

) {

}
