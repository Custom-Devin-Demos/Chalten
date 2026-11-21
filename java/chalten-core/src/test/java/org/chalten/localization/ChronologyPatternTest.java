package org.chalten.localization;

import static org.chalten.localization.ChronologyPattern.Field.DATE;
import static org.chalten.localization.ChronologyPattern.Field.DAY;
import static org.chalten.localization.ChronologyPattern.Field.DAY_OF_WEEK_NAME;
import static org.chalten.localization.ChronologyPattern.Field.DAY_OF_WEEK_SHORT_NAME;
import static org.chalten.localization.ChronologyPattern.Field.DAY_PERIOD;
import static org.chalten.localization.ChronologyPattern.Field.HOUR_OF_12_HOUR_CLOCK;
import static org.chalten.localization.ChronologyPattern.Field.HOUR_OF_24_HOUR_CLOCK;
import static org.chalten.localization.ChronologyPattern.Field.LONG_OFFSET;
import static org.chalten.localization.ChronologyPattern.Field.MINUTE;
import static org.chalten.localization.ChronologyPattern.Field.MONTH;
import static org.chalten.localization.ChronologyPattern.Field.MONTH_NAME;
import static org.chalten.localization.ChronologyPattern.Field.MONTH_SHORT_NAME;
import static org.chalten.localization.ChronologyPattern.Field.SECOND;
import static org.chalten.localization.ChronologyPattern.Field.SHORT_OFFSET;
import static org.chalten.localization.ChronologyPattern.Field.TIME;
import static org.chalten.localization.ChronologyPattern.Field.TWO_DIGIT_DAY;
import static org.chalten.localization.ChronologyPattern.Field.TWO_DIGIT_HOUR_OF_12_HOUR_CLOCK;
import static org.chalten.localization.ChronologyPattern.Field.TWO_DIGIT_HOUR_OF_24_HOUR_CLOCK;
import static org.chalten.localization.ChronologyPattern.Field.TWO_DIGIT_MINUTE;
import static org.chalten.localization.ChronologyPattern.Field.TWO_DIGIT_MONTH;
import static org.chalten.localization.ChronologyPattern.Field.TWO_DIGIT_SECOND;
import static org.chalten.localization.ChronologyPattern.Field.TWO_DIGIT_YEAR;
import static org.chalten.localization.ChronologyPattern.Field.YEAR;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.chalten.measure.InstanceCreationFailedException;
import org.junit.jupiter.api.Test;

class ChronologyPatternTest {

    private static void assertElementsOf(ChronologyPattern pattern, Object... expected) {
        assertEquals(List.of(expected), pattern.elements());
    }

    private static void assertPattern(String pattern, Object... expected) {
        assertElementsOf(ChronologyPattern.fromString(pattern), expected);
    }

    private static void assertUnsupported(String pattern, String explanation) {
        assertEquals(explanation, assertThrows(InstanceCreationFailedException.class,
                () -> ChronologyPattern.fromString(pattern)).getMessage());
    }

    @Test
    void testDay() {
        assertPattern("d", DAY);
        assertPattern("dd", TWO_DIGIT_DAY);
    }

    @Test
    void testDayOfWeek() {
        for (String pattern : List.of("E", "EE", "EEE", "ccc")) {
            assertPattern(pattern, DAY_OF_WEEK_SHORT_NAME);
        }
        for (String pattern : List.of("EEEE", "cccc")) {
            assertPattern(pattern, DAY_OF_WEEK_NAME);
        }
    }

    @Test
    void testDayPeriod() {
        assertPattern("a", DAY_PERIOD);
    }

    @Test
    void testEmptyPattern() {
        assertPattern("");
    }

    @Test
    void testHour() {
        assertPattern("h", HOUR_OF_12_HOUR_CLOCK);
        assertPattern("hh", TWO_DIGIT_HOUR_OF_12_HOUR_CLOCK);
        assertPattern("H", HOUR_OF_24_HOUR_CLOCK);
        assertPattern("HH", TWO_DIGIT_HOUR_OF_24_HOUR_CLOCK);
    }

