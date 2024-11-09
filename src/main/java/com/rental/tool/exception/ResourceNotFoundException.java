package com.rental.tool.exception;

public class ResourceNotFoundException extends RuntimeException {

    // Default constructor
    public ResourceNotFoundException() {
        super();
    }

    // Constructor that accepts a custom message
    public ResourceNotFoundException(String message) {
        super(message);
    }

    // Constructor that accepts a custom message and a cause
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor that accepts a cause
    public ResourceNotFoundException(Throwable cause) {
        super(cause);
    }
}
