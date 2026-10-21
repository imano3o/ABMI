package de.hhn.abmi.sap;

import de.hhn.abmi.dto.MaterialDocument;
import de.hhn.abmi.dto.MaterialStock;
import de.hhn.abmi.dto.PurchaseOrder;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Alles, was unsere App von SAP braucht.
 * Die Services kennen nur dieses Interface, nicht die Implementierung dahinter.
 */
public interface SapClient {

    /** Alle Bestände. */
    List<MaterialStock> getStocks();

    /** Bestand eines Materials, leer wenn SAP das Material nicht kennt. */
    Optional<MaterialStock> getStock(String material);

    /** Verbrauch buchen (Material Document, Bewegungsart 201). */
    MaterialDocument postConsumption(String material, BigDecimal quantity, String note);

    /** Nachbestellung anlegen (Purchase Order). */
    PurchaseOrder createPurchaseOrder(String material, BigDecimal quantity);

}
