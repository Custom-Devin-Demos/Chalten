package org.chalten.core;

import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;

/**
 * Smalltalk {@code TimeOffsetRuleset}: a ruleset defining the offset to UTC at each instant. Implemented by
 * {@link FixedOffsetRuleset} here and by the IANA time zone rulesets of the {@code chalten-timezones} module.
 */
public interface TimeOffsetRuleset {

    TimeOffsetAwareGregorianDateTime convertGregorianDateTime(GregorianDateTime dateTime);

    String identifier();

    TimeQuantity offsetToUniversalCoordinatedTimeAt(Instant instant);

    /** Smalltalk {@code convertToGregorianDate:withOffset:}. */
    default TimeOffsetAwareGregorianDateTime convertToGregorianDate(GregorianDateTime dateTime,
            TimeQuantity offsetToUtc) {
        return Instant.fromJulianDayNumber(dateTime.julianDayNumber()
                .minus(offsetToUtc.convertTo(TimeUnits.DAY).numericalValue())).asGregorianDateTimeIn(this);
    }

    /** Smalltalk {@code printOffset:on:}: {@code ±HH:MM}, plus {@code :SS} when there are seconds. */
    default void printOffset(TimeQuantity offset, StringBuilder stream) {
        stream.append(offset.isNegative() ? '-' : '+');
        TimeQuantity absoluteOffset = offset.abs();
        TimeQuantity hours = absoluteOffset.convertTo(TimeUnits.HOUR).truncated();
        stream.append(Smalltalk.padded(hours.numericalValue(), 2)).append(':');
        TimeQuantity minutes = absoluteOffset.minus(hours).convertTo(TimeUnits.MINUTE).truncated();
        stream.append(Smalltalk.padded(minutes.numericalValue(), 2));
        TimeQuantity seconds = absoluteOffset.minus(hours).minus(minutes).convertTo(TimeUnits.SECOND);
        if (seconds.numericalValue().isStrictlyPositive()) {
            stream.append(':').append(Smalltalk.padded(seconds.numericalValue(), 2));
        }
    }
}
