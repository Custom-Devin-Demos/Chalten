package org.chalten.hebrew;

import java.util.Objects;
import org.chalten.core.CalendarDateTime;
import org.chalten.core.GregorianDateTime;
import org.chalten.core.GregorianYear;
import org.chalten.core.TimeOfDay;

/** Smalltalk {@code HebrewDateTime}: a Hebrew date at a time of day counted from the start of the day (sunset). */
public final class HebrewDateTime implements CalendarDateTime<HebrewDateTime, HebrewDate> {

    private final HebrewDate date;
    private final TimeOfDay timeOfDay;

    private HebrewDateTime(HebrewDate date, TimeOfDay timeOfDay) {
        this.date = Objects.requireNonNull(date, "date");
        this.timeOfDay = Objects.requireNonNull(timeOfDay, "timeOfDay");
    }

    /** Smalltalk {@code HebrewDateTime class>>on:at:}. */
    public static HebrewDateTime on(HebrewDate date, TimeOfDay timeOfDay) {
        return new HebrewDateTime(date, timeOfDay);
    }

    @Override
    public HebrewDate date() {
        return date;
    }

    @Override
    public TimeOfDay timeOfDay() {
        return timeOfDay;
    }

    @Override
    public HebrewMonth month() {
        return date.month();
    }

    @Override
    public HebrewMonthOfYear monthOfYear() {
        return date.monthOfYear();
    }

    @Override
    public HebrewYear year() {
        return date.year();
    }

    /** Tishrei 1, 1 AM at 00:00 is September 7, 3761 BCE (-3760, as there is a year 0) at 18:00 (sunset). */
    @Override
    public GregorianDateTime asGregorianDateTime() {
        return CalendarDateTime.convert(this, HebrewYear.of(1).tishrei().first(),
                GregorianYear.of(-3760).september().seventh());
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof HebrewDateTime other && date.equals(other.date)
                && timeOfDay.equals(other.timeOfDay);
    }

    @Override
    public int hashCode() {
        return 31 * date.hashCode() + timeOfDay.hashCode();
    }

    @Override
    public String toString() {
        return date + " at " + timeOfDay;
    }
}
