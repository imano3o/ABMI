package de.hhn.abmi.service;

import de.hhn.abmi.dto.api.StationResponse;
import de.hhn.abmi.exception.ApiException;
import de.hhn.abmi.model.Station;
import de.hhn.abmi.model.StationRepository;
import de.hhn.abmi.sap.SapLocation;
import org.springframework.stereotype.Service;

import java.util.List;

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

    /** Prüft, ob es die Station gibt; sonst 404. */
    public Station requireStation(String code) {
        return stationRepository.findById(code)
                .orElseThrow(() -> ApiException.notFound("Unknown station: " + code));
    }

    /** Wo in SAP für diese Station gelesen und gebucht wird (Werk, Lagerort, Kostenstelle). */
    public SapLocation sapLocationOf(String code) {
        Station station = requireStation(code);
        return new SapLocation(hospitalService.requirePlant(), station.getCode(), station.getCostCenter());
    }
}
