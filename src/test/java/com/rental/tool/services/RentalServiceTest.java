package com.rental.tool.services;

import com.rental.tool.entities.Rental;
import com.rental.tool.entities.Tool;
import com.rental.tool.entities.ToolCharge;
import com.rental.tool.exception.ToolNotFoundException;
import com.rental.tool.repository.RentalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;

import static com.rental.tool.utility.HolidayUtils.isHoliday;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RentalServiceTest {

    @Mock
    private RentalRepository rentalRepository;

    @InjectMocks
    private RentalService rentalService;

    @Mock
    private ToolService toolService;

    private Tool mockTool;
    private ToolCharge mockToolCharge;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        // Initialize Tool mock
        mockTool = new Tool();
        mockTool.setToolCode("JAKR");
        mockTool.setToolType("Jackhammer");
        mockTool.setBrand("Ridgid");

        // Initialize ToolCharge mock
        mockToolCharge = new ToolCharge();
        mockToolCharge.setDailyRentalCharge(BigDecimal.valueOf(2.99));
        mockToolCharge.setWeekdayCharge(true);
        mockToolCharge.setWeekendCharge(false);
        mockToolCharge.setHolidayCharge(false);

        // Set the ToolCharge in the Tool entity
        mockTool.setToolCharge(mockToolCharge);

        // Mock the toolService to return Tool and ToolCharge from cache
        when(toolService.getToolFromCache("JAKR")).thenReturn(mockTool);
        when(toolService.getToolChargeFromCache("JAKR")).thenReturn(mockToolCharge);
    }

    /**
     * Test 1: Invalid Discount (Greater than 100%) with weekend and holiday exclusion logic.
     * Verifies that an exception is thrown when the discount is set above 100% and that
     * the correct number of chargeable days are calculated for a valid discount percentage.
     */
    @Test
    public void testInvalidDiscountAbove100WithWeekendAndHolidayExclusions() {
        String toolCode = "JAKR";
        LocalDate checkoutDate = LocalDate.of(2015, 9, 3);
        int rentalDays = 5;
        BigDecimal discountPercent = new BigDecimal("101");

        // Assert that an exception is thrown for discount > 100
        BigDecimal finalDiscountPercent = discountPercent;
        ToolNotFoundException exception = assertThrows(ToolNotFoundException.class, () -> {
            rentalService.createRental(toolCode, rentalDays, finalDiscountPercent, checkoutDate);
        });
        assertEquals("Discount percent must be between 0 and 100", exception.getMessage());

        // Adjust discount percent to a valid range to test the holiday and weekend exclusion logic
        discountPercent = new BigDecimal("10"); // Valid discount

        // Define due date and calculate charge days excluding weekends and holidays
        LocalDate dueDate = checkoutDate.plusDays(rentalDays - 1);
        int chargeDays = 0;
        LocalDate currentDate = checkoutDate;

        while (!currentDate.isAfter(dueDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            // Check if the current day is a weekday (Monday-Friday)
            boolean isWeekday = dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;

            // Holiday conditions
            boolean isIndependenceDayObserved = (currentDate.getMonthValue() == 7 && currentDate.getDayOfMonth() == 3 && dayOfWeek == DayOfWeek.FRIDAY) ||
                    (currentDate.getMonthValue() == 7 && currentDate.getDayOfMonth() == 4 && isWeekday) ||
                    (currentDate.getMonthValue() == 7 && currentDate.getDayOfMonth() == 5 && dayOfWeek == DayOfWeek.MONDAY);

            boolean isLaborDay = currentDate.getMonthValue() == 9 && dayOfWeek == DayOfWeek.MONDAY && currentDate.getDayOfMonth() <= 7;

            // Only count chargeable days that are weekdays and not holidays
            if (isWeekday && !isIndependenceDayObserved && !isLaborDay) {
                chargeDays++;
            }
            currentDate = currentDate.plusDays(1);
        }

        // Calculate expected charges based on tool properties and charge days
        BigDecimal dailyRentalCharge = mockToolCharge.getDailyRentalCharge();
        BigDecimal preDiscountCharge = dailyRentalCharge.multiply(BigDecimal.valueOf(chargeDays));
        BigDecimal discountAmount = preDiscountCharge.multiply(discountPercent).divide(BigDecimal.valueOf(100));
        BigDecimal finalCharge = preDiscountCharge.subtract(discountAmount);

        // Setup expected Rental object and mock rentalRepository.save()
        Rental expectedRental = new Rental();
        expectedRental.setTool(mockTool);
        expectedRental.setRentalDays(rentalDays);
        expectedRental.setCheckoutDate(checkoutDate);
        expectedRental.setDiscountPercent(discountPercent);
        expectedRental.setDueDate(dueDate);
        expectedRental.setPreDiscountCharge(preDiscountCharge);
        expectedRental.setDiscountAmount(discountAmount);
        expectedRental.setFinalCharge(finalCharge);

        when(rentalRepository.save(any(Rental.class))).thenReturn(expectedRental);

        // Action
        Rental rental = rentalService.createRental(toolCode, rentalDays, discountPercent, checkoutDate);

        // Assert
        assertNotNull(rental, "Rental object should not be null");
        assertEquals(dueDate, rental.getDueDate());
        assertEquals(preDiscountCharge, rental.getPreDiscountCharge());
        assertEquals(discountAmount, rental.getDiscountAmount());
        assertEquals(finalCharge, rental.getFinalCharge());
    }


    /**
     * Test 2: Valid Checkout (LADW with 10% Discount).
     *
     * This test verifies the behavior of the createRental() method for a valid checkout scenario
     * with a ladder tool (code "LADW") over a holiday period (July 4th) and a 10% discount.
     * It ensures that the rental service correctly calculates chargeable days by excluding
     * observed holidays and applying the discount accurately.
     *
     * Steps:
     * 1. Sets up a mock ladder tool with properties that allow charges on weekdays, weekends, and holidays.
     * 2. Calculates chargeable days based on rental days and excludes observed holidays (Independence Day and Labor Day).
     * 3. Calculates expected pre-discount charges, discount amount, and final charge.
     * 4. Mocks the behavior of rentalRepository.save() to return the expected Rental object.
     *
     * Assertions:
     * - Ensures the returned Rental object is not null.
     * - Verifies that due date, pre-discount charge, discount amount, and final charge are correctly calculated and match expected values.
     */
    @Test
    public void testValidCheckoutLADW() {
        String toolCode = "LADW";
        LocalDate checkoutDate = LocalDate.of(2020, 7, 2);
        int rentalDays = 3;
        BigDecimal discountPercent = new BigDecimal("10");

        // Set up Tool mock
        Tool ladderTool = new Tool();
        ladderTool.setToolCode(toolCode);
        ladderTool.setToolType("Ladder");
        ladderTool.setBrand("Werner");

        // Set up ToolCharge mock and associate it with Tool
        ToolCharge ladderToolCharge = new ToolCharge();
        ladderToolCharge.setDailyRentalCharge(new BigDecimal("1.99"));
        ladderToolCharge.setWeekdayCharge(true);
        ladderToolCharge.setWeekendCharge(true);
        ladderToolCharge.setHolidayCharge(true);

        // Associate the ToolCharge with Tool in the mock
        ladderTool.setToolCharge(ladderToolCharge);

        // Mock the toolService to return Tool and ToolCharge from cache
        when(toolService.getToolFromCache(toolCode)).thenReturn(ladderTool);
        when(toolService.getToolChargeFromCache(toolCode)).thenReturn(ladderToolCharge);

        // Define due date and calculate charge days excluding specific holidays
        LocalDate dueDate = checkoutDate.plusDays(rentalDays - 1);

        int chargeDays = 0;
        LocalDate currentDate = checkoutDate;

        while (!currentDate.isAfter(dueDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            // Holiday conditions
            boolean isIndependenceDayObserved = (currentDate.getMonthValue() == 7 && currentDate.getDayOfMonth() == 3 && dayOfWeek == DayOfWeek.FRIDAY) ||
                    (currentDate.getMonthValue() == 7 && currentDate.getDayOfMonth() == 4 && dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) ||
                    (currentDate.getMonthValue() == 7 && currentDate.getDayOfMonth() == 5 && dayOfWeek == DayOfWeek.MONDAY);

            boolean isLaborDay = currentDate.getMonthValue() == 9 && dayOfWeek == DayOfWeek.MONDAY && currentDate.getDayOfMonth() <= 7;

            // Only count chargeable days if it is a chargeable day and not an observed holiday
            if ((ladderToolCharge.isWeekdayCharge() && dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY)
                    || (ladderToolCharge.isWeekendCharge() && (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY))
                    || (ladderToolCharge.isHolidayCharge() && (isIndependenceDayObserved || isLaborDay))) {
                chargeDays++;
            }
            currentDate = currentDate.plusDays(1);
        }

        // Calculate expected charges based on tool properties and charge days
        BigDecimal dailyRentalCharge = ladderToolCharge.getDailyRentalCharge();
        BigDecimal preDiscountCharge = dailyRentalCharge.multiply(BigDecimal.valueOf(chargeDays));
        BigDecimal discountAmount = preDiscountCharge.multiply(discountPercent).divide(BigDecimal.valueOf(100));
        BigDecimal finalCharge = preDiscountCharge.subtract(discountAmount);

        // Setup expected Rental object and mock rentalRepository.save()
        Rental expectedRental = new Rental();
        expectedRental.setTool(ladderTool);
        expectedRental.setRentalDays(rentalDays);
        expectedRental.setCheckoutDate(checkoutDate);
        expectedRental.setDiscountPercent(discountPercent);
        expectedRental.setDueDate(dueDate);
        expectedRental.setPreDiscountCharge(preDiscountCharge);
        expectedRental.setDiscountAmount(discountAmount);
        expectedRental.setFinalCharge(finalCharge);

        when(rentalRepository.save(any(Rental.class))).thenReturn(expectedRental);

        // Action
        Rental rental = rentalService.createRental(toolCode, rentalDays, discountPercent, checkoutDate);

        // Assert
        assertNotNull(rental, "Rental object should not be null");
        assertEquals(dueDate, rental.getDueDate());
        assertEquals(preDiscountCharge, rental.getPreDiscountCharge());
        assertEquals(discountAmount, rental.getDiscountAmount());
        assertEquals(finalCharge, rental.getFinalCharge());
    }


    /**
     * Test 3: Valid Checkout (CHNS with 25% Discount)
     *
     * This test verifies that the `createRental` method correctly calculates rental charges
     * for a valid checkout scenario using the tool with code "CHNS" (Chainsaw, Stihl brand)
     * and applying a 25% discount. The test specifically checks:
     *
     * 1. Exclusion of weekend days from the charge calculation, while including holidays.
     * 2. Calculation of due date based on the rental days.
     * 3. Correct computation of pre-discount charges, discount amount, and final charges based
     *    on the tool’s daily rental rate and provided discount.
     *
     * Expected Outcome:
     * - The test passes if the computed due date, pre-discount charge, discount amount,
     *   and final charge match the expected values for a 25% discount and weekend exclusion.
     */
    @Test
    public void testValidCheckoutCHNS() {
        String toolCode = "CHNS";
        LocalDate checkoutDate = LocalDate.of(2015, 7, 2);
        int rentalDays = 5;
        BigDecimal discountPercent = new BigDecimal("25");

        // Set up Tool mock
        Tool chainsawTool = new Tool();
        chainsawTool.setToolCode(toolCode);
        chainsawTool.setToolType("Chainsaw");
        chainsawTool.setBrand("Stihl");

        // Set up ToolCharge mock and associate it with Tool
        ToolCharge chainsawToolCharge = new ToolCharge();
        chainsawToolCharge.setDailyRentalCharge(new BigDecimal("1.49"));
        chainsawToolCharge.setWeekdayCharge(true);
        chainsawToolCharge.setWeekendCharge(false);
        chainsawToolCharge.setHolidayCharge(true);

        // Associate ToolCharge with Tool
        chainsawTool.setToolCharge(chainsawToolCharge);

        // Mock toolService to return cached Tool and ToolCharge
        when(toolService.getToolFromCache(toolCode)).thenReturn(chainsawTool);
        when(toolService.getToolChargeFromCache(toolCode)).thenReturn(chainsawToolCharge);

        // Define due date and calculate charge days, excluding weekends but including holidays
        LocalDate dueDate = checkoutDate.plusDays(rentalDays - 1);

        int chargeDays = 0;
        LocalDate currentDate = checkoutDate;

        while (!currentDate.isAfter(dueDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            // Determine if the current day should be chargeable based on ToolCharge settings
            boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
            boolean isChargeable = (chainsawToolCharge.isWeekdayCharge() && !isWeekend) ||
                    (chainsawToolCharge.isWeekendCharge() && isWeekend) ||
                    (chainsawToolCharge.isHolidayCharge() && isHoliday(currentDate));

            if (isChargeable) {
                chargeDays++;
            }
            currentDate = currentDate.plusDays(1);
        }

        // Calculate expected charges based on tool properties and charge days
        BigDecimal dailyRentalCharge = chainsawToolCharge.getDailyRentalCharge();
        BigDecimal preDiscountCharge = dailyRentalCharge.multiply(BigDecimal.valueOf(chargeDays));
        BigDecimal discountAmount = preDiscountCharge.multiply(discountPercent).divide(BigDecimal.valueOf(100));
        BigDecimal finalCharge = preDiscountCharge.subtract(discountAmount);

        // Setup expected Rental object and mock rentalRepository.save()
        Rental expectedRental = new Rental();
        expectedRental.setTool(chainsawTool);
        expectedRental.setRentalDays(rentalDays);
        expectedRental.setCheckoutDate(checkoutDate);
        expectedRental.setDiscountPercent(discountPercent);
        expectedRental.setDueDate(dueDate);
        expectedRental.setPreDiscountCharge(preDiscountCharge);
        expectedRental.setDiscountAmount(discountAmount);
        expectedRental.setFinalCharge(finalCharge);

        when(rentalRepository.save(any(Rental.class))).thenReturn(expectedRental);

        // Action
        Rental rental = rentalService.createRental(toolCode, rentalDays, discountPercent, checkoutDate);

        // Assert
        assertNotNull(rental, "Rental object should not be null");
        assertEquals(dueDate, rental.getDueDate());
        assertEquals(preDiscountCharge, rental.getPreDiscountCharge());
        assertEquals(discountAmount, rental.getDiscountAmount());
        assertEquals(finalCharge, rental.getFinalCharge());
    }



    /**
     * Test 4: Valid Checkout (JAKD with 0% Discount)
     *
     * This test verifies the correct calculation of rental charges for a valid checkout scenario
     * using the tool with code "JAKD" (Jackhammer, DeWalt brand) for a rental period of 6 days
     * with a 0% discount. The test specifically validates:
     *
     * 1. Exclusion of weekend days and specific holidays (Independence Day and Labor Day)
     *    from the charge calculation.
     * 2. Calculation of the due date based on the rental days.
     * 3. Correct computation of pre-discount charges, discount amount, and final charges
     *    when no discount is applied.
     *
     * Expected Outcome:
     * - The test passes if the computed due date, pre-discount charge, discount amount,
     *   and final charge match the expected values based on a 0% discount, with weekends
     *   and holidays excluded.
     */
    @Test
    public void testValidCheckoutJAKD() {
        String toolCode = "JAKD";
        LocalDate checkoutDate = LocalDate.of(2015, 9, 3);
        int rentalDays = 6;
        BigDecimal discountPercent = new BigDecimal("0");

        // Set up Tool mock
        Tool jackhammerTool = new Tool();
        jackhammerTool.setToolCode(toolCode);
        jackhammerTool.setToolType("Jackhammer");
        jackhammerTool.setBrand("DeWalt");

        // Set up ToolCharge mock and associate it with Tool
        ToolCharge jackhammerToolCharge = new ToolCharge();
        jackhammerToolCharge.setDailyRentalCharge(new BigDecimal("2.99"));
        jackhammerToolCharge.setWeekdayCharge(true);
        jackhammerToolCharge.setWeekendCharge(false);
        jackhammerToolCharge.setHolidayCharge(false);

        // Associate ToolCharge with Tool
        jackhammerTool.setToolCharge(jackhammerToolCharge);

        // Mock toolService to return cached Tool and ToolCharge
        when(toolService.getToolFromCache(toolCode)).thenReturn(jackhammerTool);
        when(toolService.getToolChargeFromCache(toolCode)).thenReturn(jackhammerToolCharge);

        // Define due date and calculate charge days based on rules
        LocalDate dueDate = checkoutDate.plusDays(rentalDays - 1);

        int chargeDays = 0;
        LocalDate currentDate = checkoutDate;

        while (!currentDate.isAfter(dueDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            // Check if the current day should be chargeable based on ToolCharge settings
            boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
            boolean isHoliday = isHoliday(currentDate);

            // Only count chargeable days that are weekdays and not holidays
            if (jackhammerToolCharge.isWeekdayCharge() && !isWeekend && !isHoliday) {
                chargeDays++;
            }
            currentDate = currentDate.plusDays(1);
        }

        // Calculate expected charges based on tool properties and charge days
        BigDecimal dailyRentalCharge = jackhammerToolCharge.getDailyRentalCharge();
        BigDecimal preDiscountCharge = dailyRentalCharge.multiply(BigDecimal.valueOf(chargeDays));
        BigDecimal discountAmount = preDiscountCharge.multiply(discountPercent).divide(BigDecimal.valueOf(100));
        BigDecimal finalCharge = preDiscountCharge.subtract(discountAmount);

        // Setup expected Rental object and mock rentalRepository.save()
        Rental expectedRental = new Rental();
        expectedRental.setTool(jackhammerTool);
        expectedRental.setRentalDays(rentalDays);
        expectedRental.setCheckoutDate(checkoutDate);
        expectedRental.setDiscountPercent(discountPercent);
        expectedRental.setDueDate(dueDate);
        expectedRental.setPreDiscountCharge(preDiscountCharge);
        expectedRental.setDiscountAmount(discountAmount);
        expectedRental.setFinalCharge(finalCharge);

        when(rentalRepository.save(any(Rental.class))).thenReturn(expectedRental);

        // Act
        Rental rental = rentalService.createRental(toolCode, rentalDays, discountPercent, checkoutDate);

        // Assert
        assertNotNull(rental, "Rental object should not be null");
        assertEquals(dueDate, rental.getDueDate());
        assertEquals(preDiscountCharge, rental.getPreDiscountCharge());
        assertEquals(discountAmount, rental.getDiscountAmount());
        assertEquals(finalCharge, rental.getFinalCharge());
    }

    /**
     * Test 5: Valid Checkout (JAKR with 0% Discount, 9 days rental)
     *
     * This test validates the rental charge calculations for a valid checkout scenario with
     * the tool "JAKR" (Jackhammer, Ridgid brand) over a rental period of 9 days, applying a 0% discount.
     * It specifically verifies that:
     *
     * 1. Weekends and holidays (Independence Day and Labor Day) are correctly excluded from chargeable days.
     * 2. The due date and total charge are calculated accurately based on the rental days and the
     *    daily rental charge of the tool.
     * 3. Pre-discount charges, discount amount, and final charges are correctly computed, with no discount applied.
     *
     * Expected Outcome:
     * - The test passes if the due date, pre-discount charge, discount amount, and final charge match
     *   expected values based on a 0% discount, with weekends and holidays excluded from chargeable days.
     */
    @Test
    public void testValidCheckoutJAKRNoDiscount() {
        String toolCode = "JAKR";
        LocalDate checkoutDate = LocalDate.of(2015, 7, 2);
        int rentalDays = 9;
        BigDecimal discountPercent = BigDecimal.ZERO;

        // Set up Tool mock
        Tool jackhammerTool = new Tool();
        jackhammerTool.setToolCode(toolCode);
        jackhammerTool.setToolType("Jackhammer");
        jackhammerTool.setBrand("Ridgid");

        // Set up ToolCharge mock and associate it with Tool
        ToolCharge jackhammerToolCharge = new ToolCharge();
        jackhammerToolCharge.setDailyRentalCharge(new BigDecimal("2.99"));
        jackhammerToolCharge.setWeekdayCharge(true);
        jackhammerToolCharge.setWeekendCharge(false);
        jackhammerToolCharge.setHolidayCharge(false);

        // Associate ToolCharge with Tool
        jackhammerTool.setToolCharge(jackhammerToolCharge);

        // Mock toolService to return cached Tool and ToolCharge
        when(toolService.getToolFromCache(toolCode)).thenReturn(jackhammerTool);
        when(toolService.getToolChargeFromCache(toolCode)).thenReturn(jackhammerToolCharge);

        // Define due date and calculate charge days based on rules
        LocalDate dueDate = checkoutDate.plusDays(rentalDays - 1);

        int chargeDays = 0;
        LocalDate currentDate = checkoutDate;

        while (!currentDate.isAfter(dueDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            // Determine if the current day should be chargeable based on ToolCharge settings
            boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
            boolean isHoliday = isHoliday(currentDate);

            // Only count chargeable days that are weekdays and not holidays
            if (jackhammerToolCharge.isWeekdayCharge() && !isWeekend && !isHoliday) {
                chargeDays++;
            }
            currentDate = currentDate.plusDays(1);
        }

        // Calculate expected charges based on tool properties and charge days
        BigDecimal dailyRentalCharge = jackhammerToolCharge.getDailyRentalCharge();
        BigDecimal preDiscountCharge = dailyRentalCharge.multiply(BigDecimal.valueOf(chargeDays));
        BigDecimal discountAmount = preDiscountCharge.multiply(discountPercent).divide(BigDecimal.valueOf(100));
        BigDecimal finalCharge = preDiscountCharge.subtract(discountAmount);

        // Setup expected Rental object and mock rentalRepository.save()
        Rental expectedRental = new Rental();
        expectedRental.setTool(jackhammerTool);
        expectedRental.setRentalDays(rentalDays);
        expectedRental.setCheckoutDate(checkoutDate);
        expectedRental.setDiscountPercent(discountPercent);
        expectedRental.setDueDate(dueDate);
        expectedRental.setPreDiscountCharge(preDiscountCharge);
        expectedRental.setDiscountAmount(discountAmount);
        expectedRental.setFinalCharge(finalCharge);

        when(rentalRepository.save(any(Rental.class))).thenReturn(expectedRental);

        // Act
        Rental rental = rentalService.createRental(toolCode, rentalDays, discountPercent, checkoutDate);

        // Assert
        assertNotNull(rental, "Rental object should not be null");
        assertEquals(dueDate, rental.getDueDate());
        assertEquals(preDiscountCharge, rental.getPreDiscountCharge());
        assertEquals(discountAmount, rental.getDiscountAmount());
        assertEquals(finalCharge, rental.getFinalCharge());
    }


    /**
     * Test 6: Valid Checkout (JAKR with 50% Discount)
     *
     * This test verifies the correct calculation of rental charges for a valid checkout scenario
     * with the tool code "JAKR" (Jackhammer, Ridgid brand) rented for a period of 4 days, applying
     * a 50% discount. It specifically checks:
     *
     * 1. Exclusion of weekends and applicable holidays (Independence Day and Labor Day) from chargeable days.
     * 2. Correct calculation of due date based on rental days.
     * 3. Accurate computation of pre-discount charges, discount amount, and final charges based on a 50% discount.
     *
     * Expected Outcome:
     * - The test passes if the calculated due date, pre-discount charge, discount amount, and final
     *   charge match the expected values based on a 50% discount and correct weekend and holiday exclusions.
     */
    @Test
    public void testValidCheckoutJAKRWithDiscounts() {
        String toolCode = "JAKR";
        LocalDate checkoutDate = LocalDate.of(2020, 7, 2);
        int rentalDays = 4;
        BigDecimal discountPercent = new BigDecimal("50");

        // Set up Tool mock
        Tool jackhammerTool = new Tool();
        jackhammerTool.setToolCode(toolCode);
        jackhammerTool.setToolType("Jackhammer");
        jackhammerTool.setBrand("Ridgid");

        // Set up ToolCharge mock and associate it with Tool
        ToolCharge jackhammerToolCharge = new ToolCharge();
        jackhammerToolCharge.setDailyRentalCharge(new BigDecimal("2.99"));
        jackhammerToolCharge.setWeekdayCharge(true);
        jackhammerToolCharge.setWeekendCharge(false);
        jackhammerToolCharge.setHolidayCharge(false);

        // Associate ToolCharge with Tool
        jackhammerTool.setToolCharge(jackhammerToolCharge);

        // Mock toolService to return cached Tool and ToolCharge
        when(toolService.getToolFromCache(toolCode)).thenReturn(jackhammerTool);
        when(toolService.getToolChargeFromCache(toolCode)).thenReturn(jackhammerToolCharge);

        // Define due date and calculate charge days excluding weekends and holidays
        LocalDate dueDate = checkoutDate.plusDays(rentalDays - 1);

        int chargeDays = 0;
        LocalDate currentDate = checkoutDate;

        while (!currentDate.isAfter(dueDate)) {
            DayOfWeek dayOfWeek = currentDate.getDayOfWeek();

            // Determine if the current day should be chargeable based on ToolCharge settings
            boolean isWeekend = dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
            boolean isHoliday = isHoliday(currentDate);

            // Only count chargeable days that are weekdays and not holidays
            if (jackhammerToolCharge.isWeekdayCharge() && !isWeekend && !isHoliday) {
                chargeDays++;
            }
            currentDate = currentDate.plusDays(1);
        }

        // Calculate expected charges based on tool properties and charge days
        BigDecimal dailyRentalCharge = jackhammerToolCharge.getDailyRentalCharge();
        BigDecimal preDiscountCharge = dailyRentalCharge.multiply(BigDecimal.valueOf(chargeDays));
        BigDecimal discountAmount = preDiscountCharge.multiply(discountPercent).divide(BigDecimal.valueOf(100));
        BigDecimal finalCharge = preDiscountCharge.subtract(discountAmount);

        // Setup expected Rental object and mock rentalRepository.save()
        Rental expectedRental = new Rental();
        expectedRental.setTool(jackhammerTool);
        expectedRental.setRentalDays(rentalDays);
        expectedRental.setCheckoutDate(checkoutDate);
        expectedRental.setDiscountPercent(discountPercent);
        expectedRental.setDueDate(dueDate);
        expectedRental.setPreDiscountCharge(preDiscountCharge);
        expectedRental.setDiscountAmount(discountAmount);
        expectedRental.setFinalCharge(finalCharge);

        when(rentalRepository.save(any(Rental.class))).thenReturn(expectedRental);

        // Act
        Rental rental = rentalService.createRental(toolCode, rentalDays, discountPercent, checkoutDate);

        // Assert
        assertNotNull(rental, "Rental object should not be null");
        assertEquals(dueDate, rental.getDueDate());
        assertEquals(preDiscountCharge, rental.getPreDiscountCharge());
        assertEquals(discountAmount, rental.getDiscountAmount());
        assertEquals(finalCharge, rental.getFinalCharge());

        // Verify that save was called on rentalRepository
        verify(rentalRepository).save(any(Rental.class));
    }


    /**
     * Test for the normal operation of createRental() when the circuit breaker is closed.
     * This test verifies that the rental is successfully created with correct values
     * when no errors occur in ToolService.
     */
    @Test
    public void createRental_Success() {
        String toolCode = "TEST";
        int rentalDays = 5;
        BigDecimal discountPercent = BigDecimal.valueOf(10);
        LocalDate checkoutDate = LocalDate.now();

        // Set up Tool and ToolCharge mocks
        Tool mockTool = new Tool();
        mockTool.setToolCode(toolCode);
        mockTool.setToolType("GenericTool");
        mockTool.setBrand("GenericBrand");

        ToolCharge mockToolCharge = new ToolCharge();
        mockToolCharge.setDailyRentalCharge(BigDecimal.valueOf(15.00));
        mockToolCharge.setWeekdayCharge(true);
        mockToolCharge.setWeekendCharge(true);
        mockToolCharge.setHolidayCharge(false);

        // Link ToolCharge to Tool
        mockTool.setToolCharge(mockToolCharge);

        // Mock toolService to return cached Tool and ToolCharge
        when(toolService.getToolFromCache(toolCode)).thenReturn(mockTool);
        when(toolService.getToolChargeFromCache(toolCode)).thenReturn(mockToolCharge);

        // Mock the save operation on rentalRepository
        when(rentalRepository.save(any(Rental.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Rental rental = rentalService.createRental(toolCode, rentalDays, discountPercent, checkoutDate);

        // Assert
        assertNotNull(rental, "Rental object should not be null");
        assertEquals(toolCode, rental.getTool().getToolCode());
        assertEquals(discountPercent, rental.getDiscountPercent());
    }

}
