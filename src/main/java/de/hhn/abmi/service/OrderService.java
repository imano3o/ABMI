package de.hhn.abmi.service;

import de.hhn.abmi.dto.MaterialStock;
import de.hhn.abmi.dto.PurchaseOrder;
import de.hhn.abmi.dto.api.ReorderRequest;
import de.hhn.abmi.dto.api.ReorderResponse;
import de.hhn.abmi.model.OpenOrder;
import de.hhn.abmi.model.OpenOrderRepository;
import de.hhn.abmi.sap.SapClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class OrderService {

    private final SapClient sapClient;
    private final OpenOrderRepository openOrderRepository;

    public OrderService(SapClient sapClient, OpenOrderRepository openOrderRepository) {
        this.sapClient = sapClient;
        this.openOrderRepository = openOrderRepository;
    }

    public ReorderResponse reorder(String material, ReorderRequest request) {
        // 1. Menge muss > 0 sein
        if (request.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Die Bestellmenge muss größer als 0 sein.");
        }

        // 2. Material bei SAP holen (für Name und Einheit)
        MaterialStock materialStock = sapClient.getStock(material)
                .orElseThrow(() -> new IllegalArgumentException("Unbekanntes Material: " + material));

        // 3. Pro Material darf es nur eine offene Bestellung geben
        if (openOrderRepository.existsById(material)) {
            throw new IllegalArgumentException("Für " + material + " gibt es bereits eine offene Bestellung.");
        }

        // 4. Bei SAP bestellen
        PurchaseOrder purchaseOrder = sapClient.createPurchaseOrder(material, request.quantity());

        // 5. Offene Bestellung merken
        LocalDate today = LocalDate.now();
        openOrderRepository.save(new OpenOrder(material, purchaseOrder.purchaseOrder(), request.quantity(), today));

        // 6. Antwort bauen
        return new ReorderResponse(
                purchaseOrder.purchaseOrder(),
                material,
                materialStock.name(),
                request.quantity(),
                materialStock.unit(),
                purchaseOrder.supplier(),
                today,
                today.plusDays(3)
        );
    }

}
