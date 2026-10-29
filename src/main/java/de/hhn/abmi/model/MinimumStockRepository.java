package de.hhn.abmi.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MinimumStockRepository extends JpaRepository<MinimumStock, Long> {

    /**
     * Finds the minimum stock of a material on a station.
     *
     * <p>Spring Data derives the query from the method name.
     *
     * @param station  station code
     * @param material material number
     * @return the minimum stock, or empty if none is configured
     */
    Optional<MinimumStock> findByStationAndMaterial(String station, String material);
}
