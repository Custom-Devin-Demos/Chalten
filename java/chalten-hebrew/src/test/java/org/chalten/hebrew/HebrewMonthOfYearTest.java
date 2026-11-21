package org.chalten.hebrew;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.chalten.measure.InstanceCreationFailedException;
import org.junit.jupiter.api.Test;

class HebrewMonthOfYearTest extends HebrewCalendarTest {

    private void assertNumberOfDays(HebrewYear year, long... expected) {
        List<HebrewMonth> months = year.isLeapYear() ? HebrewMonth.allOnLeapYear() : HebrewMonth.allOnCommonYear();
        for (int index = 0; index < expected.length; index++) {
            assertEquals(days(expected[index]), months.get(index).of(year.number()).numberOfDays(),
                    months.get(index).toString());
        }
        assertEquals(months.size(), expected.length);
    }

    @Test
    void testBetweenAnd() {
        HebrewYear year2 = y(2);
        HebrewYear year3 = y(3);
        assertTrue(year2.tishrei().between(year2.tishrei(), year3.tishrei()));
        assertTrue(year3.tishrei().between(year2.tishrei(), year3.tishrei()));
        assertTrue(year3.tishrei().between(year2.tishrei(), y(4).tishrei()));
        assertTrue(year2.cheshvan().between(year2.tishrei(), year2.cheshvan()));
        assertTrue(year2.cheshvan().between(year2.tishrei(), year2.cheshvan()));
        assertTrue(year2.cheshvan().between(year2.tishrei(), year2.tevet()));
        assertFalse(y(4).tishrei().between(year2.tishrei(), year3.tishrei()));
        assertFalse(y(1).tishrei().between(year2.tishrei(), year3.tishrei()));
    }

    @Test
    void testComparing() {
        assertEquals(y(5786).tishrei(), y(5786).tishrei());
        assertNotEquals(y(5788).tishrei(), y(5786).tishrei());
        assertNotEquals(y(5788).tishrei(), y(5788).kislev());
    }

    @Test
    void testCreation() {
        HebrewMonthOfYear monthOfYear = y(5786).tevet();
        assertEquals(y(5786), monthOfYear.year());
        assertEquals(tevet, monthOfYear.month());
    }

    @Test
    void testCreationShortcut() {
        HebrewMonthOfYear monthOfYear = tevet.of(5786);
        assertEquals(y(5786), monthOfYear.year());
        assertEquals(tevet, monthOfYear.month());
        assertEquals("Adar only exists on common years",
                assertThrows(InstanceCreationFailedException.class, () -> adar.of(5787)).getMessage());
    }

    @Test
    void testCreationShortcutOnLeapYear() {
        assertEquals("Adar II only exists on leap years",
                assertThrows(InstanceCreationFailedException.class, () -> adarII.of(5786)).getMessage());
        HebrewMonthOfYear monthOfYear = adarII.of(5787);
        assertEquals(y(5787), monthOfYear.year());
        assertEquals(adarII, monthOfYear.month());
    }

    @Test
    void testGreaterThan() {
        assertIsGreaterThan(tishrei.of(2), tishrei.of(1));
        assertIsGreaterThanOrEqualTo(tishrei.of(2), tishrei.of(1));
        assertIsGreaterThanOrEqualTo(tishrei.of(1), tishrei.of(1));
        assertIsGreaterThan(cheshvan.of(1), tishrei.of(1));
        assertIsGreaterThanOrEqualTo(cheshvan.of(1), tishrei.of(1));
        assertIsGreaterThan(cheshvan.of(2), tishrei.of(1));
        denyIsGreaterThan(tishrei.of(1), tishrei.of(2025));
        denyIsGreaterThan(tishrei.of(1), cheshvan.of(1));
    }

    @Test
    void testLessThan() {
        assertIsLessThan(tishrei.of(1), tishrei.of(2));
        assertIsLessThanOrEqualTo(tishrei.of(1), tishrei.of(2));
        assertIsLessThanOrEqualTo(tishrei.of(1), tishrei.of(1));
        assertIsLessThanOrEqualTo(tishrei.of(1), cheshvan.of(1));
        assertIsLessThanOrEqualTo(cheshvan.of(1), tishrei.of(2));
        denyIsLessThan(tishrei.of(2025), tishrei.of(1));
        denyIsLessThan(cheshvan.of(1), tishrei.of(1));
    }

