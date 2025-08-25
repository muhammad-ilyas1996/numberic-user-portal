package com.medicalbillinguserportal.common.exceptions;

import com.medicalbillinguserportal.commonpersistence.dto.ExceptionResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class InvalidDateFormatException extends RuntimeException{

    public InvalidDateFormatException(String message)
    {
        super(message);
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
}
