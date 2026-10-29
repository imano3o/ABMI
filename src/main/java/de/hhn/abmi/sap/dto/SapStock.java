package de.hhn.abmi.sap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Stock entry in SAP's format.
 *
 * <p>SAP sends quantities as strings.
 */
public record SapStock(
        @JsonProperty("Material") String material,
        @JsonProperty("Plant") String plant,
        @JsonProperty("StorageLocation") String storageLocation,
        @JsonProperty("MaterialBaseUnit") String unit,
        @JsonProperty("MatlWrhsStkQtyInMatlBaseUnit") String quantity
) {
}
