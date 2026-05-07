package com.manthau.authservice.adapter.in.web;

import com.manthau.authservice.adapter.in.web.dto.MessageResponse;
import com.manthau.authservice.application.exception.ApplicationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    ResponseEntity<MessageResponse> handle(ApplicationException ex) {
        HttpStatus status = switch (ex.getErrorCode()) {
            case NOT_FOUND    -> HttpStatus.NOT_FOUND;
            case CONFLICT     -> HttpStatus.CONFLICT;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case BAD_REQUEST  -> HttpStatus.BAD_REQUEST;
        };
        return ResponseEntity.status(status).body(MessageResponse.of(ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<MessageResponse> handle(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(MessageResponse.of(message));
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<MessageResponse> handle(Exception ex) {
        return ResponseEntity.internalServerError().body(MessageResponse.of("Internal server error"));
    }
}
