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
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MedicationService {
    private final MinimumStockRepository minimumStockRepository;
    private final SapClient sapClient;


    public MedicationService(MinimumStockRepository minimumStockRepository, SapClient sapClient) {
        this.minimumStockRepository = minimumStockRepository;
        this.sapClient = sapClient;
    }

    public List<MedicationResponse> getMedications() {
        List<MedicationResponse> result = new ArrayList<>();

        for (MaterialStock stock : sapClient.getStocks()) {

            // 1. Mindestbestand aus H2 holen
            BigDecimal minimum = minimumStockRepository.findByMaterial(stock.material()).map(MinimumStock::getMinimumQuantity).orElse(BigDecimal.ZERO);

            // 2. + 3. Berechnungen (kommt in Aufgabe 4c)
            StockStatus status = calculateStatus(stock.quantity(), minimum);
            BigDecimal suggested = calculateSuggestedQuantity(stock.quantity(), minimum);

            // 4. Antwort bauen

            result.add(new MedicationResponse(
                    stock.material(),
                    stock.name(),
                    stock.quantity(),
                    stock.unit(),
                    minimum,
                    status,
                    suggested,
                    null

            ));
        }

        return result;
    }

    private StockStatus calculateStatus(BigDecimal stock, BigDecimal minimum) {
        BigDecimal half = minimum.divide(BigDecimal.TWO);

        if (stock.compareTo(BigDecimal.ZERO) == 0 || stock.compareTo(half) < 0) {
            return StockStatus.CRITICAL;
        }
        else if (stock.compareTo(minimum) < 0) {

            return StockStatus.LOW;
        }
        return StockStatus.OK;
    }

    private BigDecimal calculateSuggestedQuantity(BigDecimal stock, BigDecimal minimum) {
        BigDecimal goal = minimum.multiply(BigDecimal.TWO);
        return goal.subtract(stock).max(BigDecimal.ZERO);
    }

    public MinimumStock getMedication(String material) {
        return minimumStockRepository.findByMaterial(material)
                .orElseThrow(() -> ApiException.notFound("Unknown material: " + material));
    }

    public ConsumeResponse consume(String material, ConsumeRequest request) {
        // 1. Menge muss angegeben und > 0 sein
        if (request.quantity() == null || request.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw ApiException.badRequest("Quantity must be greater than 0.");
        }
        // 2. Bestand von SAP holen
        MaterialStock materialStock = sapClient.getStock(material)
                .orElseThrow(() -> ApiException.notFound("Unknown material: " + material));

        // 3. Menge darf nicht größer als der Bestand sein
        if (request.quantity().compareTo(materialStock.quantity()) > 0) {
            throw ApiException.badRequest("Not enough stock: " + materialStock.quantity() + " " + materialStock.unit()
                    + " available, " + request.quantity() + " requested.");
        }
        // 4. note kürzen
        String note = request.note();
        if(note != null && note.length()> 25){
            note = note.substring(0,25);
        }
        // 5. SAP buchen
        MaterialDocument document = sapClient.postConsumption(material, request.quantity(), note);

        // 6. Antwort bauen
        BigDecimal remainingStock = materialStock.quantity().subtract(request.quantity());
        BigDecimal minimum = minimumStockRepository.findByMaterial(material)
                .map(MinimumStock::getMinimumQuantity)
                .orElse(BigDecimal.ZERO);
        StockStatus status = calculateStatus(remainingStock, minimum);

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


