package de.hhn.abmi.sap.dto;

import java.util.List;

/** OData wraps lists in { "results": [...] }. */
public record SapMaterialDocumentItems(
        List<SapMaterialDocumentItem> results
) {
}
