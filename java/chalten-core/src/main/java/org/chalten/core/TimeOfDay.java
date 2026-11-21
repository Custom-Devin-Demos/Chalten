package org.chalten.core;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import org.chalten.localization.FormatterOptions;
import org.chalten.localization.LocalizedChronologyFormatter;
import org.chalten.localization.Translations;
import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnit;
import org.chalten.measure.TimeUnits;

/** Smalltalk {@code TimeOfDay}: a point in a 24-hour cycle, measured from the start of the day. */
public final class TimeOfDay implements CalendarMagnitude<TimeOfDay>, ISO8601Printable {

    static final String OUT_OF_RANGE = "A time of day must be between 0 and 24 hours";

    private static final TimeOfDay START_OF_DAY = new TimeOfDay(TimeQuantity.of(0, TimeUnits.MICROSECOND));
    private static final TimeOfDay HALF_OF_DAY = new TimeOfDay(TimeQuantity.of(12, TimeUnits.HOUR)
            .plus(TimeQuantity.of(0, TimeUnits.MINUTE)).plus(TimeQuantity.of(0, TimeUnits.SECOND))
            .plus(TimeQuantity.of(0, TimeUnits.MICROSECOND)));

    private final TimeQuantity timeFromStartOfDay;

    private TimeOfDay(TimeQuantity timeFromStartOfDay) {
        this.timeFromStartOfDay = timeFromStartOfDay;
    }

    public static TimeUnit hour() {
        return TimeUnits.HOUR;
    }

    public static TimeUnit minute() {
        return TimeUnits.MINUTE;
    }

    public static TimeUnit second() {
        return TimeUnits.SECOND;
    }

    public static TimeUnit microsecond() {
        return TimeUnits.MICROSECOND;
    }

    public static TimeOfDay startOfDay() {
        return START_OF_DAY;
    }

    public static TimeOfDay halfOfDay() {
        return HALF_OF_DAY;
    }

    public static TimeOfDay hours(long hours) {
        return hours(hours, 0);
    }

    public static TimeOfDay hours(long hours, long minutes) {
        return hours(hours, minutes, 0);
    }

    public static TimeOfDay hours(long hours, long minutes, long seconds) {
        return hours(hours, minutes, seconds, 0);
    }

    /**
     * Smalltalk {@code TimeOfDay class>>hours:minutes:seconds:microseconds:}.
     *
     * @throws InstanceCreationFailedException with every failed range check ({@code Hours must be in the range
     *         0-23}, ...), joined by a space
     */
    public static TimeOfDay hours(long hours, long minutes, long seconds, long microseconds) {
        List<String> failures = new ArrayList<>();
        if (hours < 0 || hours > 23) {
            failures.add("Hours must be in the range 0-23");
        }
        if (minutes < 0 || minutes > 59) {
            failures.add("Minutes must be in the range 0-59");
        }
        if (seconds < 0 || seconds > 59) {
            failures.add("Seconds must be in the range 0-59");
        }
        if (microseconds < 0 || microseconds > 999_999) {
            failures.add("Microseconds must be in the range 0-999999");
        }
        if (!failures.isEmpty()) {
            throw new InstanceCreationFailedException(String.join(" ", failures));
        }
        return offsetFromStartOfDay(TimeQuantity.of(hours, TimeUnits.HOUR)
                .plus(TimeQuantity.of(minutes, TimeUnits.MINUTE))
                .plus(TimeQuantity.of(seconds, TimeUnits.SECOND))
                .plus(TimeQuantity.of(microseconds, TimeUnits.MICROSECOND)));
    }

    /**
     * Smalltalk {@code TimeOfDay class>>offsetFromStartOfDay:}.
     *
     * @throws InstanceCreationFailedException {@code A time of day must be between 0 and 24 hours}
     */
    public static TimeOfDay offsetFromStartOfDay(TimeQuantity timeQuantity) {
        if (timeQuantity.isNegative() || timeQuantity.compareTo(TimeQuantity.of(24, TimeUnits.HOUR)) >= 0) {
            throw new InstanceCreationFailedException(Translations.localized(OUT_OF_RANGE));
        }
        return new TimeOfDay(timeQuantity);
    }

    public static TimeOfDay from(LocalTime time) {
        return hours(time.getHour(), time.getMinute(), time.getSecond(), time.getNano() / 1_000);
    }

    public TimeQuantity timeFromStartOfDay() {
        return timeFromStartOfDay;
    }

    public TimeOfDay plus(TimeQuantity timeQuantity) {
        return offsetFromStartOfDay(timeFromStartOfDay.plus(timeQuantity));
    }

    public TimeOfDay minus(TimeQuantity timeQuantity) {
        return offsetFromStartOfDay(timeFromStartOfDay.minus(timeQuantity));
    }

    public TimeQuantity hours() {
        return timeFromStartOfDay.convertTo(TimeUnits.HOUR).truncated();
    }

    public TimeQuantity minutes() {
        return timeFromStartOfDay.minus(hours()).convertTo(TimeUnits.MINUTE).truncated();
    }

    public TimeQuantity seconds() {
        return timeFromStartOfDay.minus(hours()).minus(minutes()).convertTo(TimeUnits.SECOND).truncated();
    }

    public TimeQuantity microseconds() {
        return timeFromStartOfDay.minus(hours()).minus(minutes()).minus(seconds()).convertTo(TimeUnits.MICROSECOND);
    }

    @Override
    public int compareTo(TimeOfDay other) {
        return timeFromStartOfDay.compareTo(other.timeFromStartOfDay);
    }

    /** Truncates sub-microsecond fractions. */
    public LocalTime toLocalTime() {
        long nanos = timeFromStartOfDay.convertTo(TimeUnits.MICROSECOND).numericalValue().times(1_000).floor()
                .longValueExact();
        return LocalTime.ofNanoOfDay(nanos);
    }

    @Override
    public void printAsISO8601On(StringBuilder stream) {
        stream.append('T').append(this);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof TimeOfDay other && timeFromStartOfDay.equals(other.timeFromStartOfDay);
    }

    @Override
    public int hashCode() {
        return timeFromStartOfDay.hashCode();
    }

    @Override
    public String toString() {
        return Smalltalk.padded(hours().numericalValue(), 2) + ':' + Smalltalk.padded(minutes().numericalValue(), 2)
                + ':' + Smalltalk.padded(seconds().numericalValue(), 2) + '.'
                + Smalltalk.padded(microseconds().numericalValue(), 6);
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
