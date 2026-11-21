package org.chalten.core;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.chalten.measure.Fraction;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;

/** Smalltalk {@code TimeOffsetAwareGregorianDateTime}: an instant seen through a UTC offset. */
public final class TimeOffsetAwareGregorianDateTime implements AbsolutePointInTime {

    private final Instant instant;
    private final TimeQuantity appliedOffset;
    private final Optional<TimeOffsetRuleset> ruleset;

    private TimeOffsetAwareGregorianDateTime(Instant instant, Optional<TimeOffsetRuleset> ruleset,
            TimeQuantity appliedOffset) {
        this.instant = Objects.requireNonNull(instant, "instant");
        this.ruleset = Objects.requireNonNull(ruleset, "ruleset");
        this.appliedOffset = Objects.requireNonNull(appliedOffset, "appliedOffset");
    }

    /** Smalltalk {@code at: anInstant}: the instant in UTC, without a ruleset. */
    public static TimeOffsetAwareGregorianDateTime at(Instant instant) {
        return at(instant, Optional.empty(), TimeQuantity.of(0, TimeUnits.DAY));
    }

    /** Smalltalk {@code at:accordingTo:shiftedBy:}. */
    public static TimeOffsetAwareGregorianDateTime at(Instant instant, Optional<TimeOffsetRuleset> ruleset,
            TimeQuantity offset) {
        return new TimeOffsetAwareGregorianDateTime(instant, ruleset, offset);
    }

    /** Smalltalk {@code basedOn:accordingTo:}. */
    public static TimeOffsetAwareGregorianDateTime basedOn(Instant instant, TimeOffsetRuleset ruleset) {
        TimeQuantity offset = ruleset.offsetToUniversalCoordinatedTimeAt(instant);
        return at(instant, Optional.of(ruleset), offset);
    }

    public static TimeOffsetAwareGregorianDateTime from(OffsetDateTime dateTime) {
        return Instant.from(dateTime.toInstant()).asGregorianDateTimeIn(FixedOffsetRuleset.from(dateTime.getOffset()));
    }

    @Override
    public Instant asInstant() {
        return instant;
    }

    public TimeQuantity appliedOffset() {
        return appliedOffset;
    }

    public Optional<TimeOffsetRuleset> ruleset() {
        return ruleset;
    }

    @Override
    public TimeQuantity microsecondsSinceEpoch() {
        return instant.microsecondsSinceEpoch();
    }

    @Override
    public Fraction julianDayNumber() {
        return instant.next(appliedOffset).julianDayNumber();
    }

    /** Smalltalk {@code asRelativePointInTime}: the naive Gregorian date-time at the applied offset. */
    public GregorianDateTime asRelativePointInTime() {
        long[] dayMonthYear = gregorianDayMonthYear();
        GregorianDate date = GregorianYear.of(dayMonthYear[2]).monthAt((int) dayMonthYear[1])
                .dateAt((int) dayMonthYear[0]);
        return withTimeOfDay(date::at);
    }

    public TimeOffsetAwareGregorianDateTime in(TimeOffsetRuleset aRuleset) {
        return instant.asGregorianDateTimeIn(aRuleset);
    }

    public TimeOffsetAwareGregorianDateTime inCoordinatedUniversalTime() {
        return instant.asGregorianDateTimeInCoordinatedUniversalTime();
    }

    @Override
    public <T> T withTimeOfDay(Function<TimeOfDay, T> block) {
        return instant.next(appliedOffset).withTimeOfDay(block);
    }

    /** Requires a whole-second offset; truncates sub-microsecond fractions. */
    public OffsetDateTime toOffsetDateTime() {
        ZoneOffset offset = ZoneOffset.ofTotalSeconds(
                appliedOffset.convertTo(TimeUnits.SECOND).numericalValue().intValueExact());
        return OffsetDateTime.ofInstant(instant.toJavaInstant(), offset);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof AbsolutePointInTime other
                && microsecondsSinceEpoch().equals(other.microsecondsSinceEpoch());
    }

    @Override
    public int hashCode() {
        return microsecondsSinceEpoch().hashCode();
    }

    @Override
    public String toString() {
        if (ruleset.isEmpty()) {
            return instant.toString();
        }
        StringBuilder stream = new StringBuilder();
        printYearMonthDateOn(stream);
        printTimeOfDayOn(stream);
        ruleset.get().printOffset(appliedOffset, stream);
        return stream.toString();
    }
}
