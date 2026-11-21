package org.chalten.core;

import java.time.YearMonth;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import org.chalten.localization.FormatterOptions;
import org.chalten.localization.LocalizedChronologyFormatter;
import org.chalten.localization.Translations;
import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;

/** Smalltalk {@code GregorianMonthOfYear}: a Gregorian month in a specific year. */
public final class GregorianMonthOfYear
        implements CalendarMonthOfYear<GregorianMonthOfYear, GregorianDate, GregorianYear>, ISO8601Printable {

    static final String CANNOT_SUBTRACT =
            "Cannot subtract two months of year when the start month is greater than the end month.";

    private final GregorianYear year;
    private final GregorianMonth month;

    GregorianMonthOfYear(GregorianYear year, GregorianMonth month) {
        this.year = year;
        this.month = month;
    }

    /** Smalltalk {@code GregorianMonthOfYear class>>year:month:}. */
    public static GregorianMonthOfYear year(GregorianYear year, GregorianMonth month) {
        return Objects.requireNonNull(year, "year").monthAt(month.positionInYear());
    }

    public static GregorianMonthOfYear from(YearMonth yearMonth) {
        return GregorianYear.number(yearMonth.getYear()).monthAt(yearMonth.getMonthValue());
    }

    @Override
    public GregorianYear year() {
        return year;
    }

    @Override
    public GregorianMonth month() {
        return month;
    }

    @Override
    public TimeQuantity numberOfDays() {
        return year.isLeapYear() ? month.numberOfDaysInLeapYear() : month.numberOfDaysInCommonYear();
    }

    /**
     * Smalltalk {@code dateAt:}.
     *
     * @throws InstanceCreationFailedException {@code <n> is an invalid day number in <month>, <year>}
     */
    @Override
    public GregorianDate dateAt(int number) {
        if (number < 1 || number > numberOfDays().numericalValue().intValueExact()) {
            throw new InstanceCreationFailedException(
                    Translations.localizedWithAll(CalendarDate.INVALID_DAY_NUMBER, number, this));
        }
        return GregorianDate.monthOfYear(this, number);
    }

    @Override
    public GregorianDate twentyNinth() {
        if (numberOfDays().compareTo(TimeQuantity.of(28, TimeUnits.DAY)) > 0) {
            return dateAt(29);
        }
        return CalendarMonthOfYear.super.twentyNinth();
    }

    @Override
    public GregorianDate thirtyFirst() {
        if (numberOfDays().compareTo(TimeQuantity.of(30, TimeUnits.DAY)) > 0) {
            return dateAt(31);
        }
        return CalendarMonthOfYear.super.thirtyFirst();
    }

    @Override
    public GregorianMonthOfYear next() {
        return CalendarMonthOfYear.super.next();
    }

    @Override
    public GregorianMonthOfYear next(TimeQuantity relativeTimeQuantity) {
        if (relativeTimeQuantity.isZero()) {
            return this;
        }
        if (relativeTimeQuantity.isNegative()) {
            return previous(relativeTimeQuantity.abs());
        }
        return when(relativeTimeQuantity,
                quantityInYears -> year.next(quantityInYears).monthAt(month.positionInYear()),
                (quantityInYears, quantityInMonths) -> {
                    GregorianMonth newMonth = month.next(quantityInMonths);
                    GregorianYear newYear = newMonth.positionInYear() < month.positionInYear()
                            ? year.next(quantityInYears.plus(TimeQuantity.of(1, TimeUnits.YEAR)))
                            : year.next(quantityInYears);
                    return newYear.monthAt(newMonth.positionInYear());
                });
    }

    @Override
    public GregorianMonthOfYear previous(TimeQuantity relativeTimeQuantity) {
        if (relativeTimeQuantity.isZero()) {
            return this;
        }
        if (relativeTimeQuantity.isNegative()) {
            return next(relativeTimeQuantity.abs());
        }
        return when(relativeTimeQuantity,
                quantityInYears -> year.previous(quantityInYears).monthAt(month.positionInYear()),
                (quantityInYears, quantityInMonths) -> {
                    GregorianMonth newMonth = month.previous(quantityInMonths);
                    GregorianYear newYear = newMonth.positionInYear() > month.positionInYear()
                            ? year.previous(quantityInYears.plus(TimeQuantity.of(1, TimeUnits.YEAR)))
                            : year.previous(quantityInYears);
                    return newYear.monthAt(newMonth.positionInYear());
                });
    }

    /** Smalltalk {@code when:isWholeYearsDo:otherwise:}. */
    private static GregorianMonthOfYear when(TimeQuantity relativeTimeQuantity,
            Function<TimeQuantity, GregorianMonthOfYear> wholeBlock,
            BiFunction<TimeQuantity, TimeQuantity, GregorianMonthOfYear> fractionalBlock) {
        TimeQuantity years = relativeTimeQuantity.convertTo(TimeUnits.YEAR);
        TimeQuantity wholeYears = years.truncated();
        TimeQuantity remaining = years.minus(wholeYears);
        return remaining.isZero()
                ? wholeBlock.apply(wholeYears)
                : fractionalBlock.apply(wholeYears, remaining.convertTo(TimeUnits.MONTH));
    }

    /** Smalltalk {@code + aQuantity}, as {@code next:}. */
    public GregorianMonthOfYear plus(TimeQuantity relativeTimeQuantity) {
        return next(relativeTimeQuantity);
    }

    /** Smalltalk {@code - aQuantity}, as {@code previous:}. */
    public GregorianMonthOfYear minus(TimeQuantity relativeTimeQuantity) {
        return previous(relativeTimeQuantity);
    }

    /**
     * Smalltalk {@code self - start} ({@code subtractionFromGregorianMonthOfYear:}): the relative time from
     * {@code start} to this month.
     *
     * @throws ArithmeticException when {@code start} is after this month
     */
    public TimeQuantity minus(GregorianMonthOfYear start) {
        if (equals(start)) {
            return TimeQuantity.of(0, TimeUnits.MONTH);
        }
        if (compareTo(start) <= 0) {
            throw new ArithmeticException(Translations.localized(CANNOT_SUBTRACT));
        }
        TimeQuantity yearsDifference = year.minus(start.year);
        int monthsDifference = month.positionInYear() - start.month.positionInYear();
        return yearsDifference.plus(TimeQuantity.of(monthsDifference, TimeUnits.MONTH));
    }

    @Override
    public int compareTo(GregorianMonthOfYear other) {
        int byYear = year.compareTo(other.year);
        return byYear != 0 ? byYear : Integer.compare(month.positionInYear(), other.month.positionInYear());
    }

    public YearMonth toYearMonth() {
        return YearMonth.of(Math.toIntExact(year.number()), month.positionInYear());
    }

    @Override
    public void printAsISO8601On(StringBuilder stream) {
        year.printAsISO8601On(stream);
        stream.append('-');
        month.printAsISO8601On(stream);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof GregorianMonthOfYear other && year.equals(other.year) && month == other.month;
    }

    @Override
    public int hashCode() {
        return 31 * year.hashCode() + month.hashCode();
    }

    @Override
    public String toString() {
        return month + ", " + year;
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
