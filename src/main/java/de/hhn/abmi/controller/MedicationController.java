package de.hhn.abmi.controller;

import de.hhn.abmi.model.MinimumStock;
import de.hhn.abmi.service.MedicationService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/medications")
public class  MedicationController {
    private MedicationService medicationService;

    public MedicationController(MedicationService medicationService) {
        this.medicationService = medicationService;
    }
    
    @GetMapping
    public List<MinimumStock> getMeds(){
        return medicationService.getMedications();
    }

    @GetMapping("/{material}")
   public MinimumStock findMed(@PathVariable String material){
        return medicationService.getMedication(material);
    }
}
