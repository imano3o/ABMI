package de.hhn.abmi.sap.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Ein Bestandseintrag (A_MatlStkInAcctMod) im SAP-Format.
 * SAP überträgt Mengen als String.
 */
public record SapStock(
        @JsonProperty("Material") String material,
        @JsonProperty("Plant") String plant,
        @JsonProperty("StorageLocation") String storageLocation,
        @JsonProperty("MaterialBaseUnit") String unit,
        @JsonProperty("MatlWrhsStkQtyInMatlBaseUnit") String quantity
) {
}
