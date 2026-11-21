package org.chalten.timezones;

import org.chalten.core.TimeOffsetAwareGregorianDateTime;

/**
 * Java replacement for the Smalltalk idiom {@code [dateTime in: zone] on: AmbiguousTimeError do: [:error | error
 * return: error earlierDateTime]}: decides what {@link TimeZoneRuleset#convertGregorianDateTime(
 * org.chalten.core.GregorianDateTime, TimeResolutionStrategy)} answers for ambiguous or skipped local times.
 */
public interface TimeResolutionStrategy {

    /** Answers a date-time for an ambiguous local time, or rethrows {@code error}. */
    TimeOffsetAwareGregorianDateTime resolveAmbiguous(AmbiguousTimeException error);

    /** Answers a date-time for a skipped local time, or rethrows {@code error}. */
    default TimeOffsetAwareGregorianDateTime resolveSkipped(SkippedTimeException error) {
        throw error;
    }

    /** Signals both exceptions, like an unhandled Smalltalk error. */
    static TimeResolutionStrategy strict() {
        return error -> {
            throw error;
        };
    }

    /** Resolves ambiguous local times to the earlier instant; skipped local times still signal. */
    static TimeResolutionStrategy earlier() {
        return AmbiguousTimeException::earlierDateTime;
    }

    /** Resolves ambiguous local times to the later instant; skipped local times still signal. */
    static TimeResolutionStrategy later() {
        return AmbiguousTimeException::laterDateTime;
    }
}
