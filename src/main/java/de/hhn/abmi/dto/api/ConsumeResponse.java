package de.hhn.abmi.dto.api;

import java.math.BigDecimal;
import java.time.LocalDate;

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
