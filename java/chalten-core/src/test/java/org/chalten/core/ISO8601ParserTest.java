package org.chalten.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.chalten.measure.CompoundTimeQuantity;
import org.chalten.measure.Fraction;
import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnit;
import org.chalten.measure.TimeUnits;
import org.chalten.testkit.ChaltenTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class ISO8601ParserTest extends ChaltenTest {

    private final TimeUnit week = TimeUnits.WEEK;
    private final TimeUnit minute = TimeUnits.MINUTE;
    private final TimeUnit second = TimeUnits.SECOND;

    private static TimeQuantity parseDuration(String string) {
        return new ISO8601Parser().parseDuration(string);
    }

    private static CompoundTimeQuantity parseTimePeriods(String string) {
        return new ISO8601Parser().parseDurationAllowingSeveralTimePeriods(string);
    }

    private static void assertRaises(Executable block, String messageText) {
        assertEquals(messageText, assertThrows(InstanceCreationFailedException.class, block).getMessage());
    }

    @Test
    void testCannotParseInvalidDuration() {
        assertRaises(() -> parseDuration("X"), "The provided duration 'X' is invalid.");
        assertRaises(() -> parseDuration("PP"), "The provided duration 'PP' is invalid.");
        assertRaises(() -> parseDuration("PT"), "The provided duration 'PT' is invalid.");
        assertRaises(() -> parseDuration("15T"), "The provided duration '15T' is invalid.");
    }

    @Test
    void testMixedTimePeriods() {
        assertRaises(() -> parseDuration("P3Y6M4DT12H30M5S"),
                "Durations with several time periods are disallowed");
        List<TimeQuantity> timePeriods = parseTimePeriods("P3Y6M4DT12H30M5S").parts();
        assertEquals(6, timePeriods.size());
        assertTrue(timePeriods.contains(TimeQuantity.of(3, year)));
        assertTrue(timePeriods.contains(TimeQuantity.of(6, month)));
        assertTrue(timePeriods.contains(TimeQuantity.of(4, day)));
        assertTrue(timePeriods.contains(TimeQuantity.of(12, hour)));
        assertTrue(timePeriods.contains(TimeQuantity.of(30, minute)));
        assertTrue(timePeriods.contains(TimeQuantity.of(5, second)));
        timePeriods = parseTimePeriods("P3Y").parts();
        assertEquals(1, timePeriods.size());
        assertTrue(timePeriods.contains(TimeQuantity.of(3, year)));
    }

    @Test
    void testParseDayDuration() {
        assertEquals(TimeQuantity.of(0, day), parseDuration("P0D"));
        assertEquals(TimeQuantity.of(1, day), parseDuration("P1D"));
        assertEquals(TimeQuantity.of(6, day), parseDuration("P6D"));
        assertEquals(TimeQuantity.of(Fraction.parse("6.5"), day), parseDuration("P6.5D"));
        assertRaises(() -> parseDuration("1D"), "The provided duration '1D' is invalid.");
        assertRaises(() -> parseDuration("P-1D"), "The provided duration 'P-1D' is invalid.");
        assertRaises(() -> parseDuration("P$D"), "The provided duration 'P$D' is invalid.");
        assertRaises(() -> parseDuration("PD"), "The provided duration 'PD' is invalid.");
    }

    @Test
    void testParseDurationUsingNonDefaultNumberReader() {
        ISO8601Parser parser = new ISO8601Parser()
                .readNumbersUsing(string -> ISO8601Parser.readInteger(string).map(Fraction::of));
        assertEquals(TimeQuantity.of(0, month), parser.parseDuration("P0M"));
        assertEquals(TimeQuantity.of(1, month), parser.parseDuration("P1M"));
        assertEquals(TimeQuantity.of(6, month), parser.parseDuration("P6M"));
        assertEquals(TimeQuantity.of(6, month), parser.parseDuration("P6.0M"));
    }

    @Test
    void testParseHourDuration() {
        assertEquals(TimeQuantity.of(0, hour), parseDuration("PT0H"));
        assertEquals(TimeQuantity.of(1, hour), parseDuration("PT1H"));
        assertEquals(TimeQuantity.of(6, hour), parseDuration("PT6H"));
        assertRaises(() -> parseDuration("P1H"), "The provided duration 'P1H' is invalid.");
        assertRaises(() -> parseDuration("1H"), "The provided duration '1H' is invalid.");
        assertRaises(() -> parseDuration("P-1H"), "The provided duration 'P-1H' is invalid.");
        assertRaises(() -> parseDuration("P$H"), "The provided duration 'P$H' is invalid.");
        assertRaises(() -> parseDuration("PH"), "The provided duration 'PH' is invalid.");
    }

    @Test
    void testParseMinuteDuration() {
        assertEquals(TimeQuantity.of(0, minute), parseDuration("PT0M"));
        assertEquals(TimeQuantity.of(6, minute), parseDuration("PT6M"));
        assertRaises(() -> parseDuration("1TM"), "The provided duration '1TM' is invalid.");
        assertRaises(() -> parseDuration("PT-1M"), "The provided duration 'PT-1M' is invalid.");
        assertRaises(() -> parseDuration("PT$M"), "The provided duration 'PT$M' is invalid.");
        assertRaises(() -> parseDuration("PTM"), "The provided duration 'PTM' is invalid.");
    }

    @Test
    void testParseMonthDuration() {
        assertEquals(TimeQuantity.of(0, month), parseDuration("P0M"));
        assertEquals(TimeQuantity.of(1, month), parseDuration("P1M"));
        assertEquals(TimeQuantity.of(6, month), parseDuration("P6M"));
        assertRaises(() -> parseDuration("1M"), "The provided duration '1M' is invalid.");
        assertRaises(() -> parseDuration("P-1M"), "The provided duration 'P-1M' is invalid.");
        assertRaises(() -> parseDuration("P$M"), "The provided duration 'P$M' is invalid.");
        assertRaises(() -> parseDuration("PM"), "The provided duration 'PM' is invalid.");
    }

    @Test
    void testParseSecondDuration() {
        assertEquals(TimeQuantity.of(0, second), parseDuration("PT0S"));
        assertEquals(TimeQuantity.of(1, second), parseDuration("PT1S"));
        assertEquals(TimeQuantity.of(Fraction.parse("6.154"), second), parseDuration("PT6.154S"));
        assertRaises(() -> parseDuration("P1S"), "The provided duration 'P1S' is invalid.");
        assertRaises(() -> parseDuration("T1S"), "The provided duration 'T1S' is invalid.");
        assertRaises(() -> parseDuration("PT-1S"), "The provided duration 'PT-1S' is invalid.");
        assertRaises(() -> parseDuration("PT$S"), "The provided duration 'PT$S' is invalid.");
        assertRaises(() -> parseDuration("PTS"), "The provided duration 'PTS' is invalid.");
    }

    @Test
    void testParseWeekDuration() {
        assertEquals(TimeQuantity.of(0, week), parseDuration("P0W"));
        assertEquals(TimeQuantity.of(1, week), parseDuration("P1W"));
        assertEquals(TimeQuantity.of(100, week), parseDuration("P100W"));
        assertRaises(() -> parseDuration("1W"), "The provided duration '1W' is invalid.");
        assertRaises(() -> parseDuration("P-1W"), "The provided duration '-1' is invalid.");
        assertRaises(() -> parseDuration("P1D1W"), "The provided duration '1D1' is invalid.");
        assertRaises(() -> parseDuration("P$W"), "The provided duration '$' is invalid.");
        assertRaises(() -> parseDuration("PW"), "The provided duration '' is invalid.");
    }

    @Test
    void testParseYearDuration() {
        assertEquals(TimeQuantity.of(0, year), parseDuration("P0Y"));
        assertEquals(TimeQuantity.of(1, year), parseDuration("P1Y"));
        assertEquals(TimeQuantity.of(Fraction.parse("1.5"), year), parseDuration("P1.5Y"));
        assertEquals(TimeQuantity.of(100, year), parseDuration("P100Y"));
        assertRaises(() -> parseDuration("1Y"), "The provided duration '1Y' is invalid.");
        assertRaises(() -> parseDuration("P-1Y"), "The provided duration 'P-1Y' is invalid.");
        assertRaises(() -> parseDuration("P$Y"), "The provided duration 'P$Y' is invalid.");
        assertRaises(() -> parseDuration("PY"), "The provided duration 'PY' is invalid.");
    }
}
