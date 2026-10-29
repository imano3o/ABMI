package de.hhn.abmi.service;

import de.hhn.abmi.dto.MaterialDocument;
import de.hhn.abmi.dto.MaterialStock;
import de.hhn.abmi.dto.api.ConsumeRequest;
import de.hhn.abmi.dto.api.ConsumeResponse;
import de.hhn.abmi.dto.api.MedicationResponse;
import de.hhn.abmi.dto.api.MinimumStockResponse;
import de.hhn.abmi.dto.api.StockStatus;
import de.hhn.abmi.exception.ApiException;
import de.hhn.abmi.model.MinimumStock;
import de.hhn.abmi.model.MinimumStockRepository;
import de.hhn.abmi.sap.SapClient;
import de.hhn.abmi.sap.SapLocation;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for medications.
 *
 * <p>Combines the stock from SAP with the minimum stock from our database.
 */
@Service
public class MedicationService {
    private final MinimumStockRepository minimumStockRepository;
    private final SapClient sapClient;
    private final StationService stationService;

    public MedicationService(MinimumStockRepository minimumStockRepository,
                             SapClient sapClient,
                             StationService stationService) {
        this.minimumStockRepository = minimumStockRepository;
        this.sapClient = sapClient;
        this.stationService = stationService;
    }

    /**
     * Returns all medications of a station with stock, minimum stock and status.
     *
     * @param station station code
     * @return one entry per material that SAP has at the station
     * @throws ApiException with status 404 if the station does not exist
     */
    public List<MedicationResponse> getMedications(String station) {
        // Station code -> plant, storage location and cost center from our master data
        SapLocation location = stationService.sapLocationOf(station);
        List<MedicationResponse> result = new ArrayList<>();

        // SapClient already returns our own MaterialStock, no SAP format here
        for (MaterialStock stock : sapClient.getStocks(location)) {

            BigDecimal minimum = getMinimum(station, stock.material());

            StockStatus status = calculateStatus(stock.quantity(), minimum);

            // MaterialStock (SAP) + minimum stock (H2) + status -> MedicationResponse for the frontend
            result.add(new MedicationResponse(
                    stock.material(),
                    stock.name(),
                    stock.quantity(),
                    stock.unit(),
                    minimum,
                    status
            ));
        }

        return result;
    }

    /** Returns the minimum stock, or {@code 0} if none is configured. */
    private BigDecimal getMinimum(String station, String material) {
        return minimumStockRepository.findByStationAndMaterial(station, material)
                .map(MinimumStock::getMinimumQuantity)
                .orElse(BigDecimal.ZERO);
    }

    private StockStatus calculateStatus(BigDecimal stock, BigDecimal minimum) {
        BigDecimal half = minimum.divide(BigDecimal.TWO);

        if (stock.compareTo(BigDecimal.ZERO) == 0 || stock.compareTo(half) < 0) {
            return StockStatus.CRITICAL;
        } else if (stock.compareTo(minimum) < 0) {
            return StockStatus.LOW;
        }
        return StockStatus.OK;
    }

    /**
     * Returns the stored minimum stock of a medication.
     *
     * @param station  station code
     * @param material material number
     * @return the minimum stock entry
     * @throws ApiException with status 404 if no minimum stock is configured
     */
    public MinimumStockResponse getMedication(String station, String material) {
        MinimumStock minimumStock = minimumStockRepository.findByStationAndMaterial(station, material)
                .orElseThrow(() -> ApiException.notFound("Unknown material " + material + " on station " + station + "."));
        // Entity -> response DTO, so the database id never reaches the frontend
        return new MinimumStockResponse(
                minimumStock.getStation(),
                minimumStock.getMaterial(),
                minimumStock.getMinimumQuantity()
        );
    }

    /**
     * Books the consumption of a medication in SAP.
     *
     * @param station  station code
     * @param material material number
     * @param request  quantity and optional note
     * @return the booked document with the remaining stock and its status
     * @throws ApiException with status 400 if the quantity is missing, not positive or larger than the stock,
     *                      and with status 404 if the station or material is unknown
     */
    public ConsumeResponse consume(String station, String material, ConsumeRequest request) {
        // Station code -> plant, storage location and cost center from our master data
        SapLocation location = stationService.sapLocationOf(station);

        if (request.quantity() == null || request.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw ApiException.badRequest("Quantity must be greater than 0.");
        }
        // Current stock from SAP, needed to reject a quantity that is larger than the stock
        MaterialStock materialStock = sapClient.getStock(material, location)
                .orElseThrow(() -> ApiException.notFound("Unknown material " + material + " on station " + station + "."));

        if (request.quantity().compareTo(materialStock.quantity()) > 0) {
            throw ApiException.badRequest("Not enough stock: " + materialStock.quantity() + " " + materialStock.unit()
                    + " available, " + request.quantity() + " requested.");
        }
        // SAP allows at most 25 characters for the header text
        String note = request.note();
        if (note != null && note.length() > 25) {
            note = note.substring(0, 25);
        }
        // Books in SAP; SapClient converts SAP's answer into our own MaterialDocument
        MaterialDocument document = sapClient.postConsumption(material, request.quantity(), note, location);

        // Computed here to avoid a second call to SAP
        BigDecimal remainingStock = materialStock.quantity().subtract(request.quantity());
        StockStatus status = calculateStatus(remainingStock, getMinimum(station, material));

        // MaterialDocument (SAP) + remaining stock + status -> ConsumeResponse for the frontend
        return new ConsumeResponse(
                document.materialDocument(),
                String.valueOf(document.postingDate().getYear()),
                material,
                request.quantity(),
                materialStock.unit(),
                document.postingDate(),
                remainingStock,
                status
        );
    }

}
