package de.hhn.abmi.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MinimumStockRepository extends JpaRepository<MinimumStock, Long> {

    Optional<MinimumStock> findByMaterial(String material);
}