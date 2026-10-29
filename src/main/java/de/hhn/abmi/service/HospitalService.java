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

    /** Für die Anzeige im Frontend; ohne Eintrag ist der Name null. */
    public HospitalResponse getHospital() {
        String name = findHospital()
                .map(Hospital::getName)
                .orElse(null);
        return new HospitalResponse(name);
    }

    /** SAP-Werk des Krankenhauses, z. B. "1710". */
    public String requirePlant() {
        return findHospital()
                .map(Hospital::getPlant)
                .orElseThrow(() -> new IllegalStateException("No hospital in table 'hospital' (see data.sql)."));
    }

    /** Es gibt genau ein Krankenhaus in der Tabelle. */
    private Optional<Hospital> findHospital() {
        return hospitalRepository.findAll().stream().findFirst();
    }
}
