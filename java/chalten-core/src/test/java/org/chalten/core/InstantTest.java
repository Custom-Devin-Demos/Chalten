package org.chalten.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.chalten.measure.Fraction;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;
import org.junit.jupiter.api.Test;

class InstantTest {

    private static void assertJulianDayNumberConversionOf(Instant instant) {
        assertEquals(instant, Instant.fromJulianDayNumber(instant.julianDayNumber()));
    }

    private static void assertJulianDayNumberOfMinusOneHalfIs(Instant instant, long number) {
        assertEquals(Fraction.of(number), instant.julianDayNumber().minus(Fraction.of(1, 2)));
    }

    private static TimeQuantity oneDay() {
        return TimeQuantity.of(1, TimeUnits.DAY);
    }

    private static TimeQuantity oneSecond() {
        return TimeQuantity.of(1, TimeUnits.SECOND);
    }

    private static TimeQuantity threeHours() {
        return TimeQuantity.of(3, TimeUnits.HOUR);
    }

    private static TimeQuantity days(long amount) {
        return TimeQuantity.of(amount, TimeUnits.DAY);
    }

    @Test
    void testAccessing() {
        Instant instant = Instant.epoch().next(days(1));
        assertEquals(TimeQuantity.of(86400, TimeUnits.SECOND), instant.secondsSinceEpoch());
        assertEquals(TimeQuantity.of(86400000, TimeUnits.MILLISECOND), instant.milisecondsSinceEpoch());
        assertEquals(days(1), instant.daysSinceEpoch());
    }

    @Test
    void testAsUnixTime() {
        assertEquals(Fraction.of(0), Instant.epoch().asUnixTime());
        assertEquals(Fraction.of(1262304000), Instant.fromUnixTime(1262304000).asUnixTime());
        assertEquals(Fraction.of(31536000), Instant.fromUnixTime(31536000).asUnixTime());
        assertEquals(Instant.fromUnixTime(31536000), Instant.epoch().next(days(365)));
    }

    @Test
    void testComparing() {
        assertEquals(Instant.epoch(), Instant.epoch());
        assertEquals(Instant.epoch().hashCode(), Instant.epoch().hashCode());
        assertEquals(Instant.fromUnixTime(1262304000), Instant.fromUnixTime(1262304000));
        assertEquals(Instant.fromUnixTime(1262304000).hashCode(), Instant.fromUnixTime(1262304000).hashCode());
        assertNotEquals(Instant.epoch(), Instant.fromUnixTime(1262304000));
        assertNotEquals(0, Instant.now());
        assertNotEquals(Instant.now(), 0);
    }

    @Test
    void testConverting() {
        Instant instant = Instant.epoch().next(days(1));
        assertEquals(instant, instant.asInstant());
    }

    @Test
    void testFromJulianDayNumber() {
        assertJulianDayNumberConversionOf(Instant.epoch());
        assertJulianDayNumberConversionOf(Instant.fromUnixTime(1357000200));
        assertJulianDayNumberConversionOf(Instant.epoch().next(oneDay()));
        assertJulianDayNumberConversionOf(Instant.epoch().previous(oneSecond()));
        assertJulianDayNumberConversionOf(Instant.epoch().previous(oneSecond().plus(oneDay())));
        assertJulianDayNumberConversionOf(
                Instant.epoch().previous(oneSecond().plus(threeHours()).plus(oneDay())));
        assertJulianDayNumberConversionOf(Instant.fromUnixTime(1262304000));
        assertJulianDayNumberConversionOf(Instant.epoch().next(days(365)));
        assertJulianDayNumberConversionOf(Instant.fromUnixTime(-31536000));
        assertJulianDayNumberConversionOf(Instant.fromUnixTime(119731017));
        assertJulianDayNumberConversionOf(Instant.fromUnixTime(1000000000));
        assertJulianDayNumberConversionOf(Instant.fromUnixTime(1234567890));
        assertJulianDayNumberConversionOf(Instant.fromUnixTime(-2208985200L));
    }

