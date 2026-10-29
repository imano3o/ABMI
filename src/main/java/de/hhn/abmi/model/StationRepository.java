package de.hhn.abmi.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StationRepository extends JpaRepository<Station, String> {

    /** Alle Stationen, sortiert nach code (Spring Data leitet die Abfrage aus dem Namen ab). */
    List<Station> findAllByOrderByCode();
}
