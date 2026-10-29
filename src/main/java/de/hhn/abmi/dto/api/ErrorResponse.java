package de.hhn.abmi.dto.api;

/** Same error format for all endpoints, so the frontend can always show the message. */
public record ErrorResponse(
        int status,
        String message
) {
}
