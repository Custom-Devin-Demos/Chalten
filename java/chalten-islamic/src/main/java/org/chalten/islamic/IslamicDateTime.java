package org.chalten.islamic;

import java.util.Objects;
import org.chalten.core.CalendarDateTime;
import org.chalten.core.GregorianDateTime;
import org.chalten.core.GregorianYear;
import org.chalten.core.TimeOfDay;

/** Smalltalk {@code IslamicDateTime}: an Islamic date at a time of day counted from the start of the day (sunset). */
public final class IslamicDateTime implements CalendarDateTime<IslamicDateTime, IslamicDate> {

    private final IslamicDate date;
    private final TimeOfDay timeOfDay;

    private IslamicDateTime(IslamicDate date, TimeOfDay timeOfDay) {
        this.date = Objects.requireNonNull(date, "date");
        this.timeOfDay = Objects.requireNonNull(timeOfDay, "timeOfDay");
    }

    /** Smalltalk {@code IslamicDateTime class>>on:at:}. */
    public static IslamicDateTime on(IslamicDate date, TimeOfDay timeOfDay) {
        return new IslamicDateTime(date, timeOfDay);
    }

    @Override
    public IslamicDate date() {
        return date;
    }

    @Override
    public TimeOfDay timeOfDay() {
        return timeOfDay;
    }

    @Override
    public IslamicMonth month() {
        return date.month();
    }

    @Override
    public IslamicMonthOfYear monthOfYear() {
        return date.monthOfYear();
    }

    @Override
    public IslamicYear year() {
        return date.year();
    }

    /** Muharram 1, 1 AH at 00:00 is July 19, 622 at 18:00 (sunset). */
    @Override
    public GregorianDateTime asGregorianDateTime() {
        return CalendarDateTime.convert(this, IslamicYear.of(1).muharram().first(),
                GregorianYear.of(622).july().nineteenth());
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof IslamicDateTime other && date.equals(other.date)
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
