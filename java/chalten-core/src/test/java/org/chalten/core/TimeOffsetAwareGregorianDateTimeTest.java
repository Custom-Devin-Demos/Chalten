package org.chalten.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.chalten.measure.TimeQuantity;
import org.chalten.testkit.ChaltenTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TimeOffsetAwareGregorianDateTimeTest extends ChaltenTest {

    private FixedOffsetRuleset utcPlus3;
    private FixedOffsetRuleset utcMinus6;

    @BeforeEach
    void setUp() {
        utcPlus3 = FixedOffsetRuleset.of(TimeQuantity.of(3, hour));
        utcMinus6 = FixedOffsetRuleset.of(TimeQuantity.of(-6, hour));
    }

    @Test
    void testAsRelativePointInTime() {
        TimeOffsetAwareGregorianDateTime unixEpoch = Instant.epoch().asGregorianDateTimeInCoordinatedUniversalTime();
        GregorianDateTime relativeDateTime = unixEpoch.asRelativePointInTime();
        assertEquals(january.first().of(1970).atMidnight(), relativeDateTime);
        unixEpoch = Instant.epoch().asGregorianDateTimeIn(utcPlus3);
        relativeDateTime = unixEpoch.asRelativePointInTime();
        assertEquals(january.first().of(1970).at(TimeOfDay.hours(3)), relativeDateTime);
    }

    @Test
    void testCreationFromInstant() {
        Instant unixEpoch = Instant.epoch();
        assertEquals(unixEpoch, unixEpoch.asGregorianDateTimeInCoordinatedUniversalTime().asInstant());
        assertEquals(unixEpoch, unixEpoch.asGregorianDateTimeInCoordinatedUniversalTime());
        assertEquals(unixEpoch.asGregorianDateTimeInCoordinatedUniversalTime(), unixEpoch);
        assertEquals("1970-01-01T00:00:00.000000Z", unixEpoch.asGregorianDateTimeInCoordinatedUniversalTime().toString());
    }

    @Test
    void testCreationFromInstantWithFixedOffset() {
        TimeOffsetAwareGregorianDateTime unixEpoch = Instant.epoch().asGregorianDateTimeIn(utcPlus3);
        assertEquals(Instant.epoch(), unixEpoch.asInstant());
        assertEquals(Instant.epoch(), unixEpoch);
        assertEquals("1970-01-01T03:00:00.000000+03:00", unixEpoch.toString());
    }

    @Test
    void testIn() {
        TimeOffsetAwareGregorianDateTime unixEpoch = Instant.epoch().asGregorianDateTimeInCoordinatedUniversalTime();
        assertEquals("1970-01-01T03:00:00.000000+03:00", unixEpoch.in(utcPlus3).toString());
        assertEquals(unixEpoch, unixEpoch.in(utcPlus3));
        assertEquals("1969-12-31T18:00:00.000000-06:00", unixEpoch.in(utcMinus6).toString());
        assertEquals(unixEpoch, unixEpoch.in(utcMinus6));
        assertEquals(unixEpoch.in(utcPlus3), unixEpoch.in(utcMinus6));
    }

    @Test
    void testInCoordinatedUniversalTime() {
        TimeOffsetAwareGregorianDateTime unixEpoch = Instant.epoch().asGregorianDateTimeInCoordinatedUniversalTime();
        assertEquals("1970-01-01T00:00:00.000000Z", unixEpoch.inCoordinatedUniversalTime().toString());
        unixEpoch = Instant.epoch().asGregorianDateTimeIn(utcPlus3);
        assertEquals("1970-01-01T03:00:00.000000+03:00", unixEpoch.toString());
        assertEquals("1970-01-01T00:00:00.000000Z", unixEpoch.inCoordinatedUniversalTime().toString());
    }
}
