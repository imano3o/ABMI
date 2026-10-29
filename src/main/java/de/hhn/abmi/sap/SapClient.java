package de.hhn.abmi.sap;

import de.hhn.abmi.dto.MaterialDocument;
import de.hhn.abmi.dto.MaterialStock;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Everything our app needs from SAP.
 * Services only know this interface, so FakeSapClient and HttpSapClient are interchangeable.
 */
public interface SapClient {

    List<MaterialStock> getStocks(SapLocation location);

    /** Empty if SAP does not know the material at this location. */
    Optional<MaterialStock> getStock(String material, SapLocation location);

    /** Books a consumption and charges it to the cost center of the location. */
    MaterialDocument postConsumption(String material, BigDecimal quantity, String note, SapLocation location);

}
