package org.chalten.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.chalten.measure.Fraction;
import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.measure.TimeQuantity;
import org.chalten.testkit.ChaltenTest;
import org.junit.jupiter.api.Test;

class GregorianDateTimeTest extends ChaltenTest {

    private static GregorianDateTime parseISO8601(String string) {
        return new ISO8601Parser().parseDateTime(string);
    }

    @Test
    void testAccessing() {
        GregorianDateTime datetime = GregorianDateTime.on(june.first().of(2025), TimeOfDay.halfOfDay());
        assertEquals(june.first().of(2025), datetime.date());
        assertEquals(1, datetime.dayNumber());
        assertEquals(GregorianYear.of(2025), datetime.year());
        assertEquals(2025, datetime.yearNumber());
        assertEquals(june.of(2025), datetime.monthOfYear());
        assertEquals(june, datetime.month());
        assertEquals(6, datetime.monthNumber());
        assertEquals(TimeOfDay.halfOfDay(), datetime.timeOfDay());
    }

    @Test
    void testAtMidnight() {
        assertEquals("June 1, 2025 at 00:00:00.000000", june.first().of(2025).atMidnight().toString());
    }

    @Test
    void testAtNoon() {
        assertEquals("June 1, 2025 at 12:00:00.000000", june.first().of(2025).atNoon().toString());
    }

    @Test
    void testAtStartOfDay() {
        assertEquals("June 1, 2025 at 00:00:00.000000", june.first().of(2025).atStartOfDay().toString());
    }

    @Test
    void testBetweenAnd() {
        GregorianDate januaryFirst = january.first().of(2024);
        GregorianDate januarySecond = january.second().of(2024);
        assertTrue(januaryFirst.atNoon().between(januaryFirst.atMidnight(), januarySecond.atMidnight()));
        assertTrue(januaryFirst.atMidnight().between(januaryFirst.atMidnight(), januarySecond.atMidnight()));
        assertTrue(januarySecond.atMidnight().between(januaryFirst.atMidnight(), januarySecond.atMidnight()));
        assertFalse(januaryFirst.atMidnight().between(januaryFirst.atNoon(), januaryFirst.atNoon()));
        assertFalse(januarySecond.atNoon().between(januaryFirst.atNoon(), januaryFirst.atNoon()));
    }

    @Test
    void testComparing() {
        assertEquals(june.first().of(2025).atNoon(), june.first().of(2025).atNoon());
        assertNotEquals(june.first().of(2025).atMidnight(), june.first().of(2025).atNoon());
        assertNotEquals(june.first().of(2025).atNoon(), june.first().of(2026).atNoon());
        assertNotEquals(june.second().of(2026).atNoon(), june.first().of(2026).atNoon());
    }

    @Test
    void testConverting() {
        GregorianDateTime datetime = GregorianDateTime.on(june.first().of(2025), TimeOfDay.halfOfDay());
        assertEquals(datetime, datetime.asGregorianDateTime());
    }

    @Test
    void testCreationShortcut() {
        GregorianDateTime datetime = june.first().of(2025).at(TimeOfDay.halfOfDay());
        assertEquals(june.first().of(2025), datetime.date());
        assertEquals(TimeOfDay.halfOfDay(), datetime.timeOfDay());
        assertEquals(june.first().of(2025).atNoon(), datetime);
        datetime = december.thirtyFirst().of(2025).atMidnight();
        assertEquals(december.thirtyFirst().of(2025), datetime.date());
        assertEquals(TimeOfDay.startOfDay(), datetime.timeOfDay());
    }

    @Test
    void testGreaterThan() {
        GregorianDate januaryFirst = january.first().of(2025);
        GregorianDate februaryFirst = february.first().of(2025);
        assertIsGreaterThan(januaryFirst.atNoon(), januaryFirst.atMidnight());
        assertIsGreaterThanOrEqualTo(januaryFirst.atNoon(), januaryFirst.atMidnight());
        assertIsGreaterThanOrEqualTo(januaryFirst.atMidnight(), januaryFirst.atMidnight());
        assertIsGreaterThan(februaryFirst.atMidnight(), januaryFirst.atMidnight());
        assertIsGreaterThanOrEqualTo(februaryFirst.atMidnight(), januaryFirst.atMidnight());
        assertIsGreaterThanOrEqualTo(februaryFirst.atMidnight(), februaryFirst.atMidnight());
        denyIsGreaterThan(februaryFirst.atMidnight(), februaryFirst.atNoon());
    }

