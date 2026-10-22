package de.hhn.abmi.sap.dto;

import java.util.List;

/** Der Teil { "results": [...] } einer OData-Liste. */
public record SapStockResults(
        List<SapStock> results
) {
}
