package org.chalten.islamic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.chalten.core.GregorianYear;
import org.chalten.measure.DiscreteInterval;
import org.chalten.measure.Fraction;
import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.measure.TimeQuantity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class IslamicYearTest extends IslamicCalendarTest {

    private static final String NEGATIVE = "Negative years are unsupported for the Islamic calendar";
    private static final String INCOMMENSURABLE = "Years in different time scales are incommensurable";

    private static void assertFails(Class<? extends Throwable> type, String message, Executable executable) {
        assertEquals(message, assertThrows(type, executable).getMessage());
    }

    @Test
    void testBetweenAnd() {
        assertTrue(y(1).between(y(1), y(2)));
        assertTrue(y(2).between(y(1), y(2)));
        assertTrue(y(2).between(y(1), y(3)));
        assertFalse(y(3).between(y(1), y(2)));
        assertFalse(y(1).between(y(2), y(3)));
    }

    @Test
    void testComparing() {
        assertEquals(y(1), y(1));
        assertEquals(y(2125), y(2125));
        assertNotEquals(y(1), y(2125));
        assertNotEquals(y(1), GregorianYear.of(1));
        assertNotEquals(GregorianYear.of(1), y(1));
    }

    @Test
    void testGreaterThan() {
        assertIsGreaterThan(y(2), y(1));
        assertIsGreaterThanOrEqualTo(y(2), y(1));
        assertIsGreaterThanOrEqualTo(y(1), y(1));
        denyIsGreaterThan(y(1), y(2025));
        assertFails(ArithmeticException.class, INCOMMENSURABLE, () -> compareUnchecked(y(2), GregorianYear.of(1)));
        assertFails(ArithmeticException.class, INCOMMENSURABLE, () -> compareUnchecked(GregorianYear.of(1), y(2)));
    }

    @Test
    void testInstanceCreation() {
        assertEquals(2025, IslamicYear.number(2025).number());
        assertFails(InstanceCreationFailedException.class, NEGATIVE, () -> IslamicYear.number(0));
        assertFails(InstanceCreationFailedException.class, NEGATIVE, () -> IslamicYear.number(-13));
        assertFails(InstanceCreationFailedException.class,
                "1.5 is an invalid number for a year in the Islamic calendar",
                () -> IslamicYear.number(Double.valueOf(1.5)));
    }

    @Test
    void testInstanceCreationShortcut() {
        assertEquals(2025, IslamicYear.of(2025).number());
        assertFails(InstanceCreationFailedException.class, NEGATIVE, () -> IslamicYear.of(0));
        assertFails(InstanceCreationFailedException.class, NEGATIVE, () -> IslamicYear.of(-13));
    }

    @Test
    void testInterval() {
        DiscreteInterval<IslamicYear> interval = y(2006).to(y(2026));
        assertEquals(21, interval.size());
        for (int index = 1; index <= interval.size(); index++) {
            assertEquals(y(2005 + index), interval.get(index - 1));
        }
    }

    @Test
    void testIsLeapYear() {
        for (long number : new long[] {1442, 1445, 1447, 1450, 1453, 1456, 1458, 1461, 1464, 1466}) {
            assertTrue(y(number).isLeapYear());
        }
        for (long number : new long[] {1443, 1444, 1446, 1448, 1449, 1451, 1452, 1454, 1455, 1457}) {
            assertFalse(y(number).isLeapYear());
        }
    }

    @Test
    void testLessThan() {
        assertIsLessThan(y(1), y(2));
        assertIsLessThanOrEqualTo(y(1), y(2));
        assertIsLessThanOrEqualTo(y(1), y(1));
        denyIsLessThan(y(2025), y(1));
        assertFails(ArithmeticException.class, INCOMMENSURABLE, () -> compareUnchecked(y(2), GregorianYear.of(1)));
        assertFails(ArithmeticException.class, INCOMMENSURABLE, () -> compareUnchecked(GregorianYear.of(0), y(1)));
    }

    @Test
    void testNext() {
        assertNextOf(y(1), y(2));
        assertNextOf(y(2), y(3));
        assertNextOf(y(2025), y(2026));
        assertEquals(y(2026), y(1).next(islamicYears(2025)));
        assertEquals(y(2026), y(2025).next(islamicMonths(12)));
        assertEquals(y(2024), y(2025).next(islamicYears(-1)));
        assertEquals(y(2030), y(2020).next(islamicYears(10)));
        assertThrows(InstanceCreationFailedException.class,
                () -> y(1).next(TimeQuantity.of(Fraction.of(1, 2), islamicYear)));
    }

    @Test
    void testNumberOfDays() {
        assertEquals(days(355), y(1442).numberOfDays());
        assertEquals(days(354), y(1443).numberOfDays());
    }

    @Test
    void testNumberOfMonths() {
        assertEquals(islamicMonths(12), y(1432).numberOfMonths());
        assertEquals(islamicMonths(12), y(1433).numberOfMonths());
    }

    @Test
    void testPrevious() {
        assertPreviousOf(y(2), y(1));
        assertPreviousOf(y(3), y(2));
        assertPreviousOf(y(2025), y(2024));
        assertEquals(y(2025), y(4050).previous(islamicYears(2025)));
        assertEquals(y(2024), y(2025).previous(islamicMonths(12)));
        assertEquals(y(2026), y(2025).previous(islamicYears(-1)));
        assertThrows(InstanceCreationFailedException.class,
                () -> y(1).previous(TimeQuantity.of(Fraction.of(1, 2), islamicYear)));
    }

    @Test
    void testPrintString() {
        assertEquals("1445 AH", y(1445).toString());
        assertEquals("1 AH", y(1).toString());
    }

    @Test
    void testYearsBetweenTwoYears() {
        assertEquals(islamicYears(6), y(1426).minus(y(1420)));
        assertEquals(islamicYears(0), y(1426).minus(y(1426)));
        assertFails(ArithmeticException.class,
                "Cannot subtract two years when the second one is greater than the first one.",
                () -> y(1408).minus(y(1409)));
    }
}
