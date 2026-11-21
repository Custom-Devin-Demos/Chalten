package org.chalten.hebrew;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.chalten.core.GregorianMonth;
import org.chalten.measure.DiscreteInterval;
import org.junit.jupiter.api.Test;

class HebrewDateTest extends HebrewCalendarTest {

    private void assertDaysAfter(long dayCount, HebrewDate date, HebrewDate anotherDate) {
        assertEquals(anotherDate, date.next(days(dayCount)));
    }

    private void assertDaysBefore(long dayCount, HebrewDate date, HebrewDate anotherDate) {
        assertEquals(anotherDate, date.previous(days(dayCount)));
    }

    /** Sends {@code first} to {@code lastDayNumber}-th to {@code month}, then {@code , yearNumber}. */
    private static void assertDayNumbers(HebrewMonth month, long yearNumber, int lastDayNumber) {
        for (int dayNumber = 1; dayNumber <= lastDayNumber; dayNumber++) {
            assertEquals(dayNumber, date(month, dayNumber, yearNumber).dayNumber());
        }
    }

    @Test
    void testAccessing() {
        HebrewDate date = adarI.first().of(2025);
        assertEquals(1, date.dayNumber());
        assertEquals(y(2025), date.year());
        assertEquals(2025, date.yearNumber());
        assertEquals(adarI.of(2025), date.monthOfYear());
        assertEquals(adarI, date.month());
        assertEquals(6, date.monthNumber());
    }

    @Test
    void testBetweenAnd() {
        assertTrue(tishrei.first().of(5675).between(tishrei.first().of(5675), tishrei.second().of(5675)));
        assertTrue(tishrei.second().of(5675).between(tishrei.first().of(5675), tishrei.second().of(5675)));
        assertTrue(tishrei.second().of(5675).between(tishrei.first().of(5675), tishrei.third().of(5675)));
        assertTrue(cheshvan.first().of(5675).between(tishrei.first().of(5675), cheshvan.first().of(5675)));
        assertTrue(cheshvan.first().of(5675).between(tishrei.first().of(5675), cheshvan.third().of(5675)));
        assertTrue(cheshvan.first().of(5675).between(tishrei.first().of(5675), adar.fifth().of(5675)));
        assertFalse(tishrei.eleventh().of(5675).between(tishrei.twelfth().of(5675), tishrei.thirtieth().of(5675)));
        assertFalse(tishrei.twentieth().of(5675).between(tishrei.first().of(5675), tishrei.eleventh().of(5675)));
    }

    @Test
    void testCannotComputeDateDistanceWhenTheSecondIsGreater() {
        assertEquals("Cannot subtract two dates when the second one is greater than the first one.",
                assertThrows(ArithmeticException.class,
                        () -> tishrei.second().of(2025).minus(tishrei.third().of(2025))).getMessage());
    }

    @Test
    void testComparing() {
        assertEquals(tishrei.first().of(5786), tishrei.first().of(5786));
        assertNotEquals(tishrei.first().of(5786), tishrei.second().of(5786));
        assertNotEquals(tishrei.first().of(5786), cheshvan.first().of(5786));
        assertNotEquals(tishrei.first().of(5786), tishrei.first().of(2024));
        assertNotEquals(tishrei.first().of(5786), january.first().of(5786));
    }

    @Test
    void testDayOfWeek() {
        assertEquals(HebrewDayOfWeek.YOM_SHLISHI, tevet.seventeenth().of(5786).dayOfWeek());
        assertEquals(HebrewDayOfWeek.YOM_REVII, tevet.eighteenth().of(5786).dayOfWeek());
        assertEquals(HebrewDayOfWeek.YOM_HAMISHI, tevet.nineteenth().of(5786).dayOfWeek());
        assertEquals(HebrewDayOfWeek.YOM_SHISHI, tevet.twentieth().of(5786).dayOfWeek());
        assertEquals(HebrewDayOfWeek.YOM_SHABBAT, tevet.twentyFirst().of(5786).dayOfWeek());
        assertEquals(HebrewDayOfWeek.YOM_RISHON, tevet.twentySecond().of(5786).dayOfWeek());
        assertEquals(HebrewDayOfWeek.YOM_SHENI, tevet.twentyThird().of(5786).dayOfWeek());
        assertEquals(HebrewDayOfWeek.YOM_SHLISHI, tevet.twentyFourth().of(5786).dayOfWeek());
    }

    @Test
    void testDaysBetweenTwoDates() {
        assertTrue(tishrei.first().of(2025).minus(tishrei.first().of(2025)).isZero());
        assertEquals(days(1), tishrei.second().of(2025).minus(tishrei.first().of(2025)));
        assertEquals(days(30), cheshvan.first().of(2025).minus(tishrei.first().of(2025)));
        assertEquals(days(30 + 29), kislev.first().of(2025).minus(tishrei.first().of(2025)));
        assertEquals(days(383), tishrei.first().of(2026).minus(tishrei.first().of(2025)));
        assertEquals(days(352), elul.twentyNinth().of(5675).minus(tishrei.first().of(5675)));
        assertEquals(days(353), tishrei.first().of(5676).minus(tishrei.first().of(5675)));
    }

