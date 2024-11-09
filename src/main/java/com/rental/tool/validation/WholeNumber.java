package com.rental.tool.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = WholeNumberValidator.class)
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface WholeNumber {
    String message() default "Discount percent must be a whole number";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
