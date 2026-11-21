package org.chalten.islamic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.chalten.core.FixedOffsetRuleset;
import org.chalten.core.TimeOfDay;
import org.chalten.measure.TimeQuantity;
import org.junit.jupiter.api.Test;

class IslamicDateTimeTest extends IslamicCalendarTest {

    @Test
    void testAccessing() {
        IslamicDateTime datetime = IslamicDateTime.on(rabiAlAwwal.first().of(1447), TimeOfDay.halfOfDay());
        assertEquals(rabiAlAwwal.first().of(1447), datetime.date());
        assertEquals(1, datetime.dayNumber());
        assertEquals(y(1447), datetime.year());
        assertEquals(1447, datetime.yearNumber());
        assertEquals(rabiAlAwwal.of(1447), datetime.monthOfYear());
        assertEquals(rabiAlAwwal, datetime.month());
        assertEquals(3, datetime.monthNumber());
        assertEquals(TimeOfDay.halfOfDay(), datetime.timeOfDay());
    }

    @Test
    void testAtMidnight() {
        assertEquals("Jumada al-Awwal 1, 4875 AH at 06:00:00.000000", jumadaAlAwwal.first().of(4875).atMidnight().toString());
    }

    @Test
    void testAtNoon() {
        assertEquals("Jumada al-Awwal 1, 4875 AH at 18:00:00.000000", jumadaAlAwwal.first().of(4875).atNoon().toString());
    }

    @Test
    void testAtStartOfDay() {
        assertEquals("Jumada al-Awwal 1, 4875 AH at 00:00:00.000000", jumadaAlAwwal.first().of(4875).atStartOfDay().toString());
    }

    @Test
    void testBetweenAnd() {
        assertTrue(rabiAlThani.first().of(1447).atNoon().between(rabiAlThani.first().of(1447).atMidnight(),
                rabiAlThani.second().of(1447).atMidnight()));
    }

    @Test
    void testComparing() {
        assertEquals(rabiAlThani.first().of(1447).atNoon(), rabiAlThani.first().of(1447).atNoon());
        assertNotEquals(rabiAlThani.first().of(1447).atNoon(), rabiAlThani.first().of(1447).atMidnight());
        assertNotEquals(rabiAlThani.first().of(1446).atNoon(), rabiAlThani.first().of(1447).atNoon());
    }

    @Test
    void testCreationShortcut() {
        IslamicDateTime datetime = rabiAlAwwal.first().of(1447).at(TimeOfDay.hours(18));
        assertEquals(rabiAlAwwal.first().of(1447), datetime.date());
        assertEquals(TimeOfDay.hours(18), datetime.timeOfDay());
        assertEquals(rabiAlAwwal.first().of(1447).atNoon(), datetime);
        datetime = rabiAlThani.sixth().of(1447).atMidnight();
        assertEquals(rabiAlThani.sixth().of(1447), datetime.date());
        assertEquals(TimeOfDay.hours(6), datetime.timeOfDay());
    }

    @Test
    void testGreaterThan() {
        assertIsGreaterThan(muharram.first().of(1447).atNoon(), muharram.first().of(1447).atMidnight());
        assertIsGreaterThanOrEqualTo(muharram.first().of(1447).atNoon(), muharram.first().of(1447).atMidnight());
        assertIsGreaterThanOrEqualTo(muharram.first().of(1447).atMidnight(), muharram.first().of(1447).atMidnight());
        assertIsGreaterThan(safar.first().of(1447).atMidnight(), muharram.first().of(1447).atMidnight());
        assertIsGreaterThanOrEqualTo(safar.first().of(1447).atMidnight(), muharram.first().of(1447).atMidnight());
        assertIsGreaterThanOrEqualTo(safar.first().of(1447).atMidnight(), safar.first().of(1447).atMidnight());
    }

    @Test
    void testIn() {
        FixedOffsetRuleset utcPlus3 = FixedOffsetRuleset.of(TimeQuantity.of(3, hour));
        FixedOffsetRuleset utcMinus6 = FixedOffsetRuleset.of(TimeQuantity.of(-6, hour));
        assertEquals("2026-02-09T18:00:00.000000+03:00",
                shaban.twentyFirst().of(1447).atStartOfDay().in(utcPlus3).toString());
        assertEquals("2026-02-09T18:00:00.000000-06:00",
                shaban.twentyFirst().of(1447).atStartOfDay().in(utcMinus6).toString());
    }

    @Test
    void testInCoordinatedUniversalTime() {
        assertEquals(july.nineteenth().of(622).at(TimeOfDay.hours(18)).inCoordinatedUniversalTime(),
                muharram.first().of(1).atStartOfDay().inCoordinatedUniversalTime());
        assertEquals(july.twentieth().of(622).atMidnight().inCoordinatedUniversalTime(),
                muharram.first().of(1).atMidnight().inCoordinatedUniversalTime());
        assertEquals(july.twentieth().of(622).atNoon().inCoordinatedUniversalTime(),
                muharram.first().of(1).atNoon().inCoordinatedUniversalTime());
        assertEquals(february.ninth().of(2026).at(TimeOfDay.hours(18)).inCoordinatedUniversalTime(),
                shaban.twentyFirst().of(1447).atStartOfDay().inCoordinatedUniversalTime());
    }

    @Test
    void testLessThan() {
        assertIsLessThan(muharram.first().of(1446).atMidnight(), muharram.first().of(1446).atNoon());
        assertIsLessThanOrEqualTo(muharram.first().of(1446).atMidnight(), muharram.first().of(1446).atNoon());
        assertIsLessThanOrEqualTo(muharram.first().of(1446).atMidnight(), muharram.first().of(1446).atMidnight());
        assertIsLessThan(muharram.first().of(1447).atMidnight(), safar.first().of(1447).atMidnight());
        assertIsLessThanOrEqualTo(muharram.first().of(1447).atMidnight(), safar.first().of(1447).atMidnight());
        denyIsLessThan(muharram.first().of(1447).atMidnight(), muharram.first().of(1).atMidnight());
        denyIsLessThan(safar.first().of(1447).atNoon(), muharram.first().of(1447).atNoon());
    }

    @Test
    void testPrintString() {
        assertEquals("Jumada al-Thani 1, 1447 AH at 18:00:00.000000", jumadaAlThani.first().of(1447).atNoon().toString());
    }
}
