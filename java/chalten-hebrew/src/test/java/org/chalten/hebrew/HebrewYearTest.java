package org.chalten.hebrew;

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

class HebrewYearTest extends HebrewCalendarTest {

    private static final String NEGATIVE = "Negative years are unsupported for the Hebrew calendar";
    private static final String INCOMMENSURABLE = "Years in different time scales are incommensurable";

    private static void assertFails(Class<? extends Throwable> type, String message, Executable executable) {
        assertEquals(message, assertThrows(type, executable).getMessage());
    }

    @Test
    void testBetweenAnd() {
        assertTrue(y(2).between(y(2), y(3)));
        assertTrue(y(3).between(y(2), y(3)));
        assertTrue(y(3).between(y(2), y(4)));
        assertFalse(y(4).between(y(2), y(3)));
        assertFalse(y(1).between(y(2), y(3)));
    }

    @Test
    void testComparing() {
        assertEquals(y(1), y(1));
        assertEquals(y(2025), y(2025));
        assertNotEquals(y(1), y(2025));
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
        assertEquals(2025, HebrewYear.number(2025).number());
        assertFails(InstanceCreationFailedException.class, NEGATIVE, () -> HebrewYear.number(0));
        assertFails(InstanceCreationFailedException.class, NEGATIVE, () -> HebrewYear.number(-13));
        assertFails(InstanceCreationFailedException.class, "1.5 is an invalid number for a year in the Hebrew calendar",
                () -> HebrewYear.number(Double.valueOf(1.5)));
    }

    @Test
    void testInstanceCreationShortcut() {
        assertEquals(2025, HebrewYear.of(2025).number());
        assertFails(InstanceCreationFailedException.class, NEGATIVE, () -> HebrewYear.of(0));
        assertFails(InstanceCreationFailedException.class, NEGATIVE, () -> HebrewYear.of(-13));
    }

    @Test
    void testInterval() {
        DiscreteInterval<HebrewYear> interval = y(2006).to(y(2026));
        assertEquals(21, interval.size());
        for (int index = 1; index <= interval.size(); index++) {
            assertEquals(y(2005 + index), interval.get(index - 1));
        }
    }

    @Test
    void testIsCompleteYear() {
        for (long number : new long[] {5661, 5663, 5666, 5669, 5672, 5678, 5682, 5685, 5686, 5693, 5696, 5702, 5705,
            5709, 5712, 5713, 5716, 5720, 5723, 5729, 5732, 5734, 5739, 5740, 5743, 5747, 5750, 5754, 5756, 5759}) {
            assertTrue(y(number).isCompleteYear());
            assertFalse(y(number).isLeapYear());
            assertEquals(days(355), y(number).numberOfDays());
        }
        for (long number : new long[] {5665, 5673, 5676, 5681, 5689, 5692, 5698, 5700, 5708, 5717, 5725, 5727, 5736,
            5744, 5752, 5760}) {
            assertTrue(y(number).isCompleteYear());
            assertTrue(y(number).isLeapYear());
            assertEquals(days(385), y(number).numberOfDays());
        }
    }

    @Test
    void testIsDeficientYear() {
        for (long number : new long[] {5675, 5683, 5690, 5699, 5710, 5726, 5737, 5753}) {
            assertTrue(y(number).isDeficientYear());
            assertFalse(y(number).isLeapYear());
            assertEquals(days(353), y(number).numberOfDays());
        }
        for (long number : new long[] {5662, 5668, 5670, 5679, 5687, 5695, 5703, 5706, 5714, 5719, 5722, 5730, 5733,
            5741, 5746, 5749, 5757}) {
            assertTrue(y(number).isDeficientYear());
            assertTrue(y(number).isLeapYear());
            assertEquals(days(383), y(number).numberOfDays());
        }
    }

    @Test
    void testIsLeapYear() {
        for (long number : new long[] {3, 6, 8, 11, 14, 17, 19, 5787, 5790, 5793}) {
            assertTrue(y(number).isLeapYear());
        }
        for (long number : new long[] {1, 2, 4, 5, 7, 9, 10, 12, 5786, 5788}) {
            assertFalse(y(number).isLeapYear());
        }
    }

