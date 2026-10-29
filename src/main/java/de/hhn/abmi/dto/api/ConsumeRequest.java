package de.hhn.abmi.dto.api;

import java.math.BigDecimal;

/** Body von POST /api/medications/{material}/consume. note ist optional. */
public record ConsumeRequest(
        BigDecimal quantity,
        String note
) {
}
