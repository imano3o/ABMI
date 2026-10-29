package de.hhn.abmi.controller;

import de.hhn.abmi.dto.api.HospitalResponse;
import de.hhn.abmi.service.HospitalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST endpoint for the hospital shown in the frontend header.
 */
@RestController
@RequestMapping("/api/hospital")
public class HospitalController {
    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    /**
     * Returns the hospital.
     *
     * @return the hospital; its name is {@code null} if none is configured
     */
    @GetMapping
    public HospitalResponse getHospital() {
        return hospitalService.getHospital();
    }
}
