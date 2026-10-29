package de.hhn.abmi.sap;

/**
 * Wo in SAP gebucht wird: Werk (Krankenhaus), Lagerort (Station) und Kostenstelle (Station).
 * Kommt aus den Stammdaten in H2, nicht aus dem Frontend.
 */
public record SapLocation(
        String plant,
        String storageLocation,
        String costCenter
) {
}
