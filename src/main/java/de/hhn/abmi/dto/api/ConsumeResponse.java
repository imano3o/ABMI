package de.hhn.abmi.dto.api;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Result of a booked consumption, so the frontend can show the new stock and status. */
public record ConsumeResponse(
        String materialDocument,
        String materialDocumentYear,
        String material,
        BigDecimal quantity,
        String unit,
        LocalDate postingDate,
        BigDecimal remainingStock,
        StockStatus status
) {
}
