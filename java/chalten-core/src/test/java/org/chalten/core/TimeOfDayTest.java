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
import org.chalten.measure.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class TimeOfDayTest {

    private final TimeUnit hour = TimeOfDay.hour();
    private final TimeUnit minute = TimeOfDay.minute();
    private final TimeUnit second = TimeOfDay.second();
    private final TimeUnit microsecond = TimeOfDay.microsecond();

    private static TimeOfDay parseISO8601(String string) {
        return new ISO8601Parser().parseTimeOfDay(string);
    }

    private static String printAsISO8601String(ISO8601Printable calendarMagnitude) {
        StringBuilder stream = new StringBuilder();
        calendarMagnitude.printAsISO8601On(stream);
        return stream.toString();
    }

    private static void shouldRaise(Executable block, String messageText) {
        assertEquals(messageText, assertThrows(InstanceCreationFailedException.class, block).getMessage());
    }

    private TimeQuantity times(long amount, TimeUnit unit) {
        return TimeQuantity.of(amount, unit);
    }

    private static String invalidTime(String string) {
        return "The provided time '" + string + "' is invalid. Ensure it follows ISO 8601 (e.g., 'T14:30:00').";
    }

    @Test
    void testAccessing() {
        TimeOfDay timeOfDay = TimeOfDay.hours(16, 15, 12, 1640);
        assertEquals(times(16, hour), timeOfDay.hours());
        assertEquals(times(15, minute), timeOfDay.minutes());
        assertEquals(times(12, second), timeOfDay.seconds());
        assertEquals(times(1640, microsecond), timeOfDay.microseconds());
    }

    @Test
    void testAddition() {
        TimeOfDay timeOfDay = TimeOfDay.hours(16, 15, 12, 1640).plus(times(4, hour));
        assertEquals(times(20, hour), timeOfDay.hours());
        assertEquals(times(15, minute), timeOfDay.minutes());
        assertEquals(times(12, second), timeOfDay.seconds());
        assertEquals(times(1640, microsecond), timeOfDay.microseconds());
    }

    @Test
    void testAdditionCannotExceed24Hours() {
        TimeOfDay timeOfDay = TimeOfDay.hours(16);
        shouldRaise(() -> timeOfDay.plus(times(8, hour)), "A time of day must be between 0 and 24 hours");
    }

    @Test
    void testAdditionOfLessThanOneHour() {
        TimeOfDay timeOfDay = TimeOfDay.hours(16, 15, 12, 1640)
                .plus(times(20, minute).plus(times(10, second)).plus(times(15, microsecond)));
        assertEquals(times(16, hour), timeOfDay.hours());
        assertEquals(times(35, minute), timeOfDay.minutes());
        assertEquals(times(22, second), timeOfDay.seconds());
        assertEquals(times(1655, microsecond), timeOfDay.microseconds());
    }

    @Test
    void testComparing() {
        assertEquals(TimeOfDay.halfOfDay(), TimeOfDay.halfOfDay());
        assertEquals(TimeOfDay.halfOfDay().hashCode(), TimeOfDay.halfOfDay().hashCode());
        assertNotEquals(TimeOfDay.startOfDay(), TimeOfDay.halfOfDay());
        assertTrue(TimeOfDay.startOfDay().isLessThan(TimeOfDay.halfOfDay()));
        assertTrue(TimeOfDay.startOfDay().isLessThanOrEqualTo(TimeOfDay.halfOfDay()));
        assertFalse(TimeOfDay.startOfDay().isLessThan(TimeOfDay.startOfDay()));
        assertTrue(TimeOfDay.startOfDay().isLessThanOrEqualTo(TimeOfDay.startOfDay()));
        assertTrue(TimeOfDay.halfOfDay().isGreaterThan(TimeOfDay.startOfDay()));
        assertTrue(TimeOfDay.halfOfDay().isGreaterThanOrEqualTo(TimeOfDay.startOfDay()));
        assertFalse(TimeOfDay.halfOfDay().isGreaterThan(TimeOfDay.halfOfDay()));
        assertTrue(TimeOfDay.halfOfDay().isGreaterThanOrEqualTo(TimeOfDay.halfOfDay()));
    }

    @Test
    void testISO8601Printing() {
        assertEquals("T12:00:00.000000", printAsISO8601String(TimeOfDay.halfOfDay()));
        assertEquals("T00:00:00.000000", printAsISO8601String(TimeOfDay.startOfDay()));
        assertEquals("T16:00:00.000000", printAsISO8601String(TimeOfDay.hours(16)));
        assertEquals("T16:15:12.001640", printAsISO8601String(TimeOfDay.hours(16, 15, 12, 1640)));
    }

    @Test
    void testInvalidCreation() {
        shouldRaise(() -> TimeOfDay.hours(24), "Hours must be in the range 0-23");
        shouldRaise(() -> TimeOfDay.hours(23, -1), "Minutes must be in the range 0-59");
        shouldRaise(() -> TimeOfDay.hours(23, 0, 60), "Seconds must be in the range 0-59");
        shouldRaise(() -> TimeOfDay.hours(23, 0, 0, 1_000_000), "Microseconds must be in the range 0-999999");
    }

    @Test
    void testParseISO8601() {
        assertEquals(TimeOfDay.halfOfDay(), parseISO8601("T12:00:00.000000"));
        assertEquals(TimeOfDay.halfOfDay(), parseISO8601("T12"));
        assertEquals(TimeOfDay.halfOfDay(), parseISO8601("T12:00"));
        assertEquals(TimeOfDay.halfOfDay(), parseISO8601("T12:00:00"));
        assertEquals(TimeOfDay.startOfDay(), parseISO8601("T00:00:00.000000"));
        assertEquals(TimeOfDay.hours(16), parseISO8601("T16:00:00.000000"));
        assertEquals(TimeOfDay.hours(16, 15, 12, 1640), parseISO8601("T16:15:12.001640"));
        assertEquals(TimeOfDay.hours(23, 59, 59, 999_999), parseISO8601("T23:59:59.999999"));
        for (String invalidString : List.of("T", "T1", "T12:", "T12:1", "T12:12:", "T12:12:1", "T12:12:12.", "TXX:",
                "T12:XX", "T12:12:XX", "T12:12:12.X", "00:00:00.000000")) {
            shouldRaise(() -> parseISO8601(invalidString), invalidTime(invalidString));
        }
        for (String invalidString : List.of("T-1:00:00.000000", "T24:00:00.000000")) {
            shouldRaise(() -> parseISO8601(invalidString), "Hours must be in the range 0-23");
        }
        shouldRaise(() -> parseISO8601("T10:60:00.000000"), "Minutes must be in the range 0-59");
        shouldRaise(() -> parseISO8601("T10:00:60.000000"), "Seconds must be in the range 0-59");
        shouldRaise(() -> parseISO8601("T10:00:00.0000001"), invalidTime("T10:00:00.0000001"));
    }

    @Test
    void testPrintString() {
        assertEquals("12:00:00.000000", TimeOfDay.halfOfDay().toString());
        assertEquals("00:00:00.000000", TimeOfDay.startOfDay().toString());
        assertEquals("16:00:00.000000", TimeOfDay.hours(16).toString());
        assertEquals("16:15:12.001640", TimeOfDay.hours(16, 15, 12, 1640).toString());
    }

    @Test
    void testSubtraction() {
        TimeOfDay timeOfDay = TimeOfDay.hours(16, 15, 12, 1640).minus(times(30, minute));
        assertEquals(times(15, hour), timeOfDay.hours());
        assertEquals(times(45, minute), timeOfDay.minutes());
        assertEquals(times(12, second), timeOfDay.seconds());
        assertEquals(times(1640, microsecond), timeOfDay.microseconds());
    }

    @Test
    void testSubtractionCannotExceed0Hours() {
        TimeOfDay timeOfDay = TimeOfDay.hours(16);
        shouldRaise(() -> timeOfDay.minus(TimeQuantity.of(Fraction.parse("16.1"), hour)),
                "A time of day must be between 0 and 24 hours");
    }

    @Test
    void testSubtractionToZero() {
        TimeOfDay timeOfDay = TimeOfDay.hours(16).minus(times(16, hour));
        assertTrue(timeOfDay.hours().isZero());
        assertTrue(timeOfDay.minutes().isZero());
        assertTrue(timeOfDay.seconds().isZero());
        assertTrue(timeOfDay.microseconds().isZero());
    }
}
