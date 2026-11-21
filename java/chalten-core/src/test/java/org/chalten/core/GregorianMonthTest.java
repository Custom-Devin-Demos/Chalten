package org.chalten.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.chalten.localization.Translations;
import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.measure.TimeQuantity;
import org.chalten.testkit.ChaltenTest;
import org.junit.jupiter.api.Test;

class GregorianMonthTest extends ChaltenTest {

    private static GregorianMonth parseISO8601(String string) {
        return new ISO8601Parser().parseMonth(string);
    }

    @Test
    void testApril() {
        assertEquals(4, GregorianMonth.APRIL.positionInYear());
        assertEquals("April", GregorianMonth.APRIL.fullName());
        assertEquals("Apr", GregorianMonth.APRIL.shortName());
        assertEquals(TimeQuantity.of(30, day), GregorianMonth.APRIL.numberOfDaysInLeapYear());
        assertEquals(TimeQuantity.of(30, day), GregorianMonth.APRIL.numberOfDaysInCommonYear());
    }

    @Test
    void testAugust() {
        assertEquals(8, GregorianMonth.AUGUST.positionInYear());
        assertEquals("August", GregorianMonth.AUGUST.fullName());
        assertEquals("Aug", GregorianMonth.AUGUST.shortName());
        assertEquals(TimeQuantity.of(31, day), GregorianMonth.AUGUST.numberOfDaysInLeapYear());
        assertEquals(TimeQuantity.of(31, day), GregorianMonth.AUGUST.numberOfDaysInCommonYear());
    }

    @Test
    void testComparing() {
        assertEquals(GregorianMonth.MAY, GregorianMonth.MAY);
        assertEquals(GregorianMonth.MAY, GregorianMonth.monthAt(5));
        assertNotEquals(GregorianMonth.JUNE, GregorianMonth.MAY);
    }

    @Test
    void testDecember() {
        assertEquals(12, GregorianMonth.DECEMBER.positionInYear());
        assertEquals("December", GregorianMonth.DECEMBER.fullName());
        assertEquals("Dec", GregorianMonth.DECEMBER.shortName());
        assertEquals(TimeQuantity.of(31, day), GregorianMonth.DECEMBER.numberOfDaysInLeapYear());
        assertEquals(TimeQuantity.of(31, day), GregorianMonth.DECEMBER.numberOfDaysInCommonYear());
    }

    @Test
    void testFebruary() {
        assertEquals(2, GregorianMonth.FEBRUARY.positionInYear());
        assertEquals("February", GregorianMonth.FEBRUARY.fullName());
        assertEquals("Feb", GregorianMonth.FEBRUARY.shortName());
        assertEquals(TimeQuantity.of(29, day), GregorianMonth.FEBRUARY.numberOfDaysInLeapYear());
        assertEquals(TimeQuantity.of(28, day), GregorianMonth.FEBRUARY.numberOfDaysInCommonYear());
    }

    @Test
    void testISO8601Printing() {
        assertEquals(List.of("01", "02", "03", "04", "05", "06", "07", "08", "09", "10", "11", "12"),
                GregorianMonth.all().stream().map(ChaltenTest::printAsISO8601String).toList());
    }

    @Test
    void testInstanceCreation() {
        for (int index = 1; index <= 12; index++) {
            assertEquals(index, GregorianMonth.all().get(index - 1).positionInYear());
        }
        assertEquals(List.of(january, february, march, april, may, june, july, august, september, october,
                november, december), GregorianMonth.all());
    }

    @Test
    void testJanuary() {
        assertEquals(1, GregorianMonth.JANUARY.positionInYear());
        assertEquals("January", GregorianMonth.JANUARY.fullName());
        assertEquals("Jan", GregorianMonth.JANUARY.shortName());
        assertEquals(TimeQuantity.of(31, day), GregorianMonth.JANUARY.numberOfDaysInLeapYear());
        assertEquals(TimeQuantity.of(31, day), GregorianMonth.JANUARY.numberOfDaysInCommonYear());
    }

