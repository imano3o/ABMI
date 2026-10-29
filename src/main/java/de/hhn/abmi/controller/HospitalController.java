package de.hhn.abmi.controller;

import de.hhn.abmi.dto.api.HospitalResponse;
import de.hhn.abmi.service.HospitalService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Provides the hospital name for the frontend header. */
@RestController
@RequestMapping("/api/hospital")
public class HospitalController {
    private final HospitalService hospitalService;

    public HospitalController(HospitalService hospitalService) {
        this.hospitalService = hospitalService;
    }

    @GetMapping
    public HospitalResponse getHospital() {
        return hospitalService.getHospital();
    }
}
