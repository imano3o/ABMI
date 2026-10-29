package de.hhn.abmi.sap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/** SAP entity A_MatlStkInAcctMod. SAP sends quantities as strings. */
public record SapStock(
        @JsonProperty("Material") String material,
        @JsonProperty("Plant") String plant,
        @JsonProperty("StorageLocation") String storageLocation,
        @JsonProperty("MaterialBaseUnit") String unit,
        @JsonProperty("MatlWrhsStkQtyInMatlBaseUnit") String quantity
) {
}
