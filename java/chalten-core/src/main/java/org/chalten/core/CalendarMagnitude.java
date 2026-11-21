package org.chalten.core;

import org.chalten.measure.TimeUnit;
import org.chalten.measure.TimeUnits;

/**
 * Smalltalk {@code CalendarMagnitude} (a {@code Magnitude}): the ordering protocol shared by calendar values.
 *
 * @param <T> the type the values are compared with
 */
public interface CalendarMagnitude<T> extends Comparable<T> {

    /** Smalltalk {@code CalendarMagnitude class>>day}. */
    static TimeUnit day() {
        return TimeUnits.DAY;
    }

    /** Smalltalk {@code CalendarMagnitude class>>month}. */
    static TimeUnit month() {
        return TimeUnits.MONTH;
    }

    /** Smalltalk {@code CalendarMagnitude class>>year}. */
    static TimeUnit year() {
        return TimeUnits.YEAR;
    }

    /** Smalltalk {@code <}. */
    default boolean isLessThan(T other) {
        return compareTo(other) < 0;
    }

    /** Smalltalk {@code <=}. */
    default boolean isLessThanOrEqualTo(T other) {
        return compareTo(other) <= 0;
    }

    /** Smalltalk {@code >}. */
    default boolean isGreaterThan(T other) {
        return compareTo(other) > 0;
    }

    /** Smalltalk {@code >=}. */
    default boolean isGreaterThanOrEqualTo(T other) {
        return compareTo(other) >= 0;
    }

    /** Smalltalk {@code between:and:}. */
    default boolean between(T min, T max) {
        return compareTo(min) >= 0 && compareTo(max) <= 0;
    }
}