    @Test
    void testISO8601Parsing() {
        assertEquals(january.first().of(2026).atStartOfDay(), parseISO8601("2026-01-01T00"));
        assertEquals(january.first().of(2026).atStartOfDay(), parseISO8601("2026-01-01T00:00"));
        assertEquals(january.first().of(2026).atStartOfDay(), parseISO8601("2026-01-01T00:00:00"));
        assertEquals(january.first().of(2026).atStartOfDay(), parseISO8601("2026-01-01T00:00:00.000"));
        assertEquals(january.first().of(2026).atStartOfDay(), parseISO8601("2026-01-01T00:00:00.000000"));
        assertEquals(january.second().of(0).atStartOfDay(), parseISO8601("0000-01-02T00"));
        assertEquals(january.thirtieth().of(150).atStartOfDay(), parseISO8601("0150-01-30T00"));
        assertEquals(january.thirtyFirst().of(-1).atStartOfDay(), parseISO8601("-0001-01-31T00"));
        assertEquals(january.tenth().of(10000).atStartOfDay(), parseISO8601("+010000-01-10T00"));
        assertEquals(january.fifteenth().of(-54000).atStartOfDay(), parseISO8601("-054000-01-15T00"));
        assertEquals(january.twentieth().of(9999000).atStartOfDay(), parseISO8601("+9999000-01-20T00"));
        assertEquals(february.twelfth().of(-9999000).atStartOfDay(), parseISO8601("-9999000-02-12T00"));
        for (String invalidString : new String[] {"2026-01-01M00"}) {
            InstanceCreationFailedException exception = assertThrows(InstanceCreationFailedException.class,
                    () -> parseISO8601(invalidString));
            assertEquals("The provided datetime '" + invalidString + "' is invalid.", exception.getMessage());
        }
    }

    @Test
    void testISO8601Printing() {
        assertEquals("2025-06-01T12:00:00.000000", printAsISO8601String(june.first().of(2025).atNoon()));
    }

    @Test
    void testIn() {
        FixedOffsetRuleset utcPlus3 = FixedOffsetRuleset.of(TimeQuantity.of(3, hour));
        FixedOffsetRuleset utcMinus6 = FixedOffsetRuleset.of(TimeQuantity.of(-6, hour));
        assertEquals("1970-01-01T00:00:00.000000+03:00", january.first().of(1970).atMidnight().in(utcPlus3).toString());
        assertEquals("1970-01-01T00:00:00.000000-06:00",
                january.first().of(1970).atMidnight().in(utcMinus6).toString());
    }

    @Test
    void testInCoordinatedUniversalTime() {
        assertEquals(Instant.epoch(), january.first().of(1970).atMidnight().inCoordinatedUniversalTime());
        assertEquals(Instant.fromUnixTime(393940800), june.twentySixth().of(1982).atNoon().inCoordinatedUniversalTime());
    }

    @Test
    void testJulianDayNumber() {
        assertEquals(Instant.epoch().julianDayNumber(), january.first().of(1970).atMidnight().julianDayNumber());
        assertEquals(Fraction.of(2440588), january.first().of(1970).atNoon().julianDayNumber());
        assertEquals(Fraction.of(2440588).plus(Fraction.of(1, 2)),
                january.second().of(1970).atMidnight().julianDayNumber());
        assertEquals(Fraction.of(210866759999L, 86400),
                december.thirtyFirst().of(1969).at(TimeOfDay.hours(23, 59, 59)).julianDayNumber());
        assertEquals(Fraction.of(210866673599L, 86400),
                december.thirtieth().of(1969).at(TimeOfDay.hours(23, 59, 59)).julianDayNumber());
        assertEquals(Fraction.of(2455197).plus(Fraction.of(1, 2)),
                january.first().of(2010).atMidnight().julianDayNumber());
        assertEquals(Fraction.of(2440952).plus(Fraction.of(1, 2)),
                january.first().of(1971).atMidnight().julianDayNumber());
        assertEquals(Fraction.of(70328830339L, 28800),
                october.seventeenth().of(1973).at(TimeOfDay.hours(18, 36, 57)).julianDayNumber());
        assertEquals(Fraction.of(132416725, 54),
                september.ninth().of(2001).at(TimeOfDay.hours(1, 46, 40)).julianDayNumber());
        assertEquals(Fraction.of(2356681421L, 960),
                february.thirteenth().of(2009).at(TimeOfDay.hours(23, 31, 30)).julianDayNumber());
        assertEquals(Fraction.of(57960493, 24), january.first().of(1900).at(TimeOfDay.hours(1)).julianDayNumber());
    }

    @Test
    void testLessThan() {
        assertIsLessThan(january.first().of(2024).atMidnight(), january.first().of(2024).atNoon());
        assertIsLessThanOrEqualTo(january.first().of(2024).atMidnight(), january.first().of(2024).atNoon());
        assertIsLessThanOrEqualTo(january.first().of(2024).atMidnight(), january.first().of(2024).atMidnight());
        assertIsLessThan(january.first().of(2025).atMidnight(), february.first().of(2025).atMidnight());
        assertIsLessThanOrEqualTo(january.first().of(2025).atMidnight(), february.first().of(2025).atMidnight());
        denyIsLessThan(january.first().of(2025).atMidnight(), january.first().of(0).atMidnight());
        denyIsLessThan(february.first().of(2025).atNoon(), january.first().of(2025).atNoon());
    }

    @Test
    void testPrintString() {
        assertEquals("June 1, 2025 at 12:00:00.000000", june.first().of(2025).atNoon().toString());
    }

    @Test
    void testPrintStringInISO8601() {
        assertEquals("2025-06-01T12:00:00.000000", june.first().of(2025).atNoon().printAsISO8601String());
    }
}
