package de.hhn.abmi.service;

import de.hhn.abmi.dto.api.HospitalResponse;
import de.hhn.abmi.model.Hospital;
import de.hhn.abmi.model.HospitalRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class HospitalService {
    private final HospitalRepository hospitalRepository;

    public HospitalService(HospitalRepository hospitalRepository) {
        this.hospitalRepository = hospitalRepository;
    }

    /** name is null if the hospital table is empty. */
    public HospitalResponse getHospital() {
        String name = findHospital()
                .map(Hospital::getName)
                .orElse(null);
        return new HospitalResponse(name);
    }

    public String requirePlant() {
        return findHospital()
                .map(Hospital::getPlant)
                .orElseThrow(() -> new IllegalStateException("No hospital in table 'hospital' (see data.sql)."));
    }

    /** The table holds exactly one hospital. */
    private Optional<Hospital> findHospital() {
        return hospitalRepository.findAll().stream().findFirst();
    }
}
