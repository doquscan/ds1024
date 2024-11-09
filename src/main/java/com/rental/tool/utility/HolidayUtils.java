package com.rental.tool.utility;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;

public class HolidayUtils {

    /**
     * Determines if the given date is a holiday based on the U.S. holidays:
     * - Independence Day (July 4th or observed if on a weekend)
     * - Labor Day (First Monday in September)
     *
     * @param date - The date to check
     * @return true if the date is a holiday, false otherwise
     */
    public static boolean isHoliday(LocalDate date) {
        // Independence Day (July 4th or observed if on a weekend)
        if (isIndependenceDay(date)) {
            return true;
        }

        // Labor Day (First Monday in September)
        if (isLaborDay(date)) {
            return true;
        }

        return false;
    }

    /**
     * Checks if the given date is Independence Day or its observed day.
     *
     * @param date - The date to check
     * @return true if the date is Independence Day or observed Independence Day, false otherwise
     */
    private static boolean isIndependenceDay(LocalDate date) {
        // Check if July 4th
        if (date.getMonth() == Month.JULY && date.getDayOfMonth() == 4) {
            return true;
        }

        // If July 4th falls on a Saturday, it is observed on the previous Friday (July 3rd)
        if (date.getMonth() == Month.JULY && date.getDayOfMonth() == 3 && date.getDayOfWeek() == DayOfWeek.FRIDAY) {
            return true;
        }

        // If July 4th falls on a Sunday, it is observed on the following Monday (July 5th)
        return date.getMonth() == Month.JULY && date.getDayOfMonth() == 5 && date.getDayOfWeek() == DayOfWeek.MONDAY;
    }

    /**
     * Checks if the given date is Labor Day (first Monday in September).
     *
     * @param date - The date to check
     * @return true if the date is Labor Day, false otherwise
     */
    private static boolean isLaborDay(LocalDate date) {
        // Labor Day is the first Monday of September
        return date.getMonth() == Month.SEPTEMBER && date.getDayOfWeek() == DayOfWeek.MONDAY && date.getDayOfMonth() <= 7;
    }
}
