package org.chalten.islamic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.chalten.measure.Fraction;
import org.chalten.measure.TimeQuantity;
import org.junit.jupiter.api.Test;

class IslamicMonthOfYearTest extends IslamicCalendarTest {

    private static final String CANNOT_CONVERT = "Cannot convert incommensurable units";

    private TimeQuantity islamicYears(Fraction amount) {
        return TimeQuantity.of(amount, islamicYear);
    }

    @Test
    void testBetweenAnd() {
        IslamicYear year2 = y(2);
        IslamicYear year3 = y(3);
        assertTrue(year2.muharram().between(year2.muharram(), year3.muharram()));
        assertTrue(year3.muharram().between(year2.muharram(), year3.muharram()));
        assertTrue(year3.muharram().between(year2.muharram(), y(4).muharram()));
        assertTrue(year2.safar().between(year2.muharram(), year2.safar()));
        assertTrue(year2.safar().between(year2.muharram(), year2.safar()));
        assertTrue(year2.safar().between(year2.muharram(), year2.rajab()));
        assertFalse(y(4).muharram().between(year2.muharram(), year3.muharram()));
        assertFalse(y(1).muharram().between(year2.muharram(), year3.muharram()));
    }

    @Test
    void testComparing() {
        assertEquals(y(1433).muharram(), y(1433).muharram());
        assertNotEquals(y(1435).muharram(), y(1433).muharram());
        assertNotEquals(y(1435).muharram(), y(1435).safar());
    }

    @Test
    void testCreation() {
        IslamicMonthOfYear monthOfYear = y(1433).jumadaAlAwwal();
        assertEquals(y(1433), monthOfYear.year());
        assertEquals(jumadaAlAwwal, monthOfYear.month());
    }

    @Test
    void testCreationShortcut() {
        IslamicMonthOfYear monthOfYear = muharram.of(2025);
        assertEquals(y(2025), monthOfYear.year());
        assertEquals(muharram, monthOfYear.month());
    }

    @Test
    void testDateCreation() {
        assertEquals(muharram.twentyNinth().of(2025), muharram.of(2025).twentyNinth());
    }

    @Test
    void testGreaterThan() {
        assertIsGreaterThan(muharram.of(2), muharram.of(1));
        assertIsGreaterThanOrEqualTo(muharram.of(2), muharram.of(1));
        assertIsGreaterThanOrEqualTo(muharram.of(1), muharram.of(1));
        assertIsGreaterThan(safar.of(1), muharram.of(1));
        assertIsGreaterThanOrEqualTo(safar.of(1), muharram.of(1));
        assertIsGreaterThan(safar.of(2), muharram.of(1));
        denyIsGreaterThan(muharram.of(1), muharram.of(2025));
        denyIsGreaterThan(muharram.of(1), safar.of(1));
    }

    @Test
    void testLessThan() {
        assertIsLessThan(muharram.of(1), muharram.of(2));
        assertIsLessThanOrEqualTo(muharram.of(1), muharram.of(2));
        assertIsLessThanOrEqualTo(muharram.of(1), muharram.of(1));
        assertIsLessThanOrEqualTo(muharram.of(1), safar.of(1));
        assertIsLessThanOrEqualTo(safar.of(1), muharram.of(2));
        denyIsLessThan(muharram.of(2025), muharram.of(1));
        denyIsLessThan(safar.of(1), muharram.of(1));
    }

    @Test
    void testNext() {
        IslamicYear baseYear = y(2025);
        assertNextOf(baseYear.muharram(), baseYear.safar());
        assertNextOf(baseYear.duAlHijjah(), y(2026).muharram());
        assertEquals(baseYear.muharram(), y(1).muharram().next(islamicYears(2024)));
        assertEquals(y(2026).rabiAlThani(), baseYear.rabiAlThani().next(islamicMonths(12)));
        assertEquals(y(2024).rajab(), baseYear.rajab().next(islamicYears(-1)));
        assertEquals(baseYear.muharram(), baseYear.rajab().next(islamicYears(Fraction.of(-1, 2))));
        assertEquals(y(2030).jumadaAlThani(), y(2020).jumadaAlThani().next(islamicYears(10)));
        assertEquals(y(2026).rabiAlAwwal(), baseYear.ramadan().next(islamicYears(Fraction.of(1, 2))));
        assertEquals(CANNOT_CONVERT, assertThrows(ArithmeticException.class,
                () -> baseYear.ramadan().next(days(30))).getMessage());
        assertEquals(CANNOT_CONVERT, assertThrows(ArithmeticException.class,
                () -> baseYear.ramadan().next(TimeQuantity.of(1, year))).getMessage());
    }

    @Test
    void testNumberOfDays() {
        long[] days = {30, 29, 30, 29, 30, 29, 30, 29, 30, 29, 30};
        for (int index = 0; index < days.length; index++) {
            assertEquals(days(days[index]), y(1443).monthAt(index + 1).numberOfDays());
            assertEquals(days(days[index]), y(1442).monthAt(index + 1).numberOfDays());
        }
        assertEquals(days(29), y(1443).duAlHijjah().numberOfDays());
        assertEquals(days(30), y(1442).duAlHijjah().numberOfDays());
    }

    @Test
    void testPrevious() {
        IslamicYear baseYear = y(2025);
        assertPreviousOf(baseYear.muharram(), y(2024).duAlHijjah());
        assertPreviousOf(baseYear.duAlHijjah(), baseYear.duAlQiDah());
        assertEquals(y(1).muharram(), baseYear.muharram().previous(islamicYears(2024)));
        assertEquals(y(2024).rabiAlThani(), baseYear.rabiAlThani().previous(islamicMonths(12)));
        assertEquals(y(2026).rajab(), baseYear.rajab().previous(islamicYears(-1)));
        assertEquals(y(2026).muharram(), baseYear.rajab().previous(islamicYears(Fraction.of(-1, 2))));
        assertEquals(y(2010).jumadaAlThani(), y(2020).jumadaAlThani().previous(islamicYears(10)));
        assertEquals(y(2024).ramadan(), baseYear.rabiAlAwwal().previous(islamicYears(Fraction.of(1, 2))));
        assertEquals(CANNOT_CONVERT, assertThrows(ArithmeticException.class,
                () -> baseYear.muharram().previous(days(30))).getMessage());
        assertEquals(CANNOT_CONVERT, assertThrows(ArithmeticException.class,
                () -> baseYear.muharram().previous(TimeQuantity.of(1, year))).getMessage());
    }

    @Test
    void testPrintString() {
        assertEquals("Muharram, 1412 AH", muharram.of(1412).toString());
    }
}
