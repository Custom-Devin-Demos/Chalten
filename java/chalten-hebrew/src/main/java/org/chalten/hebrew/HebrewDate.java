package org.chalten.hebrew;

import org.chalten.core.CalendarDate;
import org.chalten.core.DayOfWeek;
import org.chalten.core.TimeOfDay;

/** Smalltalk {@code HebrewDate}: a day of the Hebrew calendar. Days start at 18:00 (sunset) of the day before. */
public final class HebrewDate implements CalendarDate<HebrewDate, HebrewMonthOfYear, HebrewYear> {

    private final HebrewMonthOfYear monthOfYear;
    private final int dayNumber;

    private HebrewDate(HebrewMonthOfYear monthOfYear, int dayNumber) {
        this.monthOfYear = monthOfYear;
        this.dayNumber = dayNumber;
    }

    /**
     * Smalltalk {@code HebrewDate class>>monthOfYear:dayNumber:}.
     *
     * @throws org.chalten.measure.InstanceCreationFailedException {@code <n> is an invalid day number in <month>}
     */
    public static HebrewDate monthOfYear(HebrewMonthOfYear monthOfYear, int dayNumber) {
        CalendarDate.assertIsValid(dayNumber, monthOfYear);
        return new HebrewDate(monthOfYear, dayNumber);
    }

    @Override
    public int dayNumber() {
        return dayNumber;
    }

    @Override
    public HebrewMonthOfYear monthOfYear() {
        return monthOfYear;
    }

    @Override
    public HebrewMonth month() {
        return monthOfYear.month();
    }

    @Override
    public int monthNumber() {
        return month().positionIn(monthOfYear.year());
    }

    @Override
    public HebrewDateTime at(TimeOfDay timeOfDay) {
        return HebrewDateTime.on(this, timeOfDay);
    }

    @Override
    public HebrewDateTime atStartOfDay() {
        return at(TimeOfDay.startOfDay());
    }

    /** Smalltalk {@code atMidnight}: 06:00, six hours after the day started at sunset. */
    @Override
    public HebrewDateTime atMidnight() {
        return at(TimeOfDay.hours(6));
    }

    /** Smalltalk {@code atNoon}: 18:00. */
    @Override
    public HebrewDateTime atNoon() {
        return at(TimeOfDay.hours(18));
    }

    @Override
    public HebrewDayOfWeek dayOfWeek() {
        long absoluteDayNumber = year().roshHashanahDayNumber()
                + minus(year().firstDate()).numericalValue().longValueExact();
        int zeroBasedDayWeekIndex = Math.floorMod(absoluteDayNumber, 7);
        return HebrewDayOfWeek.from(DayOfWeek.dayAt((zeroBasedDayWeekIndex - 7) % 7 + 7));
    }

    @Override
    public int compareTo(HebrewDate other) {
        int byMonth = monthOfYear.compareTo(other.monthOfYear);
        return byMonth != 0 ? byMonth : Integer.compare(dayNumber, other.dayNumber);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof HebrewDate other && dayNumber == other.dayNumber
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
