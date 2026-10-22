package de.hhn.abmi.dto.api;

import java.math.BigDecimal;

public record ConsumeRequest(
        BigDecimal quantity,
        String note
) {
}
