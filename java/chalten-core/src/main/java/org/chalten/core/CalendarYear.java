package org.chalten.core;

import org.chalten.measure.DiscreteInterval;
import org.chalten.measure.Fraction;
import org.chalten.measure.Steppable;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;

/**
 * Smalltalk {@code CalendarYear}: an abstract calendar year with its relative-time arithmetic.
 *
 * @param <Y> the concrete year type
 * @param <D> the date type of the calendar
 * @param <M> the month-of-year type of the calendar
 */
public interface CalendarYear<Y extends CalendarYear<Y, D, M>, D extends CalendarDate<D, M, Y>,
        M extends CalendarMonthOfYear<M, D, Y>> extends CalendarMagnitude<Y>, Steppable<Y> {

    String INVALID_NUMBER = "{1} is an invalid number for a year in the {2} calendar";
    String INCOMMENSURABLE = "Years in different time scales are incommensurable";
    String CANNOT_SUBTRACT = "Cannot subtract two years when the second one is greater than the first one.";

    long number();

    /** Smalltalk {@code self class number:} for {@code number}, validating it is an integer. */
    Y withNumber(Fraction number);

    D firstDate();

    D lastDate();

    boolean isLeapYear();

    TimeQuantity numberOfDays();

    TimeQuantity numberOfMonths();

    @SuppressWarnings("unchecked")
    private Y self() {
        return (Y) this;
    }

    @Override
    default int compareTo(Y other) {
        assertIsCommensurableWith(other);
        return Long.compare(number(), other.number());
    }

    /** @throws ArithmeticException {@code Years in different time scales are incommensurable} */
    default void assertIsCommensurableWith(Object other) {
        if (!getClass().isInstance(other)) {
            throw new ArithmeticException(org.chalten.localization.Translations.localized(INCOMMENSURABLE));
        }
    }

    @Override
    default boolean isComparableTo(TimeQuantity quantity) {
        return quantity.isCommensurableWith(TimeUnits.YEAR);
    }

    default Y next() {
        return withNumber(Fraction.of(number() + 1));
    }

    default Y next(TimeQuantity relativeTimeQuantity) {
        return withNumber(relativeTimeQuantity.convertTo(TimeUnits.YEAR).numericalValue().plus(number()));
    }

    default Y previous() {
        return withNumber(Fraction.of(number() - 1));
    }

    default Y previous(TimeQuantity relativeTimeQuantity) {
        return next(relativeTimeQuantity.negated());
    }

    /** Smalltalk {@code + aQuantity}. */
    @Override
    default Y plus(TimeQuantity quantity) {
        return next(quantity);
    }

    /** Smalltalk {@code - aQuantity}. */
    default Y minus(TimeQuantity quantity) {
        return previous(quantity);
    }

    /**
     * Smalltalk {@code self - aCalendarYear}: the years from {@code other} to this year.
     *
     * @throws ArithmeticException when {@code other} is after this year
     */
    @Override
    default TimeQuantity minus(Y other) {
        if (compareTo(other) < 0) {
            throw new ArithmeticException(org.chalten.localization.Translations.localized(CANNOT_SUBTRACT));
        }
        return TimeQuantity.of(number() - other.number(), TimeUnits.YEAR);
    }

    default DiscreteInterval<Y> to(Y end) {
        return to(end, TimeQuantity.of(1, TimeUnits.YEAR));
    }

    default DiscreteInterval<Y> to(Y end, TimeQuantity step) {
        return DiscreteInterval.from(self(), end, step);
    }
}