    @Test
    void testFromUnixTime() {
        assertEquals(Instant.epoch(), Instant.fromUnixTime(0));
    }

    @Test
    void testJulianDayNumber() {
        assertJulianDayNumberOfMinusOneHalfIs(Instant.epoch(), 2440587);
        assertEquals(Fraction.of(117902089, 48), Instant.fromUnixTime(1357000200).julianDayNumber());
        assertJulianDayNumberOfMinusOneHalfIs(Instant.epoch().next(oneDay()), 2440588);
        assertEquals(Fraction.of(210866759999L, 86400), Instant.epoch().previous(oneSecond()).julianDayNumber());
        assertEquals(Fraction.of(210866673599L, 86400),
                Instant.epoch().previous(oneSecond().plus(oneDay())).julianDayNumber());
        assertEquals(Fraction.of(210866662799L, 86400),
                Instant.epoch().previous(oneSecond().plus(threeHours()).plus(oneDay())).julianDayNumber());
        assertJulianDayNumberOfMinusOneHalfIs(Instant.fromUnixTime(1262304000), 2455197);
        assertJulianDayNumberOfMinusOneHalfIs(Instant.epoch().next(days(365)), 2440952);
        assertJulianDayNumberOfMinusOneHalfIs(Instant.fromUnixTime(-31536000), 2440222);
        assertEquals(Fraction.of(70328830339L, 28800), Instant.fromUnixTime(119731017).julianDayNumber());
        assertEquals(Fraction.of(132416725, 54), Instant.fromUnixTime(1000000000).julianDayNumber());
        assertEquals(Fraction.of(2356681421L, 960), Instant.fromUnixTime(1234567890).julianDayNumber());
        assertEquals(Fraction.of(57960493, 24), Instant.fromUnixTime(-2208985200L).julianDayNumber());
    }

    @Test
    void testMagnitudeProtocol() {
        Instant epoch = Instant.epoch();
        Instant now = Instant.now();
        assertTrue(now.isGreaterThan(epoch));
        assertTrue(now.isGreaterThanOrEqualTo(epoch));
        assertTrue(epoch.isLessThan(now));
        assertTrue(epoch.isLessThanOrEqualTo(now));
        assertNotEquals(now, epoch);
        assertEquals(now, now);
        assertEquals(now.hashCode(), now.hashCode());
    }

    @Test
    void testPrintString() {
        assertEquals("1970-01-01T00:00:00.000000Z", Instant.epoch().toString());
        assertEquals("1970-01-02T00:00:00.000000Z", Instant.epoch().next(oneDay()).toString());
        assertEquals("1969-12-31T23:59:59.000000Z", Instant.epoch().previous(oneSecond()).toString());
        assertEquals("1970-01-02T00:00:00.000000Z", Instant.daysSinceEpoch(oneDay()).toString());
        assertEquals("1969-12-30T23:59:59.000000Z", Instant.epoch().previous(oneSecond().plus(oneDay())).toString());
        assertEquals("1969-12-30T20:59:59.000000Z",
                Instant.epoch().previous(oneSecond().plus(threeHours()).plus(oneDay())).toString());
        assertEquals("2010-01-01T00:00:00.000000Z", Instant.fromUnixTime(1262304000).toString());
        assertEquals("1971-01-01T00:00:00.000000Z", Instant.epoch().next(days(365)).toString());
        assertEquals("1971-01-01T00:00:00.000000Z", Instant.fromUnixTime(31536000).toString());
        assertEquals("1969-01-01T00:00:00.000000Z", Instant.fromUnixTime(-31536000).toString());
        assertEquals("1973-10-17T18:36:57.000000Z", Instant.fromUnixTime(119731017).toString());
        assertEquals("2001-09-09T01:46:40.000000Z", Instant.fromUnixTime(1000000000).toString());
        assertEquals("2009-02-13T23:31:30.000000Z", Instant.fromUnixTime(1234567890).toString());
        assertEquals("1900-01-01T01:00:00.000000Z", Instant.fromUnixTime(-2208985200L).toString());
    }
}
