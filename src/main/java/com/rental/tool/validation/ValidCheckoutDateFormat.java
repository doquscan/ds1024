package com.rental.tool.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CheckoutDateFormatValidator.class)
public @interface ValidCheckoutDateFormat {
    String message() default "Checkout date must be in the format MM/dd/yy";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

