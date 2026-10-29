package de.hhn.abmi.dto;

import java.math.BigDecimal;

/**
 * Stock of one material at a SAP location, in our own format.
 *
 * <p>{@link de.hhn.abmi.sap.SapClient} returns this instead of SAP's raw data,
 * so the services never need to know SAP's field names.
 *
 * @param material material number
 * @param name     display name of the material
 * @param quantity current stock
 * @param unit     unit of measure, e.g. {@code PC}
 */
public record MaterialStock(
        String material,
        String name,
        BigDecimal quantity,
        String unit
) {
}
