package com.rental.tool.controllers;

import com.rental.tool.dto.ErrorResponse;
import com.rental.tool.dto.response.RentalResponse;
import com.rental.tool.exception.ResourceNotFoundException;
import com.rental.tool.exception.ToolNotFoundException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle IllegalArgumentException exceptions and return a structured error response.
     *
     * @param ex - the exception thrown
     * @param request - web request details
     * @return ResponseEntity with a custom error response and 400 Bad Request status
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        // Create an error response object with the message from the exception
        ErrorResponse errorResponse =  new ErrorResponse(HttpStatus.BAD_REQUEST.value(), ex.getMessage());

        // Return a ResponseEntity with the custom error response and HTTP status 400 (Bad Request)
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseBody
    public ResponseEntity<RentalResponse<Object>> handleResourceNotFoundException(ResourceNotFoundException ex) {
        RentalResponse<Object> errorResponse = new RentalResponse<>(
                HttpStatus.NOT_FOUND.value(),
                ex.getMessage(),
                null // No data, just the error message
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    @ExceptionHandler(ToolNotFoundException.class)
    @ResponseBody
    public ResponseEntity<String> handleToolNotFound(ToolNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<String> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        String message = "Invalid input: One or more numeric values exceed the allowable range.";

        // Check if the exception contains information about an out-of-range int value
        if (ex.getMessage().contains("Numeric value") && ex.getMessage().contains("out of range of int")) {
            message = "Input error: A numeric field in your request exceeds the allowable range for an integer (-2147483648 to 2147483647).";
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(message);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<RentalResponse<Object>> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        FieldError::getDefaultMessage
                ));

        RentalResponse<Object> errorResponse = new RentalResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed for the request",
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseBody
    public ResponseEntity<RentalResponse<Object>> handleConstraintViolationException(ConstraintViolationException ex) {
        Map<String, String> errors = ex.getConstraintViolations().stream()
                .collect(Collectors.toMap(
                        violation -> violation.getPropertyPath().toString(),
                        ConstraintViolation::getMessage
                ));

        RentalResponse<Object> errorResponse = new RentalResponse<>(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed for the request",
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

}

