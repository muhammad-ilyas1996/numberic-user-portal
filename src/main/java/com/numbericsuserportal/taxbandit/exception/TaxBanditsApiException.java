package com.numbericsuserportal.taxbandit.exception;

import org.springframework.http.HttpStatus;

/**
 * Exception thrown when TaxBandits API returns an error.
 * Preserves upstream status code and error details for proper HTTP response mapping.
 */
public class TaxBanditsApiException extends RuntimeException {

    private final HttpStatus httpStatus;
    private final String upstreamMessage;
    private final String endpoint;

    public TaxBanditsApiException(String message, HttpStatus httpStatus, String upstreamMessage, String endpoint) {
        super(message);
        this.httpStatus = httpStatus != null ? httpStatus : HttpStatus.BAD_GATEWAY;
        this.upstreamMessage = upstreamMessage;
        this.endpoint = endpoint;
    }

    public TaxBanditsApiException(String message, HttpStatus httpStatus) {
        this(message, httpStatus, null, null);
    }

    public TaxBanditsApiException(String message, Throwable cause) {
        super(message, cause);
        this.httpStatus = HttpStatus.BAD_GATEWAY;
        this.upstreamMessage = cause != null ? cause.getMessage() : null;
        this.endpoint = null;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getUpstreamMessage() {
        return upstreamMessage;
    }

    public String getEndpoint() {
        return endpoint;
    }
}
