package org.chalten.core;

import java.time.LocalDate;
import java.util.function.Consumer;
import org.chalten.localization.FormatterOptions;
import org.chalten.localization.LocalizedChronologyFormatter;
import org.chalten.measure.Fraction;
import org.chalten.measure.InstanceCreationFailedException;

/** Smalltalk {@code GregorianDate}: a day of the proleptic Gregorian calendar, from midnight to midnight. */
public final class GregorianDate implements CalendarDate<GregorianDate, GregorianMonthOfYear, GregorianYear>,
        ISO8601Printable {

    private final GregorianMonthOfYear monthOfYear;
    private final int dayNumber;

    private GregorianDate(GregorianMonthOfYear monthOfYear, int dayNumber) {
        this.monthOfYear = monthOfYear;
        this.dayNumber = dayNumber;
    }

    /**
     * Smalltalk {@code GregorianDate class>>monthOfYear:dayNumber:}.
     *
     * @throws InstanceCreationFailedException {@code <n> is an invalid day number in <month>, <year>}
     */
    public static GregorianDate monthOfYear(GregorianMonthOfYear monthOfYear, int dayNumber) {
        CalendarDate.assertIsValid(dayNumber, monthOfYear);
        return new GregorianDate(monthOfYear, dayNumber);
    }

    /** Convenience for {@code (GregorianMonth monthAt: month) , year dateAt: day}. */
    public static GregorianDate of(long yearNumber, int monthNumber, int dayNumber) {
        return GregorianYear.number(yearNumber).monthAt(monthNumber).dateAt(dayNumber);
    }

    public static GregorianDate from(LocalDate date) {
        return of(date.getYear(), date.getMonthValue(), date.getDayOfMonth());
    }

    @Override
    public int dayNumber() {
        return dayNumber;
    }

    @Override
    public GregorianMonthOfYear monthOfYear() {
        return monthOfYear;
    }

    @Override
    public GregorianMonth month() {
        return monthOfYear.month();
    }

    @Override
    public int monthNumber() {
        return month().positionInYear();
    }

    @Override
    public GregorianDateTime at(TimeOfDay timeOfDay) {
        return GregorianDateTime.on(this, timeOfDay);
    }

    @Override
    public GregorianDateTime atStartOfDay() {
        return at(TimeOfDay.startOfDay());
    }

    @Override
    public GregorianDateTime atMidnight() {
        return atStartOfDay();
    }

    @Override
    public GregorianDateTime atNoon() {
        return at(TimeOfDay.halfOfDay());
    }

    @Override
    public DayOfWeek dayOfWeek() {
        Fraction julianDayNumber = atNoon().julianDayNumber();
        return DayOfWeek.dayAt(julianDayNumber.floorMod(Fraction.of(7)).intValueExact() + 1);
    }

    @Override
    public int compareTo(GregorianDate other) {
        int byMonth = monthOfYear.compareTo(other.monthOfYear);
        return byMonth != 0 ? byMonth : Integer.compare(dayNumber, other.dayNumber);
    }

    public LocalDate toLocalDate() {
        return LocalDate.of(Math.toIntExact(yearNumber()), monthNumber(), dayNumber);
    }

    @Override
    public void printAsISO8601On(StringBuilder stream) {
        monthOfYear.printAsISO8601On(stream);
        stream.append('-').append(Smalltalk.padded(dayNumber, 2));
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof GregorianDate other && dayNumber == other.dayNumber
                && monthOfYear.equals(other.monthOfYear);
    }

    @Override
    public int hashCode() {
        return 31 * dayNumber + monthOfYear.hashCode();
    }

    @Override
    public String toString() {
        return month() + " " + dayNumber + ", " + year();
    }

    /** Smalltalk {@code localized}: formatted in the medium style of {@link org.chalten.localization.CurrentLocale}. */
    public String localized() {
        return LocalizedChronologyFormatter.localized(this, options -> { });
    }

    /** Smalltalk {@code localizedWith:}: formatted in the style {@code configurer} chooses. */
    public String localized(Consumer<FormatterOptions> configurer) {
        return LocalizedChronologyFormatter.localized(this, configurer);
    }
}
