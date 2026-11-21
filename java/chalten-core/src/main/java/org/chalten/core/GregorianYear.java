package org.chalten.core;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Year;
import org.chalten.localization.Translations;
import org.chalten.measure.Fraction;
import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;

/** Smalltalk {@code GregorianYear}: a year of the proleptic Gregorian calendar (year 0 and negative years exist). */
public final class GregorianYear implements CalendarYear<GregorianYear, GregorianDate, GregorianMonthOfYear>,
        ISO8601Printable {

    private final long number;
    private final GregorianMonthOfYear[] months = new GregorianMonthOfYear[12];

    private GregorianYear(long number) {
        this.number = number;
        for (GregorianMonth month : GregorianMonth.values()) {
            months[month.positionInYear() - 1] = new GregorianMonthOfYear(this, month);
        }
    }

    /** Smalltalk {@code GregorianYear class>>calendarName}. */
    public static String calendarName() {
        return Translations.localized("Gregorian");
    }

    /** Smalltalk {@code GregorianYear number: anInteger}. */
    public static GregorianYear number(long number) {
        return new GregorianYear(number);
    }

    /**
     * Smalltalk {@code GregorianYear number: aNumber} for any number.
     *
     * @throws InstanceCreationFailedException {@code <n> is an invalid number for a year in the Gregorian calendar}
     *         when {@code number} is not an integer
     */
    public static GregorianYear number(Number number) {
        Fraction exact = switch (number) {
            case Fraction fraction -> fraction;
            case BigInteger integer -> Fraction.of(integer);
            case BigDecimal decimal -> Fraction.of(decimal);
            case Long value -> Fraction.of(value);
            case Integer value -> Fraction.of(value);
            case Short value -> Fraction.of(value);
            case Byte value -> Fraction.of(value);
            default -> null;
        };
        if (exact == null || !exact.isInteger()) {
            throw invalidNumber(number);
        }
        return number(exact.longValueExact());
    }

    /** Smalltalk {@code anInteger asGregorianYear}. */
    public static GregorianYear of(long number) {
        return number(number);
    }

    public static GregorianYear from(Year year) {
        return number(year.getValue());
    }

    private static InstanceCreationFailedException invalidNumber(Object number) {
        return new InstanceCreationFailedException(
                Translations.localizedWithAll(INVALID_NUMBER, number, calendarName()));
    }

    @Override
    public GregorianYear withNumber(Fraction number) {
        if (!number.isInteger()) {
            throw invalidNumber(number);
        }
        return number(number.longValueExact());
    }

    @Override
    public long number() {
        return number;
    }

    public GregorianMonthOfYear monthAt(int monthIndex) {
        return months[monthIndex - 1];
    }

    public GregorianMonthOfYear january() {
        return monthAt(1);
    }

    public GregorianMonthOfYear february() {
        return monthAt(2);
    }

    public GregorianMonthOfYear march() {
        return monthAt(3);
    }

    public GregorianMonthOfYear april() {
        return monthAt(4);
    }

    public GregorianMonthOfYear may() {
        return monthAt(5);
    }

    public GregorianMonthOfYear june() {
        return monthAt(6);
    }

    public GregorianMonthOfYear july() {
        return monthAt(7);
    }

    public GregorianMonthOfYear august() {
        return monthAt(8);
    }

    public GregorianMonthOfYear september() {
        return monthAt(9);
    }

    public GregorianMonthOfYear october() {
        return monthAt(10);
    }

    public GregorianMonthOfYear november() {
        return monthAt(11);
    }

    public GregorianMonthOfYear december() {
        return monthAt(12);
    }

    @Override
    public GregorianDate firstDate() {
        return january().first();
    }

    @Override
    public GregorianDate lastDate() {
        return december().last();
    }

    public boolean isCentennialYear() {
        return Math.floorMod(number, 100) == 0;
    }

    public boolean isYearNumberDivisibleByFour() {
        return Math.floorMod(number, 4) == 0;
    }

    public boolean isYearNumberDivisibleByFourHundred() {
        return Math.floorMod(number, 400) == 0;
    }

    @Override
    public boolean isLeapYear() {
        return isYearNumberDivisibleByFour() && !isCentennialYear()
                || isCentennialYear() && isYearNumberDivisibleByFourHundred();
    }

    @Override
    public TimeQuantity numberOfDays() {
        return TimeQuantity.of(isLeapYear() ? 366 : 365, TimeUnits.DAY);
    }

    @Override
    public TimeQuantity numberOfMonths() {
        return TimeQuantity.of(12, TimeUnits.MONTH);
    }

    public Year toYear() {
        return Year.of(Math.toIntExact(number));
    }

    @Override
    public void printAsISO8601On(StringBuilder stream) {
        if (number >= 0) {
            if (number > 9999) {
                stream.append('+');
            }
        } else {
            stream.append('-');
        }
        BigInteger absolute = BigInteger.valueOf(number).abs();
        stream.append(Smalltalk.padded(absolute, absolute.compareTo(BigInteger.valueOf(9999)) > 0 ? 6 : 4));
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof GregorianYear other && number == other.number;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(number);
    }

    @Override
    public String toString() {
        return Long.toString(number);
    }
}
