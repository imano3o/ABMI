package de.hhn.abmi.dto.api;

/**
 * Station the user can choose.
 *
 * @param code station code, also the SAP storage location
 * @param name display name
 */
public record StationResponse(
        String code,
        String name
) {
}
