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

/**
 * REST endpoints for the medications of a station.
 *
 * <p>Only maps HTTP to service calls; the logic is in {@link MedicationService}.
 */
@RestController
@RequestMapping("/api/medications")
public class MedicationController {
    private final MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }

    /**
     * Returns all medications of a station with stock and status.
     *
     * @param station station code
     * @return one entry per medication
     */
    @GetMapping
    public List<MedicationResponse> getMeds(@RequestParam String station) {
        return medicationService.getMedications(station);
    }

    /**
     * Returns the stored minimum stock of a medication.
     *
     * @param station  station code
     * @param material material number
     * @return the minimum stock entry
     */
    @GetMapping("/{material}")
    public MinimumStock findMed(@RequestParam String station, @PathVariable String material) {
        return medicationService.getMedication(station, material);
    }

    /**
     * Books the consumption of a medication in SAP.
     *
     * @param station  station code
     * @param material material number
     * @param request  quantity and optional note
     * @return the booked document with the remaining stock
     */
    @PostMapping("/{material}/consume")
    public ConsumeResponse consume(@RequestParam String station, @PathVariable String material,
                                   @RequestBody ConsumeRequest request) {
        return medicationService.consume(station, material, request);
    }
}
