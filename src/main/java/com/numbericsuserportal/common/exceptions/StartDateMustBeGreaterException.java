package com.numbericsuserportal.common.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_ACCEPTABLE)
public class StartDateMustBeGreaterException extends RuntimeException{
    public StartDateMustBeGreaterException(String message)
    {
        super(message);
    }
}