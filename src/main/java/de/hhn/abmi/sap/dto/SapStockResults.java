package de.hhn.abmi.sap.dto;

import java.util.List;

/** OData wrapper around a list of stocks (field {@code results}). */
public record SapStockResults(
        List<SapStock> results
) {
}
