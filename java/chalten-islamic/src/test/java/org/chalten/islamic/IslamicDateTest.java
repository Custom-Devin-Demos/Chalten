package org.chalten.islamic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.chalten.core.GregorianMonth;
import org.chalten.measure.DiscreteInterval;
import org.junit.jupiter.api.Test;

class IslamicDateTest extends IslamicCalendarTest {

    private void assertDaysAfter(long dayCount, IslamicDate date, IslamicDate anotherDate) {
        assertEquals(anotherDate, date.next(days(dayCount)));
    }

    private void assertDaysBefore(long dayCount, IslamicDate date, IslamicDate anotherDate) {
        assertEquals(anotherDate, date.previous(days(dayCount)));
    }

    /** Sends {@code first} to {@code lastDayNumber}-th to {@code month}, then {@code , yearNumber}. */
    private static void assertDayNumbers(IslamicMonth month, long yearNumber, int lastDayNumber) {
        for (int dayNumber = 1; dayNumber <= lastDayNumber; dayNumber++) {
            assertEquals(dayNumber, date(month, dayNumber, yearNumber).dayNumber());
        }
    }

    @Test
    void testAccessing() {
        IslamicDate date = jumadaAlThani.first().of(2025);
        assertEquals(IslamicYear.of(2025), date.year());
        assertEquals(2025, date.yearNumber());
        assertEquals(jumadaAlThani.of(2025), date.monthOfYear());
        assertEquals(jumadaAlThani, date.month());
        assertEquals(6, date.monthNumber());
    }

    @Test
    void testBetweenAnd() {
        assertTrue(muharram.first().of(2025).between(muharram.first().of(2025), muharram.second().of(2025)));
        assertTrue(muharram.second().of(2025).between(muharram.first().of(2025), muharram.second().of(2025)));
        assertTrue(muharram.second().of(2025).between(muharram.first().of(2025), muharram.third().of(2025)));
        assertTrue(safar.first().of(2025).between(muharram.first().of(2025), safar.first().of(2025)));
        assertTrue(safar.first().of(2025).between(muharram.first().of(2025), safar.third().of(2025)));
        assertTrue(safar.first().of(2025).between(muharram.first().of(2025), jumadaAlThani.fifth().of(2025)));
        assertFalse(muharram.eleventh().of(2025).between(muharram.twelfth().of(2025),
                muharram.thirtieth().of(2025)));
        assertFalse(muharram.twentieth().of(2025).between(muharram.first().of(2025),
                muharram.eleventh().of(2025)));
    }

    @Test
    void testCannotComputeDateDistanceWhenTheSecondIsGreater() {
        assertEquals("Cannot subtract two dates when the second one is greater than the first one.",
                assertThrows(ArithmeticException.class,
                        () -> muharram.second().of(2025).minus(muharram.third().of(2025))).getMessage());
    }

    @Test
    void testComparing() {
        assertEquals(muharram.first().of(2025), muharram.first().of(2025));
        assertNotEquals(muharram.first().of(2025), muharram.second().of(2025));
        assertNotEquals(muharram.first().of(2025), safar.first().of(2025));
        assertNotEquals(muharram.first().of(2025), muharram.first().of(2024));
        assertNotEquals(muharram.first().of(2025), GregorianMonth.JANUARY.first().of(2025));
    }

    @Test
    void testDayOfWeek() {
        assertEquals(IslamicDayOfWeek.YAWN_AL_ITHNAYN, rajab.first().of(1428).dayOfWeek());
        assertEquals(IslamicDayOfWeek.YAWN_ATH_THULATHA, rajab.second().of(1428).dayOfWeek());
        assertEquals(IslamicDayOfWeek.YAWN_AL_ARBAA, rajab.third().of(1428).dayOfWeek());
        assertEquals(IslamicDayOfWeek.YAWN_AL_KHAMEES, rajab.fourth().of(1428).dayOfWeek());
        assertEquals(IslamicDayOfWeek.YAWN_AL_JUMUHA, rajab.fifth().of(1428).dayOfWeek());
        assertEquals(IslamicDayOfWeek.YAWN_AS_SABT, rajab.sixth().of(1428).dayOfWeek());
        assertEquals(IslamicDayOfWeek.YAWN_AL_AHAD, rajab.seventh().of(1428).dayOfWeek());
        assertEquals(IslamicDayOfWeek.YAWN_AL_ITHNAYN, rajab.eighth().of(1428).dayOfWeek());
    }

