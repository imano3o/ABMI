package de.hhn.abmi.dto.api;

/** How urgently a medication needs to be refilled. LOW: below the minimum stock, CRITICAL: empty or below half of it. */
public enum StockStatus {
    OK,
    LOW,
    CRITICAL
}
