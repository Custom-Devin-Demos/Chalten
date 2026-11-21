package org.chalten.islamic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import org.chalten.localization.Translations;
import org.chalten.measure.TimeQuantity;
import org.junit.jupiter.api.Test;

class IslamicMonthTest extends IslamicCalendarTest {

    private void assertMonth(IslamicMonth month, int position, String name, String shortName, long leapDays,
            long commonDays) {
        assertEquals(position, month.positionInYear());
        assertEquals(name, month.fullName());
        assertEquals(shortName, month.shortName());
        assertEquals(days(leapDays), month.numberOfDaysInLeapYear());
        assertEquals(days(commonDays), month.numberOfDaysInCommonYear());
    }

    private List<IslamicMonth> inOrder() {
        return List.of(muharram, safar, rabiAlAwwal, rabiAlThani, jumadaAlAwwal, jumadaAlThani, rajab, shaban,
                ramadan, shawwal, duAlQiDah, duAlHijjah);
    }

    @Test
    void testComparing() {
        assertEquals(ramadan, ramadan);
        assertEquals(ramadan, IslamicMonth.monthAt(9));
        assertNotEquals(ramadan, safar);
        assertNotEquals(muharram, january);
    }

    @Test
    void testDuAlHijjah() {
        assertMonth(duAlHijjah, 12, "Dhu al-Hijjah", "Dhu'l-H.", 30, 29);
    }

    @Test
    void testDuAlQiDah() {
        assertMonth(duAlQiDah, 11, "Dhu al-Qi'dah", "Dhu'l-Q.", 30, 30);
    }

    @Test
    void testInstanceCreation() {
        for (int index = 1; index <= 12; index++) {
            assertEquals(index, IslamicMonth.all().get(index - 1).positionInYear());
        }
        assertEquals(inOrder(), IslamicMonth.all());
    }

    @Test
    void testJumadaAlAwwal() {
        assertMonth(jumadaAlAwwal, 5, "Jumada al-Awwal", "Jum. I", 30, 30);
    }

    @Test
    void testJumadaAlThani() {
        assertMonth(jumadaAlThani, 6, "Jumada al-Thani", "Jum. II", 29, 29);
    }

    @Test
    void testMuharram() {
        assertMonth(muharram, 1, "Muharram", "Muh.", 30, 30);
    }

    @Test
    void testNamesAreLocalized() {
        Map<String, String> spanish = Map.ofEntries(Map.entry("Muharram", "Muharram"), Map.entry("Safar", "Safar"),
                Map.entry("Rabi' al-Awwal", "Rabi' al-Awwal"), Map.entry("Rabi' al-Thani", "Rabi' al-Thani"),
                Map.entry("Jumada al-Awwal", "Yumada al-Awwal"), Map.entry("Jumada al-Thani", "Yumada al-Thani"),
                Map.entry("Rajab", "Rayab"), Map.entry("Sha'ban", "Sha'ban"), Map.entry("Ramadan", "Ramadán"),
                Map.entry("Shawwal", "Shawwal"), Map.entry("Dhu al-Qi'dah", "Dhu ul-Qádah"),
                Map.entry("Dhu al-Hijjah", "Dhu ul-Híyyah"));
        assertEquals(List.of("Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Yumada al-Awwal",
                "Yumada al-Thani", "Rayab", "Sha'ban", "Ramadán", "Shawwal", "Dhu ul-Qádah", "Dhu ul-Híyyah"),
                Translations.during(source -> spanish.getOrDefault(source, source),
                        () -> IslamicMonth.all().stream().map(IslamicMonth::fullName).toList()));
    }

    @Test
    void testNext() {
        assertEquals(muharram, muharram.next(islamicYears(1)));
        for (int count = 1; count <= 12; count++) {
            assertEquals(inOrder().get(count % 12), muharram.next(islamicMonths(count)));
        }
        assertThrows(ArithmeticException.class, () -> muharram.next(TimeQuantity.of(1, year)));
    }

    @Test
    void testPrevious() {
        assertEquals(muharram, muharram.previous(islamicYears(1)));
        for (int count = 1; count <= 12; count++) {
            assertEquals(inOrder().get((12 - count) % 12), muharram.previous(islamicMonths(count)));
        }
        assertThrows(ArithmeticException.class, () -> muharram.previous(TimeQuantity.of(1, year)));
    }

    @Test
    void testPrintString() {
        assertEquals(List.of("Muharram", "Safar", "Rabi' al-Awwal", "Rabi' al-Thani", "Jumada al-Awwal",
                "Jumada al-Thani", "Rajab", "Sha'ban", "Ramadan", "Shawwal", "Dhu al-Qi'dah", "Dhu al-Hijjah"),
                IslamicMonth.all().stream().map(IslamicMonth::toString).toList());
    }

    @Test
    void testRabiAlAwwal() {
        assertMonth(rabiAlAwwal, 3, "Rabi' al-Awwal", "Rab. I", 30, 30);
    }

    @Test
    void testRabiAlThani() {
        assertMonth(rabiAlThani, 4, "Rabi' al-Thani", "Rab. II", 29, 29);
    }

    @Test
    void testRajab() {
        assertMonth(rajab, 7, "Rajab", "Raj.", 30, 30);
    }

    @Test
    void testRamadan() {
        assertMonth(ramadan, 9, "Ramadan", "Ram.", 30, 30);
    }

    @Test
    void testSafar() {
        assertMonth(safar, 2, "Safar", "Saf.", 29, 29);
    }

    @Test
    void testShaban() {
        assertMonth(shaban, 8, "Sha'ban", "Sha.", 29, 29);
    }

    @Test
    void testShawwal() {
        assertMonth(shawwal, 10, "Shawwal", "Shaw.", 29, 29);
    }
}
