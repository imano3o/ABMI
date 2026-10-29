package de.hhn.abmi.exception;

import org.springframework.http.HttpStatus;

/**
 * Fachlicher Fehler mit HTTP-Status.
 * Die Nachricht wird dem Frontend direkt angezeigt (auf Englisch).
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }

    /** 400 – ungültige Menge, zu wenig Bestand */
    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    /** 404 – unbekannte Materialnummer */
    public static ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, message);
    }

}
