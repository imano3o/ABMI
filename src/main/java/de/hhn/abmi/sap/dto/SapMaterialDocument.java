package de.hhn.abmi.sap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Material document header as SAP expects it.
 * Used for request and response: SAP sets number, year and posting date.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record SapMaterialDocument(
        @JsonProperty("MaterialDocument") String materialDocument,
        @JsonProperty("MaterialDocumentYear") String materialDocumentYear,
        @JsonProperty("PostingDate") String postingDate,              // OData format "/Date(1792108800000)/"
        @JsonProperty("GoodsMovementCode") String goodsMovementCode,
        @JsonProperty("MaterialDocumentHeaderText") String headerText,
        @JsonProperty("to_MaterialDocumentItem") SapMaterialDocumentItems items
) {
}
