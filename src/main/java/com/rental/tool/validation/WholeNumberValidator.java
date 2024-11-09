package com.rental.tool.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.math.BigDecimal;

public class WholeNumberValidator implements ConstraintValidator<WholeNumber, BigDecimal> {

    @Override
    public boolean isValid(BigDecimal value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Other validations will handle null checks
        }
        return value.stripTrailingZeros().scale() <= 0;
    }
}
