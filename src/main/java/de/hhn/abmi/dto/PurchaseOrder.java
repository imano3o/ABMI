package de.hhn.abmi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * One item of a purchase order in SAP, in our own format.
 *
 * <p>We always order one material per purchase order, so header and item are merged into one record.
 *
 * @param purchaseOrder       order number assigned by SAP
 * @param purchaseOrderItem   item number within the order, e.g. {@code 10}
 * @param material            material number
 * @param storageLocation     SAP storage location the order is delivered to
 * @param quantity            ordered quantity
 * @param unit                unit of measure
 * @param orderDate           date the order was created
 * @param completelyDelivered {@code true} once the goods receipt has been booked
 */
public record PurchaseOrder(
        String purchaseOrder,
        String purchaseOrderItem,
        String material,
        String storageLocation,
        BigDecimal quantity,
        String unit,
        LocalDate orderDate,
        boolean completelyDelivered
) {
}
