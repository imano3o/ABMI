package de.hhn.abmi.dto.api;

/** Error format shared by all endpoints (see API.md, section 4). */
public record ErrorResponse(
        int status,
        String message
) {
}
