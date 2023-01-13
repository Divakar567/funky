package com.diva.funky.api.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidRequestDataException extends BusinessException {

    public InvalidRequestDataException(String message) {
        super(message);
    }
}
