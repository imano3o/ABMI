package de.hhn.abmi.service;

import de.hhn.abmi.dto.api.StationResponse;
import de.hhn.abmi.exception.ApiException;
import de.hhn.abmi.model.Station;
import de.hhn.abmi.model.StationRepository;
import de.hhn.abmi.sap.SapLocation;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Access to the stations and their SAP master data.
 */
@Service
public class StationService {
    private final StationRepository stationRepository;
    private final HospitalService hospitalService;

    public StationService(StationRepository stationRepository, HospitalService hospitalService) {
        this.stationRepository = stationRepository;
        this.hospitalService = hospitalService;
    }

    /**
     * Returns all stations sorted by code.
     *
     * @return all stations for the selection
     */
    public List<StationResponse> getStations() {
        return stationRepository.findAllByOrderByCode().stream()
                .map(station -> new StationResponse(station.getCode(), station.getName()))
                .toList();
    }

    /**
     * Returns the station with the given code.
     *
     * @param code station code
     * @return the station
     * @throws ApiException with status 404 if the station does not exist
     */
    public Station requireStation(String code) {
        return stationRepository.findById(code)
                .orElseThrow(() -> ApiException.notFound("Unknown station: " + code));
    }

    /**
     * Returns where SAP calls for a station read and book.
     *
     * @param code station code
     * @return plant, storage location and cost center
     * @throws ApiException with status 404 if the station does not exist
     */
    public SapLocation sapLocationOf(String code) {
        Station station = requireStation(code);
        return new SapLocation(hospitalService.requirePlant(), station.getCode(), station.getCostCenter());
    }
}
