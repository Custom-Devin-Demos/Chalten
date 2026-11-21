package org.chalten.hebrew;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.util.List;
import java.util.Map;
import org.chalten.localization.Translations;
import org.junit.jupiter.api.Test;

class HebrewMonthTest extends HebrewCalendarTest {

    private void assertMonth(HebrewMonth month, Integer positionInCommonYear, Integer positionInLeapYear,
            String name, String shortName, long deficientDays, long regularDays, long completeDays) {
        if (positionInCommonYear != null) {
            assertEquals(positionInCommonYear, month.positionInCommonYear());
        }
        if (positionInLeapYear != null) {
            assertEquals(positionInLeapYear, month.positionInLeapYear());
        }
        assertEquals(name, month.fullName());
        assertEquals(shortName, month.shortName());
        assertEquals(days(deficientDays), month.numberOfDaysInDeficientYear());
        assertEquals(days(regularDays), month.numberOfDaysInRegularYear());
        assertEquals(days(completeDays), month.numberOfDaysInCompleteYear());
    }

    @Test
    void testAdar() {
        assertMonth(adar, 6, null, "Adar", "Ad", 29, 29, 29);
    }

    @Test
    void testAdarI() {
        assertMonth(adarI, null, 6, "Adar I", "Ad I", 30, 30, 30);
    }

    @Test
    void testAdarII() {
        assertMonth(adarII, null, 7, "Adar II", "Ad II", 29, 29, 29);
    }

    @Test
    void testAv() {
        assertMonth(av, 11, 12, "Av", "Av", 30, 30, 30);
    }

    @Test
    void testCheshvan() {
        assertMonth(cheshvan, 2, 2, "Cheshvan", "Chesh", 29, 29, 30);
    }

    @Test
    void testComparing() {
        assertEquals(shevat, shevat);
        assertEquals(shevat, HebrewMonth.monthOnCommonYearAt(5));
        assertNotEquals(shevat, adar);
        assertNotEquals(shevat, adarI);
    }

    @Test
    void testElul() {
        assertMonth(elul, 12, 13, "Elul", "Elu", 29, 29, 29);
    }

    @Test
    void testInstanceCreation() {
        List<HebrewMonth> common = HebrewMonth.allOnCommonYear();
        for (int index = 1; index <= common.size(); index++) {
            assertEquals(index, common.get(index - 1).positionInCommonYear());
        }
        assertEquals(List.of(tishrei, cheshvan, kislev, tevet, shevat, adar, nisan, iyar, sivan, tammuz, av, elul),
                common);
        List<HebrewMonth> leap = HebrewMonth.allOnLeapYear();
        for (int index = 1; index <= leap.size(); index++) {
            assertEquals(index, leap.get(index - 1).positionInLeapYear());
        }
        assertEquals(List.of(tishrei, cheshvan, kislev, tevet, shevat, adarI, adarII, nisan, iyar, sivan, tammuz, av,
                elul), leap);
    }

    @Test
    void testIsGregorian() {
        assertFalse(sivan.isGregorian());
    }

    @Test
    void testIyar() {
        assertMonth(iyar, 8, 9, "Iyar", "Iya", 29, 29, 29);
    }

    @Test
    void testKislev() {
        assertMonth(kislev, 3, 3, "Kislev", "Kis", 29, 30, 30);
    }

    @Test
    void testNamesAreLocalized() {
        Map<String, String> spanish = Map.ofEntries(Map.entry("Tishrei", "Tishréi"), Map.entry("Cheshvan", "Jeshván"),
                Map.entry("Kislev", "Kislev"), Map.entry("Tevet", "Tevet"), Map.entry("Shevat", "Shevat"),
                Map.entry("Adar", "Adar"), Map.entry("Adar I", "Adar I"), Map.entry("Adar II", "Adar II"),
                Map.entry("Nisan", "Nisán"), Map.entry("Iyar", "Iyar"), Map.entry("Sivan", "Siván"),
                Map.entry("Tammuz", "Tamuz"), Map.entry("Av", "Av"), Map.entry("Elul", "Elul"));
        Translations.during(source -> spanish.getOrDefault(source, source), () -> {
            assertEquals(List.of("Tishréi", "Jeshván", "Kislev", "Tevet", "Shevat", "Adar", "Nisán", "Iyar", "Siván",
                    "Tamuz", "Av", "Elul"), HebrewMonth.allOnCommonYear().stream().map(HebrewMonth::fullName).toList());
            assertEquals(List.of("Tishréi", "Jeshván", "Kislev", "Tevet", "Shevat", "Adar I", "Adar II", "Nisán",
                    "Iyar", "Siván", "Tamuz", "Av", "Elul"),
                    HebrewMonth.allOnLeapYear().stream().map(HebrewMonth::fullName).toList());
            return null;
        });
    }

    @Test
    void testNisan() {
        assertMonth(nisan, 7, 8, "Nisan", "Nis", 30, 30, 30);
    }

    @Test
    void testPrintString() {
        assertEquals(List.of("Tishrei", "Cheshvan", "Kislev", "Tevet", "Shevat", "Adar", "Nisan", "Iyar", "Sivan",
                "Tammuz", "Av", "Elul"), HebrewMonth.allOnCommonYear().stream().map(HebrewMonth::toString).toList());
    }

    @Test
    void testShevat() {
        assertMonth(shevat, 5, 5, "Shevat", "Shev", 30, 30, 30);
    }

    @Test
    void testSivan() {
        assertMonth(sivan, 9, 10, "Sivan", "Siv", 30, 30, 30);
    }

    @Test
    void testTammuz() {
        assertMonth(tammuz, 10, 11, "Tammuz", "Tam", 29, 29, 29);
    }

    @Test
    void testTevet() {
        assertMonth(tevet, 4, 4, "Tevet", "Tev", 29, 29, 29);
    }

    @Test
    void testTishrei() {
        assertMonth(tishrei, 1, 1, "Tishrei", "Tish", 30, 30, 30);
    }
}
