package org.chalten.localization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;

class ShippedChronologyConventionsTest {

    private static final String NNBSP = "\u202F";

    private static String propertyNamed(String name, String languageTag) {
        return LocaleConventions.current().chronologyPropertyNamed(name, languageTag);
    }

    private static void assertAll(List<String> names, String languageTag, List<String> formats) {
        for (int index = 0; index < names.size(); index++) {
            assertEquals(formats.get(index), propertyNamed(names.get(index), languageTag));
        }
    }

    private static void assertConventionsForAreDates(String languageTag, List<String> dateFormats,
            List<String> timeFormats) {
        assertAll(List.of("shortDateFormat", "mediumDateFormat", "longDateFormat", "fullDateFormat"), languageTag,
                dateFormats);
        assertAll(List.of("shortTimeFormat", "mediumTimeFormat", "longTimeFormat", "fullTimeFormat"), languageTag,
                timeFormats);
    }

    private static void assertConventionsForAreYearMonths(String languageTag, List<String> yearMonthFormats,
            List<String> monthDayFormats) {
        assertAll(List.of("shortYearMonthFormat", "mediumYearMonthFormat", "longYearMonthFormat"), languageTag,
                yearMonthFormats);
        assertAll(List.of("shortMonthDayFormat", "mediumMonthDayFormat", "longMonthDayFormat"), languageTag,
                monthDayFormats);
    }

    private static void assertDateTimeFormatsForAre(String languageTag, String format) {
        for (String name : List.of("shortDateTimeFormat", "mediumDateTimeFormat", "longDateTimeFormat",
                "fullDateTimeFormat")) {
            assertEquals(format, propertyNamed(name, languageTag));
        }
    }

    private static void assertMarksForAre(String languageTag, String amMark, String pmMark) {
        assertEquals(amMark, propertyNamed("amMark", languageTag));
        assertEquals(pmMark, propertyNamed("pmMark", languageTag));
    }

    @Test
    void testArgentinianSpanish() {
        assertConventionsForAreDates("es-AR",
                List.of("d/M/yy", "d MMM y", "d 'de' MMMM 'de' y", "EEEE, d 'de' MMMM 'de' y"),
                List.of("h:mm" + NNBSP + "a", "h:mm:ss" + NNBSP + "a", "h:mm:ss" + NNBSP + "a z",
                        "h:mm:ss" + NNBSP + "a zzzz"));
        assertConventionsForAreYearMonths("es-AR", List.of("M-y", "MMM y", "MMMM 'de' y"),
                List.of("d/M", "d MMM", "d 'de' MMMM"));
        assertDateTimeFormatsForAre("es-AR", "{1}, {0}");
        assertMarksForAre("es-AR", "a." + NNBSP + "m.", "p." + NNBSP + "m.");
    }

    @Test
    void testBritishEnglish() {
        assertConventionsForAreDates("en-GB", List.of("dd/MM/y", "d MMM y", "d MMMM y", "EEEE, d MMMM y"),
                List.of("HH:mm", "HH:mm:ss", "HH:mm:ss z", "HH:mm:ss zzzz"));
        assertConventionsForAreYearMonths("en-GB", List.of("MM/y", "MMM y", "MMMM y"),
                List.of("dd/MM", "d MMM", "d MMMM"));
        assertDateTimeFormatsForAre("en-GB", "{1}, {0}");
        assertMarksForAre("en-GB", "am", "pm");
    }

    @Test
    void testDefaults() {
        assertConventionsForAreDates("ja", List.of("y-MM-dd", "y MMM d", "y MMMM d", "y MMMM d, EEEE"),
                List.of("HH:mm", "HH:mm:ss", "HH:mm:ss z", "HH:mm:ss zzzz"));
        assertConventionsForAreYearMonths("ja", List.of("y-MM", "y MMM", "y MMMM"),
                List.of("MM-dd", "MMM d", "MMMM d"));
        assertDateTimeFormatsForAre("ja", "{1} {0}");
        assertMarksForAre("ja", "AM", "PM");
    }

    @Test
    void testEnglish() {
        for (String languageTag : List.of("en", "en-US")) {
            assertConventionsForAreDates(languageTag, List.of("M/d/yy", "MMM d, y", "MMMM d, y", "EEEE, MMMM d, y"),
                    List.of("h:mm" + NNBSP + "a", "h:mm:ss" + NNBSP + "a", "h:mm:ss" + NNBSP + "a z",
                            "h:mm:ss" + NNBSP + "a zzzz"));
            assertConventionsForAreYearMonths(languageTag, List.of("M/y", "MMM y", "MMMM y"),
                    List.of("M/d", "MMM d", "MMMM d"));
            assertDateTimeFormatsForAre(languageTag, "{1}, {0}");
            assertMarksForAre(languageTag, "AM", "PM");
        }
    }

    @Test
    void testSpanish() {
        for (String languageTag : List.of("es", "es-ES")) {
            assertConventionsForAreDates(languageTag,
                    List.of("d/M/yy", "d MMM y", "d 'de' MMMM 'de' y", "EEEE, d 'de' MMMM 'de' y"),
                    List.of("H:mm", "H:mm:ss", "H:mm:ss z", "H:mm:ss (zzzz)"));
            assertConventionsForAreYearMonths(languageTag, List.of("M/y", "MMM y", "MMMM 'de' y"),
                    List.of("d/M", "d MMM", "d 'de' MMMM"));
            assertDateTimeFormatsForAre(languageTag, "{1}, {0}");
            assertMarksForAre(languageTag, "a." + NNBSP + "m.", "p." + NNBSP + "m.");
        }
    }
}
