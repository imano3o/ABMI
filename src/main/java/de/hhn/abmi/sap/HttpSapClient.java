package de.hhn.abmi.sap;

import de.hhn.abmi.dto.MaterialDocument;
import de.hhn.abmi.dto.MaterialStock;
import de.hhn.abmi.sap.dto.SapMaterialDocument;
import de.hhn.abmi.sap.dto.SapMaterialDocumentItem;
import de.hhn.abmi.sap.dto.SapMaterialDocumentItems;
import de.hhn.abmi.sap.dto.SapMaterialDocumentResponse;
import de.hhn.abmi.sap.dto.SapStock;
import de.hhn.abmi.sap.dto.SapStockResponse;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
    private static final String MATERIAL_DOCUMENT_URI = "/sap/opu/odata/sap/API_MATERIAL_DOCUMENT_SRV/A_MaterialDocumentHeader";

    private static final String GOODS_ISSUE = "03";       // GoodsMovementCode: Warenausgang
    private static final String CONSUMPTION = "201";      // GoodsMovementType: Verbrauch an Kostenstelle
    private static final Pattern ODATA_DATE = Pattern.compile("/Date\\((-?\\d+)[^)]*\\)/");

    private final RestClient restClient;

    public HttpSapClient(@Value("${sap.base-url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    @Override
    public List<MaterialStock> getStocks(SapLocation location) {
        SapStockResponse response = restClient.get()
                .uri(STOCK_URI)
                .retrieve()
                .body(SapStockResponse.class);

        List<MaterialStock> result = new ArrayList<>();
        for (SapStock sapStock : response.d().results()) {
            // Der Mock ignoriert $filter und liefert alle Lagerorte, deshalb hier selbst filtern
            if (!location.plant().equals(sapStock.plant())
                    || !location.storageLocation().equals(sapStock.storageLocation())) {
                continue;
            }
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
    public Optional<MaterialStock> getStock(String material, SapLocation location) {
        return getStocks(location).stream()
                .filter(stock -> stock.material().equals(material))
                .findFirst();
    }

    @Override
    public MaterialDocument postConsumption(String material, BigDecimal quantity, String note, SapLocation location) {
        // Werk, Lagerort und Kostenstelle kommen aus den Stammdaten (Krankenhaus + Station)
        SapMaterialDocumentItem item = new SapMaterialDocumentItem(
                material,
                location.plant(),
                location.storageLocation(),
                CONSUMPTION,
                quantity.toPlainString(),   // SAP erwartet Mengen als String
                null,                       // Einheit: SAP nimmt die Basiseinheit
                location.costCenter()
        );
        SapMaterialDocument request = new SapMaterialDocument(
                null, null, null,           // Belegnummer, Jahr und Datum vergibt SAP
                GOODS_ISSUE,
                note,
                new SapMaterialDocumentItems(List.of(item))
        );

        SapMaterialDocumentResponse response = restClient.post()
                .uri(MATERIAL_DOCUMENT_URI)
                .body(request)
                .retrieve()
                .body(SapMaterialDocumentResponse.class);

        SapMaterialDocument created = response.d();
        return new MaterialDocument(
                created.materialDocument(),
                parseODataDate(created.postingDate()),
                CONSUMPTION,
                material,
                quantity
        );
    }

    /** Wandelt das OData-Datum "/Date(1792108800000)/" in ein LocalDate um. */
    private static LocalDate parseODataDate(String value) {
        if (value == null) {
            return LocalDate.now();
        }
        Matcher matcher = ODATA_DATE.matcher(value);
        if (!matcher.matches()) {
            return LocalDate.now();
        }
        return Instant.ofEpochMilli(Long.parseLong(matcher.group(1))).atZone(ZoneOffset.UTC).toLocalDate();
    }

}
