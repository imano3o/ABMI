package de.hhn.abmi.controller;

import de.hhn.abmi.dto.api.ConsumeRequest;
import de.hhn.abmi.dto.api.ConsumeResponse;
import de.hhn.abmi.dto.api.MedicationResponse;
import de.hhn.abmi.model.MinimumStock;
import de.hhn.abmi.service.MedicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
public class MedicationController {
    private MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    @GetMapping
    public List<MedicationResponse> getMeds() {
        return medicationService.getMedications();
    }

    @GetMapping("/{material}")
    public MinimumStock findMed(@PathVariable String material) {
        return medicationService.getMedication(material);
    }

    @PostMapping("/{material}/consume")
    public ConsumeResponse consume(@PathVariable String material, @RequestBody ConsumeRequest re) {
        return medicationService.consume(material, re);
    }

}


