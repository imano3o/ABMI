package de.hhn.abmi.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Consumption booked in SAP, in our own format.
 *
 * <p>{@link de.hhn.abmi.sap.SapClient} returns this instead of SAP's raw data,
 * so the services never need to know SAP's field names.
 *
 * @param materialDocument  document number assigned by SAP
 * @param postingDate       date of the booking
 * @param goodsMovementType SAP movement type
 * @param material          material number
 * @param quantity          booked quantity
 */
public record MaterialDocument(
        String materialDocument,
        LocalDate postingDate,
        String goodsMovementType,
        String material,
        BigDecimal quantity
) {
}
