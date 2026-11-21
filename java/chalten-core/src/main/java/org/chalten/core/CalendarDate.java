package org.chalten.core;

import org.chalten.localization.Translations;
import org.chalten.measure.DiscreteInterval;
import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.measure.Steppable;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;

/**
 * Smalltalk {@code CalendarDate}: a calendar day identified by its year, month and day of month. The day arithmetic
 * ({@code next:}, {@code previous:}, {@code -}) is calendar independent and implemented here exactly as in Smalltalk.
 *
 * @param <D> the concrete date type
 * @param <M> the month-of-year type of the calendar
 * @param <Y> the year type of the calendar
 */
public interface CalendarDate<D extends CalendarDate<D, M, Y>, M extends CalendarMonthOfYear<M, D, Y>,
        Y extends CalendarYear<Y, D, M>> extends CalendarMagnitude<D>, Steppable<D> {

    String INVALID_DAY_NUMBER = "{1} is an invalid day number in {2}";
    String CANNOT_SUBTRACT = "Cannot subtract two dates when the second one is greater than the first one.";

    /**
     * Smalltalk {@code CalendarDate class>>assert:isValidIn:}.
     *
     * @throws InstanceCreationFailedException {@code <n> is an invalid day number in <month of year>}
     */
    static void assertIsValid(int dayNumber, CalendarMonthOfYear<?, ?, ?> monthOfYear) {
        if (dayNumber < 1 || dayNumber > monthOfYear.numberOfDays().numericalValue().intValueExact()) {
            throw new InstanceCreationFailedException(
                    Translations.localizedWithAll(INVALID_DAY_NUMBER, dayNumber, monthOfYear));
        }
    }

    int dayNumber();

    M monthOfYear();

    int monthNumber();

    CalendarDayOfWeek dayOfWeek();

    CalendarDateTime<?, D> at(TimeOfDay timeOfDay);

    CalendarDateTime<?, D> atMidnight();

    CalendarDateTime<?, D> atNoon();

    default CalendarDateTime<?, D> atStartOfDay() {
        return at(TimeOfDay.startOfDay());
    }

    default Y year() {
        return monthOfYear().year();
    }

    default long yearNumber() {
        return year().number();
    }

    default CalendarMonth<D> month() {
        return monthOfYear().month();
    }

    default boolean isLastDayOfMonth() {
        return monthOfYear().last().equals(this);
    }

    @SuppressWarnings("unchecked")
    private D self() {
        return (D) this;
    }

    private static TimeQuantity oneDay() {
        return TimeQuantity.of(1, TimeUnits.DAY);
    }

    @Override
    default boolean isComparableTo(TimeQuantity quantity) {
        return quantity.isCommensurableWith(TimeUnits.DAY);
    }

    default D next() {
        int nextDayNumber = dayNumber() + 1;
        return nextDayNumber > monthOfYear().numberOfDays().numericalValue().intValueExact()
                ? monthOfYear().next().first()
                : monthOfYear().dateAt(nextDayNumber);
    }

    default D previous() {
        int previousDayNumber = dayNumber() - 1;
        return previousDayNumber == 0 ? monthOfYear().previous().last() : monthOfYear().dateAt(previousDayNumber);
    }

    /** Smalltalk {@code next: timeQuantity}; the year-by-year recursion is unrolled into a loop. */
    default D next(TimeQuantity timeQuantity) {
        D date = self();
        TimeQuantity quantity = timeQuantity;
        while (true) {
            if (quantity.isZero()) {
                return date;
            }
            if (quantity.isNegative()) {
                return date.previous(quantity.abs());
            }
            TimeQuantity daysUntilEndOfYear = date.year().lastDate().minus(date);
            if (quantity.compareTo(daysUntilEndOfYear) <= 0) {
                return date.onSameYearNext(quantity);
            }
            TimeQuantity remainingDays = quantity.minus(daysUntilEndOfYear);
            date = date.year().next().firstDate();
            quantity = remainingDays.minus(oneDay());
        }
    }

    /** Smalltalk {@code onSameYearNext:}. */
    default D onSameYearNext(TimeQuantity timeQuantity) {
        TimeQuantity daysUntilEndOfMonth = monthOfYear().last().minus(self());
        if (timeQuantity.compareTo(daysUntilEndOfMonth) <= 0) {
            return monthOfYear().dateAt(
                    timeQuantity.convertTo(TimeUnits.DAY).numericalValue().plus(dayNumber()).intValueExact());
        }
        TimeQuantity remainingDays = timeQuantity.minus(daysUntilEndOfMonth);
        return monthOfYear().next().first().next(remainingDays.minus(oneDay()));
    }

    /** Smalltalk {@code previous: timeQuantity}; the year-by-year recursion is unrolled into a loop. */
    default D previous(TimeQuantity timeQuantity) {
        D date = self();
        TimeQuantity quantity = timeQuantity;
        while (true) {
            if (quantity.isZero()) {
                return date;
            }
            if (quantity.isNegative()) {
                return date.next(quantity.abs());
            }
            TimeQuantity daysFromStartOfYear = date.minus(date.year().firstDate());
            if (quantity.compareTo(daysFromStartOfYear) < 0) {
                return date.onSameYearPrevious(quantity);
            }
            TimeQuantity remainingDays = quantity.minus(daysFromStartOfYear).minus(oneDay());
            date = date.year().previous().lastDate();
            quantity = remainingDays;
        }
    }

    /** Smalltalk {@code onSameYearPrevious:}. */
    default D onSameYearPrevious(TimeQuantity timeQuantity) {
        TimeQuantity daysFromStartOfMonth = TimeQuantity.of(dayNumber(), TimeUnits.DAY);
        if (timeQuantity.compareTo(daysFromStartOfMonth) < 0) {
            return monthOfYear().dateAt(org.chalten.measure.Fraction.of(dayNumber())
                    .minus(timeQuantity.convertTo(TimeUnits.DAY).numericalValue()).intValueExact());
        }
        TimeQuantity remainingDays = timeQuantity.minus(daysFromStartOfMonth);
        return monthOfYear().previous().last().previous(remainingDays);
    }

    /** Smalltalk {@code + aTimeQuantity}. */
    @Override
    default D plus(TimeQuantity timeQuantity) {
        return next(timeQuantity);
    }

    /** Smalltalk {@code - aTimeQuantity}. */
    default D minus(TimeQuantity timeQuantity) {
        return previous(timeQuantity);
    }

    /**
     * Smalltalk {@code self - startDate} ({@code subtractionFromCalendarDate:}): the days from {@code startDate} to
     * this date.
     *
     * @throws ArithmeticException when {@code startDate} is after this date
     */
    @Override
    default TimeQuantity minus(D startDate) {
        D endDate = self();
        if (endDate.equals(startDate)) {
            return TimeQuantity.of(0, TimeUnits.DAY);
        }
        if (endDate.compareTo(startDate) <= 0) {
            throw new ArithmeticException(Translations.localized(CANNOT_SUBTRACT));
        }
        if (endDate.monthOfYear().equals(startDate.monthOfYear())) {
            return TimeQuantity.of(endDate.dayNumber() - startDate.dayNumber(), TimeUnits.DAY);
        }
        if (endDate.year().equals(startDate.year())) {
            TimeQuantity intermediateDayCount = oneDay();
            M currentMonthOfYear = startDate.monthOfYear().next();
            while (currentMonthOfYear.compareTo(endDate.monthOfYear()) < 0) {
                intermediateDayCount = intermediateDayCount.plus(currentMonthOfYear.numberOfDays());
                currentMonthOfYear = currentMonthOfYear.next();
            }
            return startDate.monthOfYear().last().minus(startDate).plus(intermediateDayCount)
                    .plus(endDate.minus(endDate.monthOfYear().first()));
        }
        Y currentYear = startDate.year().next();
        TimeQuantity currentCount = oneDay();
        while (currentYear.compareTo(endDate.year()) < 0) {
            currentCount = currentCount.plus(currentYear.numberOfDays());
            currentYear = currentYear.next();
        }
        return startDate.year().lastDate().minus(startDate).plus(currentCount)
                .plus(endDate.minus(endDate.year().firstDate()));
    }

    default DiscreteInterval<D> to(D end) {
        return to(end, oneDay());
    }

    default DiscreteInterval<D> to(D end, TimeQuantity step) {
        return DiscreteInterval.from(self(), end, step);
    }
}
