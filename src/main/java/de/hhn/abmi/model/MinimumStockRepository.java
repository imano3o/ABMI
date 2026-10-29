package de.hhn.abmi.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MinimumStockRepository extends JpaRepository<MinimumStock, Long> {

    /** Spring Data baut die Abfrage aus dem Methodennamen: WHERE station = ? AND material = ? */
    Optional<MinimumStock> findByStationAndMaterial(String station, String material);
}
