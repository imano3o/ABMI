package de.hhn.abmi.controller;

import de.hhn.abmi.dto.api.HospitalResponse;
import de.hhn.abmi.service.HospitalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Endpunkt für das Krankenhaus (Name für die Kopfzeile im Frontend). */
@RestController
@RequestMapping("/api/hospital")
public class HospitalController {
    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    /** GET /api/hospital */
    @GetMapping
    public HospitalResponse getHospital() {
        return hospitalService.getHospital();
    }
}
