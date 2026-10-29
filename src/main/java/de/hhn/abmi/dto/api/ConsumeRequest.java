package de.hhn.abmi.dto.api;

import java.math.BigDecimal;

/** Consumption entered by the user. note is optional. */
public record ConsumeRequest(
        BigDecimal quantity,
        String note
) {
}
