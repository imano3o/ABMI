package de.hhn.abmi.service;

import de.hhn.abmi.dto.MaterialDocument;
import de.hhn.abmi.dto.MaterialStock;
import de.hhn.abmi.dto.api.ConsumeRequest;
import de.hhn.abmi.dto.api.ConsumeResponse;
import de.hhn.abmi.dto.api.MedicationResponse;
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

/** Combines the stock from SAP with the minimum stock from H2. */
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

    public List<MedicationResponse> getMedications(String station) {
        SapLocation location = stationService.sapLocationOf(station);
        List<MedicationResponse> result = new ArrayList<>();

        for (MaterialStock stock : sapClient.getStocks(location)) {

            BigDecimal minimum = getMinimum(station, stock.material());

            StockStatus status = calculateStatus(stock.quantity(), minimum);

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

    /** 0 if no minimum stock is configured for this station. */
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

    public MinimumStock getMedication(String station, String material) {
        return minimumStockRepository.findByStationAndMaterial(station, material)
                .orElseThrow(() -> ApiException.notFound("Unknown material " + material + " on station " + station + "."));
    }

    public ConsumeResponse consume(String station, String material, ConsumeRequest request) {
        SapLocation location = stationService.sapLocationOf(station);

        if (request.quantity() == null || request.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw ApiException.badRequest("Quantity must be greater than 0.");
        }
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
        MaterialDocument document = sapClient.postConsumption(material, request.quantity(), note, location);

        // Computed here to avoid a second call to SAP
        BigDecimal remainingStock = materialStock.quantity().subtract(request.quantity());
        StockStatus status = calculateStatus(remainingStock, getMinimum(station, material));

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
