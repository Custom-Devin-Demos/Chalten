package org.chalten.core;

import java.time.Month;
import java.util.List;
import org.chalten.localization.Translations;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;

/** Smalltalk {@code GregorianMonth}: the twelve months of the Gregorian calendar. */
public enum GregorianMonth implements CalendarMonth<GregorianDate>, ISO8601Printable {
    JANUARY("January", "Jan", 1, 31, 31),
    FEBRUARY("February", "Feb", 2, 29, 28),
    MARCH("March", "Mar", 3, 31, 31),
    APRIL("April", "Apr", 4, 30, 30),
    MAY("May", "ShortName::May", 5, 31, 31),
    JUNE("June", "Jun", 6, 30, 30),
    JULY("July", "Jul", 7, 31, 31),
    AUGUST("August", "Aug", 8, 31, 31),
    SEPTEMBER("September", "Sep", 9, 30, 30),
    OCTOBER("October", "Oct", 10, 31, 31),
    NOVEMBER("November", "Nov", 11, 30, 30),
    DECEMBER("December", "Dec", 12, 31, 31);

    private static final List<GregorianMonth> ALL = List.of(values());

    private final String name;
    private final String shortName;
    private final int positionInYear;
    private final TimeQuantity numberOfDaysInLeapYear;
    private final TimeQuantity numberOfDaysInCommonYear;

    GregorianMonth(String name, String shortName, int positionInYear, int daysInLeapYear, int daysInCommonYear) {
        this.name = name;
        this.shortName = shortName;
        this.positionInYear = positionInYear;
        this.numberOfDaysInLeapYear = TimeQuantity.of(daysInLeapYear, TimeUnits.DAY);
        this.numberOfDaysInCommonYear = TimeQuantity.of(daysInCommonYear, TimeUnits.DAY);
    }

    /** Smalltalk {@code GregorianMonth class>>all}. */
    public static List<GregorianMonth> all() {
        return ALL;
    }

    /** Smalltalk {@code GregorianMonth class>>monthAt:} (1-based). */
    public static GregorianMonth monthAt(int index) {
        return ALL.get(index - 1);
    }

    public static GregorianMonth from(Month month) {
        return monthAt(month.getValue());
    }

    @Override
    public String fullName() {
        return Translations.localized(name);
    }

    @Override
    public String shortName() {
        return Translations.localized(shortName);
    }

    public int positionInYear() {
        return positionInYear;
    }

    public TimeQuantity numberOfDaysInLeapYear() {
        return numberOfDaysInLeapYear;
    }

    public TimeQuantity numberOfDaysInCommonYear() {
        return numberOfDaysInCommonYear;
    }

    @Override
    public boolean isGregorian() {
        return true;
    }

    /** Smalltalk {@code , yearNumber}. */
    @Override
    public GregorianMonthOfYear of(long yearNumber) {
        return GregorianYear.of(yearNumber).monthAt(positionInYear);
    }

    @Override
    public CalendarDayOfMonth<GregorianDate> dayOfMonthAt(int dayNumber) {
        return CalendarDayOfMonth.gregorianMonth(this, dayNumber);
    }

    public GregorianMonth next() {
        return next(TimeQuantity.of(1, TimeUnits.MONTH));
    }

    public GregorianMonth previous() {
        return previous(TimeQuantity.of(1, TimeUnits.MONTH));
    }

    public GregorianMonth next(TimeQuantity relativeTimeQuantity) {
        if (relativeTimeQuantity.isZero()) {
            return this;
        }
        if (relativeTimeQuantity.isNegative()) {
            return previous(relativeTimeQuantity.abs());
        }
        long numberOfMonths = relativeTimeQuantity.convertTo(TimeUnits.MONTH).numericalValue().longValueExact();
        return monthAt((int) ((positionInYear + numberOfMonths - 1) % 12) + 1);
    }

    public GregorianMonth previous(TimeQuantity relativeTimeQuantity) {
        if (relativeTimeQuantity.isZero()) {
            return this;
        }
        if (relativeTimeQuantity.isNegative()) {
            return next(relativeTimeQuantity.abs());
        }
        long numberOfMonths = relativeTimeQuantity.convertTo(TimeUnits.MONTH).numericalValue().longValueExact();
        return monthAt((int) (((positionInYear - numberOfMonths - 1) % 12 + 12) % 12) + 1);
    }

    @Override
    public CalendarDayOfMonth<GregorianDate> thirtieth() {
        if (numberOfDaysInLeapYear.compareTo(TimeQuantity.of(29, TimeUnits.DAY)) > 0) {
            return dayOfMonthAt(30);
        }
        throw Smalltalk.doesNotUnderstand(this, "thirtieth");
    }

    @Override
    public CalendarDayOfMonth<GregorianDate> thirtyFirst() {
        if (numberOfDaysInLeapYear.compareTo(TimeQuantity.of(30, TimeUnits.DAY)) > 0) {
            return dayOfMonthAt(31);
        }
        throw Smalltalk.doesNotUnderstand(this, "thirtyFirst");
    }

    public Month toMonth() {
        return Month.of(positionInYear);
    }

    @Override
    public void printAsISO8601On(StringBuilder stream) {
        stream.append(Smalltalk.padded(positionInYear, 2));
    }

    @Override
    public String toString() {
        return name;
    }
}
