package de.hhn.abmi.model;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StationRepository extends JpaRepository<Station, String> {

    List<Station> findAllByOrderByCode();
}
