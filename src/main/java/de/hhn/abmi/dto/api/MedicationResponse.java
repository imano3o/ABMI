package de.hhn.abmi.dto.api;

import java.math.BigDecimal;

/** One row of the medication list: stock from SAP, minimum stock from our database. */
public record MedicationResponse(
        String material,
        String name,
        BigDecimal stock,
        String unit,
        BigDecimal minimumStock,
        StockStatus status
) {
}
