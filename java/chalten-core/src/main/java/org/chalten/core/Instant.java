package org.chalten.core;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import org.chalten.localization.FormatterOptions;
import org.chalten.localization.LocalizedChronologyFormatter;
import org.chalten.measure.Fraction;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnit;
import org.chalten.measure.TimeUnits;

/**
 * Smalltalk {@code Instant}: a point on the universal timeline, as an exact quantity of microseconds since the Unix
 * epoch (January 1st, 1970 at 00:00:00 UTC).
 */
public final class Instant implements AbsolutePointInTime {

    private static final Fraction JULIAN_DAY_NUMBER_OF_EPOCH = Fraction.of(2440587).plus(Fraction.of(1, 2));
    private static final Instant EPOCH = microsecondsSinceEpoch(TimeQuantity.of(0, TimeUnits.MICROSECOND));

    private final TimeQuantity microsecondsSinceEpoch;

    private Instant(TimeQuantity microsecondsSinceEpoch) {
        this.microsecondsSinceEpoch = Objects.requireNonNull(microsecondsSinceEpoch, "microsecondsSinceEpoch");
    }

    public static TimeUnit day() {
        return TimeUnits.DAY;
    }

    public static TimeUnit second() {
        return TimeUnits.SECOND;
    }

    public static TimeUnit millisecond() {
        return TimeUnits.MILLISECOND;
    }

    public static TimeUnit microsecond() {
        return TimeUnits.MICROSECOND;
    }

    /** Smalltalk {@code Instant class>>julianDayNumberOfEpoch}: {@code 2440587 + (1/2)}. */
    public static Fraction julianDayNumberOfEpoch() {
        return JULIAN_DAY_NUMBER_OF_EPOCH;
    }

    public static Instant epoch() {
        return EPOCH;
    }

    public static Instant now() {
        java.time.Instant now = java.time.Instant.now();
        return microsecondsSinceEpoch(TimeQuantity.of(
                Math.addExact(Math.multiplyExact(now.getEpochSecond(), 1_000_000L), now.getNano() / 1_000),
                TimeUnits.MICROSECOND));
    }

    /** Smalltalk {@code Instant class>>microsecondsSinceEpoch:}; the quantity is kept as given. */
    public static Instant microsecondsSinceEpoch(TimeQuantity microsecondsSinceEpoch) {
        return new Instant(microsecondsSinceEpoch);
    }

    public static Instant secondsSinceEpoch(TimeQuantity secondsSinceEpoch) {
        return microsecondsSinceEpoch(secondsSinceEpoch.convertTo(TimeUnits.MICROSECOND));
    }

    public static Instant daysSinceEpoch(TimeQuantity daysSinceEpoch) {
        return microsecondsSinceEpoch(daysSinceEpoch.convertTo(TimeUnits.MICROSECOND));
    }

    public static Instant fromUnixTime(long numberOfSecondsSinceEpoch) {
        return secondsSinceEpoch(TimeQuantity.of(numberOfSecondsSinceEpoch, TimeUnits.SECOND));
    }

    public static Instant fromUnixTime(Fraction numberOfSecondsSinceEpoch) {
        return secondsSinceEpoch(TimeQuantity.of(numberOfSecondsSinceEpoch, TimeUnits.SECOND));
    }

    public static Instant fromJulianDayNumber(Fraction julianDayNumber) {
        return secondsSinceEpoch(TimeQuantity.of(julianDayNumber.minus(JULIAN_DAY_NUMBER_OF_EPOCH), TimeUnits.DAY));
    }

    /** Truncates sub-microsecond precision. */
    public static Instant from(java.time.Instant instant) {
        long micros = Math.addExact(Math.multiplyExact(instant.getEpochSecond(), 1_000_000L),
                instant.getNano() / 1_000);
        return microsecondsSinceEpoch(TimeQuantity.of(micros, TimeUnits.MICROSECOND));
    }

    @Override
    public TimeQuantity microsecondsSinceEpoch() {
        return microsecondsSinceEpoch;
    }

    /** Smalltalk {@code milisecondsSinceEpoch} (sic). */
    public TimeQuantity milisecondsSinceEpoch() {
        return millisecondsSinceEpoch();
    }

    public TimeQuantity millisecondsSinceEpoch() {
        return microsecondsSinceEpoch.convertTo(TimeUnits.MILLISECOND);
    }

    public TimeQuantity secondsSinceEpoch() {
        return microsecondsSinceEpoch.convertTo(TimeUnits.SECOND);
    }

    public TimeQuantity daysSinceEpoch() {
        return microsecondsSinceEpoch.convertTo(TimeUnits.DAY);
    }

    /** Smalltalk {@code asUnixTime}: whole seconds since the epoch, truncated towards zero. */
    public Fraction asUnixTime() {
        return secondsSinceEpoch().truncated().numericalValue();
    }

    @Override
    public Fraction julianDayNumber() {
        return JULIAN_DAY_NUMBER_OF_EPOCH.plus(daysSinceEpoch().numericalValue());
    }

    @Override
    public Instant asInstant() {
        return this;
    }

    public Instant next(TimeQuantity timePeriod) {
        return microsecondsSinceEpoch(microsecondsSinceEpoch.plus(timePeriod.convertTo(TimeUnits.MICROSECOND)));
    }

    public Instant previous(TimeQuantity timePeriod) {
        return microsecondsSinceEpoch(microsecondsSinceEpoch.minus(timePeriod.convertTo(TimeUnits.MICROSECOND)));
    }

    public TimeOffsetAwareGregorianDateTime asGregorianDateTimeIn(TimeOffsetRuleset ruleset) {
        return TimeOffsetAwareGregorianDateTime.basedOn(this, ruleset);
    }

    public TimeOffsetAwareGregorianDateTime asGregorianDateTimeInCoordinatedUniversalTime() {
        return TimeOffsetAwareGregorianDateTime.at(this);
    }

    @Override
    public <T> T withTimeOfDay(Function<TimeOfDay, T> block) {
        TimeQuantity difference = microsecondsSinceEpoch.minus(daysSinceEpoch().truncated());
        return block.apply(difference.isNegative()
                ? TimeOfDay.offsetFromStartOfDay(difference.plus(TimeQuantity.of(1, TimeUnits.DAY)))
                : TimeOfDay.offsetFromStartOfDay(difference));
    }

    /** Truncates sub-microsecond fractions. */
    public java.time.Instant toJavaInstant() {
        Fraction micros = microsecondsSinceEpoch.convertTo(TimeUnits.MICROSECOND).numericalValue();
        long wholeMicros = micros.floor().longValueExact();
        return java.time.Instant.ofEpochSecond(Math.floorDiv(wholeMicros, 1_000_000L),
                Math.floorMod(wholeMicros, 1_000_000L) * 1_000);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof AbsolutePointInTime other
                && microsecondsSinceEpoch.equals(other.microsecondsSinceEpoch());
    }

    @Override
    public int hashCode() {
        return microsecondsSinceEpoch.hashCode();
    }

    @Override
    public String toString() {
        StringBuilder stream = new StringBuilder();
        printYearMonthDateOn(stream);
        printTimeOfDayOn(stream);
        return stream.append('Z').toString();
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