    @Test
    void testJuly() {
        assertEquals(7, GregorianMonth.JULY.positionInYear());
        assertEquals("July", GregorianMonth.JULY.fullName());
        assertEquals("Jul", GregorianMonth.JULY.shortName());
        assertEquals(TimeQuantity.of(31, day), GregorianMonth.JULY.numberOfDaysInLeapYear());
        assertEquals(TimeQuantity.of(31, day), GregorianMonth.JULY.numberOfDaysInCommonYear());
    }

    @Test
    void testJune() {
        assertEquals(6, GregorianMonth.JUNE.positionInYear());
        assertEquals("June", GregorianMonth.JUNE.fullName());
        assertEquals("Jun", GregorianMonth.JUNE.shortName());
        assertEquals(TimeQuantity.of(30, day), GregorianMonth.JUNE.numberOfDaysInLeapYear());
        assertEquals(TimeQuantity.of(30, day), GregorianMonth.JUNE.numberOfDaysInCommonYear());
    }

    @Test
    void testMarch() {
        assertEquals(3, GregorianMonth.MARCH.positionInYear());
        assertEquals("March", GregorianMonth.MARCH.fullName());
        assertEquals("Mar", GregorianMonth.MARCH.shortName());
        assertEquals(TimeQuantity.of(31, day), GregorianMonth.MARCH.numberOfDaysInLeapYear());
        assertEquals(TimeQuantity.of(31, day), GregorianMonth.MARCH.numberOfDaysInCommonYear());
    }

    @Test
    void testMay() {
        assertEquals(5, GregorianMonth.MAY.positionInYear());
        assertEquals("May", GregorianMonth.MAY.fullName());
        assertEquals("May", GregorianMonth.MAY.shortName());
        assertEquals(TimeQuantity.of(31, day), GregorianMonth.MAY.numberOfDaysInLeapYear());
        assertEquals(TimeQuantity.of(31, day), GregorianMonth.MAY.numberOfDaysInCommonYear());
    }

    @Test
    void testNamesAreLocalized() {
        Map<String, String> spanish = Map.ofEntries(Map.entry("January", "Enero"), Map.entry("February", "Febrero"),
                Map.entry("March", "Marzo"), Map.entry("April", "Abril"), Map.entry("May", "Mayo"),
                Map.entry("June", "Junio"), Map.entry("July", "Julio"), Map.entry("August", "Agosto"),
                Map.entry("September", "Septiembre"), Map.entry("October", "Octubre"),
                Map.entry("November", "Noviembre"), Map.entry("December", "Diciembre"));
        assertEquals(List.of("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre",
                "Octubre", "Noviembre", "Diciembre"),
                Translations.during(source -> spanish.getOrDefault(source, source),
                        () -> GregorianMonth.all().stream().map(GregorianMonth::fullName).toList()));
    }

    @Test
    void testNext() {
        assertEquals(january, january.next(TimeQuantity.of(1, year)));
        assertEquals(february, january.next(TimeQuantity.of(1, month)));
        assertEquals(march, january.next(TimeQuantity.of(2, month)));
        assertEquals(april, january.next(TimeQuantity.of(3, month)));
        assertEquals(may, january.next(TimeQuantity.of(4, month)));
        assertEquals(june, january.next(TimeQuantity.of(5, month)));
        assertEquals(july, january.next(TimeQuantity.of(6, month)));
        assertEquals(august, january.next(TimeQuantity.of(7, month)));
        assertEquals(september, january.next(TimeQuantity.of(8, month)));
        assertEquals(october, january.next(TimeQuantity.of(9, month)));
        assertEquals(november, january.next(TimeQuantity.of(10, month)));
        assertEquals(december, january.next(TimeQuantity.of(11, month)));
        assertEquals(january, january.next(TimeQuantity.of(12, month)));
        assertEquals(february, january.next());
        assertEquals(january, december.next());
    }

