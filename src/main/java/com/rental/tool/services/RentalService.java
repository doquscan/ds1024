package com.rental.tool.services;

import com.rental.tool.dto.RentalAgreementDTO;
import com.rental.tool.entities.Rental;
import com.rental.tool.entities.Tool;
import com.rental.tool.entities.ToolCharge;
import com.rental.tool.exception.ToolNotFoundException;
import com.rental.tool.mapper.RentalMapper;
import com.rental.tool.repository.RentalRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.persistence.EntityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static com.rental.tool.utility.HolidayUtils.isHoliday;


@Service
public class RentalService {
    private static final Logger logger = LoggerFactory.getLogger(RentalService.class);

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private ToolService toolService;

    @Autowired
    private RentalRepository rentalRepository;

    @Autowired
    private RentalMapper rentalMapper;

    @Autowired
    private AuditService auditService;

    /**
     * Creates a rental entry for a given tool, calculating charges and applying discounts.
     *
     * @param toolCode       the code of the tool to be rented
     * @param rentalDays     the number of days the tool will be rented
     * @param discountPercent the discount percent applied to the rental
     * @param checkoutDate   the date of checkout for the rental
     * @return the created Rental entity
     */
    @CircuitBreaker(name = "rentalService", fallbackMethod = "createRentalFallback")
    @Transactional
    public Rental createRental(String toolCode, int rentalDays, BigDecimal discountPercent, LocalDate checkoutDate) {
        validateInput(toolCode, rentalDays, discountPercent, checkoutDate);


        Tool tool = toolService.getToolFromCache(toolCode);
        ToolCharge toolCharge = tool.getToolCharge();
        if (toolCharge == null) {
            throw new ToolNotFoundException("Tool charge information is missing for tool with code: " + toolCode);
        }
        Rental rental = new Rental();
        rental.setTool(tool);
        rental.setRentalDays(rentalDays);
        rental.setCheckoutDate(checkoutDate);
        rental.setDiscountPercent(discountPercent);

        LocalDate dueDate = checkoutDate.plusDays(rentalDays);
        rental.setDueDate(dueDate);

        int chargeDays = calculateChargeDays(toolCharge, checkoutDate, dueDate);
        // Check for possible null values in Tool object
        BigDecimal dailyRentalCharge = toolCharge.getDailyRentalCharge();
        if (dailyRentalCharge == null) {
            throw new IllegalStateException("Daily rental charge cannot be null for the tool");
        }

        BigDecimal preDiscountCharge = dailyRentalCharge.multiply(BigDecimal.valueOf(chargeDays));
        logger.info("preDiscountCharge: {}", preDiscountCharge);

        rental.setPreDiscountCharge(preDiscountCharge);
        BigDecimal discountAmount = preDiscountCharge
                                                    .multiply(discountPercent)
                                                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        logger.info("discountAmount: {}", discountAmount);
        rental.setDiscountAmount(discountAmount);

        BigDecimal finalCharge = preDiscountCharge.subtract(discountAmount);
        logger.info("finalCharge: {}", finalCharge);
        rental.setFinalCharge(finalCharge);
        // Save the rental and handle possible null rental repository
        if (rentalRepository == null) {
            throw new IllegalStateException("Rental repository is not initialized");
        }
        logger.info("Creating rental for tool code: {}, rental days: {}, discount percent: {}", toolCode, rentalDays, discountPercent);
        logger.info("Saving rental with pre-discount charge: {}, discount amount: {}, final charge: {}", preDiscountCharge, discountAmount, finalCharge);
        // Save rental to the repository
        // Save the rental using EntityManager to force flush and detect issues
//        entityManager.persist(rental);
//        entityManager.flush();
//        logger.debug("Rental object after saving: {}", rental);

        //return rental;
        Rental savedRental = rentalRepository.save(rental);
        logger.info("Rental saved with ID: {}", savedRental.getRental_id());
        return savedRental;
    }
    /**
     * Fallback method for createRental, invoked if the circuit breaker is triggered
     * due to a failure in accessing the main services.
     *
     * @param toolCode        the code of the tool to be rented
     * @param rentalDays      the number of days the tool will be rented
     * @param discountPercent the discount percent applied to the rental
     * @param checkoutDate    the date of checkout for the rental
     * @param ex              the exception that caused the fallback
     * @return a Rental object with default values to indicate fallback state
     */
    @Transactional
    public Rental createRentalFallback(String toolCode, int rentalDays, BigDecimal discountPercent, LocalDate checkoutDate, Throwable ex) {
        logger.error("Circuit breaker triggered for createRental with toolCode {}, rentalDays {}, due to exception: {}",
                toolCode, rentalDays, ex.getMessage());
        logger.error("Fallback: Circuit breaker triggered for createRental due to: {}", ex.getMessage());

        // Construct a fallback Rental object
        Rental fallbackRental = new Rental();

        // Set known values for the fallback object
        fallbackRental.setTool(null); // Tool cannot be fetched, so set to null
        fallbackRental.setRentalDays(rentalDays);
        fallbackRental.setCheckoutDate(checkoutDate);
        fallbackRental.setDiscountPercent(discountPercent);

        // Set charges to zero as the actual tool data couldn't be retrieved
        fallbackRental.setPreDiscountCharge(BigDecimal.ZERO);
        fallbackRental.setDiscountAmount(BigDecimal.ZERO);
        fallbackRental.setFinalCharge(BigDecimal.ZERO);

        // Mark due date based on checkout date + rental days
        LocalDate fallbackDueDate = checkoutDate.plusDays(rentalDays);
        fallbackRental.setDueDate(fallbackDueDate);

        // Log the return of fallback rental object
        logger.warn("Rental creation fallback invoked for toolCode {}. Returning default Rental object with zero charges.", toolCode);


        return fallbackRental;
    }

