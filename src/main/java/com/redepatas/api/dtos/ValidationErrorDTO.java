package com.redepatas.api.dtos;

import java.util.Map;

public class ValidationErrorDTO {
    private String message;
    private Map<String, String> fieldErrors;

    public ValidationErrorDTO(String message, Map<String, String> fieldErrors) {
        this.message = message;
        this.fieldErrors = fieldErrors;
    }

    public String getMessage() {
        return message;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}