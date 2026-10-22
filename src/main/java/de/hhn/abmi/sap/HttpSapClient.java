package de.hhn.abmi.sap;

import de.hhn.abmi.dto.MaterialDocument;
import de.hhn.abmi.dto.MaterialStock;
import de.hhn.abmi.dto.PurchaseOrder;
import de.hhn.abmi.sap.dto.SapStock;
import de.hhn.abmi.sap.dto.SapStockResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * Echte Anbindung an den SAP-Mock per HTTP (sap.mode: http).
 */
@Component
@ConditionalOnProperty(name = "sap.mode", havingValue = "http")
public class HttpSapClient implements SapClient {

    private static final String STOCK_URI = "/sap/opu/odata/sap/API_MATERIAL_STOCK_SRV/A_MatlStkInAcctMod";

    private final RestClient restClient;

    public HttpSapClient(@Value("${sap.base-url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    @Override
    public List<MaterialStock> getStocks() {
        SapStockResponse response = restClient.get()
                .uri(STOCK_URI)
                .retrieve()
                .body(SapStockResponse.class);

        List<MaterialStock> result = new ArrayList<>();
        for (SapStock sapStock : response.d().results()) {
            result.add(new MaterialStock(
                    sapStock.material(),
                    sapStock.material(), // TODO: Der Mock liefert keinen Namen – mit dem Team klären
                    new BigDecimal(sapStock.quantity()),
                    sapStock.unit()
            ));
        }
        return result;
    }

    @Override
    public Optional<MaterialStock> getStock(String material) {
        // Der Mock ignoriert $filter und liefert immer alle Bestände, deshalb hier selbst filtern
        return getStocks().stream()
                .filter(stock -> stock.material().equals(material))
                .findFirst();
    }

    @Override
    public MaterialDocument postConsumption(String material, BigDecimal quantity, String note) {
        throw new UnsupportedOperationException("Noch nicht im SAP-Mock umgesetzt");
    }

    @Override
    public PurchaseOrder createPurchaseOrder(String material, BigDecimal quantity) {
        throw new UnsupportedOperationException("Noch nicht im SAP-Mock umgesetzt");
    }

}
