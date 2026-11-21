package org.chalten.core;

import java.time.LocalDateTime;
import java.util.Objects;
import org.chalten.measure.Fraction;
import org.chalten.measure.TimeUnits;

/** Smalltalk {@code GregorianDateTime}: a naive Gregorian date and time of day, independent of any offset. */
public final class GregorianDateTime implements CalendarDateTime<GregorianDateTime, GregorianDate>,
        ISO8601Printable {

    private static final Fraction DAYS_PER_YEAR = Fraction.of(36525, 100);
    private static final Fraction MONTH_FACTOR = Fraction.of(306001, 10000);
    private static final Fraction CORRECTION = Fraction.of(15245, 10);

    private final GregorianDate date;
    private final TimeOfDay timeOfDay;

    private GregorianDateTime(GregorianDate date, TimeOfDay timeOfDay) {
        this.date = Objects.requireNonNull(date, "date");
        this.timeOfDay = Objects.requireNonNull(timeOfDay, "timeOfDay");
    }

    /** Smalltalk {@code GregorianDateTime class>>on:at:}. */
    public static GregorianDateTime on(GregorianDate date, TimeOfDay timeOfDay) {
        return new GregorianDateTime(date, timeOfDay);
    }

    public static GregorianDateTime from(LocalDateTime dateTime) {
        return on(GregorianDate.from(dateTime.toLocalDate()), TimeOfDay.from(dateTime.toLocalTime()));
    }

    @Override
    public GregorianDate date() {
        return date;
    }

    @Override
    public TimeOfDay timeOfDay() {
        return timeOfDay;
    }

    @Override
    public GregorianDateTime asGregorianDateTime() {
        return this;
    }

    @Override
    public GregorianMonth month() {
        return date.month();
    }

    @Override
    public GregorianMonthOfYear monthOfYear() {
        return date.monthOfYear();
    }

    @Override
    public GregorianYear year() {
        return date.year();
    }

    @Override
    public TimeOffsetAwareGregorianDateTime in(TimeOffsetRuleset ruleset) {
        return ruleset.convertGregorianDateTime(this);
    }

    @Override
    public TimeOffsetAwareGregorianDateTime inCoordinatedUniversalTime() {
        return Instant.fromJulianDayNumber(julianDayNumber()).asGregorianDateTimeInCoordinatedUniversalTime();
    }

    /** Smalltalk {@code julianDayNumber}: the exact (rational) Julian day number of this date-time. */
    public Fraction julianDayNumber() {
        long yearNumber;
        long monthNumber;
        if (month() == GregorianMonth.JANUARY || month() == GregorianMonth.FEBRUARY) {
            yearNumber = yearNumber() - 1;
            monthNumber = monthNumber() + 12L;
        } else {
            yearNumber = yearNumber();
            monthNumber = monthNumber();
        }
        long a = Math.floorDiv(yearNumber, 100);
        long b = Math.floorDiv(a, 4);
        long c = 2 - a + b;
        Fraction e = DAYS_PER_YEAR.times(yearNumber + 4716);
        Fraction f = MONTH_FACTOR.times(monthNumber + 1);
        return Fraction.of(e.truncated()).plus(Fraction.of(f.truncated())).plus(c + dayNumber())
                .minus(CORRECTION)
                .plus(timeOfDay.timeFromStartOfDay().convertTo(TimeUnits.DAY).numericalValue());
    }

    public LocalDateTime toLocalDateTime() {
        return LocalDateTime.of(date.toLocalDate(), timeOfDay.toLocalTime());
    }

    @Override
    public void printAsISO8601On(StringBuilder stream) {
        date.printAsISO8601On(stream);
        timeOfDay.printAsISO8601On(stream);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof GregorianDateTime other && date.equals(other.date)
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
