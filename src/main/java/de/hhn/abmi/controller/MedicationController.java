package de.hhn.abmi.controller;

import de.hhn.abmi.dto.api.*;
import de.hhn.abmi.model.MinimumStock;
import de.hhn.abmi.service.MedicationService;
import de.hhn.abmi.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
public class MedicationController {
    private final MedicationService medicationService;
    private final OrderService orderService;

    public MedicationController(MedicationService medicationService, OrderService orderService) {
        this.medicationService = medicationService;
        this.orderService = orderService;
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
    public ConsumeResponse consume(@PathVariable String material, @RequestBody ConsumeRequest request) {
        return medicationService.consume(material, request);
    }

    @PostMapping("/{material}/reorder")
    @ResponseStatus(HttpStatus.CREATED)
    public ReorderResponse reorder(@PathVariable String material, @RequestBody ReorderRequest request) {
        return orderService.reorder(material, request);

    }
}