    @Test
    void testLiterals() {
        assertPattern("H:mm:ss (zzzz)", HOUR_OF_24_HOUR_CLOCK, ":", TWO_DIGIT_MINUTE, ":", TWO_DIGIT_SECOND, " (",
                LONG_OFFSET, ")");
        assertPattern("dd-MMM-y", TWO_DIGIT_DAY, "-", MONTH_SHORT_NAME, "-", YEAR);
        assertPattern("h:mm\u202Fa", HOUR_OF_12_HOUR_CLOCK, ":", TWO_DIGIT_MINUTE, "\u202F", DAY_PERIOD);
    }

    @Test
    void testMinute() {
        assertPattern("m", MINUTE);
        assertPattern("mm", TWO_DIGIT_MINUTE);
    }

    @Test
    void testMissingClosingQuote() {
        assertUnsupported("d 'de MMMM", "Missing closing quote in date and time pattern");
        assertUnsupported("'", "Missing closing quote in date and time pattern");
    }

    @Test
    void testMonth() {
        assertPattern("M", MONTH);
        assertPattern("L", MONTH);
        assertPattern("MM", TWO_DIGIT_MONTH);
        assertPattern("LL", TWO_DIGIT_MONTH);
        assertPattern("MMM", MONTH_SHORT_NAME);
        assertPattern("MMMM", MONTH_NAME);
    }

    @Test
    void testNonASCIILettersAreLiteral() {
        String year = "\u5E74";
        String month = "\u6708";
        String day = "\u65E5";
        assertPattern("y" + year + "M" + month + "d" + day, YEAR, year, MONTH, month, DAY, day);
    }

    @Test
    void testOffset() {
        for (String pattern : List.of("z", "zz", "zzz", "O")) {
            assertPattern(pattern, SHORT_OFFSET);
        }
        for (String pattern : List.of("zzzz", "OOOO")) {
            assertPattern(pattern, LONG_OFFSET);
        }
    }

    @Test
    void testPlaceholders() {
        assertPattern("{1}, {0}", DATE, ", ", TIME);
        assertPattern("{1} 'at' {0}", DATE, " at ", TIME);
    }

    @Test
    void testQuotedLiterals() {
        assertPattern("d 'de' MMMM 'de' y", DAY, " de ", MONTH_NAME, " de ", YEAR);
        assertPattern("'week w'", "week w");
        assertPattern("h 'o''clock' a", HOUR_OF_12_HOUR_CLOCK, " o'clock ", DAY_PERIOD);
        assertPattern("h''a", HOUR_OF_12_HOUR_CLOCK, "'", DAY_PERIOD);
        assertPattern("''", "'");
    }

    @Test
    void testReplacingDateAndTime() {
        ChronologyPattern date = ChronologyPattern.fromString("d/M/y");
        ChronologyPattern time = ChronologyPattern.fromString("H:mm");
        assertElementsOf(ChronologyPattern.fromString("{1}, {0}").replacingDateWith(date, time),
                DAY, "/", MONTH, "/", YEAR, ", ", HOUR_OF_24_HOUR_CLOCK, ":", TWO_DIGIT_MINUTE);
        assertElementsOf(ChronologyPattern.fromString("'date' {1} 'time' {0}").replacingDateWith(date, time),
                "date ", DAY, "/", MONTH, "/", YEAR, " time ", HOUR_OF_24_HOUR_CLOCK, ":", TWO_DIGIT_MINUTE);
        assertElementsOf(date.replacingDateWith(time, time), DAY, "/", MONTH, "/", YEAR);
    }

    @Test
    void testSecond() {
        assertPattern("s", SECOND);
        assertPattern("ss", TWO_DIGIT_SECOND);
    }

    @Test
    void testUnsupportedFields() {
        for (String field : List.of("G", "yyyy", "MMMMM", "c", "B", "zzzzz", "v", "k", "S")) {
            assertUnsupported("d " + field, "The field " + field + " is not supported in date and time patterns");
        }
    }

    @Test
    void testUnsupportedPlaceholders() {
        assertUnsupported("{2}", "The placeholder {2} is not supported in date and time patterns");
        assertUnsupported("{1", "Missing closing brace in date and time pattern");
    }

    @Test
    void testYear() {
        assertPattern("y", YEAR);
        assertPattern("yy", TWO_DIGIT_YEAR);
    }
}
