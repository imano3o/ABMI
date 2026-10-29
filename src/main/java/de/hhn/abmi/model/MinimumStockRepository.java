package de.hhn.abmi.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MinimumStockRepository extends JpaRepository<MinimumStock, Long> {

    /** Spring Data derives the query from the method name. */
    Optional<MinimumStock> findByStationAndMaterial(String station, String material);
}
