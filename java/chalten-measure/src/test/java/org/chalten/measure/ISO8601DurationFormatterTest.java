package org.chalten.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.chalten.testkit.ChaltenTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

/** Port of {@code ISO8601DurationFormatterTest}. Smalltalk float literals such as {@code 1.52} are exact here. */
class ISO8601DurationFormatterTest extends ChaltenTest {

    private void assertFormattingOfEquals(TimeQuantity timePeriod, String expectedString) {
        assertEquals(expectedString, new ISO8601DurationFormatter().format(timePeriod));
    }

    private void assertFormattingOfEquals(List<TimeQuantity> timePeriods, String expectedString) {
        assertEquals(expectedString, new ISO8601DurationFormatter().format(timePeriods));
    }

    private static void shouldRaiseWithMessageText(Executable block, String messageText) {
        assertEquals(messageText, assertThrows(ChaltenAssertionException.class, block).getMessage());
    }

    private static TimeQuantity times(String number, TimeUnit unit) {
        return TimeQuantity.of(Fraction.parse(number), unit);
    }

    private TimeUnit minute() {
        return TimeUnits.MINUTE;
    }

    private TimeUnit second() {
        return TimeUnits.named("second");
    }

    private TimeUnit week() {
        return TimeUnits.named("week");
    }

    @Test
    void testCannotFormatEmptyMixedDuration() {
        shouldRaiseWithMessageText(() -> new ISO8601DurationFormatter().format(List.of()),
                "Cannot format empty time periods");
    }

    @Test
    void testCannotFormatInvalidFractions() {
        shouldRaiseWithMessageText(
                () -> new ISO8601DurationFormatter().format(List.of(times("1.5", month), times("1", day))),
                "A decimal fraction can only be applied to the lowest-order component");
        shouldRaiseWithMessageText(
                () -> new ISO8601DurationFormatter().format(List.of(times("1.5", day), times("1.2", second()))),
                "A decimal fraction can only be applied to the lowest-order component");
    }

    @Test
    void testCannotFormatMixedDurationWithRepeatedUnits() {
        shouldRaiseWithMessageText(
                () -> new ISO8601DurationFormatter().format(List.of(times("1", day), times("3", day))),
                "Cannot format time periods including invalid units");
    }

    @Test
    void testCannotFormatMixedDurationWithWeeks() {
        shouldRaiseWithMessageText(
                () -> new ISO8601DurationFormatter().format(List.of(times("1", week()), times("3", day))),
                "Cannot format time periods including invalid units");
    }

    @Test
    void testCannotFormatUnsupportedUnits() {
        shouldRaiseWithMessageText(
                () -> new ISO8601DurationFormatter().format(times("1", TimeUnits.named("microsecond"))),
                "Cannot format time periods including invalid units");
    }

    @Test
    void testFormatDayDuration() {
        assertFormattingOfEquals(times("0", day), "P0D");
        assertFormattingOfEquals(times("1", day), "P1D");
        assertFormattingOfEquals(times("100", day), "P100D");
        assertFormattingOfEquals(times("1.5", day), "P1.5D");
        assertFormattingOfEquals(times("1.52", day), "P1.52D");
    }

    @Test
    void testFormatHourDuration() {
        assertFormattingOfEquals(times("0", hour), "PT0H");
        assertFormattingOfEquals(times("1", hour), "PT1H");
        assertFormattingOfEquals(times("100", hour), "PT100H");
        assertFormattingOfEquals(times("1.5", hour), "PT1.5H");
        assertFormattingOfEquals(times("1.52", hour), "PT1.52H");
    }

    @Test
    void testFormatMinuteDuration() {
        assertFormattingOfEquals(times("0", minute()), "PT0M");
        assertFormattingOfEquals(times("1", minute()), "PT1M");
        assertFormattingOfEquals(times("100", minute()), "PT100M");
        assertFormattingOfEquals(times("1.5", minute()), "PT1.5M");
        assertFormattingOfEquals(times("1.52", minute()), "PT1.52M");
    }

    @Test
    void testFormatMixedDuration() {
        assertFormattingOfEquals(
                List.of(times("1.55", second()), times("3", minute()), times("2", hour), times("5", month),
                        times("3", year), times("4", day)),
                "P3Y5M4DT2H3M1.55S");
        assertFormattingOfEquals(List.of(times("1.55", second()), times("3", minute()), times("2", hour)),
                "PT2H3M1.55S");
    }

    @Test
    void testFormatMonthDuration() {
        assertFormattingOfEquals(times("0", month), "P0M");
        assertFormattingOfEquals(times("1", month), "P1M");
        assertFormattingOfEquals(times("100", month), "P100M");
        assertFormattingOfEquals(times("1.5", month), "P1.5M");
        assertFormattingOfEquals(times("1.52", month), "P1.52M");
    }

    @Test
    void testFormatSecondDuration() {
        assertFormattingOfEquals(times("0", second()), "PT0S");
        assertFormattingOfEquals(times("1", second()), "PT1S");
        assertFormattingOfEquals(times("100", second()), "PT100S");
        assertFormattingOfEquals(times("1.5", second()), "PT1.5S");
        assertFormattingOfEquals(times("1.52", second()), "PT1.52S");
    }

    @Test
    void testFormatWeekDuration() {
        assertFormattingOfEquals(times("0", week()), "P0W");
        assertFormattingOfEquals(times("1", week()), "P1W");
        assertFormattingOfEquals(times("100", week()), "P100W");
        assertFormattingOfEquals(times("1.5", week()), "P1.5W");
        assertFormattingOfEquals(times("1.52", week()), "P1.52W");
    }

    @Test
    void testFormatYearDuration() {
        assertFormattingOfEquals(times("0", year), "P0Y");
        assertFormattingOfEquals(times("1", year), "P1Y");
        assertFormattingOfEquals(times("100", year), "P100Y");
        assertFormattingOfEquals(times("1.5", year), "P1.5Y");
        assertFormattingOfEquals(times("1.52", year), "P1.52Y");
    }
}
