package org.chalten.measure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 * Formats time periods (durations) in the ISO 8601 extended format, e.g. {@code P3Y5M4DT2H3M1.55S}: the port of
 * Chalten's {@code ISO8601DurationFormatter}.
 *
 * <p>Supported units, in output order: year, month, day, hour, minute and second. A single week quantity is formatted
 * on its own ({@code P1W}). Non-integer amounts are printed as Smalltalk prints their {@code asFloat}, and only the
 * lowest-order component may have one.
 */
public final class ISO8601DurationFormatter {

    static final String EMPTY_PERIODS = "Cannot format empty time periods";
    static final String INVALID_FRACTION = "A decimal fraction can only be applied to the lowest-order component";
    static final String INVALID_UNITS = "Cannot format time periods including invalid units";

    private static final List<TimeUnit> SUPPORTED_UNITS_IN_ORDER = List.of(
            TimeUnits.YEAR, TimeUnits.MONTH, TimeUnits.DAY, TimeUnits.HOUR, TimeUnits.MINUTE, TimeUnits.SECOND);

    public ISO8601DurationFormatter() {
    }

    /** Smalltalk {@code format: aQuantity}. */
    public String format(TimeQuantity timePeriod) {
        StringBuilder stream = new StringBuilder("P");
        if (timePeriod.unit() == TimeUnits.WEEK) {
            formatSingleTimePeriod(timePeriod, stream);
        } else {
            formatTimePeriods(List.of(timePeriod), stream);
        }
        return stream.toString();
    }

    /**
     * Smalltalk {@code format: aCollection}.
     *
     * @throws ChaltenAssertionException if the collection is empty, has a fraction before its last component, or has
     *         repeated or unsupported units
     */
    public String format(Collection<TimeQuantity> timePeriods) {
        StringBuilder stream = new StringBuilder("P");
        formatTimePeriods(timePeriods, stream);
        return stream.toString();
    }

    /** Formats the parts of {@code timePeriods}, as {@link #format(Collection)}. */
    public String format(CompoundTimeQuantity timePeriods) {
        return format(timePeriods.parts());
    }

    private static void formatTimePeriods(Collection<TimeQuantity> timePeriods, StringBuilder stream) {
        if (timePeriods.isEmpty()) {
            throw new ChaltenAssertionException(EMPTY_PERIODS);
        }
        List<TimeQuantity> remainingPeriods = new ArrayList<>(timePeriods);
        boolean switchToTimeSection = false;
        for (TimeUnit unit : SUPPORTED_UNITS_IN_ORDER) {
            if (remainingPeriods.isEmpty()) {
                break;
            }
            if (unit == TimeUnits.HOUR) {
                switchToTimeSection = true;
            }
            TimeQuantity period = detect(remainingPeriods, unit);
            if (period != null) {
                remainingPeriods.remove(period);
                if (switchToTimeSection) {
                    stream.append('T');
                    switchToTimeSection = false;
                }
                if (!remainingPeriods.isEmpty() && !period.amount().isInteger()) {
                    throw new ChaltenAssertionException(INVALID_FRACTION);
                }
                formatSingleTimePeriod(period, stream);
            }
        }
        if (!remainingPeriods.isEmpty()) {
            throw new ChaltenAssertionException(INVALID_UNITS);
        }
    }

    private static TimeQuantity detect(List<TimeQuantity> periods, TimeUnit unit) {
        for (TimeQuantity period : periods) {
            if (period.unit() == unit) {
                return period;
            }
        }
        return null;
    }

    private static void formatSingleTimePeriod(TimeQuantity timePeriod, StringBuilder stream) {
        Fraction number = timePeriod.amount();
        stream.append(number.isInteger() ? number.toString() : SmalltalkFloatPrinter.print(number.doubleValue()));
        stream.append(timePeriod.unit().symbol().substring(0, 1).toUpperCase(Locale.ROOT));
    }
}
