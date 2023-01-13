package com.diva.funky.api.exception;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ResourceLimitExceedException extends BusinessException {

    private List<String> exceededResources;

    public ResourceLimitExceedException(String message) {
        super(message);
    }

    public ResourceLimitExceedException(String message, List<String> exceededResources) {
        super(message);
        this.exceededResources = exceededResources;
    }
}
