package org.chalten.localization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import org.chalten.core.DayOfWeek;
import org.chalten.core.FixedOffsetRuleset;
import org.chalten.core.GregorianDateTime;
import org.chalten.core.GregorianMonth;
import org.chalten.core.GregorianYear;
import org.chalten.core.Instant;
import org.chalten.core.TimeOfDay;
import org.chalten.core.TimeOffsetAwareGregorianDateTime;
import org.chalten.measure.ChaltenAssertionException;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;
import org.junit.jupiter.api.Test;

class GregorianLocalizationTest {

    private static final String NNBSP = "\u202F";
    private static final List<Consumer<FormatterOptions>> STYLES = List.of(FormatterOptions::shortStyle,
            FormatterOptions::mediumStyle, FormatterOptions::longStyle, FormatterOptions::fullStyle);

    private final GregorianMonth january = GregorianMonth.JANUARY;
    private final GregorianMonth march = GregorianMonth.MARCH;
    private final GregorianMonth december = GregorianMonth.DECEMBER;

    /** {@code expected} holds the short, medium, long and full localizations, in that order. */
    private static <T> void assertLocalizationsOf(T chronologyObject, Function<T, String> localized,
            Function<Consumer<FormatterOptions>, String> localizedWith, String languageTag, String... expected) {
        CurrentLocale.use(languageTag, () -> {
            for (int index = 0; index < STYLES.size(); index++) {
                assertEquals(expected[index], localizedWith.apply(STYLES.get(index)));
            }
            assertEquals(expected[1], localized.apply(chronologyObject));
            return null;
        });
    }

    private static void assertLocalizationsOf(GregorianDateTime dateTime, String languageTag, String... expected) {
        assertLocalizationsOf(dateTime, GregorianDateTime::localized, dateTime::localized, languageTag, expected);
    }

    private static void assertLocalizationsOf(TimeOffsetAwareGregorianDateTime dateTime, String languageTag,
            String... expected) {
        assertLocalizationsOf(dateTime, TimeOffsetAwareGregorianDateTime::localized, dateTime::localized,
                languageTag, expected);
    }

    private static void assertLocalizationsOf(Instant instant, String languageTag, String... expected) {
        assertLocalizationsOf(instant, Instant::localized, instant::localized, languageTag, expected);
    }

    private static void assertLocalizationsOf(TimeOfDay timeOfDay, String languageTag, String... expected) {
        assertLocalizationsOf(timeOfDay, TimeOfDay::localized, timeOfDay::localized, languageTag, expected);
    }

    private static void assertLocalizationsOf(org.chalten.core.GregorianDate date, String languageTag,
            String... expected) {
        assertLocalizationsOf(date, org.chalten.core.GregorianDate::localized, date::localized, languageTag,
                expected);
    }

    private static void assertLocalizationsOf(org.chalten.core.CalendarDayOfMonth<?> dayOfMonth, String languageTag,
            String... expected) {
        assertLocalizationsOf(dayOfMonth, org.chalten.core.CalendarDayOfMonth::localized, dayOfMonth::localized,
                languageTag, expected);
    }

    private static void assertLocalizationsOf(org.chalten.core.GregorianMonthOfYear monthOfYear, String languageTag,
            String... expected) {
        assertLocalizationsOf(monthOfYear, org.chalten.core.GregorianMonthOfYear::localized, monthOfYear::localized,
                languageTag, expected);
    }

    private static void assertLocalizationsOf(GregorianMonth month, String languageTag, String... expected) {
        assertLocalizationsOf(month, GregorianMonth::localized, month::localized, languageTag, expected);
    }

    private static void assertLocalizationsOf(DayOfWeek dayOfWeek, String languageTag, String... expected) {
        assertLocalizationsOf(dayOfWeek, DayOfWeek::localized, dayOfWeek::localized, languageTag, expected);
    }

    private static void assertLocalizationsOf(GregorianYear year, String languageTag, String... expected) {
        assertLocalizationsOf(year, GregorianYear::localized, year::localized, languageTag, expected);
    }

    private void assertOffsetIsShownAs(TimeQuantity offset, String shortOffset, String longOffset) {
        TimeOffsetAwareGregorianDateTime dateTime = marchFifthAt(timeOfDay()).in(FixedOffsetRuleset.of(offset));
        CurrentLocale.use("en-GB", () -> {
            assertEquals("5 March 2026, 14:30:45 " + shortOffset, dateTime.localized(FormatterOptions::longStyle));
            assertEquals("Thursday, 5 March 2026, 14:30:45 " + longOffset,
                    dateTime.localized(FormatterOptions::fullStyle));
            return null;
        });
    }

