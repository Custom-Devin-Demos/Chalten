package org.chalten.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.List;
import org.chalten.measure.DiscreteInterval;
import org.chalten.measure.Fraction;
import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.measure.TimeQuantity;
import org.chalten.testkit.ChaltenTest;
import org.junit.jupiter.api.Test;

class GregorianYearTest extends ChaltenTest {

    private static GregorianYear parseISO8601(String string) {
        return new ISO8601Parser().parseYear(string);
    }

    private static GregorianYear y(long number) {
        return GregorianYear.of(number);
    }

    @Test
    void testBetweenAnd() {
        assertTrue(y(0).between(y(0), y(1)));
        assertTrue(y(1).between(y(0), y(1)));
        assertTrue(y(1).between(y(0), y(2)));
        assertFalse(y(2).between(y(0), y(1)));
        assertFalse(y(-1).between(y(0), y(1)));
    }

    @Test
    void testComparing() {
        assertEquals(y(0), y(0));
        assertEquals(y(0).hashCode(), y(0).hashCode());
        assertEquals(y(2025), y(2025));
        assertEquals(y(2025).hashCode(), y(2025).hashCode());
        assertNotEquals(y(2025), y(0));
    }

    @Test
    void testGreaterThan() {
        assertIsGreaterThan(y(1), y(0));
        assertIsGreaterThanOrEqualTo(y(1), y(0));
        assertIsGreaterThanOrEqualTo(y(0), y(0));
        denyIsGreaterThan(y(0), y(2025));
    }

    @Test
    void testISO8601Printing() {
        assertEquals("2026", printAsISO8601String(y(2026)));
        assertEquals("0000", printAsISO8601String(y(0)));
        assertEquals("0150", printAsISO8601String(y(150)));
        assertEquals("-0001", printAsISO8601String(y(-1)));
        assertEquals("+010000", printAsISO8601String(y(10000)));
        assertEquals("-054000", printAsISO8601String(y(-54000)));
        assertEquals("+9999000", printAsISO8601String(y(9999000)));
        assertEquals("-9999000", printAsISO8601String(y(-9999000)));
    }

    @Test
    void testInstanceCreation() {
        assertEquals(2025, GregorianYear.number(2025).number());
        assertEquals(0, GregorianYear.number(0).number());
        assertEquals(-13, GregorianYear.number(-13).number());
        InstanceCreationFailedException exception = assertThrows(InstanceCreationFailedException.class,
                () -> GregorianYear.number(new BigDecimal("1.5")));
        assertEquals("1.5 is an invalid number for a year in the Gregorian calendar", exception.getMessage());
    }

    @Test
    void testInstanceCreationShortcut() {
        assertEquals(2025, y(2025).number());
        assertEquals(0, y(0).number());
        assertEquals(-13, y(-13).number());
    }

    @Test
    void testInterval() {
        DiscreteInterval<GregorianYear> interval = y(2006).to(y(2026));
        assertEquals(21, interval.size());
        for (int index = 1; index <= interval.size(); index++) {
            assertEquals(y(2005 + index), interval.get(index - 1));
        }
    }

    @Test
    void testIntervalWithStep() {
        DiscreteInterval<GregorianYear> interval = y(2006).to(y(2026), TimeQuantity.of(2, year));
        assertEquals(11, interval.size());
        for (int index = 1; index <= interval.size(); index++) {
            assertEquals(y(2006 + 2 * (index - 1)), interval.get(index - 1));
        }
    }

    @Test
    void testIsLeapYear() {
        for (long number : List.of(-4L, 0L, 400L, 1600L, 1996L, 2000L, 2004L, 2008L, 2012L)) {
            assertTrue(y(number).isLeapYear(), () -> number + " is leap");
        }
        for (long number : List.of(-1L, 100L, 1700L, 1800L, 1900L, 2007L, 2009L, 2025L, 2100L)) {
            assertFalse(y(number).isLeapYear(), () -> number + " is not leap");
        }
    }

    @Test
    void testLessThan() {
        assertIsLessThan(y(0), y(1));
        assertIsLessThanOrEqualTo(y(0), y(1));
        assertIsLessThanOrEqualTo(y(0), y(0));
        denyIsLessThan(y(2025), y(0));
    }

    @Test
    void testNext() {
        assertNextOf(y(0), y(1));
        assertNextOf(y(-1), y(0));
        assertNextOf(y(2025), y(2026));
        assertEquals(y(2025), y(0).next(TimeQuantity.of(2025, year)));
        assertEquals(y(2026), y(2025).next(TimeQuantity.of(12, month)));
        assertEquals(y(2024), y(2025).next(TimeQuantity.of(-1, year)));
        assertEquals(y(2030), y(2020).next(TimeQuantity.of(1, decade)));
        assertThrows(InstanceCreationFailedException.class, () -> y(0).next(TimeQuantity.of(Fraction.of(1, 2), year)));
    }

    @Test
    void testNumberOfDays() {
        assertEquals(TimeQuantity.of(366, day), y(2008).numberOfDays());
        assertEquals(TimeQuantity.of(365, day), y(2007).numberOfDays());
    }

    @Test
    void testNumberOfMonths() {
        assertEquals(TimeQuantity.of(12, month), y(2008).numberOfMonths());
        assertEquals(TimeQuantity.of(12, month), y(2007).numberOfMonths());
    }

    @Test
    void testParseISO8601() {
        assertEquals(y(2026), parseISO8601("2026"));
        assertEquals(y(0), parseISO8601("0000"));
        assertEquals(y(150), parseISO8601("0150"));
        assertEquals(y(-1), parseISO8601("-0001"));
        assertEquals(y(10000), parseISO8601("+010000"));
        assertEquals(y(-54000), parseISO8601("-054000"));
        assertEquals(y(9999000), parseISO8601("+9999000"));
        assertEquals(y(-9999000), parseISO8601("-9999000"));
        for (String invalid : List.of("A", "1E10", "", "++2026", "--2022", "+X", "2022.1", "2022-01", "-2022-", "1",
                "11", "111")) {
            InstanceCreationFailedException exception = assertThrows(InstanceCreationFailedException.class,
                    () -> parseISO8601(invalid));
            assertEquals("The provided year '" + invalid + "' is invalid. Years must be 4 digits (e.g., 2026) or"
                    + " expanded ISO 8601 (e.g., +010000).", exception.getMessage());
        }
    }

    @Test
    void testPrevious() {
        assertPreviousOf(y(0), y(-1));
        assertPreviousOf(y(1), y(0));
        assertPreviousOf(y(2025), y(2024));
        assertEquals(y(-2025), y(0).previous(TimeQuantity.of(2025, year)));
        assertEquals(y(2024), y(2025).previous(TimeQuantity.of(12, month)));
        assertEquals(y(2026), y(2025).previous(TimeQuantity.of(-1, year)));
        assertThrows(InstanceCreationFailedException.class,
                () -> y(0).previous(TimeQuantity.of(Fraction.of(1, 2), year)));
    }

    @Test
    void testPrintString() {
        assertEquals("2025", y(2025).toString());
        assertEquals("0", y(0).toString());
        assertEquals("-1", y(-1).toString());
    }

    @Test
    void testYearsBetweenTwoYears() {
        assertEquals(TimeQuantity.of(6, year), y(2026).minus(y(2020)));
        assertEquals(TimeQuantity.of(0, year), y(2026).minus(y(2026)));
        ArithmeticException exception = assertThrows(ArithmeticException.class, () -> y(2008).minus(y(2009)));
        assertEquals("Cannot subtract two years when the second one is greater than the first one.",
                exception.getMessage());
    }
}
