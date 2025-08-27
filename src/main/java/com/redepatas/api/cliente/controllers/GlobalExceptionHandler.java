package com.redepatas.api.cliente.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.redepatas.api.cliente.dtos.ValidationErrorDTO;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorDTO> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        ValidationErrorDTO errorDTO = new ValidationErrorDTO("Erro de validação", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorDTO);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        if (ex.getCause() instanceof UnrecognizedPropertyException) {
            UnrecognizedPropertyException unrecognizedEx = (UnrecognizedPropertyException) ex.getCause();
            String fieldName = unrecognizedEx.getPropertyName();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Campo desconhecido: '" + fieldName + "'. Verifique se o nome do campo está correto.");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Erro ao processar JSON: " + ex.getMessage());
    }

    @ExceptionHandler(JsonMappingException.class)
    public ResponseEntity<String> handleJsonMappingException(JsonMappingException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Erro de mapeamento JSON: " + ex.getMessage());
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<String> handleResponseStatusException(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(ex.getReason());
    }
}