    /** The medium patterns and the marks are needed to create a formatter. */
    private static LocaleConventions conventionsWith(String json) {
        LocaleConventions conventions = new LocaleConventions();
        conventions.loadJSONChronologyConventionsFromString("{\"*\": {\"mediumDateFormat\": \"MMM d, y\", "
                + "\"mediumTimeFormat\": \"h:mm:ss a\", \"mediumDateTimeFormat\": \"{1}, {0}\", "
                + "\"mediumYearMonthFormat\": \"MMM y\", \"mediumMonthDayFormat\": \"MMM d\", \"amMark\": \"AM\", "
                + "\"pmMark\": \"PM\"}}");
        conventions.loadJSONChronologyConventionsFromString(json);
        return conventions;
    }

    private static String format(Object chronologyObject, String languageTag, LocaleConventions conventions) {
        return LocalizedChronologyFormatter.forLanguage(languageTag, conventions, options -> { })
                .format(chronologyObject);
    }

    private GregorianDateTime marchFifthAt(TimeOfDay timeOfDay) {
        return march.fifth().of(2026).at(timeOfDay);
    }

    /** March 5, 2026 at 14:30:45, three hours behind UTC. */
    private TimeOffsetAwareGregorianDateTime offsetAwareDateTime() {
        return marchFifthAt(timeOfDay()).in(FixedOffsetRuleset.of(hours(-3)));
    }

    private static TimeOfDay timeOfDay() {
        return TimeOfDay.hours(14, 30, 45);
    }

    private static TimeQuantity hours(long amount) {
        return TimeQuantity.of(amount, TimeUnits.HOUR);
    }

    private static TimeQuantity minutes(long amount) {
        return TimeQuantity.of(amount, TimeUnits.MINUTE);
    }

    private static TimeQuantity seconds(long amount) {
        return TimeQuantity.of(amount, TimeUnits.SECOND);
    }

    private static String pm(String time) {
        return time + NNBSP + "PM";
    }

    private static String pmES(String time) {
        return time + NNBSP + "p." + NNBSP + "m.";
    }

    @Test
    void testDateTimes() {
        GregorianDateTime dateTime = marchFifthAt(timeOfDay());
        assertLocalizationsOf(dateTime, "en", "3/5/26, " + pm("2:30"), "Mar 5, 2026, " + pm("2:30:45"),
                "March 5, 2026, " + pm("2:30:45"), "Thursday, March 5, 2026, " + pm("2:30:45"));
        assertLocalizationsOf(dateTime, "en-GB", "05/03/2026, 14:30", "5 Mar 2026, 14:30:45",
                "5 March 2026, 14:30:45", "Thursday, 5 March 2026, 14:30:45");
        assertLocalizationsOf(dateTime, "es", "5/3/26, 14:30", "5 Mar 2026, 14:30:45", "5 de Marzo de 2026, 14:30:45",
                "Jueves, 5 de Marzo de 2026, 14:30:45");
        assertLocalizationsOf(dateTime, "es-AR", "5/3/26, " + pmES("2:30"), "5 Mar 2026, " + pmES("2:30:45"),
                "5 de Marzo de 2026, " + pmES("2:30:45"), "Jueves, 5 de Marzo de 2026, " + pmES("2:30:45"));
    }

    @Test
    void testDates() {
        org.chalten.core.GregorianDate date = march.fifth().of(2026);
        assertLocalizationsOf(date, "en", "3/5/26", "Mar 5, 2026", "March 5, 2026", "Thursday, March 5, 2026");
        assertLocalizationsOf(date, "en-GB", "05/03/2026", "5 Mar 2026", "5 March 2026", "Thursday, 5 March 2026");
        assertLocalizationsOf(date, "es", "5/3/26", "5 Mar 2026", "5 de Marzo de 2026", "Jueves, 5 de Marzo de 2026");
        assertLocalizationsOf(date, "es-AR", "5/3/26", "5 Mar 2026", "5 de Marzo de 2026",
                "Jueves, 5 de Marzo de 2026");
    }

    @Test
    void testDatesPadTheirNumbers() {
        assertLocalizationsOf(january.second().of(2005), "en", "1/2/05", "Jan 2, 2005", "January 2, 2005",
                "Sunday, January 2, 2005");
        assertLocalizationsOf(january.second().of(2005), "en-GB", "02/01/2005", "2 Jan 2005", "2 January 2005",
                "Sunday, 2 January 2005");
        assertLocalizationsOf(december.thirtyFirst().of(1900), "es", "31/12/00", "31 Dic 1900",
                "31 de Diciembre de 1900", "Lunes, 31 de Diciembre de 1900");
    }

