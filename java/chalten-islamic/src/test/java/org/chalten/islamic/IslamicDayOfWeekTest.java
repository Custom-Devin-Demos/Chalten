package org.chalten.islamic;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.chalten.core.DayOfWeek;
import org.chalten.localization.Translations;
import org.junit.jupiter.api.Test;

class IslamicDayOfWeekTest extends IslamicCalendarTest {

    private static final List<DayOfWeek> ISO_DAYS = List.of(DayOfWeek.SUNDAY, DayOfWeek.MONDAY, DayOfWeek.TUESDAY,
            DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY);

    private List<IslamicDayOfWeek> daysOfWeek() {
        return List.of(IslamicDayOfWeek.YAWN_AL_AHAD, IslamicDayOfWeek.YAWN_AL_ITHNAYN,
                IslamicDayOfWeek.YAWN_ATH_THULATHA, IslamicDayOfWeek.YAWN_AL_ARBAA, IslamicDayOfWeek.YAWN_AL_KHAMEES,
                IslamicDayOfWeek.YAWN_AL_JUMUHA, IslamicDayOfWeek.YAWN_AS_SABT);
    }

    @Test
    void testAsISODayOfWeek() {
        for (int index = 0; index < 7; index++) {
            assertEquals(ISO_DAYS.get(index), daysOfWeek().get(index).asISODayOfWeek());
        }
    }

    @Test
    void testAsIslamicDayOfWeek() {
        for (int index = 0; index < 7; index++) {
            IslamicDayOfWeek dayOfWeek = daysOfWeek().get(index);
            assertEquals(dayOfWeek, IslamicDayOfWeek.from(ISO_DAYS.get(index)));
            assertEquals(dayOfWeek, dayOfWeek.asIslamicDayOfWeek());
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
        assertEquals(List.of("Yawn al-Ahad", "Yawn al-Ithnayn", "Yawn ath-Thulatha", "Yawn al-Arba'a'",
                "Yawn al-Khamees", "Yawn al-Jumu'ah", "Yawn as-Sabt"),
                daysOfWeek().stream().map(IslamicDayOfWeek::fullName).toList());
    }

    @Test
    void testNamesAreLocalized() {
        Map<String, String> spanish = Map.of("Yawn al-Ahad", "al-ahad", "Yawn al-Ithnayn", "al-ithnáyn",
                "Yawn ath-Thulatha", "al-tlat", "Yawn al-Arba'a", "al-arba'a", "Yawn al-Khamees", "al-jmís",
                "Yawn al-Jumu'ah", "al-yumua'a", "Yawn as-Sabt", "al-sabt");
        assertEquals(List.of("al-ahad", "al-ithnáyn", "al-tlat", "Yawn al-Arba'a'", "al-jmís", "al-yumua'a",
                "al-sabt"), Translations.during(source -> spanish.getOrDefault(source, source),
                        () -> daysOfWeek().stream().map(IslamicDayOfWeek::fullName).toList()));
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
        assertEquals("Yawn as-Sabt", IslamicDayOfWeek.YAWN_AS_SABT.toString());
    }

    @Test
    void testShortNames() {
        assertEquals(List.of("Ahd", "Ith", "Thu", "Arb", "Kha", "Jum", "Sab"),
                daysOfWeek().stream().map(IslamicDayOfWeek::shortName).toList());
    }

    @Test
    void testShortNamesAreLocalized() {
        Map<String, String> spanish = Map.of("Ahd", "aha", "Ith", "ith", "Thu", "tla", "Arb", "arb", "Kha", "jmi",
                "Jum", "yum", "Sab", "sab");
        assertEquals(List.of("aha", "ith", "tla", "arb", "jmi", "yum", "sab"),
                Translations.during(source -> spanish.getOrDefault(source, source),
                        () -> daysOfWeek().stream().map(IslamicDayOfWeek::shortName).toList()));
    }
}
