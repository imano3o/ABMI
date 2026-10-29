package de.hhn.abmi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Our own type, so the services do not depend on the SAP format. */
public record MaterialDocument(
        String materialDocument,
        LocalDate postingDate,
        String goodsMovementType,
        String material,
        BigDecimal quantity
) {
}
