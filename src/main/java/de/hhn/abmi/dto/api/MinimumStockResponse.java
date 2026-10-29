package de.hhn.abmi.dto.api;

import java.math.BigDecimal;

/**
 * Minimum stock of one medication on a station.
 *
 * @param station         station code
 * @param material        material number
 * @param minimumQuantity minimum stock the station should keep
 */
public record MinimumStockResponse(
        String station,
        String material,
        BigDecimal minimumQuantity
) {
}
