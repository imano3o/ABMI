package de.hhn.abmi.dto.api;

import java.math.BigDecimal;

/**
 * Order quantity entered by the user, usually the proposal or a changed value.
 *
 * @param quantity amount to order; must be greater than 0, a multiple of the package size
 *                 and at most twice the target stock
 */
public record OrderRequest(
        BigDecimal quantity
) {
}
