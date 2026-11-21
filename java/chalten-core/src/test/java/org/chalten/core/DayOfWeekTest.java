package org.chalten.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Map;
import org.chalten.localization.Translations;
import org.junit.jupiter.api.Test;

class DayOfWeekTest {

    private List<DayOfWeek> daysOfWeek() {
        return List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY);
    }

    private static <T> T inSpanish(Map<String, String> translations, java.util.function.Supplier<T> action) {
        return Translations.during(source -> translations.getOrDefault(source, source), action);
    }

    @Test
    void testConverting() {
        daysOfWeek().forEach(dayOfWeek -> assertEquals(dayOfWeek, dayOfWeek.asISODayOfWeek()));
    }

    @Test
    void testInstanceCreation() {
        for (int index = 1; index <= 7; index++) {
            assertEquals(index, daysOfWeek().get(index - 1).positionInWeek());
        }
    }

    @Test
    void testNames() {
        assertEquals(List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"),
                daysOfWeek().stream().map(DayOfWeek::fullName).toList());
    }

    @Test
    void testNamesAreLocalized() {
        Map<String, String> spanish = Map.of("Monday", "Lunes", "Tuesday", "Martes", "Wednesday", "Miércoles",
                "Thursday", "Jueves", "Friday", "Viernes", "Saturday", "Sábado", "Sunday", "Domingo");
        assertEquals(List.of("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado", "Domingo"),
                inSpanish(spanish, () -> daysOfWeek().stream().map(DayOfWeek::fullName).toList()));
    }

    @Test
    void testNext() {
        assertEquals(DayOfWeek.TUESDAY, DayOfWeek.MONDAY.next());
        assertEquals(DayOfWeek.WEDNESDAY, DayOfWeek.TUESDAY.next());
        assertEquals(DayOfWeek.THURSDAY, DayOfWeek.WEDNESDAY.next());
        assertEquals(DayOfWeek.FRIDAY, DayOfWeek.THURSDAY.next());
        assertEquals(DayOfWeek.SATURDAY, DayOfWeek.FRIDAY.next());
        assertEquals(DayOfWeek.SUNDAY, DayOfWeek.SATURDAY.next());
        assertEquals(DayOfWeek.MONDAY, DayOfWeek.SUNDAY.next());
    }

    @Test
    void testPrevious() {
        assertEquals(DayOfWeek.TUESDAY.previous(), DayOfWeek.MONDAY);
        assertEquals(DayOfWeek.WEDNESDAY.previous(), DayOfWeek.TUESDAY);
        assertEquals(DayOfWeek.THURSDAY.previous(), DayOfWeek.WEDNESDAY);
        assertEquals(DayOfWeek.FRIDAY.previous(), DayOfWeek.THURSDAY);
        assertEquals(DayOfWeek.SATURDAY.previous(), DayOfWeek.FRIDAY);
        assertEquals(DayOfWeek.SUNDAY.previous(), DayOfWeek.SATURDAY);
        assertEquals(DayOfWeek.MONDAY.previous(), DayOfWeek.SUNDAY);
    }

    @Test
    void testPrintString() {
        assertEquals("Sunday", DayOfWeek.SUNDAY.toString());
    }

    @Test
    void testShortNames() {
        assertEquals(List.of("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su"),
                daysOfWeek().stream().map(DayOfWeek::shortName).toList());
    }

    @Test
    void testShortNamesAreLocalized() {
        Map<String, String> spanish = Map.of("Mo", "LU", "Tu", "MA", "We", "MI", "Th", "JU", "Fr", "VI",
                "Sa", "SA", "Su", "DO");
        assertEquals(List.of("LU", "MA", "MI", "JU", "VI", "SA", "DO"),
                inSpanish(spanish, () -> daysOfWeek().stream().map(DayOfWeek::shortName).toList()));
    }
}
