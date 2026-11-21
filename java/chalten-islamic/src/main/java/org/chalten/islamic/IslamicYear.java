package org.chalten.islamic;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.chalten.core.CalendarYear;
import org.chalten.localization.Translations;
import org.chalten.measure.DiscreteInterval;
import org.chalten.measure.Fraction;
import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;

/** Smalltalk {@code IslamicYear}: a year of the tabular Islamic calendar, counted Anno Hegirae (AH). */
public final class IslamicYear implements CalendarYear<IslamicYear, IslamicDate, IslamicMonthOfYear> {

    static final String NEGATIVE_YEARS = "Negative years are unsupported for the Islamic calendar";
    private static final Map<Long, IslamicYear> YEARS = new ConcurrentHashMap<>();

    private final long number;
    private final IslamicMonthOfYear[] months = new IslamicMonthOfYear[12];

    private IslamicYear(long number) {
        this.number = number;
    }

    /** Smalltalk {@code IslamicYear class>>calendarName}. */
    public static String calendarName() {
        return Translations.localized("Islamic");
    }

    /**
     * Smalltalk {@code IslamicYear class>>number:}.
     *
     * @throws InstanceCreationFailedException {@code Negative years are unsupported for the Islamic calendar}
     */
    public static IslamicYear number(long number) {
        if (number <= 0) {
            throw new InstanceCreationFailedException(Translations.localized(NEGATIVE_YEARS));
        }
        return YEARS.computeIfAbsent(number, IslamicYear::new);
    }

    /**
     * Smalltalk {@code IslamicYear class>>number:} for any number.
     *
     * @throws InstanceCreationFailedException {@code <n> is an invalid number for a year in the Islamic calendar}
     */
    public static IslamicYear number(Number number) {
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

    /** Smalltalk {@code anInteger asIslamicYear}. */
    public static IslamicYear of(long number) {
        return number(number);
    }

    private static InstanceCreationFailedException invalidNumber(Object number) {
        return new InstanceCreationFailedException(
                Translations.localizedWithAll(INVALID_NUMBER, number, calendarName()));
    }

    @Override
    public IslamicYear withNumber(Fraction number) {
        if (!number.isInteger()) {
            throw invalidNumber(number);
        }
        return number(number.longValueExact());
    }

    @Override
    public long number() {
        return number;
    }

    @Override
    public boolean isComparableTo(TimeQuantity quantity) {
        return quantity.isCommensurableWith(IslamicCalendar.year());
    }

    /** Smalltalk {@code next:}, for quantities of Islamic years or months. */
    @Override
    public IslamicYear next(TimeQuantity relativeTimeQuantity) {
        return withNumber(relativeTimeQuantity.convertTo(IslamicCalendar.year()).numericalValue().plus(number));
    }

    @Override
    public TimeQuantity minus(IslamicYear other) {
        if (compareTo(other) < 0) {
            throw new ArithmeticException(Translations.localized(CANNOT_SUBTRACT));
        }
        return TimeQuantity.of(number - other.number, IslamicCalendar.year());
    }

    @Override
    public DiscreteInterval<IslamicYear> to(IslamicYear end) {
        return to(end, TimeQuantity.of(1, IslamicCalendar.year()));
    }

    public IslamicMonthOfYear monthAt(int monthIndex) {
        synchronized (months) {
            IslamicMonthOfYear month = months[monthIndex - 1];
            if (month == null) {
                month = IslamicMonthOfYear.year(this, IslamicMonth.monthAt(monthIndex));
                months[monthIndex - 1] = month;
            }
            return month;
        }
    }

    public IslamicMonthOfYear muharram() {
        return monthAt(1);
    }

    public IslamicMonthOfYear safar() {
        return monthAt(2);
    }

    public IslamicMonthOfYear rabiAlAwwal() {
        return monthAt(3);
    }

    public IslamicMonthOfYear rabiAlThani() {
        return monthAt(4);
    }

    public IslamicMonthOfYear jumadaAlAwwal() {
        return monthAt(5);
    }

    public IslamicMonthOfYear jumadaAlThani() {
        return monthAt(6);
    }

    public IslamicMonthOfYear rajab() {
        return monthAt(7);
    }

    public IslamicMonthOfYear shaban() {
        return monthAt(8);
    }

    public IslamicMonthOfYear ramadan() {
        return monthAt(9);
    }

    public IslamicMonthOfYear shawwal() {
        return monthAt(10);
    }

    public IslamicMonthOfYear duAlQiDah() {
        return monthAt(11);
    }

    public IslamicMonthOfYear duAlHijjah() {
        return monthAt(12);
    }

    @Override
    public IslamicDate firstDate() {
        return muharram().first();
    }

    @Override
    public IslamicDate lastDate() {
        return duAlHijjah().last();
    }

    @Override
    public boolean isLeapYear() {
        return Math.floorMod(14 + 11 * number, 30) < 11;
    }

    @Override
    public TimeQuantity numberOfDays() {
        return TimeQuantity.of(isLeapYear() ? 355 : 354, TimeUnits.DAY);
    }

    @Override
    public TimeQuantity numberOfMonths() {
        return TimeQuantity.of(12, IslamicCalendar.month());
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof IslamicYear other && number == other.number;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(number);
    }

    @Override
    public String toString() {
        return number + " AH";
    }
}
