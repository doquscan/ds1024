package com.rental.tool.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class CheckoutDateFormatValidator implements ConstraintValidator<ValidCheckoutDateFormat, LocalDate> {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM/dd/yy");

    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        if (date == null) {
            return true; // Null is handled by @NotNull annotation
        }

        try {
            // Try formatting the date to ensure it matches the pattern
            String formattedDate = date.format(DATE_FORMATTER);
            LocalDate parsedDate = LocalDate.parse(formattedDate, DATE_FORMATTER);
            return date.equals(parsedDate);
        } catch (DateTimeParseException e) {
            return false;
        }
    }
}
