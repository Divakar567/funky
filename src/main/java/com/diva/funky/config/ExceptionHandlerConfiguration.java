package com.diva.funky.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.diva.funky.api.exception.BusinessException;
import com.diva.funky.api.exception.InvalidRequestDataException;
import com.diva.funky.api.exception.ResourceAlreadyExistsException;
import com.diva.funky.api.exception.ResourceLimitExceedException;
import com.diva.funky.api.exception.ResourceNotFoundException;
import com.diva.funky.api.model.ErrorModel;

@RestControllerAdvice(basePackages = "com.diva.funky")
public class ExceptionHandlerConfiguration extends ResponseEntityExceptionHandler {

//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    protected ResponseEntity<Object> handleException(MethodArgumentNotValidException ex) {
//        ErrorModel response = ErrorModel.builder()
//                .error("INVALID_REQUEST_PAYLOAD")
//                .errorText(StringUtils.join(ex.getBindingResult().getAllErrors()
//                        .stream().map(DefaultMessageSourceResolvable::getDefaultMessage).toList(), ","))
//                .build();
//        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
//    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<Object> handleException(AccessDeniedException ex) {
        ErrorModel response = ErrorModel.builder().error("RESOURCE_ACCESS_DENIED").errorText(ex.getMessage()).build();
        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorModel> handleException(ResourceNotFoundException ex) {
        ErrorModel response = ErrorModel.builder().error("RESOURCE_NOT_FOUND").errorText(ex.getMessage()).build();
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ResourceAlreadyExistsException.class)
    public ResponseEntity<ErrorModel> handleException(ResourceAlreadyExistsException ex) {
        ErrorModel response = ErrorModel.builder().error("RESOURCE_ALREADY_EXISTS").errorText(ex.getMessage()).build();
        return new ResponseEntity<>(response, HttpStatus.CONFLICT);
    }

    @ExceptionHandler(InvalidRequestDataException.class)
    public ResponseEntity<ErrorModel> handleException(InvalidRequestDataException ex) {
        ErrorModel response = ErrorModel.builder().error("INVALID_REQUEST_PAYLOAD").errorText(ex.getMessage()).build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceLimitExceedException.class)
    public ResponseEntity<ErrorModel> handleException(ResourceLimitExceedException ex) {
        ErrorModel response = ErrorModel.builder().error(ex.getMessage())
                .errorText(ex.getMessage() + ": " + ex.getExceededResources().toString()).build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorModel> handleException(BusinessException ex) {
        ErrorModel response = ErrorModel.builder().error("INVALID_REQUEST").errorText(ex.getMessage()).build();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorModel> handleException(HttpClientErrorException ex) {
        ErrorModel response = ErrorModel.builder().error("FAILED").errorText(ex.getMessage()).build();
        return new ResponseEntity<>(response, ex.getStatusCode());
    }

    @ExceptionHandler(HttpServerErrorException.class)
    public ResponseEntity<ErrorModel> handleException(HttpServerErrorException ex) {
        ErrorModel response = ErrorModel.builder().error("FAILED").errorText(ex.getMessage()).build();
        return new ResponseEntity<>(response, ex.getStatusCode());
    }

}
