package org.chalten.core;

import java.util.Objects;
import org.chalten.localization.Translations;
import org.chalten.measure.ChaltenAssertionException;

/**
 * Smalltalk {@code CalendarDayOfMonth}: an ordinal day within a calendar month, independent of any year.
 *
 * @param month the calendar month
 * @param dayNumber the ordinal of the day in the month
 * @param <D> the date type of the calendar
 */
public record CalendarDayOfMonth<D extends CalendarDate<D, ?, ?>>(CalendarMonth<D> month, int dayNumber) {

    static final String NON_GREGORIAN = "Cannot use non-gregorian months for days of gregorian months";

    public CalendarDayOfMonth {
        Objects.requireNonNull(month, "month");
    }

    /**
     * Smalltalk {@code CalendarDayOfMonth class>>gregorianMonth:dayNumber:}.
     *
     * @throws ChaltenAssertionException {@code Cannot use non-gregorian months for days of gregorian months}
     */
    public static <D extends CalendarDate<D, ?, ?>> CalendarDayOfMonth<D> gregorianMonth(CalendarMonth<D> month,
            int dayNumber) {
        if (!month.isGregorian()) {
            throw new ChaltenAssertionException(Translations.localized(NON_GREGORIAN));
        }
        return new CalendarDayOfMonth<>(month, dayNumber);
    }

    /** Smalltalk {@code , yearNumber}: this day in the month of {@code yearNumber}. */
    public D of(long yearNumber) {
        return month.of(yearNumber).dateAt(dayNumber);
    }

    @Override
    public String toString() {
        return month + " " + dayNumber;
    }
}
