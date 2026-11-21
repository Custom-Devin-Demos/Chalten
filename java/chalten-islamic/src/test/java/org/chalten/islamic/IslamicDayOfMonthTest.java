package org.chalten.islamic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.chalten.core.CalendarDayOfMonth;
import org.chalten.core.GregorianMonth;
import org.chalten.measure.ChaltenAssertionException;
import org.chalten.measure.InstanceCreationFailedException;
import org.junit.jupiter.api.Test;

class IslamicDayOfMonthTest extends IslamicCalendarTest {

    /** Sends {@code first} to the {@code lastDayNumber}-th to {@code month}, then {@code , yearNumber}. */
    private static void assertPrintStrings(IslamicMonth month, long yearNumber, int lastDayNumber) {
        for (int dayNumber = 1; dayNumber <= lastDayNumber; dayNumber++) {
            assertEquals(month + " " + dayNumber + ", " + yearNumber + " AH",
                    date(month, dayNumber, yearNumber).toString());
        }
    }

    @Test
    void testCannotCreateWithMonthOfAnotherCalendar() {
        assertEquals("Cannot use non-gregorian months for days of gregorian months",
                assertThrows(ChaltenAssertionException.class,
                        () -> CalendarDayOfMonth.gregorianMonth(muharram, 1)).getMessage());
        assertEquals("Cannot use non-islamic months for days of islamic months",
                assertThrows(ChaltenAssertionException.class,
                        () -> IslamicCalendar.islamicMonth(GregorianMonth.APRIL, 1)).getMessage());
    }

    @Test
    void testComparing() {
        assertEquals(ramadan.first(), ramadan.first());
        assertNotEquals(ramadan.first(), ramadan.second());
        assertNotEquals(shaban.first(), ramadan.first());
    }

    @Test
    void testDateCreation() {
        IslamicDate date = safar.nineteenth().of(1447);
        assertEquals(safar, date.month());
        assertEquals(19, date.dayNumber());
        assertEquals(1447, date.yearNumber());
    }

    @Test
    void testInstanceCreation() {
        CalendarDayOfMonth<IslamicDate> dayOfMonth = IslamicCalendar.islamicMonth(ramadan, 1);
        assertEquals(ramadan, dayOfMonth.month());
        assertEquals(1, dayOfMonth.dayNumber());
    }

    @Test
    void testInstanceCreationShortcut() {
        CalendarDayOfMonth<IslamicDate> dayOfMonth = safar.first();
        assertEquals(safar, dayOfMonth.month());
        assertEquals(1, dayOfMonth.dayNumber());
    }

    @Test
    void testInstanceCreationShortcutForDuAlQiDah() {
        assertPrintStrings(duAlQiDah, 1417, 30);
    }

    @Test
    void testInstanceCreationShortcutForDuAlQiHijjah() {
        assertPrintStrings(duAlHijjah, 1417, 29);
        assertEquals("Dhu al-Hijjah 30, 1442 AH", duAlHijjah.thirtieth().of(1442).toString());
        assertEquals("30 is an invalid day number in Dhu al-Hijjah, 1443 AH",
                assertThrows(InstanceCreationFailedException.class, () -> duAlHijjah.thirtieth().of(1443))
                        .getMessage());
    }

    @Test
    void testInstanceCreationShortcutForJumadaI() {
        assertPrintStrings(jumadaAlAwwal, 1417, 30);
    }

    @Test
    void testInstanceCreationShortcutForJumadaII() {
        assertPrintStrings(jumadaAlThani, 1417, 29);
    }

    @Test
    void testInstanceCreationShortcutForMuharram() {
        assertPrintStrings(muharram, 1417, 30);
    }

    @Test
    void testInstanceCreationShortcutForRabiI() {
        assertPrintStrings(rabiAlAwwal, 1417, 30);
    }

    @Test
    void testInstanceCreationShortcutForRabiII() {
        assertPrintStrings(rabiAlThani, 1417, 29);
    }

    @Test
    void testInstanceCreationShortcutForRajab() {
        assertPrintStrings(rajab, 1417, 30);
    }

    @Test
    void testInstanceCreationShortcutForRamadan() {
        assertPrintStrings(ramadan, 1417, 30);
    }

    @Test
    void testInstanceCreationShortcutForSafar() {
        assertPrintStrings(safar, 1417, 29);
    }

    @Test
    void testInstanceCreationShortcutForShaban() {
        assertPrintStrings(shaban, 1417, 29);
    }

    @Test
    void testInstanceCreationShortcutForShawwal() {
        assertPrintStrings(shawwal, 1417, 29);
    }

    @Test
    void testPrintString() {
        assertEquals("Safar 1", safar.first().toString());
    }
}
