package de.hhn.abmi.service;

import de.hhn.abmi.dto.MaterialDocument;
import de.hhn.abmi.dto.PurchaseOrder;
import de.hhn.abmi.dto.api.GoodsReceiptResponse;
import de.hhn.abmi.dto.api.MedicationResponse;
import de.hhn.abmi.dto.api.OrderProposalResponse;
import de.hhn.abmi.dto.api.PurchaseOrderResponse;
import de.hhn.abmi.dto.api.StockStatus;
import de.hhn.abmi.exception.ApiException;
import de.hhn.abmi.model.MinimumStock;
import de.hhn.abmi.model.MinimumStockRepository;
import de.hhn.abmi.sap.SapClient;
import de.hhn.abmi.sap.SapLocation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Business logic for reordering.
 *
 * <p>Decides how much to order: refill up to the target stock, minus what is already on its way,
 * rounded up to whole packages. SAP only records the order and the goods receipt.
 */
@Service
public class OrderService {
    private final MedicationService medicationService;
    private final MinimumStockRepository minimumStockRepository;
    private final SapClient sapClient;
    private final StationService stationService;

    public OrderService(MedicationService medicationService,
                        MinimumStockRepository minimumStockRepository,
                        SapClient sapClient,
                        StationService stationService) {
        this.medicationService = medicationService;
        this.minimumStockRepository = minimumStockRepository;
        this.sapClient = sapClient;
        this.stationService = stationService;
    }

