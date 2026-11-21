package org.chalten.hebrew;

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

/** Smalltalk {@code HebrewYear}: a year of the Hebrew calendar, counted Anno Mundi (AM). */
public final class HebrewYear implements CalendarYear<HebrewYear, HebrewDate, HebrewMonthOfYear> {

    static final String NEGATIVE_YEARS = "Negative years are unsupported for the Hebrew calendar";
    private static final int PARTS_PER_HOUR = 1080;
    private static final Map<Long, HebrewYear> YEARS = new ConcurrentHashMap<>();

    private final long number;
    private final HebrewMonthOfYear[] months;
    private volatile TimeQuantity numberOfDays;

    private HebrewYear(long number) {
        this.number = number;
        this.months = new HebrewMonthOfYear[isLeapYear() ? 13 : 12];
    }

    /** Smalltalk {@code HebrewYear class>>calendarName}. */
    public static String calendarName() {
        return Translations.localized("Hebrew");
    }

    /**
     * Smalltalk {@code HebrewYear class>>number:}.
     *
     * @throws InstanceCreationFailedException {@code Negative years are unsupported for the Hebrew calendar}
     */
    public static HebrewYear number(long number) {
        if (number <= 0) {
            throw new InstanceCreationFailedException(Translations.localized(NEGATIVE_YEARS));
        }
        return YEARS.computeIfAbsent(number, HebrewYear::new);
    }