    @Test
    void testGreaterThan() {
        assertIsGreaterThan(tishrei.first().of(2025), tishrei.first().of(2024));
        assertIsGreaterThanOrEqualTo(tishrei.first().of(2025), tishrei.first().of(2024));
        assertIsGreaterThanOrEqualTo(tishrei.first().of(2025), tishrei.first().of(2025));
        assertIsGreaterThan(cheshvan.first().of(2025), tishrei.first().of(2025));
        assertIsGreaterThanOrEqualTo(cheshvan.first().of(2025), tishrei.first().of(2025));
        assertIsGreaterThanOrEqualTo(tishrei.first().of(2025), elul.first().of(2024));
        assertIsGreaterThan(cheshvan.second().of(2025), cheshvan.first().of(2025));
        assertIsGreaterThanOrEqualTo(cheshvan.second().of(2025), cheshvan.first().of(2025));
        denyIsGreaterThan(tishrei.first().of(1), tishrei.first().of(2025));
        denyIsGreaterThan(tishrei.first().of(2025), cheshvan.first().of(2025));
    }

    @Test
    void testInstanceCreationFor29DaysMonth() {
        assertDayNumbers(tevet, 2025, 29);
        assertThrows(UnsupportedOperationException.class, () -> tevet.thirtieth().of(2025));
    }

    @Test
    void testInstanceCreationFor30DaysMonth() {
        assertDayNumbers(nisan, 2025, 30);
    }

    @Test
    void testInstanceCreationForCheshvanOnCompleteYear() {
        assertTrue(y(5678).isCompleteYear());
        assertDayNumbers(cheshvan, 5678, 30);
    }

    @Test
    void testInstanceCreationForCheshvanOnDeficientYear() {
        assertTrue(y(5675).isDeficientYear());
        assertDayNumbers(cheshvan, 5675, 29);
        assertThrows(UnsupportedOperationException.class, () -> cheshvan.of(5675).thirtieth());
    }

    @Test
    void testInstanceCreationForCheshvanOnRegularYear() {
        assertTrue(y(5674).isRegularYear());
        assertDayNumbers(cheshvan, 5674, 29);
        assertThrows(UnsupportedOperationException.class, () -> cheshvan.of(5674).thirtieth());
    }

    @Test
    void testInstanceCreationForKislevOnCompleteYear() {
        assertTrue(y(5678).isCompleteYear());
        assertDayNumbers(kislev, 5678, 30);
    }

    @Test
    void testInstanceCreationForKislevOnDeficientYear() {
        assertTrue(y(5675).isDeficientYear());
        assertDayNumbers(kislev, 5675, 29);
        assertThrows(UnsupportedOperationException.class, () -> kislev.of(5675).thirtieth());
    }

    @Test
    void testInstanceCreationForKislevOnRegularYear() {
        assertTrue(y(5674).isRegularYear());
        assertDayNumbers(kislev, 5674, 30);
    }

    @Test
    void testInterval() {
        DiscreteInterval<HebrewDate> interval = tishrei.first().of(5786).to(tishrei.thirtieth().of(5786));
        assertEquals(30, interval.size());
        for (int index = 1; index <= interval.size(); index++) {
            assertEquals(index, interval.get(index - 1).dayNumber());
            assertEquals(tishrei.of(5786), interval.get(index - 1).monthOfYear());
        }
    }

    @Test
    void testIntervalWithStep() {
        DiscreteInterval<HebrewDate> interval =
                tishrei.first().of(5786).to(tishrei.thirtieth().of(5786), days(2));
        assertEquals(15, interval.size());
        for (int index = 1; index <= interval.size(); index++) {
            assertEquals(index * 2 - 1, interval.get(index - 1).dayNumber());
            assertEquals(tishrei.of(5786), interval.get(index - 1).monthOfYear());
        }
    }

    @Test
    void testIsLastDayOfMonth() {
        assertTrue(tevet.twentyNinth().of(2025).isLastDayOfMonth());
        assertTrue(nisan.thirtieth().of(2025).isLastDayOfMonth());
        assertTrue(cheshvan.thirtieth().of(5678).isLastDayOfMonth());
        assertFalse(cheshvan.twentyNinth().of(5678).isLastDayOfMonth());
        assertTrue(cheshvan.twentyNinth().of(5675).isLastDayOfMonth());
        assertTrue(cheshvan.twentyNinth().of(5674).isLastDayOfMonth());
        assertTrue(kislev.thirtieth().of(5678).isLastDayOfMonth());
        assertFalse(kislev.twentyNinth().of(5678).isLastDayOfMonth());
        assertTrue(kislev.twentyNinth().of(5675).isLastDayOfMonth());
        assertTrue(kislev.thirtieth().of(5674).isLastDayOfMonth());
        assertFalse(kislev.twentyNinth().of(5674).isLastDayOfMonth());
    }

