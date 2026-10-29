package de.hhn.abmi.sap.dto;

import java.util.List;

/** OData wrapper around a list of document items (field {@code results}). */
public record SapMaterialDocumentItems(
        List<SapMaterialDocumentItem> results
) {
}
