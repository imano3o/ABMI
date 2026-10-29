package de.hhn.abmi.sap;

import de.hhn.abmi.dto.MaterialDocument;
import de.hhn.abmi.dto.MaterialStock;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Access to everything the app needs from SAP.
 *
 * <p>Services only use this interface and never see how SAP is called.
 * Implementations throw {@link org.springframework.web.client.RestClientException}
 * if SAP is not reachable or returns an error.
 */
public interface SapClient {

    /**
     * Returns all stocks at a location.
     *
     * @param location plant and storage location to read
     * @return the stocks; empty if SAP has none there
     */
    List<MaterialStock> getStocks(SapLocation location);

    /**
     * Returns the stock of one material at a location.
     *
     * @param material material number
     * @param location plant and storage location to read
     * @return the stock, or empty if SAP does not know the material there
     */
    Optional<MaterialStock> getStock(String material, SapLocation location);

    /**
     * Books a consumption and charges it to the cost center of the location.
     *
     * @param material material number
     * @param quantity amount to book, greater than 0
     * @param note     header text with at most 25 characters, may be {@code null}
     * @param location plant, storage location and cost center to book on
     * @return the booked material document
     */
    MaterialDocument postConsumption(String material, BigDecimal quantity, String note, SapLocation location);

}
