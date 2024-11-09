package com.rental.tool.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Table(name = "rental")
@Entity
public class Rental {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long rental_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tool_code", nullable = false)
    private Tool tool;

    @Column(name = "rental_days", nullable = false)
    @Min(value = 1, message = "Rental days must be at least 1")
    private int rentalDays;

    @Column(name = "checkout_date", nullable = false)
    @NotNull(message = "Checkout date cannot be null")
    private LocalDate checkoutDate;

    @Column(name = "discount_percent", nullable = false,precision = 10, scale = 2)
    @DecimalMin(value = "0.0", inclusive = true, message = "Discount percent cannot be negative")
    @DecimalMax(value = "100.0", inclusive = true, message = "Discount percent cannot exceed 100")
    private BigDecimal discountPercent;

    @Column(name = "pre_discount_charge",precision = 10, scale = 2)
    @DecimalMin(value = "0.0", inclusive = true, message = "Pre-discount charge cannot be negative")
    private BigDecimal preDiscountCharge;

    @Column(name = "discount_amount",precision = 10, scale = 2)
    @DecimalMin(value = "0.0", inclusive = true, message = "Discount amount cannot be negative")
    private BigDecimal discountAmount;

    @Column(name = "final_charge",precision = 10, scale = 2)
    @DecimalMin(value = "0.0", inclusive = true, message = "Final charge cannot be negative")
    private BigDecimal finalCharge;

    @Column(name = "due_date")
    private LocalDate dueDate;
}

