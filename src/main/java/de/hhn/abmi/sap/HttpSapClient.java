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
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * {@link SapClient} that calls the SAP mock over HTTP (OData v2).
 */
@Component
public class HttpSapClient implements SapClient {

    private static final String STOCK_URI = "/sap/opu/odata/sap/API_MATERIAL_STOCK_SRV/A_MatlStkInAcctMod";
    private static final String MATERIAL_DOCUMENT_URI = "/sap/opu/odata/sap/API_MATERIAL_DOCUMENT_SRV/A_MaterialDocumentHeader";

    private static final String GOODS_ISSUE = "03";       // GoodsMovementCode: goods issue
    private static final String CONSUMPTION = "201";      // GoodsMovementType: consumption for a cost center
    private static final Pattern ODATA_DATE = Pattern.compile("/Date\\((-?\\d+)[^)]*\\)/");

    private final RestClient restClient;

    public HttpSapClient(@Value("${sap.base-url}") String baseUrl) {
        this.restClient = RestClient.create(baseUrl);
    }

    @Override
    public List<MaterialStock> getStocks(SapLocation location) {
        // Jackson turns SAP's JSON into SapStockResponse -> SapStockResults -> SapStock
        SapStockResponse response = restClient.get()
                .uri(STOCK_URI)
                .retrieve()
                .body(SapStockResponse.class);

        List<MaterialStock> result = new ArrayList<>();
        for (SapStock sapStock : response.d().results()) {
            // The mock ignores $filter and returns all storage locations, so we filter here
            if (!location.plant().equals(sapStock.plant())
                    || !location.storageLocation().equals(sapStock.storageLocation())) {
                continue;
            }
            // SapStock (SAP format) -> MaterialStock (our format); the quantity string becomes a number
            result.add(new MaterialStock(
                    sapStock.material(),
                    sapStock.material(), // TODO: the mock returns no name, clarify with the team
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
        // Our parameters -> SAP format: one item inside a material document header
        SapMaterialDocumentItem item = new SapMaterialDocumentItem(
                material,
                location.plant(),
                location.storageLocation(),
                CONSUMPTION,
                quantity.toPlainString(),   // SAP expects quantities as strings
                null,                       // no unit: SAP uses the base unit
                location.costCenter()
        );
        SapMaterialDocument request = new SapMaterialDocument(
                null, null, null,           // number, year and date are set by SAP
                GOODS_ISSUE,
                note,
                new SapMaterialDocumentItems(List.of(item))
        );

        SapMaterialDocumentResponse response = restClient.post()
                .uri(MATERIAL_DOCUMENT_URI)
                .body(request)
                .retrieve()
                .body(SapMaterialDocumentResponse.class);

        // SapMaterialDocument (SAP format) -> MaterialDocument (our format)
        SapMaterialDocument created = response.d();
        return new MaterialDocument(
                created.materialDocument(),
                parseODataDate(created.postingDate()),
                CONSUMPTION,
                material,
                quantity
        );
    }

    /**
     * Converts SAP's OData date, e.g. {@code /Date(1792108800000)/}, into a {@link LocalDate}.
     *
     * <p>Falls back to today if the value is missing or unreadable,
     * because the booking itself has already succeeded at this point.
     *
     * @param value date as sent by SAP, may be {@code null}
     * @return the posting date
     */
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
