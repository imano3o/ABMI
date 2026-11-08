package de.hhn.abmi.sap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/** One item of a purchase order in SAP's format (A_PurchaseOrderItem). */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record SapPurchaseOrderItem(
        @JsonProperty("PurchaseOrderItem") String purchaseOrderItem,
        @JsonProperty("Material") String material,
        @JsonProperty("Plant") String plant,
        @JsonProperty("StorageLocation") String storageLocation,
        @JsonProperty("OrderQuantity") String quantity,
        @JsonProperty("PurchaseOrderQuantityUnit") String unit,
        @JsonProperty("IsCompletelyDelivered") Boolean completelyDelivered
) {
}
