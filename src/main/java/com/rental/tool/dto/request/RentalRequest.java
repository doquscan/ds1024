package com.rental.tool.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.rental.tool.validation.FutureOrPresentDate;
import com.rental.tool.validation.ValidCheckoutDateFormat;
import jakarta.validation.constraints.*;
import com.rental.tool.validation.WholeNumber;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class RentalRequest {
    @NotNull(message = "Tool code cannot be null")
    private String toolCode;

    @Min(value = 1, message = "Rental days must be at least 1 and cannot be negative")
    private int rentalDays;

    @WholeNumber(message = "Discount percent must be a whole number")
    @DecimalMin(value = "0.0", inclusive = true, message = "Discount percent must be at least 0")
    @DecimalMax(value = "100.0", inclusive = true, message = "Discount percent cannot exceed 100")
    @NotNull(message = "Discount percent is required and must be a valid value between 0-100")
    private BigDecimal discountPercent;


    @NotNull(message = "Checkout date cannot be null")
    @FutureOrPresentDate(message = "Checkout date cannot be in the past")
    private LocalDate checkoutDate;
}

