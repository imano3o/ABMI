package de.hhn.abmi.sap;

import de.hhn.abmi.dto.MaterialDocument;
import de.hhn.abmi.dto.MaterialStock;
import de.hhn.abmi.dto.PurchaseOrder;
import de.hhn.abmi.sap.dto.SapMaterialDocument;
import de.hhn.abmi.sap.dto.SapMaterialDocumentItem;
import de.hhn.abmi.sap.dto.SapMaterialDocumentItems;
import de.hhn.abmi.sap.dto.SapMaterialDocumentResponse;
import de.hhn.abmi.sap.dto.SapPurchaseOrder;
import de.hhn.abmi.sap.dto.SapPurchaseOrderItem;
import de.hhn.abmi.sap.dto.SapPurchaseOrderItems;
import de.hhn.abmi.sap.dto.SapPurchaseOrderListResponse;
import de.hhn.abmi.sap.dto.SapPurchaseOrderResponse;
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
    private static final String PURCHASE_ORDER_URI = "/sap/opu/odata/sap/API_PURCHASEORDER_PROCESS_SRV/A_PurchaseOrder";

    private static final String GOODS_ISSUE = "03";       // GoodsMovementCode: goods issue
    private static final String CONSUMPTION = "201";      // GoodsMovementType: consumption for a cost center
    private static final String GOODS_RECEIPT_CODE = "01"; // GoodsMovementCode: goods receipt for a purchase order
    private static final String GOODS_RECEIPT = "101";    // GoodsMovementType: goods receipt for a purchase order
    private static final String STANDARD_ORDER = "NB";    // PurchaseOrderType: standard purchase order
    private static final Pattern ODATA_DATE = Pattern.compile("/Date\\((-?\\d+)[^)]*\\)/");

    private final RestClient restClient;
    private final PurchasingSettings purchasing;

    public HttpSapClient(@Value("${sap.base-url}") String baseUrl, PurchasingSettings purchasing) {
        this.restClient = RestClient.create(baseUrl);
        this.purchasing = purchasing;
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
                    sapStock.material(), // SAP returns no name; MedicationService takes it from our master data
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
                location.costCenter(),
                null, null                  // no purchase order: this is a consumption
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

    @Override
    public List<PurchaseOrder> getPurchaseOrders(SapLocation location) {
        // The mock ignores $filter and $expand: it returns all orders with their items, so we filter here
        SapPurchaseOrderListResponse response = restClient.get()
                .uri(PURCHASE_ORDER_URI)
                .retrieve()
                .body(SapPurchaseOrderListResponse.class);

        List<PurchaseOrder> result = new ArrayList<>();
        for (SapPurchaseOrder order : response.d().results()) {
            for (SapPurchaseOrderItem item : order.items().results()) {
                if (location.plant().equals(item.plant()) && location.storageLocation().equals(item.storageLocation())) {
                    result.add(toPurchaseOrder(order, item));
                }
            }
        }
        return result;
    }

    @Override
    public PurchaseOrder createPurchaseOrder(String material, BigDecimal quantity, String unit, SapLocation location) {
        // Our parameters -> SAP format; supplier and purchasing org come from application.yaml, not from the user
        SapPurchaseOrderItem item = new SapPurchaseOrderItem(
                null,                       // item number is set by SAP
                material,
                location.plant(),
                location.storageLocation(),
                quantity.toPlainString(),   // SAP expects quantities as strings
                unit,
                null
        );
        SapPurchaseOrder request = new SapPurchaseOrder(
                null,                       // number is set by SAP
                purchasing.companyCode(),
                STANDARD_ORDER,
                purchasing.supplier(),
                purchasing.purchasingOrganization(),
                purchasing.purchasingGroup(),
                null,                       // date is set by SAP
                new SapPurchaseOrderItems(List.of(item))
        );

        SapPurchaseOrderResponse response = restClient.post()
                .uri(PURCHASE_ORDER_URI)
                .body(request)
                .retrieve()
                .body(SapPurchaseOrderResponse.class);

        // SapPurchaseOrder (SAP format) -> PurchaseOrder (our format)
        SapPurchaseOrder created = response.d();
        return toPurchaseOrder(created, created.items().results().getFirst());
    }

    @Override
    public MaterialDocument postGoodsReceipt(PurchaseOrder order, SapLocation location) {
        // Same API as the consumption, only the movement type differs and the order is referenced
        SapMaterialDocumentItem item = new SapMaterialDocumentItem(
                order.material(),
                location.plant(),
                location.storageLocation(),
                GOODS_RECEIPT,
                order.quantity().toPlainString(),
                order.unit(),
                null,                       // no cost center: a goods receipt goes into stock
                order.purchaseOrder(),
                order.purchaseOrderItem()
        );
        SapMaterialDocument request = new SapMaterialDocument(
                null, null, null,
                GOODS_RECEIPT_CODE,
                "Lieferung " + order.purchaseOrder(),
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
                GOODS_RECEIPT,
                order.material(),
                order.quantity()
        );
    }

    /** SapPurchaseOrder + one of its items (SAP format) -> PurchaseOrder (our format) */
    private static PurchaseOrder toPurchaseOrder(SapPurchaseOrder order, SapPurchaseOrderItem item) {
        return new PurchaseOrder(
                order.purchaseOrder(),
                item.purchaseOrderItem(),
                item.material(),
                item.storageLocation(),
                new BigDecimal(item.quantity()),
                item.unit(),
                parseODataDate(order.purchaseOrderDate()),
                Boolean.TRUE.equals(item.completelyDelivered())
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
