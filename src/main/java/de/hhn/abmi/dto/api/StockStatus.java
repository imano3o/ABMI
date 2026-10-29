package de.hhn.abmi.dto.api;

/** How urgently a medication needs to be refilled. */
public enum StockStatus {
    /** Stock is at or above the minimum. */
    OK,
    /** Stock is below the minimum. */
    LOW,
    /** Stock is empty or below half of the minimum. */
    CRITICAL
}
