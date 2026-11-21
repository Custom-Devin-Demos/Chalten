package org.chalten.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.chalten.measure.Fraction;
import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.measure.TimeQuantity;
import org.chalten.testkit.ChaltenTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class GregorianMonthOfYearTest extends ChaltenTest {

    private static GregorianMonthOfYear parseISO8601(String string) {
        return new ISO8601Parser().parseMonthOfYear(string);
    }

    private static void assertRaises(Executable block, String messageText) {
        assertEquals(messageText, assertThrows(InstanceCreationFailedException.class, block).getMessage());
    }

    private static Object perform(Object receiver, String selector) {
        try {
            return receiver.getClass().getMethod(selector).invoke(receiver);
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(selector, e);
        }
    }

    @Test
    void testBetweenAnd() {
        GregorianYear year0 = GregorianYear.of(0);
        GregorianYear year1 = GregorianYear.of(1);
        assertTrue(year0.january().between(year0.january(), year1.january()));
        assertTrue(year1.january().between(year0.january(), year1.january()));
        assertTrue(year1.january().between(year0.january(), GregorianYear.of(2).january()));
        assertTrue(year0.february().between(year0.january(), year0.february()));
        assertTrue(year0.february().between(year0.january(), year0.february()));
        assertTrue(year0.february().between(year0.january(), year0.july()));
        assertFalse(GregorianYear.of(2).january().between(year0.january(), year1.january()));
        assertFalse(GregorianYear.of(-1).january().between(year0.january(), year1.january()));
    }

    @Test
    void testComparing() {
        assertEquals(GregorianYear.of(2024).january(), GregorianYear.of(2024).january());
        assertNotEquals(GregorianYear.of(2024).january(), GregorianYear.of(2025).january());
        assertNotEquals(GregorianYear.of(2025).february(), GregorianYear.of(2025).january());
    }

    @Test
    void testCreation() {
        GregorianMonthOfYear monthOfYear = GregorianYear.of(2025).june();
        assertEquals(GregorianYear.of(2025), monthOfYear.year());
        assertEquals(june, monthOfYear.month());
    }

    @Test
    void testCreationShortcut() {
        GregorianMonthOfYear monthOfYear = january.of(2025);
        assertEquals(GregorianYear.of(2025), monthOfYear.year());
        assertEquals(january, monthOfYear.month());
    }

    @Test
    void testDateCreation() {
        GregorianMonthOfYear monthOfYear = december.of(2025);
        for (String selector : List.of("first", "second", "third", "fourth", "fifth", "sixth", "seventh", "eighth",
                "ninth", "tenth", "eleventh", "twelfth", "thirteenth", "fourteenth", "fifteenth", "sixteenth",
                "seventeenth", "eighteenth", "nineteenth", "twentieth", "twentyFirst", "twentySecond", "twentyThird",
                "twentyFourth", "twentyFifth", "twentySixth", "twentySeventh", "twentyEighth", "twentyNinth",
                "thirtieth", "thirtyFirst")) {
            CalendarDayOfMonth<?> dayOfMonth = (CalendarDayOfMonth<?>) perform(december, selector);
            assertEquals(dayOfMonth.of(2025), perform(monthOfYear, selector), selector);
        }
    }

    @Test
    void testGreaterThan() {
        assertIsGreaterThan(january.of(1), january.of(0));
        assertIsGreaterThanOrEqualTo(january.of(1), january.of(0));
        assertIsGreaterThanOrEqualTo(january.of(0), january.of(0));
        assertIsGreaterThan(february.of(0), january.of(0));
        assertIsGreaterThanOrEqualTo(february.of(0), january.of(0));
        assertIsGreaterThan(february.of(1), january.of(0));
        denyIsGreaterThan(january.of(0), january.of(2025));
        denyIsGreaterThan(january.of(0), february.of(0));
    }

    @Test
    void testISO8601Printing() {
        assertEquals("2026-10", printAsISO8601String(october.of(2026)));
        assertEquals("2026-02", printAsISO8601String(february.of(2026)));
        assertEquals("0000-02", printAsISO8601String(february.of(0)));
        assertEquals("0150-02", printAsISO8601String(february.of(150)));
        assertEquals("-0001-02", printAsISO8601String(february.of(-1)));
        assertEquals("+010000-02", printAsISO8601String(february.of(10000)));
        assertEquals("-054000-02", printAsISO8601String(february.of(-54000)));
        assertEquals("+9999000-02", printAsISO8601String(february.of(9999000)));
        assertEquals("-9999000-02", printAsISO8601String(february.of(-9999000)));
    }

    @Test
    void testLessThan() {
        assertIsLessThan(january.of(0), january.of(1));
        assertIsLessThanOrEqualTo(january.of(0), january.of(1));
        assertIsLessThanOrEqualTo(january.of(0), january.of(0));
        assertIsLessThanOrEqualTo(january.of(0), february.of(0));
        assertIsLessThanOrEqualTo(february.of(0), january.of(1));
        denyIsLessThan(january.of(2025), january.of(0));
        denyIsLessThan(february.of(0), january.of(0));
    }

    @Test
    void testMonthsBetweenTwoMonthsOfYear() {
        assertEquals(TimeQuantity.of(0, month), december.of(2026).minus(december.of(2026)));
        assertEquals(TimeQuantity.of(1, month), december.of(2026).minus(november.of(2026)));
        assertEquals(TimeQuantity.of(2, month), december.of(2026).minus(october.of(2026)));
        assertEquals(TimeQuantity.of(3, month), december.of(2026).minus(september.of(2026)));
        assertEquals(TimeQuantity.of(4, month), december.of(2026).minus(august.of(2026)));
        assertEquals(TimeQuantity.of(5, month), december.of(2026).minus(july.of(2026)));
        assertEquals(TimeQuantity.of(6, month), december.of(2026).minus(june.of(2026)));
        assertEquals(TimeQuantity.of(7, month), december.of(2026).minus(may.of(2026)));
        assertEquals(TimeQuantity.of(8, month), december.of(2026).minus(april.of(2026)));
        assertEquals(TimeQuantity.of(9, month), december.of(2026).minus(march.of(2026)));
        assertEquals(TimeQuantity.of(10, month), december.of(2026).minus(february.of(2026)));
        assertEquals(TimeQuantity.of(11, month), december.of(2026).minus(january.of(2026)));
        assertEquals(TimeQuantity.of(12, month), december.of(2026).minus(december.of(2025)));
        assertEquals(TimeQuantity.of(24, month), december.of(2027).minus(december.of(2025)));
        assertEquals(TimeQuantity.of(26, month), july.of(2027).minus(may.of(2025)));
        ArithmeticException exception = assertThrows(ArithmeticException.class,
                () -> december.of(2008).minus(december.of(2009)));
        assertEquals("Cannot subtract two months of year when the start month is greater than the end month.",
                exception.getMessage());
    }

    @Test
    void testNext() {
        GregorianYear baseYear = GregorianYear.of(2025);
        assertNextOf(baseYear.january(), baseYear.february());
        assertNextOf(baseYear.december(), GregorianYear.of(2026).january());
        assertEquals(baseYear.january(), GregorianYear.of(0).january().next(TimeQuantity.of(2025, year)));
        assertEquals(GregorianYear.of(2026).april(), baseYear.april().next(TimeQuantity.of(12, month)));
        assertEquals(GregorianYear.of(2024).july(), baseYear.july().next(TimeQuantity.of(-1, year)));
        assertEquals(baseYear.january(), baseYear.july().next(TimeQuantity.of(Fraction.of(-1, 2), year)));
        assertEquals(GregorianYear.of(2030).june(), GregorianYear.of(2020).june().next(TimeQuantity.of(1, decade)));
        assertEquals(GregorianYear.of(2026).march(), baseYear.september().next(TimeQuantity.of(Fraction.of(1, 2), year)));
        ArithmeticException exception = assertThrows(ArithmeticException.class,
                () -> baseYear.next(TimeQuantity.of(30, day)));
        assertEquals("Cannot convert incommensurable units", exception.getMessage());
    }

    @Test
    void testNumberOfDays() {
        assertEquals(TimeQuantity.of(31, day), GregorianYear.of(2025).january().numberOfDays());
        assertEquals(TimeQuantity.of(31, day), GregorianYear.of(2020).january().numberOfDays());
        assertEquals(TimeQuantity.of(28, day), GregorianYear.of(2025).february().numberOfDays());
        assertEquals(TimeQuantity.of(29, day), GregorianYear.of(2020).february().numberOfDays());
        assertEquals(TimeQuantity.of(31, day), GregorianYear.of(2025).march().numberOfDays());
        assertEquals(TimeQuantity.of(31, day), GregorianYear.of(2020).march().numberOfDays());
        assertEquals(TimeQuantity.of(30, day), GregorianYear.of(2025).april().numberOfDays());
        assertEquals(TimeQuantity.of(30, day), GregorianYear.of(2020).april().numberOfDays());
        assertEquals(TimeQuantity.of(31, day), GregorianYear.of(2025).may().numberOfDays());
        assertEquals(TimeQuantity.of(31, day), GregorianYear.of(2020).may().numberOfDays());
        assertEquals(TimeQuantity.of(30, day), GregorianYear.of(2025).june().numberOfDays());
        assertEquals(TimeQuantity.of(30, day), GregorianYear.of(2020).june().numberOfDays());
        assertEquals(TimeQuantity.of(31, day), GregorianYear.of(2025).july().numberOfDays());
        assertEquals(TimeQuantity.of(31, day), GregorianYear.of(2020).july().numberOfDays());
        assertEquals(TimeQuantity.of(31, day), GregorianYear.of(2025).august().numberOfDays());
        assertEquals(TimeQuantity.of(31, day), GregorianYear.of(2020).august().numberOfDays());
        assertEquals(TimeQuantity.of(30, day), GregorianYear.of(2025).september().numberOfDays());
        assertEquals(TimeQuantity.of(30, day), GregorianYear.of(2020).september().numberOfDays());
        assertEquals(TimeQuantity.of(31, day), GregorianYear.of(2025).october().numberOfDays());
        assertEquals(TimeQuantity.of(31, day), GregorianYear.of(2020).october().numberOfDays());
        assertEquals(TimeQuantity.of(30, day), GregorianYear.of(2025).november().numberOfDays());
        assertEquals(TimeQuantity.of(30, day), GregorianYear.of(2020).november().numberOfDays());
        assertEquals(TimeQuantity.of(31, day), GregorianYear.of(2025).december().numberOfDays());
        assertEquals(TimeQuantity.of(31, day), GregorianYear.of(2020).december().numberOfDays());
    }

    @Test
    void testParseISO8601() {
        assertEquals(january.of(2026), parseISO8601("2026-01"));
        assertEquals(january.of(0), parseISO8601("0000-01"));
        assertEquals(january.of(150), parseISO8601("0150-01"));
        assertEquals(january.of(-1), parseISO8601("-0001-01"));
        assertEquals(january.of(10000), parseISO8601("+010000-01"));
        assertEquals(january.of(-54000), parseISO8601("-054000-01"));
        assertEquals(january.of(9999000), parseISO8601("+9999000-01"));
        assertEquals(january.of(-9999000), parseISO8601("-9999000-01"));
        for (String invalid : List.of("A", "1E10", "", "++2026", "--2022", "+X", "2022.1", "-2022-", "1", "11", "111",
                "-12", "1-01", "11-01", "111-01", "2026-")) {
            assertRaises(() -> parseISO8601(invalid), "The provided month of year '" + invalid + "' is invalid. Months"
                    + " of year must be 4 digits (e.g., 2026) or expanded ISO 8601 (e.g., +010000) for the year and"
                    + " exactly 2 digits for the month.");
        }
        List<String> invalidYears = List.of("222-010", "AAAA-01", "z+a12-01");
        List<String> yearStrings = List.of("222", "AAAA", "z+a12");
        for (int index = 0; index < invalidYears.size(); index++) {
            String invalid = invalidYears.get(index);
            assertRaises(() -> parseISO8601(invalid), "The provided year '" + yearStrings.get(index) + "' is invalid."
                    + " Years must be 4 digits (e.g., 2026) or expanded ISO 8601 (e.g., +010000).");
        }
        List<String> invalidMonths = List.of("2026-A1", "2026-1E", "2026-+2", "2026--2", "2026-+X", "2026-20", "2026-00",
                "2026-100", "2026-AA", "2026-111");
        List<String> monthStrings = List.of("A1", "1E", "+2", "2", "+X", "20", "00", "100", "AA", "111");
        for (int index = 0; index < invalidMonths.size(); index++) {
            String invalid = invalidMonths.get(index);
            assertRaises(() -> parseISO8601(invalid), "The provided month '" + monthStrings.get(index) + "' is invalid."
                    + " Months must be 2 digits between 01 and 12 (e.g., 05).");
        }
    }

    @Test
    void testPrevious() {
        GregorianYear baseYear = GregorianYear.of(2025);
        assertPreviousOf(baseYear.january(), GregorianYear.of(2024).december());
        assertPreviousOf(baseYear.december(), baseYear.november());
        assertEquals(GregorianYear.of(0).january(), baseYear.january().previous(TimeQuantity.of(2025, year)));
        assertEquals(GregorianYear.of(2024).april(), baseYear.april().previous(TimeQuantity.of(12, month)));
        assertEquals(GregorianYear.of(2026).july(), baseYear.july().previous(TimeQuantity.of(-1, year)));
        assertEquals(GregorianYear.of(2026).january(), baseYear.july().previous(TimeQuantity.of(Fraction.of(-1, 2), year)));
        assertEquals(GregorianYear.of(2010).june(), GregorianYear.of(2020).june().previous(TimeQuantity.of(1, decade)));
        assertEquals(GregorianYear.of(2024).september(),
                baseYear.march().previous(TimeQuantity.of(Fraction.of(1, 2), year)));
        ArithmeticException exception = assertThrows(ArithmeticException.class,
                () -> baseYear.previous(TimeQuantity.of(30, day)));
        assertEquals("Cannot convert incommensurable units", exception.getMessage());
    }

    @Test
    void testPrintString() {
        assertEquals("February, 2025", february.of(2025).toString());
    }
}
