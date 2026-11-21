package org.chalten.hebrew;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.chalten.core.FixedOffsetRuleset;
import org.chalten.core.TimeOfDay;
import org.chalten.measure.TimeQuantity;
import org.junit.jupiter.api.Test;

class HebrewDateTimeTest extends HebrewCalendarTest {

    @Test
    void testAccessing() {
        HebrewDateTime datetime = HebrewDateTime.on(kislev.first().of(1447), TimeOfDay.halfOfDay());
        assertEquals(kislev.first().of(1447), datetime.date());
        assertEquals(1, datetime.dayNumber());
        assertEquals(y(1447), datetime.year());
        assertEquals(1447, datetime.yearNumber());
        assertEquals(kislev.of(1447), datetime.monthOfYear());
        assertEquals(kislev, datetime.month());
        assertEquals(3, datetime.monthNumber());
        assertEquals(TimeOfDay.halfOfDay(), datetime.timeOfDay());
    }

    @Test
    void testAtMidnight() {
        assertEquals("Nisan 1, 5679 AM at 06:00:00.000000", nisan.first().of(5679).atMidnight().toString());
    }

    @Test
    void testAtNoon() {
        assertEquals("Nisan 1, 5679 AM at 18:00:00.000000", nisan.first().of(5679).atNoon().toString());
    }

    @Test
    void testAtStartOfDay() {
        assertEquals("Nisan 1, 5679 AM at 00:00:00.000000", nisan.first().of(5679).atStartOfDay().toString());
    }

    @Test
    void testBetweenAnd() {
        assertTrue(tevet.first().of(5679).atNoon().between(tevet.first().of(5679).atStartOfDay(),
                tevet.second().of(5679).atStartOfDay()));
    }

    @Test
    void testComparing() {
        assertEquals(tevet.first().of(5675).atNoon(), tevet.first().of(5675).atNoon());
        assertNotEquals(tevet.first().of(5675).atNoon(), tevet.first().of(5675).atMidnight());
        assertNotEquals(tevet.first().of(5674).atNoon(), tevet.first().of(5675).atNoon());
    }

    @Test
    void testCreationShortcut() {
        HebrewDateTime datetime = kislev.first().of(5675).at(TimeOfDay.hours(18));
        assertEquals(kislev.first().of(5675), datetime.date());
        assertEquals(TimeOfDay.hours(18), datetime.timeOfDay());
        assertEquals(kislev.first().of(5675).atNoon(), datetime);
        datetime = tevet.sixth().of(5675).atMidnight();
        assertEquals(tevet.sixth().of(5675), datetime.date());
        assertEquals(TimeOfDay.hours(6), datetime.timeOfDay());
    }

    @Test
    void testGreaterThan() {
        assertIsGreaterThan(tishrei.first().of(5675).atNoon(), tishrei.first().of(5675).atMidnight());
        assertIsGreaterThanOrEqualTo(tishrei.first().of(5675).atNoon(), tishrei.first().of(5675).atMidnight());
        assertIsGreaterThanOrEqualTo(tishrei.first().of(5675).atMidnight(), tishrei.first().of(5675).atMidnight());
        assertIsGreaterThan(cheshvan.first().of(5675).atMidnight(), tishrei.first().of(5675).atMidnight());
        assertIsGreaterThanOrEqualTo(cheshvan.first().of(5675).atMidnight(), tishrei.first().of(5675).atMidnight());
        assertIsGreaterThanOrEqualTo(cheshvan.first().of(5675).atMidnight(),
                cheshvan.first().of(5675).atMidnight());
    }

    @Test
    void testIn() {
        FixedOffsetRuleset utcPlus3 = FixedOffsetRuleset.of(TimeQuantity.of(3, hour));
        FixedOffsetRuleset utcMinus6 = FixedOffsetRuleset.of(TimeQuantity.of(-6, hour));
        assertEquals("2026-02-09T18:00:00.000000+03:00",
                shevat.twentySecond().of(5786).atStartOfDay().in(utcPlus3).toString());
        assertEquals("2026-02-09T18:00:00.000000-06:00",
                shevat.twentySecond().of(5786).atStartOfDay().in(utcMinus6).toString());
    }

    @Test
    void testInCoordinatedUniversalTime() {
        assertEquals(september.seventh().of(-3760).at(TimeOfDay.hours(18)).inCoordinatedUniversalTime(),
                tishrei.first().of(1).atStartOfDay().inCoordinatedUniversalTime());
        assertEquals(september.eighth().of(-3760).atMidnight().inCoordinatedUniversalTime(),
                tishrei.first().of(1).atMidnight().inCoordinatedUniversalTime());
        assertEquals(september.eighth().of(-3760).atNoon().inCoordinatedUniversalTime(),
                tishrei.first().of(1).atNoon().inCoordinatedUniversalTime());
        assertEquals(february.ninth().of(2026).at(TimeOfDay.hours(18)).inCoordinatedUniversalTime(),
                shevat.twentySecond().of(5786).atStartOfDay().inCoordinatedUniversalTime());
    }

    @Test
    void testLessThan() {
        assertIsLessThan(tishrei.first().of(5679).atMidnight(), tishrei.first().of(5679).atNoon());
        assertIsLessThanOrEqualTo(tishrei.first().of(5679).atMidnight(), tishrei.first().of(5679).atNoon());
        assertIsLessThanOrEqualTo(tishrei.first().of(5679).atMidnight(), tishrei.first().of(5679).atMidnight());
        assertIsLessThan(tishrei.first().of(5680).atMidnight(), cheshvan.first().of(5680).atMidnight());
        assertIsLessThanOrEqualTo(tishrei.first().of(5680).atMidnight(), cheshvan.first().of(5680).atMidnight());
        denyIsLessThan(tishrei.first().of(5680).atMidnight(), tishrei.first().of(1).atMidnight());
        denyIsLessThan(cheshvan.first().of(5680).atNoon(), tishrei.first().of(5680).atNoon());
    }

    @Test
    void testPrintString() {
        assertEquals("Nisan 1, 5679 AM at 18:00:00.000000", nisan.first().of(5679).atNoon().toString());
    }
}
