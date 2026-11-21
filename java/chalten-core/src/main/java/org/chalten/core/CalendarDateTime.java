package org.chalten.core;

import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;

/**
 * Smalltalk {@code CalendarDateTime}: a naive date and time of day, independent of any offset.
 *
 * @param <T> the concrete date-time type
 * @param <D> the date type of the calendar
 */
public interface CalendarDateTime<T extends CalendarDateTime<T, D>, D extends CalendarDate<D, ?, ?>>
        extends CalendarMagnitude<T> {

    D date();

    TimeOfDay timeOfDay();

    GregorianDateTime asGregorianDateTime();

    /**
     * Smalltalk {@code CalendarDateTime class>>convert:toGregorianUsing:asReferencePointTo:}: converts a date-time of
     * a calendar whose days start at {@code date atMidnight} using a known pair of equivalent dates.
     */
    static <D extends CalendarDate<D, ?, ?>> GregorianDateTime convert(CalendarDateTime<?, D> dateTime,
            D fixedNonGregorianDate, GregorianDate fixedGregorianDate) {
        TimeQuantity numberOfDaysSinceFixedDate = dateTime.date().minus(fixedNonGregorianDate);
        TimeOfDay midnight = dateTime.date().atMidnight().timeOfDay();
        TimeQuantity midnightTime = midnight.timeFromStartOfDay();
        if (dateTime.timeOfDay().compareTo(midnight) < 0) {
            return fixedGregorianDate.next(numberOfDaysSinceFixedDate).at(dateTime.timeOfDay()
                    .plus(TimeQuantity.of(24, TimeUnits.HOUR).minus(midnightTime)));
        }
        return fixedGregorianDate.next(numberOfDaysSinceFixedDate.plus(TimeQuantity.of(1, TimeUnits.DAY)))
                .at(dateTime.timeOfDay().minus(midnightTime));
    }

    @Override
    default int compareTo(T other) {
        if (date().compareTo(other.date()) < 0
                || date().equals(other.date()) && timeOfDay().compareTo(other.timeOfDay()) < 0) {
            return -1;
        }
        return equals(other) ? 0 : 1;
    }

    default int dayNumber() {
        return date().dayNumber();
    }

    default CalendarMonth<D> month() {
        return date().month();
    }

    default int monthNumber() {
        return date().monthNumber();
    }

    default CalendarMonthOfYear<?, D, ?> monthOfYear() {
        return date().monthOfYear();
    }

    default CalendarYear<?, D, ?> year() {
        return date().year();
    }

    default long yearNumber() {
        return date().yearNumber();
    }

    default TimeOffsetAwareGregorianDateTime in(TimeOffsetRuleset ruleset) {
        return asGregorianDateTime().in(ruleset);
    }

    default TimeOffsetAwareGregorianDateTime inCoordinatedUniversalTime() {
        return asGregorianDateTime().inCoordinatedUniversalTime();
    }
}
