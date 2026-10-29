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

/**
 * Fachlogik für Medikamente: verbindet den Bestand aus SAP mit dem Mindestbestand aus H2.
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

    /** Alle Medikamente der Station mit Bestand, Mindestbestand und Status. */
    public List<MedicationResponse> getMedications(String station) {
        SapLocation location = stationService.sapLocationOf(station);
        List<MedicationResponse> result = new ArrayList<>();

        for (MaterialStock stock : sapClient.getStocks(location)) {

            // 1. Mindestbestand aus H2 holen
            BigDecimal minimum = getMinimum(station, stock.material());

            // 2. Status berechnen
            StockStatus status = calculateStatus(stock.quantity(), minimum);

            // 3. Antwort bauen
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

    /** Mindestbestand der Station aus H2, 0 falls keiner hinterlegt ist. */
    private BigDecimal getMinimum(String station, String material) {
        return minimumStockRepository.findByStationAndMaterial(station, material)
                .map(MinimumStock::getMinimumQuantity)
                .orElse(BigDecimal.ZERO);
    }

    /** CRITICAL bei 0 oder unter der Hälfte des Minimums, LOW unter dem Minimum, sonst OK. */
    private StockStatus calculateStatus(BigDecimal stock, BigDecimal minimum) {
        BigDecimal half = minimum.divide(BigDecimal.TWO);

        if (stock.compareTo(BigDecimal.ZERO) == 0 || stock.compareTo(half) < 0) {
            return StockStatus.CRITICAL;
        } else if (stock.compareTo(minimum) < 0) {
            return StockStatus.LOW;
        }
        return StockStatus.OK;
    }

    /** Mindestbestand-Eintrag aus H2; 404, wenn es keinen für diese Station gibt. */
    public MinimumStock getMedication(String station, String material) {
        return minimumStockRepository.findByStationAndMaterial(station, material)
                .orElseThrow(() -> ApiException.notFound("Unknown material " + material + " on station " + station + "."));
    }

    /** Prüft die Menge, bucht den Verbrauch in SAP und gibt den neuen Bestand zurück. */
    public ConsumeResponse consume(String station, String material, ConsumeRequest request) {
        SapLocation location = stationService.sapLocationOf(station);

        // 1. Menge muss angegeben und > 0 sein
        if (request.quantity() == null || request.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw ApiException.badRequest("Quantity must be greater than 0.");
        }
        // 2. Bestand von SAP holen
        MaterialStock materialStock = sapClient.getStock(material, location)
                .orElseThrow(() -> ApiException.notFound("Unknown material " + material + " on station " + station + "."));

        // 3. Menge darf nicht größer als der Bestand sein
        if (request.quantity().compareTo(materialStock.quantity()) > 0) {
            throw ApiException.badRequest("Not enough stock: " + materialStock.quantity() + " " + materialStock.unit()
                    + " available, " + request.quantity() + " requested.");
        }
        // 4. note kürzen: Der Kopftext eines SAP-Materialbelegs hat maximal 25 Zeichen
        String note = request.note();
        if (note != null && note.length() > 25) {
            note = note.substring(0, 25);
        }
        // 5. SAP buchen
        MaterialDocument document = sapClient.postConsumption(material, request.quantity(), note, location);

        // 6. Antwort bauen (Restbestand selbst berechnen, statt SAP noch einmal zu fragen)
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