    @Test
    void testIsRegularYear() {
        for (long number : new long[] {5664, 5667, 5671, 5674, 5677, 5680, 5688, 5691, 5694, 5697, 5701, 5704, 5707,
            5715, 5718, 5721, 5724, 5728, 5731, 5735, 5742, 5745, 5748, 5751, 5758}) {
            assertTrue(y(number).isRegularYear());
            assertFalse(y(number).isLeapYear());
            assertEquals(days(354), y(number).numberOfDays());
        }
        for (long number : new long[] {5684, 5711, 5738, 5755}) {
            assertTrue(y(number).isRegularYear());
            assertTrue(y(number).isLeapYear());
            assertEquals(days(384), y(number).numberOfDays());
        }
    }

    @Test
    void testLessThan() {
        assertIsLessThan(y(1), y(2));
        assertIsLessThanOrEqualTo(y(1), y(2));
        assertIsLessThanOrEqualTo(y(1), y(1));
        denyIsLessThan(y(2025), y(1));
        assertFails(ArithmeticException.class, INCOMMENSURABLE, () -> compareUnchecked(y(2), GregorianYear.of(1)));
        assertFails(ArithmeticException.class, INCOMMENSURABLE, () -> compareUnchecked(GregorianYear.of(1), y(2)));
    }

    @Test
    void testNext() {
        assertNextOf(y(1), y(2));
        assertNextOf(y(2), y(3));
        assertNextOf(y(2025), y(2026));
        assertEquals(y(2026), y(1).next(hebrewYears(2025)));
        assertEquals(y(2024), y(2025).next(hebrewYears(-1)));
        assertEquals(y(2030), y(2020).next(hebrewYears(10)));
        assertThrows(ArithmeticException.class, () -> y(2025).next(hebrewMonths(12)));
        assertThrows(InstanceCreationFailedException.class,
                () -> y(1).next(TimeQuantity.of(Fraction.of(1, 2), hebrewYear)));
    }

    @Test
    void testNumberOfDays() {
        long[] expected = {353, 354, 385, 355, 383, 354, 355, 383, 354, 355, 385, 354, 353, 385, 354, 385, 353, 354,
            385, 355, 353, 384};
        for (int index = 0; index < expected.length; index++) {
            assertEquals(days(expected[index]), y(5761 + index).numberOfDays());
        }
    }

    @Test
    void testNumberOfMonths() {
        long[] expected = {12, 13, 12, 12, 13, 12, 12, 13, 12, 13, 12, 12, 13, 12, 12, 13, 12, 13, 12};
        for (int index = 0; index < expected.length; index++) {
            assertEquals(hebrewMonths(expected[index]), y(5786 + index).numberOfMonths());
        }
    }

    @Test
    void testPrevious() {
        assertPreviousOf(y(2), y(1));
        assertPreviousOf(y(3), y(2));
        assertPreviousOf(y(2025), y(2024));
        assertEquals(y(2025), y(4050).previous(hebrewYears(2025)));
        assertEquals(y(2026), y(2025).previous(hebrewYears(-1)));
        assertThrows(ArithmeticException.class, () -> y(2025).previous(hebrewMonths(12)));
        assertThrows(InstanceCreationFailedException.class,
                () -> y(1).previous(TimeQuantity.of(Fraction.of(1, 2), hebrewYear)));
    }

    @Test
    void testPrintString() {
        assertEquals("1445 AM", y(1445).toString());
        assertEquals("1 AM", y(1).toString());
    }

    @Test
    void testYearsBetweenTwoYears() {
        assertEquals(hebrewYears(6), y(5726).minus(y(5720)));
        assertEquals(hebrewYears(0), y(5726).minus(y(5726)));
        assertFails(ArithmeticException.class,
                "Cannot subtract two years when the second one is greater than the first one.",
                () -> y(5708).minus(y(5709)));
    }
}
