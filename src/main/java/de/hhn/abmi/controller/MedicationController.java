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

/** Endpunkte für die Medikamente einer Station. Die Logik steckt im MedicationService. */
@RestController
@RequestMapping("/api/medications")
public class MedicationController {
    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    /** GET /api/medications?station=ST01 – alle Medikamente der Station mit Bestand und Status. */
    @GetMapping
    public List<MedicationResponse> getMeds(@RequestParam String station) {
        return medicationService.getMedications(station);
    }

    /** GET /api/medications/{material}?station=ST01 – Mindestbestand eines Medikaments aus H2. */
    @GetMapping("/{material}")
    public MinimumStock findMed(@RequestParam String station, @PathVariable String material) {
        return medicationService.getMedication(station, material);
    }

    /** POST /api/medications/{material}/consume?station=ST01 – Verbrauch in SAP buchen. */
    @PostMapping("/{material}/consume")
    public ConsumeResponse consume(@RequestParam String station, @PathVariable String material,
                                   @RequestBody ConsumeRequest request) {
        return medicationService.consume(station, material, request);
    }
}
