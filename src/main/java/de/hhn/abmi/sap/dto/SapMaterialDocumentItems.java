package de.hhn.abmi.sap.dto;

import java.util.List;

/** OData wraps every list in an object with a results field. */
public record SapMaterialDocumentItems(
        List<SapMaterialDocumentItem> results
) {
}
