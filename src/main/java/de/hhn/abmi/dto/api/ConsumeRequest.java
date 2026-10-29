package de.hhn.abmi.dto.api;

import java.math.BigDecimal;

/** note is optional and is cut to 25 characters before it is sent to SAP. */
public record ConsumeRequest(
        BigDecimal quantity,
        String note
) {
}
