package de.hhn.abmi.service;

import de.hhn.abmi.dto.api.HospitalResponse;
import de.hhn.abmi.model.Hospital;
import de.hhn.abmi.model.HospitalRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Access to the hospital master data.
 */
@Service
public class HospitalService {
    private final HospitalRepository hospitalRepository;

    public HospitalService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

    /**
     * Returns the hospital for the frontend.
     *
     * @return the hospital; its name is {@code null} if none is configured
     */
    public HospitalResponse getHospital() {
        String name = findHospital()
                .map(Hospital::getName)
                .orElse(null);
        // Entity -> response DTO; the SAP plant stays in the backend
        return new HospitalResponse(name);
    }

    /**
     * Returns the SAP plant of the hospital.
     *
     * @return the plant
     * @throws IllegalStateException if no hospital is configured, because no SAP call works without a plant
     */
    public String requirePlant() {
        return findHospital()
                .map(Hospital::getPlant)
                .orElseThrow(() -> new IllegalStateException("No hospital in table 'hospital' (see data.sql)."));
    }

    /** Returns the only hospital; the table holds exactly one row. */
    private Optional<Hospital> findHospital() {
        return hospitalRepository.findAll().stream().findFirst();
    }
}
