package com.rental.tool.dto;

import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Data
public class RentalAgreementDTO {
    private static final Logger logger = LoggerFactory.getLogger(RentalAgreementDTO.class);

    private String toolCode;
    private String toolType;
    private String toolBrand;
    private int rentalDays;
    private LocalDate checkoutDate;
    private LocalDate dueDate;
    private BigDecimal dailyRentalCharge;
    private boolean weekdayCharge;
    private boolean weekendCharge;
    private boolean holidayCharge;
    private long chargeDays;
    private BigDecimal preDiscountCharge;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal finalCharge;

    private String errorMessage;

    // Default constructor
    public RentalAgreementDTO() {}

    // Constructor for errors
    public RentalAgreementDTO(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    /**
     * /*
     * This method prints the details of the rental agreement in a human-readable format.
     * It formats the dates using the DateTimeFormatter class to display the format MM/dd/yy.
     * It uses the formatCurrency() method to print the currency values like $9,999.99.
     * It uses the formatPercent() method to format the discount percentage like 99%.
     * formatCurrency(BigDecimal value):
     *
     * This helper method takes a BigDecimal and returns a string formatted as US currency ($9,999.99).
     * It uses String.format() with locale US to format the number with commas as thousand separators and two decimal places for cents.
     * formatPercent(BigDecimal value):
     *
     * This helper method formats the discount percentage. It takes the BigDecimal value and converts it to a percentage string like 99%.
     * Example Output:
     * Let’s assume we have the following rental details:
     *
     *
     * Tool Code: LADW
     * Tool Type: Ladder
     * Brand: Werner
     * Rental Days: 5
     * Checkout Date: 2024-07-01
     * Due Date: 2024-07-06
     * Daily Rental Charge: $1.99
     * Charge Days: 4
     * Pre-Discount Charge: $7.96
     * Discount Percent: 10%
     * Discount Amount: $0.80
     * Final Charge: $7.16
     * If you call the printAgreement() method on the RentalAgreementDTO with the above values, the console output would be:
     *
     */
    public void printAgreement() {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yy");
        logger.info("Values before formatting for Checkout date: {}, Daily rental charge:: {}," +
                " Pre-discount charge: {} ,Discount amount: {}, Final charge: {}", checkoutDate, dailyRentalCharge,
                preDiscountCharge,discountAmount,finalCharge);

        System.out.println("Tool code: " + toolCode);
        System.out.println("Tool type: " + toolType);
        System.out.println("Brand: " + toolBrand);
        System.out.println("Rental days: " + rentalDays);
        System.out.println("Checkout date: " + checkoutDate.format(dateFormatter));
        System.out.println("Due date: " + dueDate.format(dateFormatter));
        System.out.println("Daily rental charge: " + formatCurrency(dailyRentalCharge));
        System.out.println("Weekday charge applicable: " + weekdayCharge);
        System.out.println("Weekend charge applicable: " + weekendCharge);
        System.out.println("Holiday charge applicable: " + holidayCharge);
        System.out.println("Charge days: " + chargeDays);
        System.out.println("Pre-discount charge: " + formatCurrency(preDiscountCharge));
        System.out.println("Discount percent: " + formatPercent(discountPercent));
        System.out.println("Discount amount: " + formatCurrency(discountAmount));
        System.out.println("Final charge: " + formatCurrency(finalCharge));
    }

    /**
     * Format a BigDecimal value as currency: $9,999.99
     *
     * @param value The BigDecimal value to format
     * @return A formatted string representing the currency
     */
    private String formatCurrency(BigDecimal value) {
        if (value == null) {
            return "$0.00"; // Fallback if the value is null
        }
        return String.format(Locale.US, "$%,.2f", value);
    }

    /**
     * Format a BigDecimal value as a percentage: 99%
     *
     * @param value The BigDecimal value to format
     * @return A formatted string representing the percentage
     */
    private String formatPercent(BigDecimal value) {
        return String.format(Locale.US, "%.0f%%", value);
    }
    // Print the agreement as text
    @Override
    public String toString() {
        if (errorMessage != null) {
            return "Error: " + errorMessage;
        }
        return String.format("Tool code: %s\nTool type: %s\nBrand: %s\nRental days: %d\nCheckout date: %s\nDue date: %s\nDaily rental charge: $%,.2f\nWeekday charge applicable: %s\nWeekend charge applicable: %s\nHoliday charge applicable: %s\nCharge days: %d\nPre-discount charge: $%,.2f\nDiscount percent: %.0f%%\nDiscount amount: $%,.2f\nFinal charge: $%,.2f",
                toolCode, toolType, toolBrand, rentalDays, checkoutDate.format(DateTimeFormatter.ofPattern("MM/dd/yy")),
                dueDate.format(DateTimeFormatter.ofPattern("MM/dd/yy")), dailyRentalCharge, weekdayCharge, weekendCharge,
                holidayCharge, chargeDays, preDiscountCharge, discountPercent, discountAmount, finalCharge);
    }
}

