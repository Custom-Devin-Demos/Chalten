package org.chalten.hebrew;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.chalten.core.CalendarDayOfMonth;
import org.chalten.measure.InstanceCreationFailedException;
import org.junit.jupiter.api.Test;

class HebrewDayOfMonthTest extends HebrewCalendarTest {

    /** Sends {@code first} to the {@code lastDayNumber}-th to {@code month}, then {@code , yearNumber}. */
    private static void assertPrintStrings(HebrewMonth month, long yearNumber, int lastDayNumber) {
        for (int dayNumber = 1; dayNumber <= lastDayNumber; dayNumber++) {
            assertEquals(month + " " + dayNumber + ", " + yearNumber + " AM",
                    date(month, dayNumber, yearNumber).toString());
        }
    }

    @Test
    void testComparing() {
        assertEquals(tishrei.first(), tishrei.first());
        assertNotEquals(tishrei.first(), tishrei.second());
        assertNotEquals(cheshvan.first(), tishrei.first());
    }

    @Test
    void testDateCreation() {
        HebrewDate date = tishrei.nineteenth().of(1447);
        assertEquals(tishrei, date.month());
        assertEquals(19, date.dayNumber());
        assertEquals(1447, date.yearNumber());
    }

    @Test
    void testInstanceCreation() {
        CalendarDayOfMonth<HebrewDate> dayOfMonth = HebrewCalendar.hebrewMonth(tishrei, 1);
        assertEquals(tishrei, dayOfMonth.month());
        assertEquals(1, dayOfMonth.dayNumber());
    }

    @Test
    void testInstanceCreationShortcut() {
        CalendarDayOfMonth<HebrewDate> dayOfMonth = cheshvan.first();
        assertEquals(cheshvan, dayOfMonth.month());
        assertEquals(1, dayOfMonth.dayNumber());
    }

    @Test
    void testInstanceCreationShortcutForAdar() {
        assertPrintStrings(adar, 5794, 29);
    }

    @Test
    void testInstanceCreationShortcutForAdarI() {
        assertPrintStrings(adarI, 5793, 30);
    }

    @Test
    void testInstanceCreationShortcutForAdarII() {
        assertPrintStrings(adarII, 5793, 29);
    }

    @Test
    void testInstanceCreationShortcutForAv() {
        assertPrintStrings(av, 5793, 30);
    }

    @Test
    void testInstanceCreationShortcutForCheshvan() {
        assertPrintStrings(cheshvan, 5793, 29);
        assertEquals("Cheshvan 30, 5759 AM", cheshvan.thirtieth().of(5759).toString());
        assertEquals("30 is an invalid day number in Cheshvan, 5793 AM",
                assertThrows(InstanceCreationFailedException.class, () -> cheshvan.thirtieth().of(5793))
                        .getMessage());
    }

    @Test
    void testInstanceCreationShortcutForElul() {
        assertPrintStrings(elul, 5793, 29);
    }

    @Test
    void testInstanceCreationShortcutForIyar() {
        assertPrintStrings(iyar, 5793, 29);
    }

    @Test
    void testInstanceCreationShortcutForKislev() {
        assertPrintStrings(kislev, 5793, 29);
        assertEquals("Kislev 30, 5759 AM", kislev.thirtieth().of(5759).toString());
        assertEquals("30 is an invalid day number in Kislev, 5793 AM",
                assertThrows(InstanceCreationFailedException.class, () -> kislev.thirtieth().of(5793))
                        .getMessage());
    }

    @Test
    void testInstanceCreationShortcutForNisan() {
        assertPrintStrings(nisan, 5793, 30);
    }

    @Test
    void testInstanceCreationShortcutForShevat() {
        assertPrintStrings(shevat, 5793, 30);
    }

    @Test
    void testInstanceCreationShortcutForSivan() {
        assertPrintStrings(sivan, 5793, 30);
    }

    @Test
    void testInstanceCreationShortcutForTammuz() {
        assertPrintStrings(tammuz, 5793, 29);
    }

    @Test
    void testInstanceCreationShortcutForTevet() {
        assertPrintStrings(tevet, 5793, 29);
    }

    @Test
    void testInstanceCreationShortcutForTishrei() {
        assertPrintStrings(tishrei, 5793, 30);
    }

    @Test
    void testPrintString() {
        assertEquals("Tishrei 1", tishrei.first().toString());
    }
}
