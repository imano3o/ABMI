package de.hhn.abmi.sap;

/**
 * Where to read and book in SAP. Built from our master data, never from user input,
 * so nobody can book on another station's cost center.
 */
public record SapLocation(
        String plant,
        String storageLocation,
        String costCenter
) {
}
