package com.numbericsuserportal.common.exceptions;

import com.numbericsuserportal.commonpersistence.dto.ExceptionResponseDto;
import com.numbericsuserportal.taxbandit.exception.TaxBanditsApiException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaxBanditsApiException.class)
    public ResponseEntity<?> handleTaxBanditsApiException(TaxBanditsApiException ex, WebRequest webRequest) {
        HttpStatus status = ex.getHttpStatus();
        Map<String, Object> body = Map.of(
            "success", false,
            "error", ex.getMessage(),
            "upstreamMessage", ex.getUpstreamMessage() != null ? ex.getUpstreamMessage() : "",
            "endpoint", ex.getEndpoint() != null ? ex.getEndpoint() : ""
        );
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ExceptionResponseDto> handleNoResourceFound(NoResourceFoundException ex, WebRequest webRequest) {
        ExceptionResponseDto dto = new ExceptionResponseDto(
                webRequest.getDescription(false),
                HttpStatus.NOT_FOUND,
                "Endpoint not found: " + ex.getResourcePath() + ". Check the URL path.",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(dto);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, String>> handleResponseStatus(ResponseStatusException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        String reason = ex.getReason() != null ? ex.getReason() : status.getReasonPhrase();
        return ResponseEntity.status(status).body(Map.of("error", reason));
    }

    @ExceptionHandler(InvalidDateFormatException.class)
    public ResponseEntity<ExceptionResponseDto> handleInvalidDateFormatException(InvalidDateFormatException ex, WebRequest webRequest)
    {
        ExceptionResponseDto exceptionResponseDto=new ExceptionResponseDto(
                webRequest.getDescription(false),
                HttpStatus.NOT_ACCEPTABLE,
                ex.getMessage(),
                LocalDateTime.now()

        );
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(exceptionResponseDto);
    }
    @ExceptionHandler(StartDateMustBeGreaterException.class)
    public ResponseEntity<ExceptionResponseDto> handleStartDateMustBeGreaterException(StartDateMustBeGreaterException ex, WebRequest webRequest)
    {
        ExceptionResponseDto exceptionResponseDto=new ExceptionResponseDto(
                webRequest.getDescription(false),
                HttpStatus.NOT_ACCEPTABLE,
                ex.getMessage(),
                LocalDateTime.now()

        );
        return ResponseEntity.status(HttpStatus.NOT_ACCEPTABLE).body(exceptionResponseDto);
    }
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ExceptionResponseDto> handleGlobalException(Exception ex, WebRequest webRequest)
    {
        ExceptionResponseDto exceptionResponseDto=new ExceptionResponseDto(
                webRequest.getDescription(false),
                HttpStatus.INTERNAL_SERVER_ERROR,
                ex.getMessage(),
                LocalDateTime.now()

        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(exceptionResponseDto);
    }
}
