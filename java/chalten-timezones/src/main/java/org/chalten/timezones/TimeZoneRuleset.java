package org.chalten.timezones;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import org.chalten.core.FixedOffsetRuleset;
import org.chalten.core.GregorianDateTime;
import org.chalten.core.Instant;
import org.chalten.core.TimeOffsetAwareGregorianDateTime;
import org.chalten.core.TimeOffsetRuleset;
import org.chalten.localization.Translations;
import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;

/**
 * Smalltalk {@code TimeZoneRuleset}: the offsets to UTC of a geographic area, as a sequence of
 * {@link FixedOffsetRuleset}s and the instants at which they transition.
 */
public final class TimeZoneRuleset implements TimeOffsetRuleset {

    static final String INVALID_TRANSITIONS = "Must be exactly one more offset ruleset than transitions between them";
    static final String AMBIGUOUS =
            "Cannot convert the local date time into an absolute point in time because it's ambiguous in this timezone";
    static final String SKIPPED = "The local time doesn't exist in the timezone due to daylight saving time changes.";

    private final String identifier;
    private final List<FixedOffsetRuleset> fixedOffsetRulesets;
    private final List<Instant> transitions;

    private TimeZoneRuleset(String identifier, List<FixedOffsetRuleset> fixedOffsetRulesets,
            List<Instant> transitions) {
        this.identifier = identifier;
        this.fixedOffsetRulesets = fixedOffsetRulesets;
        this.transitions = transitions;
    }

    /**
     * Smalltalk {@code TimeZoneRuleset class>>identifiedBy:withAll:transitioningOn:}. {@code rulesets.get(i)} applies
     * before {@code transitions.get(i)}; the last ruleset applies from the last transition on.
     *
     * @throws InstanceCreationFailedException {@code Must be exactly one more offset ruleset than transitions between
     *         them}
     */
    public static TimeZoneRuleset identifiedBy(String identifier, List<FixedOffsetRuleset> rulesets,
            List<Instant> transitions) {
        Objects.requireNonNull(identifier, "identifier");
        if (rulesets.size() != transitions.size() + 1) {
            throw new InstanceCreationFailedException(Translations.localized(INVALID_TRANSITIONS));
        }
        return new TimeZoneRuleset(identifier, List.copyOf(rulesets), List.copyOf(transitions));
    }

    @Override
    public String identifier() {
        return identifier;
    }

    public List<FixedOffsetRuleset> fixedOffsetRulesets() {
        return fixedOffsetRulesets;
    }

    public List<Instant> transitions() {
        return transitions;
    }

    /** Smalltalk {@code offsetToUniversalCoordinatedTimeAt:}: binary search of the last transition at or before. */
    @Override
    public TimeQuantity offsetToUniversalCoordinatedTimeAt(Instant instant) {
        int low = 0;
        int high = transitions.size();
        while (low < high) {
            int middle = (low + high) >>> 1;
            if (transitions.get(middle).compareTo(instant) <= 0) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return fixedOffsetRulesets.get(low).offsetToUniversalCoordinatedTimeAt(instant);
    }

    /**
     * Smalltalk {@code convertGregorianDateTime:}: compares the offsets one day before and after the local
     * date-time read as UTC.
     *
     * @throws AmbiguousTimeException when the local date-time happens twice
     * @throws SkippedTimeException when the local date-time does not exist
     */
    @Override
    public TimeOffsetAwareGregorianDateTime convertGregorianDateTime(GregorianDateTime dateTime) {
        Instant instantInUtc = dateTime.inCoordinatedUniversalTime().asInstant();
        TimeQuantity oneDay = TimeQuantity.of(1, TimeUnits.DAY);
        TimeQuantity earlierOffset = offsetToUniversalCoordinatedTimeAt(instantInUtc.previous(oneDay));
        TimeQuantity laterOffset = offsetToUniversalCoordinatedTimeAt(instantInUtc.next(oneDay));
        if (earlierOffset.equals(laterOffset)) {
            return convertToGregorianDate(dateTime, earlierOffset);
        }
        if (earlierOffset.compareTo(laterOffset) < 0) {
            throw new SkippedTimeException(Translations.localized(SKIPPED));
        }
        throw new AmbiguousTimeException(convertToGregorianDate(dateTime, earlierOffset),
                convertToGregorianDate(dateTime, laterOffset), Translations.localized(AMBIGUOUS));
    }

    /** {@link #convertGregorianDateTime(GregorianDateTime)} resolving ambiguous or skipped times with a strategy. */
    public TimeOffsetAwareGregorianDateTime convertGregorianDateTime(GregorianDateTime dateTime,
            TimeResolutionStrategy strategy) {
        try {
            return convertGregorianDateTime(dateTime);
        } catch (AmbiguousTimeException error) {
            return strategy.resolveAmbiguous(error);
        } catch (SkippedTimeException error) {
            return strategy.resolveSkipped(error);
        }
    }

    /** The {@code java.time} zone with the same IANA identifier. */
    public ZoneId toZoneId() {
        return ZoneId.of(identifier);
    }

    /**
     * {@code dateTime} as a {@link ZonedDateTime} in the zone of its ruleset (its applied offset otherwise).
     * Truncates below the microsecond.
     */
    public static ZonedDateTime toZonedDateTime(TimeOffsetAwareGregorianDateTime dateTime) {
        ZoneId zone = dateTime.ruleset()
                .filter(TimeZoneRuleset.class::isInstance)
                .map(ruleset -> ((TimeZoneRuleset) ruleset).toZoneId())
                .orElseGet(() -> dateTime.toOffsetDateTime().getOffset());
        return ZonedDateTime.ofInstant(dateTime.asInstant().toJavaInstant(), zone);
    }

    @Override
    public String toString() {
        return identifier;
    }
}
