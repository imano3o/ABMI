package de.hhn.abmi.service;


import de.hhn.abmi.model.MinimumStock;
import de.hhn.abmi.model.MinimumStockRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicationService {
    private final MinimumStockRepository minimumStockRepository;


    public MedicationService(MinimumStockRepository minimumStockRepository){
        this.minimumStockRepository = minimumStockRepository;
    }

    public List<MinimumStock> getMedications(){
        return minimumStockRepository.findAll();
    }

    public MinimumStock getMedication(String material) {
        return minimumStockRepository.findByMaterial(material).orElseThrow();
    }
}