    @Test
    void testDaysBetweenTwoDates() {
        assertTrue(muharram.first().of(2025).minus(muharram.first().of(2025)).isZero());
        assertEquals(days(1), muharram.second().of(2025).minus(muharram.first().of(2025)));
        assertEquals(days(30), safar.first().of(2025).minus(muharram.first().of(2025)));
        assertEquals(days(30 + 29), rabiAlAwwal.first().of(2025).minus(muharram.first().of(2025)));
        assertEquals(days(354), muharram.first().of(2026).minus(muharram.first().of(2025)));
        assertEquals(days(354), duAlHijjah.thirtieth().of(1442).minus(muharram.first().of(1442)));
        assertEquals(days(355), muharram.first().of(1443).minus(muharram.first().of(1442)));
    }

    @Test
    void testGreaterThan() {
        assertIsGreaterThan(muharram.first().of(2025), muharram.first().of(2024));
        assertIsGreaterThanOrEqualTo(muharram.first().of(2025), muharram.first().of(2024));
        assertIsGreaterThanOrEqualTo(muharram.first().of(2025), muharram.first().of(2025));
        assertIsGreaterThan(safar.first().of(2025), muharram.first().of(2025));
        assertIsGreaterThanOrEqualTo(safar.first().of(2025), muharram.first().of(2025));
        assertIsGreaterThanOrEqualTo(muharram.first().of(2025), duAlHijjah.first().of(2024));
        assertIsGreaterThan(safar.second().of(2025), safar.first().of(2025));
        assertIsGreaterThanOrEqualTo(safar.second().of(2025), safar.first().of(2025));
        denyIsGreaterThan(muharram.first().of(1), muharram.first().of(2025));
        denyIsGreaterThan(muharram.first().of(2025), safar.first().of(2025));
    }

    @Test
    void testInstanceCreationFor29DaysMonth() {
        assertDayNumbers(shawwal, 2025, 29);
        assertThrows(UnsupportedOperationException.class, () -> shawwal.thirtieth().of(2025));
    }

    @Test
    void testInstanceCreationFor30DaysMonth() {
        assertDayNumbers(ramadan, 2025, 30);
    }

    @Test
    void testInstanceCreationForDuAlhijjahOnCommonYear() {
        assertDayNumbers(duAlHijjah, 1443, 29);
        assertThrows(UnsupportedOperationException.class, () -> duAlHijjah.of(1443).thirtieth());
    }

    @Test
    void testInstanceCreationForDuAlhijjahOnLeapYear() {
        assertDayNumbers(duAlHijjah, 1442, 30);
    }

    @Test
    void testInterval() {
        DiscreteInterval<IslamicDate> interval = muharram.first().of(2025).to(muharram.thirtieth().of(2025));
        assertEquals(30, interval.size());
        for (int index = 1; index <= interval.size(); index++) {
            assertEquals(index, interval.get(index - 1).dayNumber());
            assertEquals(muharram.of(2025), interval.get(index - 1).monthOfYear());
        }
    }

    @Test
    void testIntervalWithStep() {
        DiscreteInterval<IslamicDate> interval =
                muharram.first().of(1443).to(muharram.thirtieth().of(1443), days(2));
        assertEquals(15, interval.size());
        for (int index = 1; index <= interval.size(); index++) {
            assertEquals(index * 2 - 1, interval.get(index - 1).dayNumber());
            assertEquals(muharram.of(1443), interval.get(index - 1).monthOfYear());
        }
    }

    @Test
    void testIsLastDayOfMonth() {
        assertTrue(shawwal.twentyNinth().of(2025).isLastDayOfMonth());
        assertTrue(ramadan.thirtieth().of(2025).isLastDayOfMonth());
        assertTrue(duAlHijjah.twentyNinth().of(1443).isLastDayOfMonth());
        assertTrue(duAlHijjah.thirtieth().of(1442).isLastDayOfMonth());
        assertFalse(duAlHijjah.twentyNinth().of(1442).isLastDayOfMonth());
    }

