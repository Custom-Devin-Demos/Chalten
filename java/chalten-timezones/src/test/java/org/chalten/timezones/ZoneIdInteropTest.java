package org.chalten.timezones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import org.chalten.core.Instant;
import org.chalten.core.TimeOfDay;
import org.chalten.core.TimeOffsetAwareGregorianDateTime;
import org.chalten.testkit.ChaltenTest;
import org.junit.jupiter.api.Test;

/** Java-only: {@code java.time.ZoneId} interop and ambiguity resolution strategies. */
class ZoneIdInteropTest extends ChaltenTest {

    // The JDK and the OS ship different tzdata releases, which disagree on some old or predicted transitions.
    private static final long PAST_DATA_LIMIT = 1_735_689_600L;

    private final IANATimeZoneDatabase database = IANATimeZoneDatabase.current();

    @Test
    void atZoneIdAnswersTheSameRuleset() {
        assertSame(database.at("Europe/Paris"), database.at(ZoneId.of("Europe/Paris")));
        assertEquals(ZoneId.of("Europe/Paris"), database.at("Europe/Paris").toZoneId());
    }

    @Test
    void offsetsAgreeWithJavaTimeAroundTheTransitions() {
        for (String identifier : List.of("America/New_York", "America/Argentina/Buenos_Aires", "Europe/Paris",
                "Europe/London", "Asia/Tokyo", "Australia/Melbourne", "America/Los_Angeles")) {
            TimeZoneRuleset ruleset = database.at(identifier);
            ZoneId zoneId = ruleset.toZoneId();
            for (Instant transition : ruleset.transitions()) {
                long seconds = transition.asUnixTime().numerator().longValueExact();
                if (seconds < 0 || seconds > PAST_DATA_LIMIT) {
                    continue;
                }
                for (long delta : new long[] {-1, 0, 1}) {
                    Instant instant = Instant.fromUnixTime(seconds + delta);
                    long expected = zoneId.getRules().getOffset(instant.toJavaInstant()).getTotalSeconds();
                    assertEquals(expected, ruleset.offsetToUniversalCoordinatedTimeAt(instant)
                            .convertTo(org.chalten.measure.TimeUnits.SECOND).numericalValue().longValueExact(),
                            identifier + " at " + (seconds + delta));
                }
            }
        }
    }

    @Test
    void toZonedDateTimeKeepsTheRegion() {
        TimeOffsetAwareGregorianDateTime dateTime =
                Instant.fromUnixTime(1762061400).asGregorianDateTimeIn(database.at("America/New_York"));
        ZonedDateTime zoned = TimeZoneRuleset.toZonedDateTime(dateTime);
        assertEquals(ZoneId.of("America/New_York"), zoned.getZone());
        assertEquals("2025-11-02T01:30-04:00[America/New_York]", zoned.toString());
    }

    @Test
    void resolutionStrategies() {
        TimeZoneRuleset newYork = database.at("America/New_York");
        var ambiguous = november.second().of(2025).at(TimeOfDay.hours(1, 30));
        assertEquals("2025-11-02T01:30:00.000000-04:00",
                newYork.convertGregorianDateTime(ambiguous, TimeResolutionStrategy.earlier()).toString());
        assertEquals("2025-11-02T01:30:00.000000-05:00",
                newYork.convertGregorianDateTime(ambiguous, TimeResolutionStrategy.later()).toString());
    }
}
