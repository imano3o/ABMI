package de.hhn.abmi.sap;

/**
 * plant comes from the hospital, storage location and cost center from the station.
 * Always built from the master data in H2, never from frontend input.
 */
public record SapLocation(
        String plant,
        String storageLocation,
        String costCenter
) {
}
