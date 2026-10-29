package de.hhn.abmi.dto.api;

/**
 * Error body returned by all endpoints.
 *
 * <p>One format everywhere, so the frontend can always show the message.
 *
 * @param status  HTTP status code
 * @param message text shown to the user
 */
public record ErrorResponse(
        int status,
        String message
) {
}
