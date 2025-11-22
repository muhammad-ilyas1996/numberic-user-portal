package com.numbericsuserportal.common.exceptions;

import com.numbericsuserportal.commonpersistence.dto.ExceptionResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ControllerAdvice
public class GlobalExceptionHandler {

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
