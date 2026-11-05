package de.hhn.abmi.sap;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Purchasing master data from {@code sap.purchasing} in application.yaml.
 *
 * <p>Kept out of the frontend on purpose: a nurse decides that something is ordered,
 * but not from which supplier or on which company code.
 *
 * @param supplier               SAP supplier number of the pharmacy wholesaler
 * @param companyCode            company code of the hospital
 * @param purchasingOrganization purchasing organization that places the order
 * @param purchasingGroup        buyer group responsible for medications
 */
@ConfigurationProperties("sap.purchasing")
public record PurchasingSettings(
        String supplier,
        String companyCode,
        String purchasingOrganization,
        String purchasingGroup
) {
}
