package de.hhn.abmi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** A booked consumption in our own format, so the services do not depend on SAP. */
public record MaterialDocument(
        String materialDocument,
        LocalDate postingDate,
        String goodsMovementType,
        String material,
        BigDecimal quantity
) {
}
