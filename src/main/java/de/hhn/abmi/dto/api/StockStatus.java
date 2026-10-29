package de.hhn.abmi.dto.api;

/**
 * Ampel für den Bestand im Vergleich zum Mindestbestand.
 * OK = mindestens Mindestbestand, LOW = darunter, CRITICAL = leer oder unter der Hälfte.
 */
public enum StockStatus {
    OK,
    LOW,
    CRITICAL
}
