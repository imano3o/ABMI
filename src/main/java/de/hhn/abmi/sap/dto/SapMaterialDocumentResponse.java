package de.hhn.abmi.sap.dto;

/** OData-v2-Antwort auf einen POST: { "d": { ... } } */
public record SapMaterialDocumentResponse(
        SapMaterialDocument d
) {
}
