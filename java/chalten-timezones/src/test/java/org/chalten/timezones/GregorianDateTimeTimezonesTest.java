package org.chalten.timezones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.chalten.core.TimeOfDay;
import org.chalten.testkit.ChaltenTest;
import org.junit.jupiter.api.Test;

/** Smalltalk {@code GregorianDateTimeTest} extension methods of {@code Chalten-Timezones-Tests}. */
class GregorianDateTimeTimezonesTest extends ChaltenTest {

    @Test
    void testInTimeZone() {
        TimeZoneRuleset buenosAires = IANATimeZoneDatabase.current().at("America/Argentina/Buenos_Aires");
        TimeZoneRuleset tokyo = IANATimeZoneDatabase.current().at("Asia/Tokyo");
        assertEquals("1970-01-01T00:00:00.000000-03:00",
                january.first().of(1970).atMidnight().in(buenosAires).toString());
        assertEquals("1970-01-01T00:00:00.000000+09:00", january.first().of(1970).atMidnight().in(tokyo).toString());
    }

    @Test
    void testInTimeZoneWhenLocalTimeIsAmbiguous() {
        TimeZoneRuleset newYork = IANATimeZoneDatabase.current().at("America/New_York");
        AmbiguousTimeException error = assertThrows(AmbiguousTimeException.class,
                () -> november.second().of(2025).at(TimeOfDay.hours(1, 30)).in(newYork));
        assertEquals("2025-11-02T01:30:00.000000-04:00", error.earlierDateTime().toString());
        assertEquals("2025-11-02T01:30:00.000000-05:00", error.laterDateTime().toString());
        assertTrue(error.earlierDateTime().compareTo(error.laterDateTime()) < 0);
        assertEquals(
                "Cannot convert the local date time into an absolute point in time because it's ambiguous in this timezone",
                error.messageText());
    }

    @Test
    void testInTimeZoneWhenLocalTimeIsSkipped() {
        TimeZoneRuleset newYork = IANATimeZoneDatabase.current().at("America/New_York");
        SkippedTimeException error = assertThrows(SkippedTimeException.class,
                () -> march.dayOfMonthAt(9).of(2025).at(TimeOfDay.hours(2, 30)).in(newYork));
        assertEquals("The local time doesn't exist in the timezone due to daylight saving time changes.",
                error.messageText());
    }
}