    @Test
    void testNovember() {
        assertEquals(11, GregorianMonth.NOVEMBER.positionInYear());
        assertEquals("November", GregorianMonth.NOVEMBER.fullName());
        assertEquals("Nov", GregorianMonth.NOVEMBER.shortName());
        assertEquals(TimeQuantity.of(30, day), GregorianMonth.NOVEMBER.numberOfDaysInLeapYear());
        assertEquals(TimeQuantity.of(30, day), GregorianMonth.NOVEMBER.numberOfDaysInCommonYear());
    }

    @Test
    void testOctober() {
        assertEquals(10, GregorianMonth.OCTOBER.positionInYear());
        assertEquals("October", GregorianMonth.OCTOBER.fullName());
        assertEquals("Oct", GregorianMonth.OCTOBER.shortName());
        assertEquals(TimeQuantity.of(31, day), GregorianMonth.OCTOBER.numberOfDaysInLeapYear());
        assertEquals(TimeQuantity.of(31, day), GregorianMonth.OCTOBER.numberOfDaysInCommonYear());
    }

    @Test
    void testParseISO8601() {
        assertEquals(january, parseISO8601("01"));
        assertEquals(february, parseISO8601("02"));
        assertEquals(march, parseISO8601("03"));
        assertEquals(april, parseISO8601("04"));
        assertEquals(may, parseISO8601("05"));
        assertEquals(june, parseISO8601("06"));
        assertEquals(july, parseISO8601("07"));
        assertEquals(august, parseISO8601("08"));
        assertEquals(september, parseISO8601("09"));
        assertEquals(october, parseISO8601("10"));
        assertEquals(november, parseISO8601("11"));
        assertEquals(december, parseISO8601("12"));
        for (String invalid : List.of("A", "1E", "", "+2", "-2", "+X", "20", "00", "100", "AA", "-1", "111")) {
            InstanceCreationFailedException exception = assertThrows(InstanceCreationFailedException.class,
                    () -> parseISO8601(invalid));
            assertEquals("The provided month '" + invalid + "' is invalid. Months must be 2 digits between 01 and 12"
                    + " (e.g., 05).", exception.getMessage());
        }
    }

    @Test
    void testPrevious() {
        assertEquals(january, january.previous(TimeQuantity.of(1, year)));
        assertEquals(december, january.previous(TimeQuantity.of(1, month)));
        assertEquals(november, january.previous(TimeQuantity.of(2, month)));
        assertEquals(october, january.previous(TimeQuantity.of(3, month)));
        assertEquals(september, january.previous(TimeQuantity.of(4, month)));
        assertEquals(august, january.previous(TimeQuantity.of(5, month)));
        assertEquals(july, january.previous(TimeQuantity.of(6, month)));
        assertEquals(june, january.previous(TimeQuantity.of(7, month)));
        assertEquals(may, january.previous(TimeQuantity.of(8, month)));
        assertEquals(april, january.previous(TimeQuantity.of(9, month)));
        assertEquals(march, january.previous(TimeQuantity.of(10, month)));
        assertEquals(february, january.previous(TimeQuantity.of(11, month)));
        assertEquals(january, january.previous(TimeQuantity.of(12, month)));
    }

    @Test
    void testPrintString() {
        assertEquals(List.of("January", "February", "March", "April", "May", "June", "July", "August", "September",
                "October", "November", "December"), GregorianMonth.all().stream().map(Object::toString).toList());
    }

    @Test
    void testSeptember() {
        assertEquals(9, GregorianMonth.SEPTEMBER.positionInYear());
        assertEquals("September", GregorianMonth.SEPTEMBER.fullName());
        assertEquals("Sep", GregorianMonth.SEPTEMBER.shortName());
        assertEquals(TimeQuantity.of(30, day), GregorianMonth.SEPTEMBER.numberOfDaysInLeapYear());
        assertEquals(TimeQuantity.of(30, day), GregorianMonth.SEPTEMBER.numberOfDaysInCommonYear());
    }
}
