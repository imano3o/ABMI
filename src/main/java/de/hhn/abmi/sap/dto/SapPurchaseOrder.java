package de.hhn.abmi.sap.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Purchase order header in SAP's format (A_PurchaseOrder).
 *
 * <p>Used for both request and response; SAP fills in number and date.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public record SapPurchaseOrder(
        @JsonProperty("PurchaseOrder") String purchaseOrder,
        @JsonProperty("CompanyCode") String companyCode,
        @JsonProperty("PurchaseOrderType") String purchaseOrderType,
        @JsonProperty("Supplier") String supplier,
        @JsonProperty("PurchasingOrganization") String purchasingOrganization,
        @JsonProperty("PurchasingGroup") String purchasingGroup,
        @JsonProperty("PurchaseOrderDate") String purchaseOrderDate,      // OData format "/Date(1792108800000)/"
        @JsonProperty("to_PurchaseOrderItem") SapPurchaseOrderItems items
) {
}
