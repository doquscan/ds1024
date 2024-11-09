package com.rental.tool.dto.response;

import lombok.Data;

import java.util.Map;

@Data
public class RentalResponse<T> {
    private int status;
    private String message;
    private T data;
    private Map<String, String> errors;

    // Constructor for success response
    public RentalResponse(int status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Constructor for error response
    public RentalResponse(int status, String message, Map<String, String> errors) {
        this.status = status;
        this.message = message;
        this.errors = errors;
    }

}

