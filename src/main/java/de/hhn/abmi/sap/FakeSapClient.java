package de.hhn.abmi.sap;

import de.hhn.abmi.dto.MaterialDocument;
import de.hhn.abmi.dto.MaterialStock;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * Stands in for SAP when the SAP mock is not running.
 * Stocks are kept in memory only, so every restart resets them.
 * Checks nothing, that is the service's job.
 */
@Component
@ConditionalOnProperty(name = "sap.mode", havingValue = "fake", matchIfMissing = true)
public class FakeSapClient implements SapClient {

    /** storage location -> (material -> stock) */
    private final Map<String, Map<String, MaterialStock>> stocks = new LinkedHashMap<>();
    private final AtomicLong nextMaterialDocument = new AtomicLong(4900000001L);

    public FakeSapClient() {
        add("ST01", "MED-PCM500", "Paracetamol 500 mg Tabletten", 45);
        add("ST01", "MED-MORPH10", "Morphin 10 mg Ampullen", 8);
        add("ST01", "MED-AMOXI1000", "Amoxicillin 1000 mg Tabletten", 64);
        add("ST01", "MED-NACL500", "NaCl 0,9 % 500 ml", 25);

        add("ST02", "MED-HEP5000", "Heparin 5.000 I.E. Ampullen", 35);
        add("ST02", "MED-FUROS40", "Furosemid 40 mg Tabletten", 90);
        add("ST02", "MED-ENOX40", "Enoxaparin 40 mg Fertigspritze", 4);

        add("ST03", "MED-ENOX40", "Enoxaparin 40 mg Fertigspritze", 6);
        add("ST03", "MED-ONDAN4", "Ondansetron 4 mg Tabletten", 3);
        add("ST03", "MED-METAM500", "Metamizol 500 mg Tabletten", 18);
        add("ST03", "MED-NACL500", "NaCl 0,9 % 500 ml", 40);
    }

    private void add(String storageLocation, String material, String name, int quantity) {
        stocks.computeIfAbsent(storageLocation, key -> new LinkedHashMap<>())
                .put(material, new MaterialStock(material, name, BigDecimal.valueOf(quantity), "PC"));
    }

    private Map<String, MaterialStock> stocksOf(String storageLocation) {
        return stocks.getOrDefault(storageLocation, Map.of());
    }

    @Override
    public List<MaterialStock> getStocks(SapLocation location) {
        return List.copyOf(stocksOf(location.storageLocation()).values());
    }

    @Override
    public Optional<MaterialStock> getStock(String material, SapLocation location) {
        return Optional.ofNullable(stocksOf(location.storageLocation()).get(material));
    }

    @Override
    public MaterialDocument postConsumption(String material, BigDecimal quantity, String note, SapLocation location) {
        Map<String, MaterialStock> stocksOfLocation = stocks.get(location.storageLocation());
        MaterialStock old = stocksOfLocation.get(material);
        stocksOfLocation.put(material,
                new MaterialStock(material, old.name(), old.quantity().subtract(quantity), old.unit()));

        return new MaterialDocument(
                String.valueOf(nextMaterialDocument.getAndIncrement()),
                LocalDate.now(),
                "201",
                material,
                quantity
        );
    }

}
