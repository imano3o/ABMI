package de.hhn.abmi.exception;

import de.hhn.abmi.dto.api.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

/**
 * Turns exceptions from all controllers into the error format from API.md:
 * { "status": 400, "message": "..." }
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException e) {
        return error(e.getStatus(), e.getMessage());
    }

    /** Request body is missing or not valid JSON. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableRequest(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "Invalid request (JSON missing or malformed).");
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParameter(MissingServletRequestParameterException e) {
        return error(HttpStatus.BAD_REQUEST, "Parameter '" + e.getParameterName() + "' is missing.");
    }

    /** SAP mock is not reachable or returned an error status. */
    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponse> handleSapError(RestClientException e) {
        return error(HttpStatus.BAD_GATEWAY, "SAP is not reachable or returned an error.");
    }

    /** For functions the SAP mock does not support yet. */
    @ExceptionHandler(UnsupportedOperationException.class)
    public ResponseEntity<ErrorResponse> handleNotSupported(UnsupportedOperationException e) {
        return error(HttpStatus.BAD_GATEWAY, "SAP does not support this function yet.");
    }

    private ResponseEntity<ErrorResponse> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErrorResponse(status.value(), message));
    }

}
