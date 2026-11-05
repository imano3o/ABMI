package de.hhn.abmi.dto.api;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A purchase order of a station.
 *
 * @param purchaseOrder order number assigned by SAP
 * @param material      material number
 * @param quantity      ordered quantity
 * @param unit          unit of measure
 * @param orderDate     date the order was created
 * @param delivered     {@code true} once the goods receipt has been booked
 */
public record PurchaseOrderResponse(
        String purchaseOrder,
        String material,
        BigDecimal quantity,
        String unit,
        LocalDate orderDate,
        boolean delivered
) {
}
