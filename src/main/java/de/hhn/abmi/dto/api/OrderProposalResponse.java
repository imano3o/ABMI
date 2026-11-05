package de.hhn.abmi.dto.api;

import java.math.BigDecimal;

/**
 * Order proposal for one medication that is below its minimum stock.
 *
 * <p>All inputs of the formula are included, so the frontend can show how the quantity was calculated.
 *
 * @param material          material number
 * @param name              display name
 * @param stock             current stock from SAP
 * @param unit              unit of measure
 * @param minimumStock      minimum stock from our database
 * @param targetStock       stock to refill up to
 * @param openOrderQuantity quantity already ordered but not yet delivered
 * @param packageSize       pieces per package
 * @param proposedQuantity  quantity to order; {@code 0} if the open orders already cover the need
 * @param status            current status of the medication
 */
public record OrderProposalResponse(
        String material,
        String name,
        BigDecimal stock,
        String unit,
        BigDecimal minimumStock,
        BigDecimal targetStock,
        BigDecimal openOrderQuantity,
        BigDecimal packageSize,
        BigDecimal proposedQuantity,
        StockStatus status
) {
}