    @Test
    void testNext() {
        HebrewYear baseYear = y(5678);
        HebrewYear nextToBaseYear = baseYear.next();
        assertFalse(baseYear.isLeapYear());
        assertTrue(baseYear.isCompleteYear());
        assertTrue(nextToBaseYear.isLeapYear());
        assertTrue(nextToBaseYear.isDeficientYear());
        List<HebrewMonthOfYear> sequence = List.of(baseYear.tishrei(), baseYear.cheshvan(), baseYear.kislev(),
                baseYear.tevet(), baseYear.shevat(), baseYear.adar(), baseYear.nisan(), baseYear.iyar(),
                baseYear.sivan(), baseYear.tammuz(), baseYear.av(), baseYear.elul(), nextToBaseYear.tishrei(),
                nextToBaseYear.cheshvan(), nextToBaseYear.kislev(), nextToBaseYear.tevet(), nextToBaseYear.shevat(),
                nextToBaseYear.adarI(), nextToBaseYear.adarII(), nextToBaseYear.nisan(), nextToBaseYear.iyar(),
                nextToBaseYear.sivan(), nextToBaseYear.tammuz(), nextToBaseYear.av(), nextToBaseYear.elul(),
                nextToBaseYear.next().tishrei());
        for (int index = 0; index < sequence.size() - 1; index++) {
            assertNextOf(sequence.get(index), sequence.get(index + 1));
        }
        assertEquals(nextToBaseYear.tishrei(), baseYear.tishrei().next(hebrewMonths(12)));
        assertEquals(baseYear.tishrei(), nextToBaseYear.tishrei().next(hebrewMonths(-12)));
        assertEquals(nextToBaseYear.adarII(), baseYear.tishrei().next(hebrewMonths(18)));
        assertEquals(nextToBaseYear.next().tishrei(), baseYear.tishrei().next(hebrewMonths(25)));
        assertEquals(baseYear.next(hebrewYears(19)).tishrei(), baseYear.tishrei().next(hebrewMonths(235)));
    }

    @Test
    void testNumberOfDaysOnCommonCompleteYear() {
        assertFalse(y(5678).isLeapYear());
        assertTrue(y(5678).isCompleteYear());
        assertNumberOfDays(y(5678), 30, 30, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29);
    }

    @Test
    void testNumberOfDaysOnCommonDeficientYear() {
        assertFalse(y(5675).isLeapYear());
        assertTrue(y(5675).isDeficientYear());
        assertNumberOfDays(y(5675), 30, 29, 29, 29, 30, 29, 30, 29, 30, 29, 30, 29);
    }

    @Test
    void testNumberOfDaysOnCommonRegularYear() {
        assertFalse(y(5674).isLeapYear());
        assertTrue(y(5674).isRegularYear());
        assertNumberOfDays(y(5674), 30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30, 29);
    }

    @Test
    void testNumberOfDaysOnLeapCompleteYear() {
        assertTrue(y(5676).isLeapYear());
        assertTrue(y(5676).isCompleteYear());
        assertNumberOfDays(y(5676), 30, 30, 30, 29, 30, 30, 29, 30, 29, 30, 29, 30, 29);
    }

    @Test
    void testNumberOfDaysOnLeapDeficientYear() {
        assertTrue(y(5679).isLeapYear());
        assertTrue(y(5679).isDeficientYear());
        assertNumberOfDays(y(5679), 30, 29, 29, 29, 30, 30, 29, 30, 29, 30, 29, 30, 29);
    }

    @Test
    void testNumberOfDaysOnLeapRegularYear() {
        assertTrue(y(5684).isLeapYear());
        assertTrue(y(5684).isRegularYear());
        assertNumberOfDays(y(5684), 30, 29, 30, 29, 30, 30, 29, 30, 29, 30, 29, 30, 29);
    }

    @Test
    void testPrevious() {
        HebrewYear baseYear = y(5679);
        HebrewYear previousToBaseYear = baseYear.previous();
        assertTrue(baseYear.isLeapYear());
        assertTrue(baseYear.isDeficientYear());
        assertFalse(previousToBaseYear.isLeapYear());
        assertTrue(previousToBaseYear.isCompleteYear());
        List<HebrewMonthOfYear> sequence = List.of(previousToBaseYear.tishrei(), previousToBaseYear.cheshvan(),
                previousToBaseYear.kislev(), previousToBaseYear.tevet(), previousToBaseYear.shevat(),
                previousToBaseYear.adar(), previousToBaseYear.nisan(), previousToBaseYear.iyar(),
                previousToBaseYear.sivan(), previousToBaseYear.tammuz(), previousToBaseYear.av(),
                previousToBaseYear.elul(), baseYear.tishrei(), baseYear.cheshvan(), baseYear.kislev(),
                baseYear.tevet(), baseYear.shevat(), baseYear.adarI(), baseYear.adarII(), baseYear.nisan(),
                baseYear.iyar(), baseYear.sivan(), baseYear.tammuz(), baseYear.av(), baseYear.elul());
        for (int index = sequence.size() - 1; index > 0; index--) {
            assertPreviousOf(sequence.get(index), sequence.get(index - 1));
        }
        assertPreviousOf(previousToBaseYear.next().tishrei(), previousToBaseYear.elul());
        assertEquals(previousToBaseYear.tishrei(), baseYear.tishrei().previous(hebrewMonths(12)));
        assertEquals(baseYear.tishrei(), previousToBaseYear.tishrei().previous(hebrewMonths(-12)));
        assertEquals(previousToBaseYear.tishrei(), baseYear.adarII().previous(hebrewMonths(18)));
        assertEquals(previousToBaseYear.previous().tishrei(), baseYear.tishrei().previous(hebrewMonths(24)));
        assertEquals(previousToBaseYear.previous().elul(), baseYear.elul().previous(hebrewMonths(25)));
        assertEquals(baseYear.previous(hebrewYears(19)).tishrei(), baseYear.tishrei().previous(hebrewMonths(235)));
    }

    @Test
    void testPrintString() {
        assertEquals("Tevet, 1412 AM", tevet.of(1412).toString());
    }
}