    @Test
    void testLessThan() {
        assertIsLessThan(muharram.first().of(2024), muharram.first().of(2025));
        assertIsLessThanOrEqualTo(muharram.first().of(2024), muharram.first().of(2025));
        assertIsLessThanOrEqualTo(muharram.first().of(2025), muharram.first().of(2025));
        assertIsLessThan(muharram.first().of(2025), safar.first().of(2025));
        assertIsLessThanOrEqualTo(muharram.first().of(2025), safar.first().of(2025));
        assertIsLessThanOrEqualTo(duAlHijjah.first().of(2024), muharram.first().of(2025));
        assertIsLessThan(safar.first().of(2025), safar.second().of(2025));
        assertIsLessThanOrEqualTo(safar.first().of(2025), safar.second().of(2025));
        denyIsLessThan(muharram.first().of(2025), muharram.first().of(1));
        denyIsLessThan(safar.first().of(2025), muharram.first().of(2025));
        // Java's static types reject comparing dates of different calendars; the years still refuse it.
        assertEquals("Years in different time scales are incommensurable",
                assertThrows(ArithmeticException.class, () -> compareUnchecked(muharram.first().of(2025).year(),
                        GregorianMonth.JANUARY.first().of(2025).year())).getMessage());
    }

    @Test
    void testNext() {
        assertNextOf(jumadaAlThani.first().of(2025), jumadaAlThani.second().of(2025));
        assertNextOf(jumadaAlThani.twentyNinth().of(2025), rajab.first().of(2025));
        assertNextOf(duAlHijjah.twentyNinth().of(2025), muharram.first().of(2026));
        assertNextOf(safar.twentyEighth().of(2020), safar.twentyNinth().of(2020));
        assertNextOf(duAlHijjah.twentyNinth().of(1442), duAlHijjah.thirtieth().of(1442));
        assertDaysAfter(10, muharram.first().of(1443), muharram.eleventh().of(1443));
        assertDaysAfter(29, muharram.first().of(1443), muharram.thirtieth().of(1443));
        assertDaysAfter(30, muharram.first().of(1443), safar.first().of(1443));
        assertDaysAfter(354, muharram.first().of(1443), muharram.first().of(1444));
        assertDaysAfter(355, muharram.first().of(1442), muharram.first().of(1443));
        assertDaysAfter(0, muharram.first().of(1443), muharram.first().of(1443));
        assertDaysAfter(354 + 355 + 354 + 354, muharram.first().of(1441), muharram.first().of(1445));
        assertDaysAfter(354 + 355 + 354 + 354 + 30 + 29, muharram.first().of(1441), rabiAlAwwal.first().of(1445));
    }

    @Test
    void testPrevious() {
        assertPreviousOf(jumadaAlThani.second().of(2025), jumadaAlThani.first().of(2025));
        assertPreviousOf(jumadaAlThani.first().of(2025), jumadaAlAwwal.thirtieth().of(2025));
        assertPreviousOf(muharram.first().of(2025), duAlHijjah.twentyNinth().of(2024));
        assertPreviousOf(muharram.first().of(1443), duAlHijjah.thirtieth().of(1442));
        assertPreviousOf(duAlHijjah.thirtieth().of(1442), duAlHijjah.twentyNinth().of(1442));
        assertDaysBefore(10, duAlHijjah.twentyNinth().of(2008), duAlHijjah.nineteenth().of(2008));
        assertDaysBefore(28, duAlHijjah.twentyNinth().of(2008), duAlHijjah.first().of(2008));
        assertDaysBefore(29, duAlHijjah.twentyNinth().of(2008), duAlQiDah.thirtieth().of(2008));
        assertDaysBefore(354, duAlHijjah.thirtieth().of(1442), muharram.first().of(1442));
        assertDaysBefore(355, muharram.first().of(1443), muharram.first().of(1442));
        assertDaysBefore(0, duAlHijjah.twentyNinth().of(2008), duAlHijjah.twentyNinth().of(2008));
        assertDaysBefore(354 + 355 + 354 + 354, muharram.first().of(1445), muharram.first().of(1441));
        assertDaysBefore(354 + 355 + 354 + 354 + 30 + 29, rabiAlAwwal.first().of(1445),
                muharram.first().of(1441));
    }

    @Test
    void testPrintString() {
        assertEquals("Rabi' al-Awwal 1, 2025 AH", rabiAlAwwal.of(2025).first().toString());
        assertEquals("Muharram 12, 2025 AH", muharram.of(2025).twelfth().toString());
    }

    @Test
    void testSubtraction() {
        assertEquals(days(0), muharram.first().of(2025).minus(muharram.first().of(2025)));
        assertEquals(days(1), muharram.second().of(2025).minus(muharram.first().of(2025)));
        assertEquals(days(354), muharram.first().of(2026).minus(muharram.first().of(2025)));
    }
}
