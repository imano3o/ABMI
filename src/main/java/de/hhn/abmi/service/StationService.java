package de.hhn.abmi.service;

import de.hhn.abmi.dto.api.StationResponse;
import de.hhn.abmi.exception.ApiException;
import de.hhn.abmi.model.Station;
import de.hhn.abmi.model.StationRepository;
import de.hhn.abmi.sap.SapLocation;
import org.springframework.stereotype.Service;

import java.util.List;

/** Reads the stations and their SAP master data from the database. */
@Service
public class StationService {
    private final StationRepository stationRepository;
    private final HospitalService hospitalService;

    public StationService(StationRepository stationRepository, HospitalService hospitalService) {
        this.stationRepository = stationRepository;
        this.hospitalService = hospitalService;
    }

    public List<StationResponse> getStations() {
        return stationRepository.findAllByOrderByCode().stream()
                .map(station -> new StationResponse(station.getCode(), station.getName()))
                .toList();
    }

    public Station requireStation(String code) {
        return stationRepository.findById(code)
                .orElseThrow(() -> ApiException.notFound("Unknown station: " + code));
    }

    /** Where SAP calls for this station read and book. */
    public SapLocation sapLocationOf(String code) {
        Station station = requireStation(code);
        return new SapLocation(hospitalService.requirePlant(), station.getCode(), station.getCostCenter());
    }
}
