package de.hhn.abmi.dto.api;

import java.math.BigDecimal;

/**
 * Consumption entered by the user.
 *
 * @param quantity amount to take out, must be greater than 0
 * @param note     optional note; cut to 25 characters because SAP allows no more
 */
public record ConsumeRequest(
        BigDecimal quantity,
        String note
) {
}
