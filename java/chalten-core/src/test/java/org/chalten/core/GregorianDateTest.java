package org.chalten.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.UnaryOperator;
import org.chalten.measure.DiscreteInterval;
import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.measure.TimeQuantity;
import org.chalten.testkit.ChaltenTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class GregorianDateTest extends ChaltenTest {

    private void assertDaysAfter(long dayCount, GregorianDate date, GregorianDate anotherDate) {
        assertEquals(anotherDate, date.next(TimeQuantity.of(dayCount, day)));
    }

    private void assertDaysBefore(long dayCount, GregorianDate date, GregorianDate anotherDate) {
        assertEquals(anotherDate, date.previous(TimeQuantity.of(dayCount, day)));
    }

    private static GregorianDate parseISO8601(String string) {
        return new ISO8601Parser().parseDate(string);
    }

    private static void assertRaises(Executable block, String messageText) {
        assertEquals(messageText, assertThrows(InstanceCreationFailedException.class, block).getMessage());
    }

    private static void assertEachRaises(List<String> invalidStrings, List<String> parts,
            UnaryOperator<String> messageText) {
        for (int index = 0; index < invalidStrings.size(); index++) {
            String invalidString = invalidStrings.get(index);
            assertRaises(() -> parseISO8601(invalidString), messageText.apply(parts.get(index)));
        }
    }

    @Test
    void testAccessing() {
        GregorianDate date = june.first().of(2025);
        assertEquals(1, date.dayNumber());
        assertEquals(GregorianYear.of(2025), date.year());
        assertEquals(2025, date.yearNumber());
        assertEquals(june.of(2025), date.monthOfYear());
        assertEquals(june, date.month());
        assertEquals(6, date.monthNumber());
    }

    @Test
    void testBetweenAnd() {
        assertTrue(january.first().of(2025).between(january.first().of(2025), january.second().of(2025)));
        assertTrue(january.second().of(2025).between(january.first().of(2025), january.second().of(2025)));
        assertTrue(january.second().of(2025).between(january.first().of(2025), january.third().of(2025)));
        assertTrue(february.first().of(2025).between(january.first().of(2025), february.first().of(2025)));
        assertTrue(february.first().of(2025).between(january.first().of(2025), february.third().of(2025)));
        assertTrue(february.first().of(2025).between(january.first().of(2025), july.fifth().of(2025)));
        assertFalse(january.eleventh().of(2025).between(january.twelfth().of(2025), january.thirtieth().of(2025)));
        assertFalse(january.twentieth().of(2025).between(january.first().of(2025), january.eleventh().of(2025)));
    }

    @Test
    void testCannotComputeDateDistanceWhenTheSecondIsGreater() {
        ArithmeticException exception = assertThrows(ArithmeticException.class,
                () -> january.second().of(2025).minus(january.third().of(2025)));
        assertEquals("Cannot subtract two dates when the second one is greater than the first one.",
                exception.getMessage());
    }

    @Test
    void testComparing() {
        assertEquals(january.first().of(2025), january.first().of(2025));
        assertEquals(january.first().of(2025).hashCode(), january.first().of(2025).hashCode());
        assertNotEquals(january.second().of(2025), january.first().of(2025));
        assertNotEquals(february.first().of(2025), january.first().of(2025));
        assertNotEquals(january.first().of(2024), january.first().of(2025));
    }

    @Test
    void testDayOfWeek() {
        assertEquals(DayOfWeek.WEDNESDAY, january.first().of(2025).dayOfWeek());
        assertEquals(DayOfWeek.THURSDAY, january.second().of(2025).dayOfWeek());
        assertEquals(DayOfWeek.SATURDAY, february.first().of(2025).dayOfWeek());
        assertEquals(DayOfWeek.SATURDAY, march.first().of(2025).dayOfWeek());
        assertEquals(DayOfWeek.THURSDAY, january.first().of(2026).dayOfWeek());
        assertEquals(DayOfWeek.WEDNESDAY, december.thirtyFirst().of(2008).dayOfWeek());
        assertEquals(DayOfWeek.THURSDAY, january.first().of(2009).dayOfWeek());
        assertEquals(DayOfWeek.FRIDAY, january.second().of(2009).dayOfWeek());
        assertEquals(DayOfWeek.SATURDAY, january.third().of(2009).dayOfWeek());
        assertEquals(DayOfWeek.SUNDAY, january.fourth().of(2009).dayOfWeek());
        assertEquals(DayOfWeek.MONDAY, january.fifth().of(2009).dayOfWeek());
        assertEquals(DayOfWeek.TUESDAY, january.sixth().of(2009).dayOfWeek());
        assertEquals(DayOfWeek.WEDNESDAY, january.seventh().of(2009).dayOfWeek());
    }

    @Test
    void testDaysBetweenTwoDates() {
        assertTrue(january.first().of(2025).minus(january.first().of(2025)).isZero());
        assertEquals(TimeQuantity.of(1, day), january.second().of(2025).minus(january.first().of(2025)));
        assertEquals(TimeQuantity.of(31, day), february.first().of(2025).minus(january.first().of(2025)));
        assertEquals(TimeQuantity.of(31 + 28, day), march.first().of(2025).minus(january.first().of(2025)));
        assertEquals(TimeQuantity.of(365, day), january.first().of(2026).minus(january.first().of(2025)));
        assertEquals(TimeQuantity.of(365, day), december.thirtyFirst().of(2008).minus(january.first().of(2008)));
        assertEquals(TimeQuantity.of(366, day), january.first().of(2009).minus(january.first().of(2008)));
        assertThrows(ArithmeticException.class, () -> january.first().of(2008).minus(january.first().of(2009)));
    }

    @Test
    void testGreaterThan() {
        assertIsGreaterThan(january.first().of(2025), january.first().of(2024));
        assertIsGreaterThanOrEqualTo(january.first().of(2025), january.first().of(2024));
        assertIsGreaterThanOrEqualTo(january.first().of(2025), january.first().of(2025));
        assertIsGreaterThan(february.first().of(2025), january.first().of(2025));
        assertIsGreaterThanOrEqualTo(february.first().of(2025), january.first().of(2025));
        assertIsGreaterThanOrEqualTo(january.first().of(2025), december.first().of(2024));
        assertIsGreaterThan(february.second().of(2025), february.first().of(2025));
        assertIsGreaterThanOrEqualTo(february.second().of(2025), february.first().of(2025));
        denyIsGreaterThan(january.first().of(0), january.first().of(2025));
        denyIsGreaterThan(january.first().of(2025), february.first().of(2025));
    }

    @Test
    void testISO8601Printing() {
        assertEquals("2025-01-12", printAsISO8601String(january.of(2025).twelfth()));
        assertEquals("2025-04-01", printAsISO8601String(april.of(2025).first()));
        assertEquals("0000-04-01", printAsISO8601String(april.of(0).first()));
        assertEquals("0150-04-01", printAsISO8601String(april.of(150).first()));
        assertEquals("-0001-04-01", printAsISO8601String(april.of(-1).first()));
        assertEquals("+010000-04-01", printAsISO8601String(april.of(10000).first()));
        assertEquals("-054000-04-01", printAsISO8601String(april.of(-54000).first()));
        assertEquals("+9999000-04-01", printAsISO8601String(april.of(9999000).first()));
        assertEquals("-9999000-04-01", printAsISO8601String(april.of(-9999000).first()));
    }

    @Test
    void testInstanceCreationFor30DaysMonth() {
        assertEquals(1, april.first().of(2025).dayNumber());
        assertEquals(2, april.second().of(2025).dayNumber());
        assertEquals(3, april.third().of(2025).dayNumber());
        assertEquals(4, april.fourth().of(2025).dayNumber());
        assertEquals(5, april.fifth().of(2025).dayNumber());
        assertEquals(6, april.sixth().of(2025).dayNumber());
        assertEquals(7, april.seventh().of(2025).dayNumber());
        assertEquals(8, april.eighth().of(2025).dayNumber());
        assertEquals(9, april.ninth().of(2025).dayNumber());
        assertEquals(10, april.tenth().of(2025).dayNumber());
        assertEquals(11, april.eleventh().of(2025).dayNumber());
        assertEquals(12, april.twelfth().of(2025).dayNumber());
        assertEquals(13, april.thirteenth().of(2025).dayNumber());
        assertEquals(14, april.fourteenth().of(2025).dayNumber());
        assertEquals(15, april.fifteenth().of(2025).dayNumber());
        assertEquals(16, april.sixteenth().of(2025).dayNumber());
        assertEquals(17, april.seventeenth().of(2025).dayNumber());
        assertEquals(18, april.eighteenth().of(2025).dayNumber());
        assertEquals(19, april.nineteenth().of(2025).dayNumber());
        assertEquals(20, april.twentieth().of(2025).dayNumber());
        assertEquals(21, april.twentyFirst().of(2025).dayNumber());
        assertEquals(22, april.twentySecond().of(2025).dayNumber());
        assertEquals(23, april.twentyThird().of(2025).dayNumber());
        assertEquals(24, april.twentyFourth().of(2025).dayNumber());
        assertEquals(25, april.twentyFifth().of(2025).dayNumber());
        assertEquals(26, april.twentySixth().of(2025).dayNumber());
        assertEquals(27, april.twentySeventh().of(2025).dayNumber());
        assertEquals(28, april.twentyEighth().of(2025).dayNumber());
        assertEquals(29, april.twentyNinth().of(2025).dayNumber());
        assertEquals(30, april.thirtieth().of(2025).dayNumber());
        assertThrows(UnsupportedOperationException.class, () -> april.thirtyFirst());
    }

    @Test
    void testInstanceCreationFor31DaysMonth() {
        assertEquals(1, january.first().of(2025).dayNumber());
        assertEquals(2, january.second().of(2025).dayNumber());
        assertEquals(3, january.third().of(2025).dayNumber());
        assertEquals(4, january.fourth().of(2025).dayNumber());
        assertEquals(5, january.fifth().of(2025).dayNumber());
        assertEquals(6, january.sixth().of(2025).dayNumber());
        assertEquals(7, january.seventh().of(2025).dayNumber());
        assertEquals(8, january.eighth().of(2025).dayNumber());
        assertEquals(9, january.ninth().of(2025).dayNumber());
        assertEquals(10, january.tenth().of(2025).dayNumber());
        assertEquals(11, january.eleventh().of(2025).dayNumber());
        assertEquals(12, january.twelfth().of(2025).dayNumber());
        assertEquals(13, january.thirteenth().of(2025).dayNumber());
        assertEquals(14, january.fourteenth().of(2025).dayNumber());
        assertEquals(15, january.fifteenth().of(2025).dayNumber());
        assertEquals(16, january.sixteenth().of(2025).dayNumber());
        assertEquals(17, january.seventeenth().of(2025).dayNumber());
        assertEquals(18, january.eighteenth().of(2025).dayNumber());
        assertEquals(19, january.nineteenth().of(2025).dayNumber());
        assertEquals(20, january.twentieth().of(2025).dayNumber());
        assertEquals(21, january.twentyFirst().of(2025).dayNumber());
        assertEquals(22, january.twentySecond().of(2025).dayNumber());
        assertEquals(23, january.twentyThird().of(2025).dayNumber());
        assertEquals(24, january.twentyFourth().of(2025).dayNumber());
        assertEquals(25, january.twentyFifth().of(2025).dayNumber());
        assertEquals(26, january.twentySixth().of(2025).dayNumber());
        assertEquals(27, january.twentySeventh().of(2025).dayNumber());
        assertEquals(28, january.twentyEighth().of(2025).dayNumber());
        assertEquals(29, january.twentyNinth().of(2025).dayNumber());
        assertEquals(30, january.thirtieth().of(2025).dayNumber());
        assertEquals(31, january.thirtyFirst().of(2025).dayNumber());
    }

    @Test
    void testInstanceCreationForFebruaryOnCommonYear() {
        assertEquals(1, february.first().of(2025).dayNumber());
        assertEquals(2, february.second().of(2025).dayNumber());
        assertEquals(3, february.third().of(2025).dayNumber());
        assertEquals(4, february.fourth().of(2025).dayNumber());
        assertEquals(5, february.fifth().of(2025).dayNumber());
        assertEquals(6, february.sixth().of(2025).dayNumber());
        assertEquals(7, february.seventh().of(2025).dayNumber());
        assertEquals(8, february.eighth().of(2025).dayNumber());
        assertEquals(9, february.ninth().of(2025).dayNumber());
        assertEquals(10, february.tenth().of(2025).dayNumber());
        assertEquals(11, february.eleventh().of(2025).dayNumber());
        assertEquals(12, february.twelfth().of(2025).dayNumber());
        assertEquals(13, february.thirteenth().of(2025).dayNumber());
        assertEquals(14, february.fourteenth().of(2025).dayNumber());
        assertEquals(15, february.fifteenth().of(2025).dayNumber());
        assertEquals(16, february.sixteenth().of(2025).dayNumber());
        assertEquals(17, february.seventeenth().of(2025).dayNumber());
        assertEquals(18, february.eighteenth().of(2025).dayNumber());
        assertEquals(19, february.nineteenth().of(2025).dayNumber());
        assertEquals(20, february.twentieth().of(2025).dayNumber());
        assertEquals(21, february.twentyFirst().of(2025).dayNumber());
        assertEquals(22, february.twentySecond().of(2025).dayNumber());
        assertEquals(23, february.twentyThird().of(2025).dayNumber());
        assertEquals(24, february.twentyFourth().of(2025).dayNumber());
        assertEquals(25, february.twentyFifth().of(2025).dayNumber());
        assertEquals(26, february.twentySixth().of(2025).dayNumber());
        assertEquals(27, february.twentySeventh().of(2025).dayNumber());
        assertEquals(28, february.twentyEighth().of(2025).dayNumber());
        InstanceCreationFailedException exception = assertThrows(InstanceCreationFailedException.class,
                () -> february.twentyNinth().of(2025));
        assertEquals("29 is an invalid day number in February, 2025", exception.getMessage());
        assertThrows(UnsupportedOperationException.class, () -> february.thirtieth());
        assertThrows(UnsupportedOperationException.class, () -> february.thirtyFirst());
    }

    @Test
    void testInstanceCreationForFebruaryOnLeapYear() {
        assertEquals(1, february.first().of(2020).dayNumber());
        assertEquals(2, february.second().of(2020).dayNumber());
        assertEquals(3, february.third().of(2020).dayNumber());
        assertEquals(4, february.fourth().of(2020).dayNumber());
        assertEquals(5, february.fifth().of(2020).dayNumber());
        assertEquals(6, february.sixth().of(2020).dayNumber());
        assertEquals(7, february.seventh().of(2020).dayNumber());
        assertEquals(8, february.eighth().of(2020).dayNumber());
        assertEquals(9, february.ninth().of(2020).dayNumber());
        assertEquals(10, february.tenth().of(2020).dayNumber());
        assertEquals(11, february.eleventh().of(2020).dayNumber());
        assertEquals(12, february.twelfth().of(2020).dayNumber());
        assertEquals(13, february.thirteenth().of(2020).dayNumber());
        assertEquals(14, february.fourteenth().of(2020).dayNumber());
        assertEquals(15, february.fifteenth().of(2020).dayNumber());
        assertEquals(16, february.sixteenth().of(2020).dayNumber());
        assertEquals(17, february.seventeenth().of(2020).dayNumber());
        assertEquals(18, february.eighteenth().of(2020).dayNumber());
        assertEquals(19, february.nineteenth().of(2020).dayNumber());
        assertEquals(20, february.twentieth().of(2020).dayNumber());
        assertEquals(21, february.twentyFirst().of(2020).dayNumber());
        assertEquals(22, february.twentySecond().of(2020).dayNumber());
        assertEquals(23, february.twentyThird().of(2020).dayNumber());
        assertEquals(24, february.twentyFourth().of(2020).dayNumber());
        assertEquals(25, february.twentyFifth().of(2020).dayNumber());
        assertEquals(26, february.twentySixth().of(2020).dayNumber());
        assertEquals(27, february.twentySeventh().of(2020).dayNumber());
        assertEquals(28, february.twentyEighth().of(2020).dayNumber());
        assertEquals(29, february.twentyNinth().of(2020).dayNumber());
        assertThrows(UnsupportedOperationException.class, () -> february.thirtieth());
        assertThrows(UnsupportedOperationException.class, () -> february.thirtyFirst());
    }

    @Test
    void testInterval() {
        DiscreteInterval<GregorianDate> interval = january.first().of(2025).to(january.thirtyFirst().of(2025));
        assertEquals(31, interval.size());
        for (int index = 1; index <= interval.size(); index++) {
            GregorianDate date = interval.get(index - 1);
            assertEquals(index, date.dayNumber());
            assertEquals(january.of(2025), date.monthOfYear());
        }
    }

    @Test
    void testIntervalWithStep() {
        DiscreteInterval<GregorianDate> interval = january.first().of(2025)
                .to(january.thirtieth().of(2025), TimeQuantity.of(2, day));
        assertEquals(15, interval.size());
        for (int index = 1; index <= interval.size(); index++) {
            GregorianDate date = interval.get(index - 1);
            assertEquals(index * 2 - 1, date.dayNumber());
            assertEquals(january.of(2025), date.monthOfYear());
        }
    }

    @Test
    void testIsLastDayOfMonth() {
        assertTrue(february.twentyEighth().of(2025).isLastDayOfMonth());
        assertTrue(february.twentyNinth().of(2000).isLastDayOfMonth());
        assertTrue(january.thirtyFirst().of(2025).isLastDayOfMonth());
        assertFalse(february.twentyEighth().of(2000).isLastDayOfMonth());
        assertFalse(january.first().of(2025).isLastDayOfMonth());
    }

    @Test
    void testLessThan() {
        assertIsLessThan(january.first().of(2024), january.first().of(2025));
        assertIsLessThanOrEqualTo(january.first().of(2024), january.first().of(2025));
        assertIsLessThanOrEqualTo(january.first().of(2025), january.first().of(2025));
        assertIsLessThan(january.first().of(2025), february.first().of(2025));
        assertIsLessThanOrEqualTo(january.first().of(2025), february.first().of(2025));
        assertIsLessThanOrEqualTo(december.first().of(2024), january.first().of(2025));
        assertIsLessThan(february.first().of(2025), february.second().of(2025));
        assertIsLessThanOrEqualTo(february.first().of(2025), february.second().of(2025));
        denyIsLessThan(january.first().of(2025), january.first().of(0));
        denyIsLessThan(february.first().of(2025), january.first().of(2025));
    }

    @Test
    void testNext() {
        assertNextOf(june.first().of(2025), june.second().of(2025));
        assertNextOf(june.thirtieth().of(2025), july.first().of(2025));
        assertNextOf(december.thirtyFirst().of(2025), january.first().of(2026));
        assertNextOf(february.twentyEighth().of(2020), february.twentyNinth().of(2020));
        assertNextOf(february.twentyEighth().of(2025), march.first().of(2025));
        assertDaysAfter(10, january.first().of(2027), january.eleventh().of(2027));
        assertDaysAfter(30, january.first().of(2027), january.thirtyFirst().of(2027));
        assertDaysAfter(31, january.first().of(2027), february.first().of(2027));
        assertDaysAfter(365, january.first().of(2027), january.first().of(2028));
        assertDaysAfter(366, january.first().of(2024), january.first().of(2025));
        assertDaysAfter(0, january.first().of(2027), january.first().of(2027));
        assertDaysAfter(365 + 366 + 365 + 365, january.first().of(2007), january.first().of(2011));
        assertDaysAfter(365 + 366 + 365 + 365 + 31 + 28, january.first().of(2007), march.first().of(2011));
    }

    @Test
    void testParseISO8601() {
        assertEquals(january.first().of(2026), parseISO8601("2026-01-01"));
        assertEquals(january.second().of(0), parseISO8601("0000-01-02"));
        assertEquals(january.thirtieth().of(150), parseISO8601("0150-01-30"));
        assertEquals(january.thirtyFirst().of(-1), parseISO8601("-0001-01-31"));
        assertEquals(january.tenth().of(10000), parseISO8601("+010000-01-10"));
        assertEquals(january.fifteenth().of(-54000), parseISO8601("-054000-01-15"));
        assertEquals(january.twentieth().of(9999000), parseISO8601("+9999000-01-20"));
        assertEquals(february.twelfth().of(-9999000), parseISO8601("-9999000-02-12"));
        for (String invalidString : List.of("A", "1E10", "", "++2026", "--2022", "+X", "2022.1", "-2022-", "1", "11",
                "111", "-12", "1-01", "11-01", "111-01", "2026-", "2026", "2026-01", "2026-01-", "2026-01-1")) {
            assertRaises(() -> parseISO8601(invalidString), "The provided date '" + invalidString + "' is invalid. Dates"
                    + " must be 4 digits (e.g., 2026) or expanded ISO 8601 (e.g., +010000) for the year, exactly 2 digits"
                    + " for the month and exactly 2 digits for the day.");
        }
        assertEachRaises(List.of("222-010-12", "AAAA-01-12", "z+a12-01-12"), List.of("222", "AAAA", "z+a12"),
                yearString -> "The provided year '" + yearString + "' is invalid. Years must be 4 digits (e.g., 2026) or"
                        + " expanded ISO 8601 (e.g., +010000).");
        assertEachRaises(List.of("2026-A1-12", "2026-1E-12", "2026-+2-12", "2026--2-12", "2026-+X-12", "2026-20-12",
                "2026-00-12", "2026-100-12", "2026-AA-12", "2026-111-12"),
                List.of("A1", "1E", "+2", "2", "+X", "20", "00", "100", "AA", "111"),
                monthString -> "The provided month '" + monthString + "' is invalid. Months must be 2 digits between 01"
                        + " and 12 (e.g., 05).");
        assertEachRaises(List.of("2026-02-00", "2026-02-32", "2026-02-29", "2026-02-30"), List.of("0", "32", "29", "30"),
                dayString -> dayString + " is an invalid day number in February, 2026");
        assertRaises(() -> parseISO8601("2026-01-100"), "The provided day number '100' is invalid. Day numbers must be 2"
                + " digits between 01 and maximum of the month (e.g., 05).");
    }

    @Test
    void testPrevious() {
        assertPreviousOf(june.second().of(2025), june.first().of(2025));
        assertPreviousOf(june.first().of(2025), may.thirtyFirst().of(2025));
        assertPreviousOf(january.first().of(2025), december.thirtyFirst().of(2024));
        assertPreviousOf(february.twentyNinth().of(2020), february.twentyEighth().of(2020));
        assertPreviousOf(march.first().of(2025), february.twentyEighth().of(2025));
        assertDaysBefore(10, december.thirtyFirst().of(2008), december.twentyFirst().of(2008));
        assertDaysBefore(30, december.thirtyFirst().of(2008), december.first().of(2008));
        assertDaysBefore(31, december.thirtyFirst().of(2008), november.thirtieth().of(2008));
        assertDaysBefore(365, december.thirtyFirst().of(2008), january.first().of(2008));
        assertDaysBefore(366, january.first().of(2005), january.first().of(2004));
        assertDaysBefore(0, december.thirtyFirst().of(2008), december.thirtyFirst().of(2008));
        assertDaysBefore(365 + 366 + 365 + 365, january.first().of(2011), january.first().of(2007));
        assertDaysBefore(365 + 366 + 365 + 365 + 31 + 28, march.first().of(2011), january.first().of(2007));
    }

    @Test
    void testPrintString() {
        assertEquals("April 1, 2025", GregorianMonth.APRIL.of(2025).first().toString());
        assertEquals("January 12, 2025", GregorianMonth.JANUARY.of(2025).twelfth().toString());
    }

    @Test
    void testSubtraction() {
        assertEquals(TimeQuantity.of(364, day), GregorianYear.of(2026).lastDate().minus(GregorianYear.of(2026).firstDate()));
        assertEquals(TimeQuantity.of(365, day), GregorianYear.of(2027).lastDate().minus(GregorianYear.of(2026).lastDate()));
        assertEquals(TimeQuantity.of(1461, day), GregorianYear.of(2027).lastDate().minus(GregorianYear.of(2023).lastDate()));
    }
}