    /**
     * Processes the rental agreement, performs mapping, logging, and audit.
     *
     * @param rental the Rental entity to process
     * @param toolCode the tool code for logging purposes
     * @param checkoutDate the checkout date for logging purposes
     * @param transactionId the generated transaction ID for audit logging
     * @param username the username of the current user
     * @return the mapped RentalAgreementDTO
     */
    public RentalAgreementDTO processRentalAgreement(Rental rental, String toolCode, LocalDate checkoutDate, String transactionId, String username) {
        logger.info("processRentalAgreement Executed: {} ",rental.toString());
        logger.info("processRentalAgreement Executed: ");
        // Fetch tool from the toolService based on toolCode
        Tool tool = toolService.getToolFromCache(toolCode);
        // Check if the tool exists
        if (tool == null) {
            throw new ToolNotFoundException("Tool not found for code: " + toolCode);
        }
        // Set the fetched tool to the rental entity
        rental.setTool(tool);

        logger.info("Before mapping - Pre-discount charge: {}, Discount amount: {}, Final charge: {}",
                rental.getPreDiscountCharge(), rental.getDiscountAmount(), rental.getFinalCharge());

        // Use the RentalMapper to map the Rental entity to RentalAgreementDTO
        RentalAgreementDTO rentalAgreement = rentalMapper.toRentalAgreementDTO(rental);

        // Print the rental agreement to the console
        rentalAgreement.printAgreement();

        // Log the transaction
        logger.info("Audit Log: checkoutTool requested for toolCode: {}, checkoutDate: {}", toolCode, checkoutDate);

        // Log successful audit
        auditService.logAudit(transactionId, username, true, "Transaction successful for toolCode: " + toolCode);

        return rentalAgreement;
    }

    private void validateInput(String toolCode, int rentalDays, BigDecimal discountPercent, LocalDate checkoutDate) {
        if (toolCode == null || toolCode.trim().isEmpty()) {
            throw new ToolNotFoundException("Tool code cannot be null or empty");
        }
        if (checkoutDate == null) {
            throw new ToolNotFoundException("Checkout date cannot be null");
        }
        if (discountPercent == null) {
            throw new ToolNotFoundException("Discount percent cannot be null");
        }
        if (rentalDays < 1) {
            throw new ToolNotFoundException("Rental days must be 1 or greater");
        }
        if (discountPercent.compareTo(BigDecimal.ZERO) < 0 || discountPercent.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new ToolNotFoundException("Discount percent must be between 0 and 100");
        }
    }
    private int calculateChargeDays(ToolCharge toolCharge, LocalDate checkoutDate, LocalDate dueDate) {
        int chargeableDays = 0;
        for (LocalDate date = checkoutDate.plusDays(1); !date.isAfter(dueDate); date = date.plusDays(1)) {
            if (isChargeableDay(toolCharge, date)) {
                chargeableDays++;
            }
        }
        return chargeableDays;
    }

    private boolean isChargeableDay(ToolCharge toolCharge, LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        boolean isWeekend = (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY);
        boolean isHoliday = isHoliday(date);

        if (isHoliday && !toolCharge.isHolidayCharge()) {
            return false;
        }
        if (isWeekend && !toolCharge.isWeekendCharge()) {
            return false;
        }
        return toolCharge.isWeekdayCharge();
    }
}