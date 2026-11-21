package org.chalten.timezones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.NoSuchElementException;
import org.chalten.core.Instant;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;
import org.chalten.testkit.ChaltenTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class IANATimeZoneDatabaseTest extends ChaltenTest {

    private TimeZoneRuleset buenosAiresTimeZoneRuleset;

    private TimeQuantity offsetInBuenosAiresAtUnixTime(long secondCount) {
        return buenosAiresTimeZoneRuleset.offsetToUniversalCoordinatedTimeAt(Instant.fromUnixTime(secondCount));
    }

    @BeforeEach
    void setUp() {
        buenosAiresTimeZoneRuleset = IANATimeZoneDatabase.current().at("America/Argentina/Buenos_Aires");
    }

    @Test
    void testAt() {
        assertEquals("America/Argentina/Buenos_Aires", buenosAiresTimeZoneRuleset.identifier());
    }

    @Test
    void testAtInvalidTimeZone() {
        NoSuchElementException error =
                assertThrows(NoSuchElementException.class, () -> IANATimeZoneDatabase.current().at("XXX"));
        assertEquals("XXX is not a valid time zone identifier", error.getMessage());
    }

    @Test
    void testOffsetToUniversalCoordinatedTimeAt() {
        assertEquals(TimeQuantity.of(-3, hour), buenosAiresTimeZoneRuleset.offsetToUniversalCoordinatedTimeAt(
                Instant.now()));
        assertEquals(TimeQuantity.of(-3, hour), buenosAiresTimeZoneRuleset.offsetToUniversalCoordinatedTimeAt(
                Instant.epoch()));
    }

    @Test
    void testOffsetToUniversalCoordinatedTimeAtTransitionMoment() {
        assertEquals(TimeQuantity.of(-3, hour), offsetInBuenosAiresAtUnixTime(1224385199));
        assertEquals(TimeQuantity.of(-2, hour), offsetInBuenosAiresAtUnixTime(1224385200));
        assertEquals(TimeQuantity.of(-2, hour), offsetInBuenosAiresAtUnixTime(1224385201));
    }

    @Test
    void testOffsetToUniversalCoordinatedTimeBeforeFirstTransition() {
        assertEquals(TimeQuantity.of(-3, hour).minus(TimeQuantity.of(53, TimeUnits.MINUTE))
                .minus(TimeQuantity.of(48, TimeUnits.SECOND)), offsetInBuenosAiresAtUnixTime(-2208988800L));
    }

    @Test
    void testTimeZoneIdentifiers() {
        List<String> identifiers = IANATimeZoneDatabase.current().timeZoneIdentifiers();
        assertFalse(identifiers.isEmpty());
        for (String each : List.of("Africa/Johannesburg", "America/Argentina/Buenos_Aires", "America/Lima",
                "America/Los_Angeles", "America/New_York", "America/Vancouver", "Asia/Dubai", "Asia/Hong_Kong",
                "Asia/Seoul", "Asia/Shanghai", "Asia/Singapore", "Asia/Tokyo", "Australia/Melbourne",
                "Europe/Athens", "Europe/Berlin", "Europe/Brussels", "Europe/London", "Europe/Madrid",
                "Europe/Paris", "Europe/Rome", "Europe/Zurich", "Pacific/Honolulu")) {
            assertTrue(identifiers.contains(each), each);
        }
    }
}
