package de.hhn.abmi.sap;

import de.hhn.abmi.dto.MaterialDocument;
import de.hhn.abmi.dto.MaterialStock;
import de.hhn.abmi.dto.PurchaseOrder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Component;

/**
 * Ersatz für SAP, solange der SAP-Mock nicht läuft.
 * Hält die Bestände nur im Speicher: Nach jedem Neustart sind die Startwerte wieder da.
 * Prüft nichts (z. B. ob genug Bestand da ist) – das ist Aufgabe des Services.
 */
@Component
public class FakeSapClient implements SapClient {

    private static final String SUPPLIER = "17300001";

    private final Map<String, MaterialStock> stocks = new LinkedHashMap<>();
    private final AtomicLong nextMaterialDocument = new AtomicLong(4900000001L);
    private final AtomicLong nextPurchaseOrder = new AtomicLong(4500000001L);

    public FakeSapClient() {
        add("MED-ENOX40", "Enoxaparin 40 mg Fertigspritze", 6);
        add("MED-ONDAN4", "Ondansetron 4 mg Tabletten", 3);
        add("MED-METAM500", "Metamizol 500 mg Tabletten", 18);
        add("MED-NACL500", "NaCl 0,9 % 500 ml", 40);
    }

    private void add(String material, String name, int quantity) {
        stocks.put(material, new MaterialStock(material, name, BigDecimal.valueOf(quantity), "PC"));
    }

    @Override
    public List<MaterialStock> getStocks() {
        return List.copyOf(stocks.values());
    }

    @Override
    public Optional<MaterialStock> getStock(String material) {
        return Optional.ofNullable(stocks.get(material));
    }

    @Override
    public MaterialDocument postConsumption(String material, BigDecimal quantity, String note) {
        MaterialStock old = stocks.get(material);
        stocks.put(material, new MaterialStock(material, old.name(), old.quantity().subtract(quantity), old.unit()));

        return new MaterialDocument(
                String.valueOf(nextMaterialDocument.getAndIncrement()),
                LocalDate.now(),
                "201",
                material,
                quantity
        );
    }

    @Override
    public PurchaseOrder createPurchaseOrder(String material, BigDecimal quantity) {
        return new PurchaseOrder(
                String.valueOf(nextPurchaseOrder.getAndIncrement()),
                material,
                SUPPLIER,
                quantity
        );
    }

}
