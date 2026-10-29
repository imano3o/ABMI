package de.hhn.abmi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A booked consumption in our own simple format.
 * SapClient returns this instead of SAP's raw data,
 * so the services never need to know SAP's field names.
 */
public record MaterialDocument(
        String materialDocument,
        LocalDate postingDate,
        String goodsMovementType,
        String material,
        BigDecimal quantity
) {
}
