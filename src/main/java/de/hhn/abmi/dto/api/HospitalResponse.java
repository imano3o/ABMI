package de.hhn.abmi.dto.api;

/** name is null if no hospital is configured. */
public record HospitalResponse(
        String name
) {
}
