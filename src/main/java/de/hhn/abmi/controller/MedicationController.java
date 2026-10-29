package de.hhn.abmi.controller;

import de.hhn.abmi.dto.api.ConsumeRequest;
import de.hhn.abmi.dto.api.ConsumeResponse;
import de.hhn.abmi.dto.api.MedicationResponse;
import de.hhn.abmi.model.MinimumStock;
import de.hhn.abmi.service.MedicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
public class MedicationController {
    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping
    public List<MedicationResponse> getMeds(@RequestParam String station) {
        return medicationService.getMedications(station);
    }

    @GetMapping("/{material}")
    public MinimumStock findMed(@RequestParam String station, @PathVariable String material) {
        return medicationService.getMedication(station, material);
    }

    @PostMapping("/{material}/consume")
    public ConsumeResponse consume(@RequestParam String station, @PathVariable String material,
                                   @RequestBody ConsumeRequest request) {
        return medicationService.consume(station, material, request);
    }
}
