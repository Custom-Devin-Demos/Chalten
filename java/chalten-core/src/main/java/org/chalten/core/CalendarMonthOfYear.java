package org.chalten.core;

import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;

/**
 * Smalltalk {@code CalendarMonthOfYear}: a calendar month in a specific year.
 *
 * @param <M> the concrete month-of-year type
 * @param <D> the date type of the calendar
 * @param <Y> the year type of the calendar
 */
public interface CalendarMonthOfYear<M extends CalendarMonthOfYear<M, D, Y>, D extends CalendarDate<D, M, Y>,
        Y extends CalendarYear<Y, D, M>> extends CalendarMagnitude<M> {

    D dateAt(int number);

    CalendarMonth<D> month();

    Y year();

    TimeQuantity numberOfDays();

    M next(TimeQuantity relativeTimeQuantity);

    M previous(TimeQuantity relativeTimeQuantity);

    default M next() {
        return next(TimeQuantity.of(1, TimeUnits.MONTH));
    }

    default M previous() {
        return previous(TimeQuantity.of(1, TimeUnits.MONTH));
    }

    default D last() {
        return dateAt(numberOfDays().numericalValue().intValueExact());
    }

    default D first() {
        return dateAt(1);
    }

    default D second() {
        return dateAt(2);
    }

    default D third() {
        return dateAt(3);
    }

    default D fourth() {
        return dateAt(4);
    }

    default D fifth() {
        return dateAt(5);
    }

    default D sixth() {
        return dateAt(6);
    }

    default D seventh() {
        return dateAt(7);
    }

    default D eighth() {
        return dateAt(8);
    }

    default D ninth() {
        return dateAt(9);
    }

    default D tenth() {
        return dateAt(10);
    }

    default D eleventh() {
        return dateAt(11);
    }

    default D twelfth() {
        return dateAt(12);
    }

    default D thirteenth() {
        return dateAt(13);
    }

    default D fourteenth() {
        return dateAt(14);
    }

    default D fifteenth() {
        return dateAt(15);
    }

    default D sixteenth() {
        return dateAt(16);
    }

    default D seventeenth() {
        return dateAt(17);
    }

    default D eighteenth() {
        return dateAt(18);
    }

    default D nineteenth() {
        return dateAt(19);
    }

    default D twentieth() {
        return dateAt(20);
    }

    default D twentyFirst() {
        return dateAt(21);
    }

    default D twentySecond() {
        return dateAt(22);
    }

    default D twentyThird() {
        return dateAt(23);
    }

    default D twentyFourth() {
        return dateAt(24);
    }

    default D twentyFifth() {
        return dateAt(25);
    }

    default D twentySixth() {
        return dateAt(26);
    }

    default D twentySeventh() {
        return dateAt(27);
    }

    default D twentyEighth() {
        return dateAt(28);
    }

    /**
     * Smalltalk {@code twentyNinth}, only understood by months with more than 28 days.
     *
     * @throws UnsupportedOperationException (Smalltalk {@code MessageNotUnderstood}) otherwise
     */
    default D twentyNinth() {
        throw Smalltalk.doesNotUnderstand(this, "twentyNinth");
    }

    /**
     * Smalltalk {@code thirtieth}.
     *
     * @throws UnsupportedOperationException (Smalltalk {@code MessageNotUnderstood}) for months of 29 days or less
     */
    default D thirtieth() {
        if (numberOfDays().compareTo(TimeQuantity.of(29, TimeUnits.DAY)) > 0) {
            return dateAt(30);
        }
        throw Smalltalk.doesNotUnderstand(this, "thirtieth");
    }

    /**
     * Smalltalk {@code thirtyFirst}, only understood by months with 31 days.
     *
     * @throws UnsupportedOperationException (Smalltalk {@code MessageNotUnderstood}) otherwise
     */
    default D thirtyFirst() {
        throw Smalltalk.doesNotUnderstand(this, "thirtyFirst");
    }
}
