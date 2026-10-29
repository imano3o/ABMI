package de.hhn.abmi.sap.dto;

/** OData v2 wrapper around the created document (field {@code d}). */
public record SapMaterialDocumentResponse(
        SapMaterialDocument d
) {
}
