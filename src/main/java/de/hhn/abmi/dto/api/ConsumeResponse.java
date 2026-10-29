package de.hhn.abmi.dto.api;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Result of a booked consumption.
 *
 * @param materialDocument     document number assigned by SAP
 * @param materialDocumentYear year of the document, part of its key in SAP
 * @param material             material number
 * @param quantity             booked quantity
 * @param unit                 unit of measure
 * @param postingDate          date of the booking
 * @param remainingStock       stock after the booking
 * @param status               status for the remaining stock
 */
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