    @Test
    void testLessThan() {
        assertIsLessThan(tishrei.first().of(2024), tishrei.first().of(2025));
        assertIsLessThanOrEqualTo(tishrei.first().of(2024), tishrei.first().of(2025));
        assertIsLessThanOrEqualTo(tishrei.first().of(2025), tishrei.first().of(2025));
        assertIsLessThan(tishrei.first().of(2025), cheshvan.first().of(2025));
        assertIsLessThanOrEqualTo(tishrei.first().of(2025), cheshvan.first().of(2025));
        assertIsLessThanOrEqualTo(elul.first().of(2024), tishrei.first().of(2025));
        assertIsLessThan(cheshvan.first().of(2025), cheshvan.second().of(2025));
        assertIsLessThanOrEqualTo(cheshvan.first().of(2025), cheshvan.second().of(2025));
        denyIsLessThan(tishrei.first().of(2025), tishrei.first().of(1));
        denyIsLessThan(cheshvan.first().of(2025), tishrei.first().of(2025));
        // Java's static types reject comparing dates of different calendars; the years still refuse it.
        assertEquals("Years in different time scales are incommensurable",
                assertThrows(ArithmeticException.class, () -> compareUnchecked(tishrei.first().of(2025).year(),
                        GregorianMonth.JANUARY.first().of(2025).year())).getMessage());
    }

    @Test
    void testNext() {
        assertNextOf(adarI.first().of(2025), adarI.second().of(2025));
        assertNextOf(adarI.thirtieth().of(2025), adarII.first().of(2025));
        assertNextOf(elul.twentyNinth().of(2025), tishrei.first().of(2026));
        assertNextOf(cheshvan.twentyEighth().of(2020), cheshvan.twentyNinth().of(2020));
        assertNextOf(elul.twentyNinth().of(1442), tishrei.first().of(1443));
        assertDaysAfter(10, tishrei.first().of(1443), tishrei.eleventh().of(1443));
        assertDaysAfter(29, tishrei.first().of(1443), tishrei.thirtieth().of(1443));
        assertDaysAfter(30, tishrei.first().of(1443), cheshvan.first().of(1443));
        assertDaysAfter(355, tishrei.first().of(1443), tishrei.first().of(1444));
        assertDaysAfter(383, tishrei.first().of(1442), tishrei.first().of(1443));
        assertDaysAfter(0, tishrei.first().of(1443), tishrei.first().of(1443));
        assertDaysAfter(354 + 383 + 355 + 385, tishrei.first().of(1441), tishrei.first().of(1445));
        assertDaysAfter(354 + 383 + 355 + 385 + 30 + 29, tishrei.first().of(1441), kislev.first().of(1445));
    }

    @Test
    void testPrevious() {
        assertPreviousOf(adarI.second().of(2025), adarI.first().of(2025));
        assertPreviousOf(adarI.first().of(2025), shevat.thirtieth().of(2025));
        assertPreviousOf(tishrei.first().of(2025), elul.twentyNinth().of(2024));
        assertPreviousOf(tishrei.first().of(1443), elul.twentyNinth().of(1442));
        assertPreviousOf(elul.twentyNinth().of(1442), elul.twentyEighth().of(1442));
        assertDaysBefore(10, elul.twentyNinth().of(2008), elul.nineteenth().of(2008));
        assertDaysBefore(28, elul.twentyNinth().of(2008), elul.first().of(2008));
        assertDaysBefore(29, elul.twentyNinth().of(2008), av.thirtieth().of(2008));
        assertDaysBefore(382, elul.twentyNinth().of(1442), tishrei.first().of(1442));
        assertDaysBefore(383, tishrei.first().of(1443), tishrei.first().of(1442));
        assertDaysBefore(0, elul.twentyNinth().of(2008), elul.twentyNinth().of(2008));
        assertDaysBefore(354 + 383 + 355 + 385, tishrei.first().of(1445), tishrei.first().of(1441));
        assertDaysBefore(354 + 383 + 355 + 385 + 30 + 29, kislev.first().of(1445), tishrei.first().of(1441));
    }

    @Test
    void testPrintString() {
        assertEquals("Adar II 1, 2025 AM", adarII.of(2025).first().toString());
        assertEquals("Tishrei 12, 2025 AM", tishrei.of(2025).twelfth().toString());
    }

    @Test
    void testSubtraction() {
        assertEquals(days(0), tishrei.first().of(5786).minus(tishrei.first().of(5786)));
        assertEquals(days(1), tishrei.second().of(5786).minus(tishrei.first().of(5786)));
        assertEquals(days(354), tishrei.first().of(5787).minus(tishrei.first().of(5786)));
    }
}
