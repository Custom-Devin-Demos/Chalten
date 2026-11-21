package org.chalten.hebrew;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.chalten.core.DayOfWeek;
import org.chalten.localization.Translations;
import org.junit.jupiter.api.Test;

class HebrewDayOfWeekTest extends HebrewCalendarTest {

    private static final List<DayOfWeek> ISO_DAYS = List.of(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);

    private List<HebrewDayOfWeek> daysOfWeek() {
        return List.of(HebrewDayOfWeek.YOM_RISHON, HebrewDayOfWeek.YOM_SHENI, HebrewDayOfWeek.YOM_SHLISHI,
                HebrewDayOfWeek.YOM_REVII, HebrewDayOfWeek.YOM_HAMISHI, HebrewDayOfWeek.YOM_SHISHI,
                HebrewDayOfWeek.YOM_SHABBAT);
    }

    @Test
    void testAsHebrewDayOfWeek() {
        for (int index = 0; index < 7; index++) {
            HebrewDayOfWeek dayOfWeek = daysOfWeek().get(index);
            assertEquals(dayOfWeek, HebrewDayOfWeek.from(ISO_DAYS.get(index)));
            assertEquals(dayOfWeek, dayOfWeek.asHebrewDayOfWeek());
        }
    }

    @Test
    void testAsISODayOfWeek() {
        for (int index = 0; index < 7; index++) {
            assertEquals(ISO_DAYS.get(index), daysOfWeek().get(index).asISODayOfWeek());
        }
    }

    @Test
    void testInstanceCreation() {
        for (int index = 1; index <= 7; index++) {
            assertEquals(index, daysOfWeek().get(index - 1).positionInWeek());
        }
    }

    @Test
    void testNames() {
        assertEquals(List.of("Yom Rishon", "Yom Sheni", "Yom Shlishi", "Yom Revii", "Yom Hamishi", "Yom Shishi",
                "Yom Shabbat"), daysOfWeek().stream().map(HebrewDayOfWeek::fullName).toList());
    }

    @Test
    void testNamesAreLocalized() {
        Map<String, String> spanish = Map.of("Yom Rishon", "Yom Rishón", "Yom Sheni", "Yom Shení", "Yom Shlishi",
                "Yom Shelishi", "Yom Revii", "Yom Revií", "Yom Hamishi", "Yom Jamishi", "Yom Shishi", "Yom Shishí",
                "Yom Shabbat", "Shabat");
        assertEquals(List.of("Yom Rishón", "Yom Shení", "Yom Shelishi", "Yom Revií", "Yom Jamishi", "Yom Shishí",
                "Shabat"), Translations.during(source -> spanish.getOrDefault(source, source),
                        () -> daysOfWeek().stream().map(HebrewDayOfWeek::fullName).toList()));
    }

    @Test
    void testNext() {
        for (int index = 0; index < 7; index++) {
            assertEquals(daysOfWeek().get((index + 1) % 7), daysOfWeek().get(index).next());
        }
    }

    @Test
    void testPrevious() {
        for (int index = 0; index < 7; index++) {
            assertEquals(daysOfWeek().get(index), daysOfWeek().get((index + 1) % 7).previous());
        }
    }

    @Test
    void testPrintString() {
        assertEquals("Yom Shabbat", HebrewDayOfWeek.YOM_SHABBAT.toString());
    }

    @Test
    void testShortNames() {
        assertEquals(List.of("Rish", "She", "Shli", "Rev", "Ham", "Shi", "Shab"),
                daysOfWeek().stream().map(HebrewDayOfWeek::shortName).toList());
    }

    @Test
    void testShortNamesAreLocalized() {
        Map<String, String> hebrew = Map.of("Rish", "א׳", "She", "ב׳", "Shli", "ג׳", "Rev", "ד׳", "Ham", "ה׳",
                "Shi", "ו׳", "Shab", "ש׳");
        assertEquals(List.of("א׳", "ב׳", "ג׳", "ד׳", "ה׳", "ו׳", "ש׳"),
                Translations.during(source -> hebrew.getOrDefault(source, source),
                        () -> daysOfWeek().stream().map(HebrewDayOfWeek::shortName).toList()));
    }
}
