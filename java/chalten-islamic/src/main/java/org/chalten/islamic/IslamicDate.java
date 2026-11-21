package org.chalten.islamic;

import org.chalten.core.CalendarDate;
import org.chalten.core.TimeOfDay;

/** Smalltalk {@code IslamicDate}: a day of the tabular Islamic calendar, starting at 18:00 (sunset) the day before. */
public final class IslamicDate implements CalendarDate<IslamicDate, IslamicMonthOfYear, IslamicYear> {

    private final IslamicMonthOfYear monthOfYear;
    private final int dayNumber;

    private IslamicDate(IslamicMonthOfYear monthOfYear, int dayNumber) {
        this.monthOfYear = monthOfYear;
        this.dayNumber = dayNumber;
    }

    /** Smalltalk {@code IslamicDate class>>monthOfYear:dayNumber:}. */
    public static IslamicDate monthOfYear(IslamicMonthOfYear monthOfYear, int dayNumber) {
        CalendarDate.assertIsValid(dayNumber, monthOfYear);
        return new IslamicDate(monthOfYear, dayNumber);
    }

    @Override
    public int dayNumber() {
        return dayNumber;
    }

    @Override
    public IslamicMonthOfYear monthOfYear() {
        return monthOfYear;
    }

    @Override
    public IslamicMonth month() {
        return monthOfYear.month();
    }

    @Override
    public int monthNumber() {
        return month().positionInYear();
    }

    @Override
    public IslamicDateTime at(TimeOfDay timeOfDay) {
        return IslamicDateTime.on(this, timeOfDay);
    }

    @Override
    public IslamicDateTime atStartOfDay() {
        return at(TimeOfDay.startOfDay());
    }

    /** Smalltalk {@code atMidnight}: 06:00, six hours after the day started at sunset. */
    @Override
    public IslamicDateTime atMidnight() {
        return at(TimeOfDay.hours(6));
    }

    /** Smalltalk {@code atNoon}: 18:00. */
    @Override
    public IslamicDateTime atNoon() {
        return at(TimeOfDay.hours(18));
    }

    @Override
    public IslamicDayOfWeek dayOfWeek() {
        long yearNumber = yearNumber();
        long fixedDayNumber = dayNumber + Math.ceilDiv(59L * (monthNumber() - 1), 2) + (yearNumber - 1) * 354
                + Math.floorDiv(3 + 11 * yearNumber, 30) + 227015;
        int zeroBasedIndex = Math.floorMod(fixedDayNumber, 7);
        return IslamicDayOfWeek.dayAt(zeroBasedIndex == 0 ? 7 : zeroBasedIndex);
    }

    @Override
    public int compareTo(IslamicDate other) {
        int byMonth = monthOfYear.compareTo(other.monthOfYear);
        return byMonth != 0 ? byMonth : Integer.compare(dayNumber, other.dayNumber);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof IslamicDate other && dayNumber == other.dayNumber
                && monthOfYear.equals(other.monthOfYear);
    }

    @Override
    public int hashCode() {
        return 31 * monthOfYear.hashCode() + dayNumber;
    }

    @Override
    public String toString() {
        return month() + " " + dayNumber + ", " + year();
    }
}
