package de.hhn.abmi.dto.api;

/**
 * Hospital data for the frontend.
 *
 * @param name hospital name, or {@code null} if no hospital is configured
 */
public record HospitalResponse(
        String name
) {
}
