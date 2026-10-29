package de.hhn.abmi.sap;

/**
 * Place in SAP where stock is read and consumption is booked.
 *
 * <p>Always built from our master data, never from user input,
 * so nobody can book on another station's cost center.
 *
 * @param plant           SAP plant of the hospital
 * @param storageLocation SAP storage location of the station
 * @param costCenter      cost center the consumption is charged to
 */
public record SapLocation(
        String plant,
        String storageLocation,
        String costCenter
) {
}