    /**
     * Smalltalk {@code HebrewYear class>>number:} for any number.
     *
     * @throws InstanceCreationFailedException {@code <n> is an invalid number for a year in the Hebrew calendar}
     *         when {@code number} is not an integer
     */
    public static HebrewYear number(Number number) {
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

    /** Smalltalk {@code anInteger asHebrewYear}. */
    public static HebrewYear of(long number) {
        return number(number);
    }

    private static InstanceCreationFailedException invalidNumber(Object number) {
        return new InstanceCreationFailedException(
                Translations.localizedWithAll(INVALID_NUMBER, number, calendarName()));
    }

    @Override
    public HebrewYear withNumber(Fraction number) {
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
        return quantity.isCommensurableWith(HebrewCalendar.year());
    }

    /**
     * Smalltalk {@code next:}.
     *
     * @throws ArithmeticException when {@code relativeTimeQuantity} is not measured in Hebrew years
     */
    @Override
    public HebrewYear next(TimeQuantity relativeTimeQuantity) {
        return withNumber(relativeTimeQuantity.convertTo(HebrewCalendar.year()).numericalValue().plus(number));
    }

    /** Smalltalk {@code -}: the Hebrew years between {@code other} and this year. */
    @Override
    public TimeQuantity minus(HebrewYear other) {
        if (compareTo(other) < 0) {
            throw new ArithmeticException(Translations.localized(CANNOT_SUBTRACT));
        }
        return TimeQuantity.of(number - other.number, HebrewCalendar.year());
    }

    @Override
    public DiscreteInterval<HebrewYear> to(HebrewYear end) {
        return to(end, TimeQuantity.of(1, HebrewCalendar.year()));
    }

    public HebrewMonthOfYear monthAt(int monthIndex) {
        synchronized (months) {
            HebrewMonthOfYear month = months[monthIndex - 1];
            if (month == null) {
                month = HebrewMonthOfYear.year(this, isLeapYear()
                        ? HebrewMonth.monthOnLeapYearAt(monthIndex)
                        : HebrewMonth.monthOnCommonYearAt(monthIndex));
                months[monthIndex - 1] = month;
            }
            return month;
        }
    }

    private HebrewMonthOfYear monthOfYear(HebrewMonth month) {
        return monthAt(month.positionIn(this));
    }

    public HebrewMonthOfYear tishrei() {
        return monthOfYear(HebrewMonth.TISHREI);
    }

    public HebrewMonthOfYear cheshvan() {
        return monthOfYear(HebrewMonth.CHESHVAN);
    }

    public HebrewMonthOfYear kislev() {
        return monthOfYear(HebrewMonth.KISLEV);
    }

    public HebrewMonthOfYear tevet() {
        return monthOfYear(HebrewMonth.TEVET);
    }

    public HebrewMonthOfYear shevat() {
        return monthOfYear(HebrewMonth.SHEVAT);
    }

    /** @throws InstanceCreationFailedException {@code Adar only exists on common years} */
    public HebrewMonthOfYear adar() {
        return monthOfYear(HebrewMonth.ADAR);
    }

    /** @throws InstanceCreationFailedException {@code Adar I only exists on leap years} */
    public HebrewMonthOfYear adarI() {
        return monthOfYear(HebrewMonth.ADAR_I);
    }

    /** @throws InstanceCreationFailedException {@code Adar II only exists on leap years} */
    public HebrewMonthOfYear adarII() {
        return monthOfYear(HebrewMonth.ADAR_II);
    }

    public HebrewMonthOfYear nisan() {
        return monthOfYear(HebrewMonth.NISAN);
    }

    public HebrewMonthOfYear iyar() {
        return monthOfYear(HebrewMonth.IYAR);
    }

    public HebrewMonthOfYear sivan() {
        return monthOfYear(HebrewMonth.SIVAN);
    }

    public HebrewMonthOfYear tammuz() {
        return monthOfYear(HebrewMonth.TAMMUZ);
    }

    public HebrewMonthOfYear av() {
        return monthOfYear(HebrewMonth.AV);
    }

    public HebrewMonthOfYear elul() {
        return monthOfYear(HebrewMonth.ELUL);
    }

    @Override
    public HebrewDate firstDate() {
        return tishrei().first();
    }

    @Override
    public HebrewDate lastDate() {
        return elul().twentyNinth();
    }

    @Override
    public boolean isLeapYear() {
        return Math.floorMod(1 + 7 * number, 19) < 7;
    }

    public boolean isCompleteYear() {
        long days = numberOfDaysAsLong();
        return days == 355 || days == 385;
    }

    public boolean isDeficientYear() {
        long days = numberOfDaysAsLong();
        return days == 353 || days == 383;
    }

    public boolean isRegularYear() {
        long days = numberOfDaysAsLong();
        return days == 354 || days == 384;
    }

    private long numberOfDaysAsLong() {
        return numberOfDays().numericalValue().longValueExact();
    }

    @Override
    public TimeQuantity numberOfDays() {
        TimeQuantity days = numberOfDays;
        if (days == null) {
            days = TimeQuantity.of(next().roshHashanahDayNumber() - roshHashanahDayNumber(), TimeUnits.DAY);
            numberOfDays = days;
        }
        return days;
    }

    @Override
    public TimeQuantity numberOfMonths() {
        return TimeQuantity.of(isLeapYear() ? 13 : 12, HebrewCalendar.month());
    }

    /** Smalltalk {@code roshHashanahDayNumber}: days from the molad of Tohu to Tishrei 1 of this year. */
    long roshHashanahDayNumber() {
        long partsPerDay = 24L * PARTS_PER_HOUR;
        long lunarMonthDurationInParts = 29 * partsPerDay + 12L * PARTS_PER_HOUR + 793;
        long moladTohuInParts = partsPerDay + 5L * PARTS_PER_HOUR + 204;
        long elapsedMonths = Math.floorDiv(235 * number - 234, 19);
        long moladOfTishreiInParts = moladTohuInParts + elapsedMonths * lunarMonthDurationInParts;
        long moladOfTishreiInDays = Math.floorDiv(moladOfTishreiInParts, partsPerDay);
        long remainingMoladOfTishreiParts = Math.floorMod(moladOfTishreiInParts, partsPerDay);
        long dayOfWeekIndex = Math.floorMod(moladOfTishreiInDays, 7) + 1;
        moladOfTishreiInDays += roshHashanahPostponementOffsetFor(remainingMoladOfTishreiParts, dayOfWeekIndex);
        dayOfWeekIndex = Math.floorMod(moladOfTishreiInDays, 7) + 1;
        if (dayOfWeekIndex == 1 || dayOfWeekIndex == 4 || dayOfWeekIndex == 6) {
            moladOfTishreiInDays++;
        }
        return moladOfTishreiInDays;
    }

    private int roshHashanahPostponementOffsetFor(long remainingParts, long dayOfWeekIndex) {
        if (remainingParts >= 18L * PARTS_PER_HOUR) {
            return 1;
        }
        if (!isLeapYear() && dayOfWeekIndex == 3 && remainingParts >= 9L * PARTS_PER_HOUR + 204) {
            return 2;
        }
        if (number != 1 && previous().isLeapYear() && dayOfWeekIndex == 2
                && remainingParts >= 15L * PARTS_PER_HOUR + 589) {
            return 1;
        }
        return 0;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof HebrewYear other && number == other.number;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(number);
    }

    @Override
    public String toString() {
        return number + " AM";
    }
}
