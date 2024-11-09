package com.rental.tool.validation;

import java.util.Map;

public class ErrorResponseSignature {
    private int status;
    private String message;
    private Map<String, String> errors;

    public ErrorResponseSignature(int status, String message, Map<String, String> errors) {
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

    // Getters and setters
}