    @Test
    void testDaysOfMonth() {
        org.chalten.core.CalendarDayOfMonth<?> dayOfMonth = march.fifth();
        assertLocalizationsOf(dayOfMonth, "en", "3/5", "Mar 5", "March 5", "March 5");
        assertLocalizationsOf(dayOfMonth, "en-GB", "05/03", "5 Mar", "5 March", "5 March");
        assertLocalizationsOf(dayOfMonth, "es", "5/3", "5 Mar", "5 de Marzo", "5 de Marzo");
        assertLocalizationsOf(dayOfMonth, "es-AR", "5/3", "5 Mar", "5 de Marzo", "5 de Marzo");
    }

    @Test
    void testDaysOfWeek() {
        for (String languageTag : List.of("en", "en-GB")) {
            assertLocalizationsOf(DayOfWeek.THURSDAY, languageTag, "Th", "Th", "Thursday", "Thursday");
        }
        for (String languageTag : List.of("es", "es-AR")) {
            assertLocalizationsOf(DayOfWeek.THURSDAY, languageTag, "Ju", "Ju", "Jueves", "Jueves");
        }
    }

    @Test
    void testFormattersUseTheirOwnLanguage() {
        CurrentLocale.use("en", () -> {
            assertEquals("Jueves, 5 de Marzo de 2026",
                    LocalizedChronologyFormatter.forLanguage("es-AR", FormatterOptions::fullStyle)
                            .format(march.fifth().of(2026)));
            assertEquals("Mar 5, 2026", march.fifth().of(2026).localized());
            return null;
        });
    }

    @Test
    void testInstants() {
        Instant instant = offsetAwareDateTime().asInstant();
        assertLocalizationsOf(instant, "en", "3/5/26, " + pm("5:30"), "Mar 5, 2026, " + pm("5:30:45"),
                "March 5, 2026, " + pm("5:30:45") + " UTC", "Thursday, March 5, 2026, " + pm("5:30:45") + " UTC");
        assertLocalizationsOf(instant, "en-GB", "05/03/2026, 17:30", "5 Mar 2026, 17:30:45",
                "5 March 2026, 17:30:45 UTC", "Thursday, 5 March 2026, 17:30:45 UTC");
        assertLocalizationsOf(instant, "es", "5/3/26, 17:30", "5 Mar 2026, 17:30:45",
                "5 de Marzo de 2026, 17:30:45 UTC", "Jueves, 5 de Marzo de 2026, 17:30:45 (UTC)");
        assertLocalizationsOf(instant, "es-AR", "5/3/26, " + pmES("5:30"), "5 Mar 2026, " + pmES("5:30:45"),
                "5 de Marzo de 2026, " + pmES("5:30:45") + " UTC",
                "Jueves, 5 de Marzo de 2026, " + pmES("5:30:45") + " UTC");
    }

    @Test
    void testInstantsAreShownOnTheirUTCDate() {
        Instant instant = marchFifthAt(TimeOfDay.hours(22)).in(FixedOffsetRuleset.of(hours(-3))).asInstant();
        String am = "1:00" + NNBSP + "AM";
        String amWithSeconds = "1:00:00" + NNBSP + "AM";
        assertLocalizationsOf(instant, "en", "3/6/26, " + am, "Mar 6, 2026, " + amWithSeconds,
                "March 6, 2026, " + amWithSeconds + " UTC", "Friday, March 6, 2026, " + amWithSeconds + " UTC");
    }

    @Test
    void testMidnightAndNoon() {
        String am = NNBSP + "AM";
        assertLocalizationsOf(TimeOfDay.startOfDay(), "en", "12:00" + am, "12:00:00" + am, "12:00:00" + am,
                "12:00:00" + am);
        assertLocalizationsOf(TimeOfDay.hours(12), "en", pm("12:00"), pm("12:00:00"), pm("12:00:00"),
                pm("12:00:00"));
        assertLocalizationsOf(TimeOfDay.hours(0, 5), "en-GB", "00:05", "00:05:00", "00:05:00", "00:05:00");
        assertLocalizationsOf(TimeOfDay.hours(0, 5), "es", "0:05", "0:05:00", "0:05:00", "0:05:00");
        assertLocalizationsOf(TimeOfDay.hours(12), "es-AR", pmES("12:00"), pmES("12:00:00"), pmES("12:00:00"),
                pmES("12:00:00"));
    }

    @Test
    void testMonths() {
        for (String languageTag : List.of("en", "en-GB")) {
            assertLocalizationsOf(march, languageTag, "Mar", "Mar", "March", "March");
        }
        for (String languageTag : List.of("es", "es-AR")) {
            assertLocalizationsOf(march, languageTag, "Mar", "Mar", "Marzo", "Marzo");
        }
        assertLocalizationsOf(january, "es", "Ene", "Ene", "Enero", "Enero");
    }

