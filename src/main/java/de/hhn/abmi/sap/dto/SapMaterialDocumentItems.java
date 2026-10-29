package de.hhn.abmi.sap.dto;

import java.util.List;

/** Der Teil { "results": [...] } unter to_MaterialDocumentItem. */
public record SapMaterialDocumentItems(
        List<SapMaterialDocumentItem> results
) {
}
