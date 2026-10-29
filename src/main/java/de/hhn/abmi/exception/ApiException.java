package de.hhn.abmi.exception;

import org.springframework.http.HttpStatus;

/**
 * Business error that is returned to the client with a specific HTTP status.
 *
 * <p>The message is shown to the user as it is, so it must be readable.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() { return status; }

    /**
     * Creates an error with status 400 Bad Request.
     *
     * @param message text shown to the user
     * @return the new exception
     */
    public static ApiException badRequest(String message) {
        return new ApiException(HttpStatus.BAD_REQUEST, message);
    }

    /**
     * Creates an error with status 404 Not Found.
     *
     * @param message text shown to the user
     * @return the new exception
     */
    public static ApiException notFound(String message) {
        return new ApiException(HttpStatus.NOT_FOUND, message);
    }

}
