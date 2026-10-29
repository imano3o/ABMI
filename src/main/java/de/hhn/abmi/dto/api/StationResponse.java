package de.hhn.abmi.dto.api;

/** A station the user can choose. */
public record StationResponse(
        String code,
        String name
) {
}
