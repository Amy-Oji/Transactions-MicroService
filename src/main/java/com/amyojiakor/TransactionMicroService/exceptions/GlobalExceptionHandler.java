package com.amyojiakor.TransactionMicroService.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ApiError> handleExceptions(Exception e) {
        ApiError error = new ApiError("Bad Request Exception:: " + e.getMessage());
        HttpStatus status = HttpStatus.BAD_REQUEST;
        return new ResponseEntity<>(error, status);
    }
}
