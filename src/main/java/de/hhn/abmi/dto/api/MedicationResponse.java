package de.hhn.abmi.dto.api;

import java.math.BigDecimal;

/**
 * One row of the medication list.
 *
 * @param material     material number
 * @param name         display name
 * @param stock        current stock from SAP
 * @param unit         unit of measure
 * @param minimumStock minimum stock from our database, {@code 0} if none is configured
 * @param status       how urgently the medication needs to be refilled
 */
public record MedicationResponse(
        String material,
        String name,
        BigDecimal stock,
        String unit,
        BigDecimal minimumStock,
        StockStatus status
) {
}
