package de.hhn.abmi.dto.api;

/** LOW: below the minimum stock. CRITICAL: empty or below half of the minimum stock. */
public enum StockStatus {
    OK,
    LOW,
    CRITICAL
}
