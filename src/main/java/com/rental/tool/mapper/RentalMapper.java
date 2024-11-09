package com.rental.tool.mapper;

import com.rental.tool.dto.RentalAgreementDTO;
import com.rental.tool.entities.Rental;
import com.rental.tool.entities.ToolCharge;
import com.rental.tool.exception.ToolNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import com.rental.tool.entities.Tool;

@Component
public class RentalMapper {
    private static final Logger logger = LoggerFactory.getLogger(RentalMapper.class);

    /**
     * Maps a Rental entity to RentalAgreementDTO.
     *
     * @param rental - Rental entity to map
     * @return RentalAgreementDTO containing rental agreement details
     */
    public RentalAgreementDTO toRentalAgreementDTO(Rental rental) {
        logger.info("Date of checkout : {}", rental.getCheckoutDate());
        logger.info("getDiscountAmount: {}", rental.getDiscountAmount());
        logger.info("getPreDiscountCharge: {}", rental.getPreDiscountCharge());
        logger.info("getFinalCharge: {}", rental.getFinalCharge());
        RentalAgreementDTO agreementDTO = new RentalAgreementDTO();
        Tool tool = rental.getTool();

        if (tool == null) {
            throw new ToolNotFoundException("Tool is missing for rental with ID: " + rental.getRental_id());
        }
        logger.info("Doca checkout : {}", rental.getCheckoutDate());
        ToolCharge toolCharge = tool.getToolCharge();
        if (toolCharge == null) {
            throw new ToolNotFoundException("Tool charge information is missing for tool with code: " + tool.getToolCode());
        }
        // Setting tool details
        agreementDTO.setToolCode(tool.getToolCode() != null ? tool.getToolCode() : "N/A");
        agreementDTO.setToolType(tool.getToolType() != null ? tool.getToolType() : "N/A");
        agreementDTO.setToolBrand(tool.getBrand() != null ? tool.getBrand() : "N/A");

        // Setting rental charge details
        agreementDTO.setDailyRentalCharge(toolCharge.getDailyRentalCharge() != null ? toolCharge.getDailyRentalCharge() : BigDecimal.valueOf(0.0));
        agreementDTO.setWeekdayCharge(toolCharge.isWeekdayCharge());
        agreementDTO.setWeekendCharge(toolCharge.isWeekendCharge());
        agreementDTO.setHolidayCharge(toolCharge.isHolidayCharge());

        agreementDTO.setRentalDays(rental.getRentalDays());
        agreementDTO.setCheckoutDate(rental.getCheckoutDate());
        agreementDTO.setDueDate(rental.getDueDate());
        agreementDTO.setChargeDays(calculateChargeDays(rental.getCheckoutDate(), rental.getDueDate()));
        agreementDTO.setPreDiscountCharge(rental.getPreDiscountCharge());
        agreementDTO.setDiscountPercent(rental.getDiscountPercent());
        agreementDTO.setDiscountAmount(rental.getDiscountAmount());
        agreementDTO.setFinalCharge(rental.getFinalCharge());

        return agreementDTO;
    }

    /**
     * Calculate the number of chargeable days between the checkout and due dates.
     *
     * @param checkoutDate - Checkout date
     * @param dueDate - Due date
     * @return Number of chargeable days
     */
    private long calculateChargeDays(LocalDate checkoutDate, LocalDate dueDate) {
        // You may need to customize this depending on the specific business rules (e.g., holidays, weekends)
        return ChronoUnit.DAYS.between(checkoutDate, dueDate);
    }
}

