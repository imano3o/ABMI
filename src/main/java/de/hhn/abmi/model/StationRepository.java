package de.hhn.abmi.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StationRepository extends JpaRepository<Station, String> {

    /**
     * Returns all stations sorted by code.
     *
     * <p>Spring Data derives the query from the method name.
     *
     * @return all stations
     */
    List<Station> findAllByOrderByCode();
}
