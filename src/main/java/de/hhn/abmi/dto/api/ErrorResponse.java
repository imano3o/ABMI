package de.hhn.abmi.dto.api;

/** Einheitliches Fehlerformat aller Endpunkte (siehe API.md Abschnitt 4). */
public record ErrorResponse(
        int status,
        String message
) {
}
