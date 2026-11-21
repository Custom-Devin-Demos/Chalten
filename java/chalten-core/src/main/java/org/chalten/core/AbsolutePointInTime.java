package org.chalten.core;

import java.util.function.Function;
import org.chalten.measure.Fraction;
import org.chalten.measure.TimeQuantity;

/**
 * Smalltalk {@code AbsolutePointInTime}: a location-independent point on the universal timeline. Any two absolute
 * points in time compare and are equal by their microseconds since the Unix epoch.
 */
public sealed interface AbsolutePointInTime extends CalendarMagnitude<AbsolutePointInTime>
        permits Instant, TimeOffsetAwareGregorianDateTime {

    /** Smalltalk {@code Object>>isAbsolutePointInTime} / {@code AbsolutePointInTime>>isAbsolutePointInTime}. */
    static boolean isAbsolutePointInTime(Object object) {
        return object instanceof AbsolutePointInTime;
    }

    TimeQuantity microsecondsSinceEpoch();

    Instant asInstant();

    Fraction julianDayNumber();

    /** Smalltalk {@code withTimeOfDayDo:}. */
    <T> T withTimeOfDay(Function<TimeOfDay, T> block);

    @Override
    default int compareTo(AbsolutePointInTime other) {
        return microsecondsSinceEpoch().compareTo(other.microsecondsSinceEpoch());
    }

    /** Smalltalk {@code withGregorianDayMonthYearDo:}: {@code {dayNumber, monthNumber, yearNumber}}. */
    default long[] gregorianDayMonthYear() {
        return EdwardGrahamRichardsAlgorithm.instance().executeFor(julianDayNumber());
    }

    /** Smalltalk {@code printYearMonthDateOn:}; the year is printed unpadded. */
    default void printYearMonthDateOn(StringBuilder stream) {
        long[] dayMonthYear = gregorianDayMonthYear();
        stream.append(dayMonthYear[2]).append('-').append(Smalltalk.padded(dayMonthYear[1], 2)).append('-')
                .append(Smalltalk.padded(dayMonthYear[0], 2));
    }

    /** Smalltalk {@code printTimeOfDayOn:}. */
    default void printTimeOfDayOn(StringBuilder stream) {
        stream.append('T');
        withTimeOfDay(timeOfDay -> stream.append(timeOfDay));
    }
}
