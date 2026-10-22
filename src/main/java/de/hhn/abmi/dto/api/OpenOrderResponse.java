package de.hhn.abmi.dto.api;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Offene Bestellung in der Medikamentenliste ("openOrder"). */
public record OpenOrderResponse(
        String purchaseOrder,
        BigDecimal quantity,
        LocalDate orderedAt
) {
}
