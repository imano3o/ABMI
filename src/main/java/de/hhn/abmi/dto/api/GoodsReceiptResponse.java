package de.hhn.abmi.dto.api;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Result of a booked goods receipt.
 *
 * @param materialDocument document number assigned by SAP
 * @param purchaseOrder    the delivered order
 * @param material         material number
 * @param quantity         received quantity
 * @param unit             unit of measure
 * @param postingDate      date of the booking
 */
public record GoodsReceiptResponse(
        String materialDocument,
        String purchaseOrder,
        String material,
        BigDecimal quantity,
        String unit,
        LocalDate postingDate
) {
}
