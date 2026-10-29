package de.hhn.abmi.dto;

import java.math.BigDecimal;

/**
 * Stock of one material in our own simple format.
 * SapClient returns this instead of SAP's raw data,
 * so the services never need to know SAP's field names.
 */
public record MaterialStock(
        String material,
        String name,
        BigDecimal quantity,
        String unit
) {
}
