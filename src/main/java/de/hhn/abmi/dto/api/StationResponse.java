package de.hhn.abmi.dto.api;

/** Eine Station für die Auswahl im Frontend. */
public record StationResponse(
        String code,
        String name
) {
}
