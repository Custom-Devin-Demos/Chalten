package org.chalten.timezones;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.chalten.core.FixedOffsetRuleset;
import org.chalten.core.Instant;
import org.chalten.core.TimeOffsetAwareGregorianDateTime;
import org.chalten.measure.TimeQuantity;
import org.chalten.testkit.ChaltenTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** Smalltalk {@code TimeOffsetAwareGregorianDateTimeTest} extension methods of {@code Chalten-Timezones-Tests}. */
class TimeOffsetAwareGregorianDateTimeTimezonesTest extends ChaltenTest {

    private FixedOffsetRuleset utcMinus6;

    @BeforeEach
    void setUp() {
        utcMinus6 = FixedOffsetRuleset.of(TimeQuantity.of(-6, hour));
    }

    @Test
    void testInTimeZone() {
        TimeOffsetAwareGregorianDateTime unixEpoch = Instant.epoch().asGregorianDateTimeInCoordinatedUniversalTime();
        TimeZoneRuleset buenosAires = IANATimeZoneDatabase.current().at("America/Argentina/Buenos_Aires");
        TimeZoneRuleset tokyo = IANATimeZoneDatabase.current().at("Asia/Tokyo");
        assertEquals("1969-12-31T21:00:00.000000-03:00", unixEpoch.in(buenosAires).toString());
        assertEquals(unixEpoch, unixEpoch.in(buenosAires));
        assertEquals(unixEpoch.in(utcMinus6), unixEpoch.in(buenosAires));
        assertEquals("1970-01-01T09:00:00.000000+09:00", unixEpoch.in(tokyo).toString());
        assertEquals(unixEpoch.in(tokyo), unixEpoch.in(buenosAires));
    }

    @Test
    void testInTimeZoneWhenLocalTimeIsAmbiguous() {
        TimeOffsetAwareGregorianDateTime first =
                Instant.fromUnixTime(1762061400).asGregorianDateTimeInCoordinatedUniversalTime();
        TimeOffsetAwareGregorianDateTime second =
                Instant.fromUnixTime(1762065000).asGregorianDateTimeInCoordinatedUniversalTime();
        TimeZoneRuleset newYork = IANATimeZoneDatabase.current().at("America/New_York");
        assertEquals("2025-11-02T01:30:00.000000-04:00", first.in(newYork).toString());
        assertEquals("2025-11-02T01:30:00.000000-05:00", second.in(newYork).toString());
        assertEquals("2025-11-02T01:30:00.000000", printAsISO8601String(first.in(newYork).asRelativePointInTime()));
        assertEquals("2025-11-02T01:30:00.000000", printAsISO8601String(second.in(newYork).asRelativePointInTime()));
        assertEquals(second.in(newYork).asRelativePointInTime(), first.in(newYork).asRelativePointInTime());
    }
}