    @Test
    void testMonthsOfYear() {
        org.chalten.core.GregorianMonthOfYear monthOfYear = march.of(2026);
        assertLocalizationsOf(monthOfYear, "en", "3/2026", "Mar 2026", "March 2026", "March 2026");
        assertLocalizationsOf(monthOfYear, "en-GB", "03/2026", "Mar 2026", "March 2026", "March 2026");
        assertLocalizationsOf(monthOfYear, "es", "3/2026", "Mar 2026", "Marzo de 2026", "Marzo de 2026");
        assertLocalizationsOf(monthOfYear, "es-AR", "3-2026", "Mar 2026", "Marzo de 2026", "Marzo de 2026");
    }

    @Test
    void testOffsetAwareDateTimes() {
        TimeOffsetAwareGregorianDateTime dateTime = offsetAwareDateTime();
        assertLocalizationsOf(dateTime, "en", "3/5/26, " + pm("2:30"), "Mar 5, 2026, " + pm("2:30:45"),
                "March 5, 2026, " + pm("2:30:45") + " UTC-3",
                "Thursday, March 5, 2026, " + pm("2:30:45") + " UTC-03:00");
        assertLocalizationsOf(dateTime, "en-GB", "05/03/2026, 14:30", "5 Mar 2026, 14:30:45",
                "5 March 2026, 14:30:45 UTC-3", "Thursday, 5 March 2026, 14:30:45 UTC-03:00");
        assertLocalizationsOf(dateTime, "es", "5/3/26, 14:30", "5 Mar 2026, 14:30:45",
                "5 de Marzo de 2026, 14:30:45 UTC-3", "Jueves, 5 de Marzo de 2026, 14:30:45 (UTC-03:00)");
        assertLocalizationsOf(dateTime, "es-AR", "5/3/26, " + pmES("2:30"), "5 Mar 2026, " + pmES("2:30:45"),
                "5 de Marzo de 2026, " + pmES("2:30:45") + " UTC-3",
                "Jueves, 5 de Marzo de 2026, " + pmES("2:30:45") + " UTC-03:00");
    }

    @Test
    void testOffsets() {
        assertOffsetIsShownAs(hours(5).plus(minutes(30)), "UTC+5:30", "UTC+05:30");
        assertOffsetIsShownAs(hours(10), "UTC+10", "UTC+10:00");
        assertOffsetIsShownAs(hours(-10), "UTC-10", "UTC-10:00");
        assertOffsetIsShownAs(hours(0), "UTC", "UTC");
        assertOffsetIsShownAs(hours(-4).minus(minutes(16)).minus(seconds(48)), "UTC-4:16:48", "UTC-04:16:48");
    }

    @Test
    void testPatternsNeedingPartsTheObjectDoesNotHave() {
        LocaleConventions conventions = conventionsWith("{\"*\": {\"mediumMonthDayFormat\": \"d MMM y\"}}");
        assertEquals("The pattern needs a year the formatted object doesn't have",
                assertThrows(ChaltenAssertionException.class, () -> format(march.fifth(), "en", conventions))
                        .getMessage());
    }

    @Test
    void testPlaceholdersOutsideCombiningPatterns() {
        LocaleConventions conventions = conventionsWith("{\"*\": {\"mediumDateFormat\": \"d/M/y {0}\"}}");
        assertEquals("The time placeholder can only be used in a pattern combining a date and a time",
                assertThrows(ChaltenAssertionException.class,
                        () -> format(march.fifth().of(2026), "en", conventions)).getMessage());
    }

    @Test
    void testSecondsAreShownWithoutTheirFraction() {
        assertLocalizationsOf(TimeOfDay.hours(14, 30, 45, 999999), "es", "14:30", "14:30:45", "14:30:45",
                "14:30:45");
    }

    @Test
    void testTimesOfDay() {
        assertLocalizationsOf(timeOfDay(), "en", pm("2:30"), pm("2:30:45"), pm("2:30:45"), pm("2:30:45"));
        assertLocalizationsOf(timeOfDay(), "en-GB", "14:30", "14:30:45", "14:30:45", "14:30:45");
        assertLocalizationsOf(timeOfDay(), "es", "14:30", "14:30:45", "14:30:45", "14:30:45");
        assertLocalizationsOf(timeOfDay(), "es-AR", pmES("2:30"), pmES("2:30:45"), pmES("2:30:45"),
                pmES("2:30:45"));
    }

    @Test
    void testYears() {
        for (String languageTag : List.of("en", "en-GB", "es", "es-AR")) {
            assertLocalizationsOf(GregorianYear.of(2026), languageTag, "2026", "2026", "2026", "2026");
            assertLocalizationsOf(GregorianYear.of(12026), languageTag, "12026", "12026", "12026", "12026");
        }
    }
}
