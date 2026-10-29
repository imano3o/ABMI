package de.hhn.abmi.sap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** SAP entity A_MaterialDocumentItem. */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record SapMaterialDocumentItem(
        @JsonProperty("Material") String material,
        @JsonProperty("Plant") String plant,
        @JsonProperty("StorageLocation") String storageLocation,
        @JsonProperty("GoodsMovementType") String goodsMovementType,
        @JsonProperty("QuantityInEntryUnit") String quantity,
        @JsonProperty("EntryUnit") String unit,
        @JsonProperty("CostCenter") String costCenter
) {
}
