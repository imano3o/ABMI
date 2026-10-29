package de.hhn.abmi.sap;

import de.hhn.abmi.dto.MaterialDocument;
import de.hhn.abmi.dto.MaterialStock;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Alles, was unsere App von SAP braucht.
 * Die Services kennen nur dieses Interface, nicht die Implementierung dahinter.
 * location sagt, für welches Werk und welchen Lagerort (= Station) gelesen bzw. gebucht wird.
 */
public interface SapClient {

    /** Alle Bestände an diesem Ort. */
    List<MaterialStock> getStocks(SapLocation location);

    /** Bestand eines Materials an diesem Ort, leer wenn SAP es dort nicht kennt. */
    Optional<MaterialStock> getStock(String material, SapLocation location);

    /** Verbrauch buchen (Material Document, Bewegungsart 201) auf die Kostenstelle des Orts. */
    MaterialDocument postConsumption(String material, BigDecimal quantity, String note, SapLocation location);

}