    /**
     * Calculates how much to order.
     *
     * <p>Formula: {@code target - stock - already ordered}, rounded up to whole packages.
     * Returns {@code 0} if stock plus open orders already reach the target, which prevents double orders.
     *
     * @param stock       current stock
     * @param target      stock to refill up to
     * @param openOrdered quantity ordered but not yet delivered
     * @param packageSize pieces per package, greater than 0
     * @return quantity to order, a multiple of the package size
     */
    public static BigDecimal calculateOrderQuantity(BigDecimal stock, BigDecimal target,
                                                    BigDecimal openOrdered, BigDecimal packageSize) {
        BigDecimal need = target.subtract(stock).subtract(openOrdered);
        if (need.signum() <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal packages = need.divide(packageSize, 0, RoundingMode.CEILING);
        return packages.multiply(packageSize);
    }

    /**
     * Checks a quantity the user typed in.
     *
     * <p>The cap of twice the target stock catches typos like 300 instead of 30,
     * which would otherwise go straight to the supplier.
     *
     * @param quantity    quantity to order
     * @param packageSize pieces per package
     * @param target      target stock of the medication
     * @throws ApiException with status 400 if a rule is broken
     */
    public static void checkOrderQuantity(BigDecimal quantity, BigDecimal packageSize, BigDecimal target) {
        if (quantity == null || quantity.signum() <= 0) {
            throw ApiException.badRequest("Order quantity must be greater than 0.");
        }
        if (quantity.remainder(packageSize).signum() != 0) {
            throw ApiException.badRequest("Order quantity must be a multiple of the package size "
                    + packageSize.stripTrailingZeros().toPlainString() + ".");
        }
        BigDecimal max = target.multiply(BigDecimal.TWO);
        if (quantity.compareTo(max) > 0) {
            throw ApiException.badRequest("Order quantity must not exceed "
                    + max.stripTrailingZeros().toPlainString() + " (twice the target stock).");
        }
    }

    /**
     * Returns an order proposal for every medication of a station that is below its minimum stock.
     *
     * @param station station code
     * @return proposals, most urgent first; medications with status OK are left out
     * @throws ApiException with status 404 if the station does not exist
     */
    public List<OrderProposalResponse> getProposals(String station) {
        SapLocation location = stationService.sapLocationOf(station);
        Map<String, BigDecimal> openByMaterial = openQuantityByMaterial(sapClient.getPurchaseOrders(location));

        List<OrderProposalResponse> result = new ArrayList<>();
        // Reuses the list with status, so the traffic light and the proposal always agree
        for (MedicationResponse medication : medicationService.getMedications(station)) {
            if (medication.status() == StockStatus.OK) {
                continue;
            }
            MinimumStock settings = minimumStockRepository.findByStationAndMaterial(station, medication.material())
                    .orElse(null);
            if (settings == null) {
                continue; // only configured medications get a proposal
            }
            BigDecimal open = openByMaterial.getOrDefault(medication.material(), BigDecimal.ZERO);
            BigDecimal proposed = calculateOrderQuantity(
                    medication.stock(), settings.getTargetQuantity(), open, settings.getPackageSize());

            result.add(new OrderProposalResponse(
                    medication.material(),
                    medication.name(),
                    medication.stock(),
                    medication.unit(),
                    medication.minimumStock(),
                    settings.getTargetQuantity(),
                    open,
                    settings.getPackageSize(),
                    proposed,
                    medication.status()
            ));
        }
        result.sort(Comparator.comparing(OrderProposalResponse::status).reversed());
        return result;
    }

    /**
     * Creates a purchase order in SAP for every proposal with a quantity above 0.
     *
     * @param station station code
     * @return the created orders; empty if nothing needs to be ordered
     * @throws ApiException with status 404 if the station does not exist
     */
    public List<PurchaseOrderResponse> placeOrders(String station) {
        SapLocation location = stationService.sapLocationOf(station);
        List<PurchaseOrderResponse> created = new ArrayList<>();
        for (OrderProposalResponse proposal : getProposals(station)) {
            if (proposal.proposedQuantity().signum() <= 0) {
                continue;
            }
            PurchaseOrder order = sapClient.createPurchaseOrder(
                    proposal.material(), proposal.proposedQuantity(), proposal.unit(), location);
            created.add(toResponse(order));
        }
        return created;
    }

    /**
     * Orders one medication with a quantity chosen by the user instead of the proposal.
     *
     * @param station  station code
     * @param material material number
     * @param quantity quantity to order
     * @return the created order
     * @throws ApiException with status 404 if the station, the material or its minimum stock is unknown,
     *                      and with status 400 if the quantity breaks a rule of {@link #checkOrderQuantity}
     */
    public PurchaseOrderResponse placeOrder(String station, String material, BigDecimal quantity) {
        SapLocation location = stationService.sapLocationOf(station);
        // Package size and target are needed for the rules, so only configured medications can be ordered
        MinimumStock settings = minimumStockRepository.findByStationAndMaterial(station, material)
                .orElseThrow(() -> ApiException.notFound("No minimum stock configured for " + material + " on station " + station + "."));
        checkOrderQuantity(quantity, settings.getPackageSize(), settings.getTargetQuantity());

        // The unit comes from SAP, so it always matches the stock
        String unit = sapClient.getStock(material, location)
                .orElseThrow(() -> ApiException.notFound("Unknown material " + material + " on station " + station + "."))
                .unit();
        return toResponse(sapClient.createPurchaseOrder(material, quantity, unit, location));
    }

    /**
     * Returns all purchase orders of a station, open ones first.
     *
     * @param station station code
     * @return the orders
     * @throws ApiException with status 404 if the station does not exist
     */
    public List<PurchaseOrderResponse> getOrders(String station) {
        SapLocation location = stationService.sapLocationOf(station);
        return sapClient.getPurchaseOrders(location).stream()
                .sorted(Comparator.comparing(PurchaseOrder::completelyDelivered)
                        .thenComparing(PurchaseOrder::purchaseOrder, Comparator.reverseOrder()))
                .map(OrderService::toResponse)
                .toList();
    }

    /**
     * Books the delivery of a purchase order: SAP raises the stock by the ordered quantity.
     *
     * @param station       station code
     * @param purchaseOrder order number
     * @return the booked goods receipt
     * @throws ApiException with status 404 if the order does not belong to the station,
     *                      and with status 400 if it has already been delivered
     */
    public GoodsReceiptResponse receive(String station, String purchaseOrder) {
        SapLocation location = stationService.sapLocationOf(station);
        // Looked up in the station's own orders, so a station cannot receive another station's delivery
        PurchaseOrder order = sapClient.getPurchaseOrders(location).stream()
                .filter(o -> o.purchaseOrder().equals(purchaseOrder))
                .findFirst()
                .orElseThrow(() -> ApiException.notFound("Unknown purchase order " + purchaseOrder + " on station " + station + "."));
        if (order.completelyDelivered()) {
            throw ApiException.badRequest("Purchase order " + purchaseOrder + " has already been delivered.");
        }

        MaterialDocument document = sapClient.postGoodsReceipt(order, location);
        return new GoodsReceiptResponse(
                document.materialDocument(),
                order.purchaseOrder(),
                order.material(),
                order.quantity(),
                order.unit(),
                document.postingDate()
        );
    }

    private static Map<String, BigDecimal> openQuantityByMaterial(List<PurchaseOrder> orders) {
        Map<String, BigDecimal> result = new HashMap<>();
        for (PurchaseOrder order : orders) {
            if (!order.completelyDelivered()) {
                result.merge(order.material(), order.quantity(), BigDecimal::add);
            }
        }
        return result;
    }

    private static PurchaseOrderResponse toResponse(PurchaseOrder order) {
        // PurchaseOrder (SAP) -> PurchaseOrderResponse for the frontend, without SAP item numbers
        return new PurchaseOrderResponse(
                order.purchaseOrder(),
                order.material(),
                order.quantity(),
                order.unit(),
                order.orderDate(),
                order.completelyDelivered()
        );
    }
}
