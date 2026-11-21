package org.chalten.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.testkit.ChaltenTest;
import org.junit.jupiter.api.Test;

class GregorianDayOfMonthTest extends ChaltenTest {

    @Test
    void testComparing() {
        assertEquals(GregorianMonth.APRIL.first(), GregorianMonth.APRIL.first());
        assertNotEquals(GregorianMonth.APRIL.second(), GregorianMonth.APRIL.first());
        assertNotEquals(GregorianMonth.MARCH.first(), GregorianMonth.APRIL.first());
    }

    @Test
    void testDateCreation() {
        GregorianDate date = GregorianMonth.APRIL.first().of(2025);
        assertEquals(GregorianMonth.APRIL, date.month());
        assertEquals(1, date.dayNumber());
        assertEquals(2025, date.yearNumber());
    }

    @Test
    void testInstanceCreation() {
        CalendarDayOfMonth<GregorianDate> dayOfMonth = CalendarDayOfMonth.gregorianMonth(GregorianMonth.APRIL, 1);
        assertEquals(GregorianMonth.APRIL, dayOfMonth.month());
        assertEquals(1, dayOfMonth.dayNumber());
    }

    @Test
    void testInstanceCreationShortcut() {
        CalendarDayOfMonth<GregorianDate> dayOfMonth = GregorianMonth.APRIL.first();
        assertEquals(GregorianMonth.APRIL, dayOfMonth.month());
        assertEquals(1, dayOfMonth.dayNumber());
    }

    @Test
    void testInstanceCreationShortcutForApril() {
        assertEquals("April 1, 2025", april.first().of(2025).toString());
        assertEquals("April 2, 2025", april.second().of(2025).toString());
        assertEquals("April 3, 2025", april.third().of(2025).toString());
        assertEquals("April 4, 2025", april.fourth().of(2025).toString());
        assertEquals("April 5, 2025", april.fifth().of(2025).toString());
        assertEquals("April 6, 2025", april.sixth().of(2025).toString());
        assertEquals("April 7, 2025", april.seventh().of(2025).toString());
        assertEquals("April 8, 2025", april.eighth().of(2025).toString());
        assertEquals("April 9, 2025", april.ninth().of(2025).toString());
        assertEquals("April 10, 2025", april.tenth().of(2025).toString());
        assertEquals("April 11, 2025", april.eleventh().of(2025).toString());
        assertEquals("April 12, 2025", april.twelfth().of(2025).toString());
        assertEquals("April 13, 2025", april.thirteenth().of(2025).toString());
        assertEquals("April 14, 2025", april.fourteenth().of(2025).toString());
        assertEquals("April 15, 2025", april.fifteenth().of(2025).toString());
        assertEquals("April 16, 2025", april.sixteenth().of(2025).toString());
        assertEquals("April 17, 2025", april.seventeenth().of(2025).toString());
        assertEquals("April 18, 2025", april.eighteenth().of(2025).toString());
        assertEquals("April 19, 2025", april.nineteenth().of(2025).toString());
        assertEquals("April 20, 2025", april.twentieth().of(2025).toString());
        assertEquals("April 21, 2025", april.twentyFirst().of(2025).toString());
        assertEquals("April 22, 2025", april.twentySecond().of(2025).toString());
        assertEquals("April 23, 2025", april.twentyThird().of(2025).toString());
        assertEquals("April 24, 2025", april.twentyFourth().of(2025).toString());
        assertEquals("April 25, 2025", april.twentyFifth().of(2025).toString());
        assertEquals("April 26, 2025", april.twentySixth().of(2025).toString());
        assertEquals("April 27, 2025", april.twentySeventh().of(2025).toString());
        assertEquals("April 28, 2025", april.twentyEighth().of(2025).toString());
        assertEquals("April 29, 2025", april.twentyNinth().of(2025).toString());
        assertEquals("April 30, 2025", april.thirtieth().of(2025).toString());
        assertThrows(UnsupportedOperationException.class, () -> april.thirtyFirst());
    }

    @Test
    void testInstanceCreationShortcutForAugust() {
        assertEquals("August 1, 2025", august.first().of(2025).toString());
        assertEquals("August 2, 2025", august.second().of(2025).toString());
        assertEquals("August 3, 2025", august.third().of(2025).toString());
        assertEquals("August 4, 2025", august.fourth().of(2025).toString());
        assertEquals("August 5, 2025", august.fifth().of(2025).toString());
        assertEquals("August 6, 2025", august.sixth().of(2025).toString());
        assertEquals("August 7, 2025", august.seventh().of(2025).toString());
        assertEquals("August 8, 2025", august.eighth().of(2025).toString());
        assertEquals("August 9, 2025", august.ninth().of(2025).toString());
        assertEquals("August 10, 2025", august.tenth().of(2025).toString());
        assertEquals("August 11, 2025", august.eleventh().of(2025).toString());
        assertEquals("August 12, 2025", august.twelfth().of(2025).toString());
        assertEquals("August 13, 2025", august.thirteenth().of(2025).toString());
        assertEquals("August 14, 2025", august.fourteenth().of(2025).toString());
        assertEquals("August 15, 2025", august.fifteenth().of(2025).toString());
        assertEquals("August 16, 2025", august.sixteenth().of(2025).toString());
        assertEquals("August 17, 2025", august.seventeenth().of(2025).toString());
        assertEquals("August 18, 2025", august.eighteenth().of(2025).toString());
        assertEquals("August 19, 2025", august.nineteenth().of(2025).toString());
        assertEquals("August 20, 2025", august.twentieth().of(2025).toString());
        assertEquals("August 21, 2025", august.twentyFirst().of(2025).toString());
        assertEquals("August 22, 2025", august.twentySecond().of(2025).toString());
        assertEquals("August 23, 2025", august.twentyThird().of(2025).toString());
        assertEquals("August 24, 2025", august.twentyFourth().of(2025).toString());
        assertEquals("August 25, 2025", august.twentyFifth().of(2025).toString());
        assertEquals("August 26, 2025", august.twentySixth().of(2025).toString());
        assertEquals("August 27, 2025", august.twentySeventh().of(2025).toString());
        assertEquals("August 28, 2025", august.twentyEighth().of(2025).toString());
        assertEquals("August 29, 2025", august.twentyNinth().of(2025).toString());
        assertEquals("August 30, 2025", august.thirtieth().of(2025).toString());
        assertEquals("August 31, 2025", august.thirtyFirst().of(2025).toString());
    }

    @Test
    void testInstanceCreationShortcutForDecember() {
        assertEquals("December 1, 2025", december.first().of(2025).toString());
        assertEquals("December 2, 2025", december.second().of(2025).toString());
        assertEquals("December 3, 2025", december.third().of(2025).toString());
        assertEquals("December 4, 2025", december.fourth().of(2025).toString());
        assertEquals("December 5, 2025", december.fifth().of(2025).toString());
        assertEquals("December 6, 2025", december.sixth().of(2025).toString());
        assertEquals("December 7, 2025", december.seventh().of(2025).toString());
        assertEquals("December 8, 2025", december.eighth().of(2025).toString());
        assertEquals("December 9, 2025", december.ninth().of(2025).toString());
        assertEquals("December 10, 2025", december.tenth().of(2025).toString());
        assertEquals("December 11, 2025", december.eleventh().of(2025).toString());
        assertEquals("December 12, 2025", december.twelfth().of(2025).toString());
        assertEquals("December 13, 2025", december.thirteenth().of(2025).toString());
        assertEquals("December 14, 2025", december.fourteenth().of(2025).toString());
        assertEquals("December 15, 2025", december.fifteenth().of(2025).toString());
        assertEquals("December 16, 2025", december.sixteenth().of(2025).toString());
        assertEquals("December 17, 2025", december.seventeenth().of(2025).toString());
        assertEquals("December 18, 2025", december.eighteenth().of(2025).toString());
        assertEquals("December 19, 2025", december.nineteenth().of(2025).toString());
        assertEquals("December 20, 2025", december.twentieth().of(2025).toString());
        assertEquals("December 21, 2025", december.twentyFirst().of(2025).toString());
        assertEquals("December 22, 2025", december.twentySecond().of(2025).toString());
        assertEquals("December 23, 2025", december.twentyThird().of(2025).toString());
        assertEquals("December 24, 2025", december.twentyFourth().of(2025).toString());
        assertEquals("December 25, 2025", december.twentyFifth().of(2025).toString());
        assertEquals("December 26, 2025", december.twentySixth().of(2025).toString());
        assertEquals("December 27, 2025", december.twentySeventh().of(2025).toString());
        assertEquals("December 28, 2025", december.twentyEighth().of(2025).toString());
        assertEquals("December 29, 2025", december.twentyNinth().of(2025).toString());
        assertEquals("December 30, 2025", december.thirtieth().of(2025).toString());
        assertEquals("December 31, 2025", december.thirtyFirst().of(2025).toString());
    }

    @Test
    void testInstanceCreationShortcutForFebruary() {
        assertEquals("February 1, 2025", february.first().of(2025).toString());
        assertEquals("February 2, 2025", february.second().of(2025).toString());
        assertEquals("February 3, 2025", february.third().of(2025).toString());
        assertEquals("February 4, 2025", february.fourth().of(2025).toString());
        assertEquals("February 5, 2025", february.fifth().of(2025).toString());
        assertEquals("February 6, 2025", february.sixth().of(2025).toString());
        assertEquals("February 7, 2025", february.seventh().of(2025).toString());
        assertEquals("February 8, 2025", february.eighth().of(2025).toString());
        assertEquals("February 9, 2025", february.ninth().of(2025).toString());
        assertEquals("February 10, 2025", february.tenth().of(2025).toString());
        assertEquals("February 11, 2025", february.eleventh().of(2025).toString());
        assertEquals("February 12, 2025", february.twelfth().of(2025).toString());
        assertEquals("February 13, 2025", february.thirteenth().of(2025).toString());
        assertEquals("February 14, 2025", february.fourteenth().of(2025).toString());
        assertEquals("February 15, 2025", february.fifteenth().of(2025).toString());
        assertEquals("February 16, 2025", february.sixteenth().of(2025).toString());
        assertEquals("February 17, 2025", february.seventeenth().of(2025).toString());
        assertEquals("February 18, 2025", february.eighteenth().of(2025).toString());
        assertEquals("February 19, 2025", february.nineteenth().of(2025).toString());
        assertEquals("February 20, 2025", february.twentieth().of(2025).toString());
        assertEquals("February 21, 2025", february.twentyFirst().of(2025).toString());
        assertEquals("February 22, 2025", february.twentySecond().of(2025).toString());
        assertEquals("February 23, 2025", february.twentyThird().of(2025).toString());
        assertEquals("February 24, 2025", february.twentyFourth().of(2025).toString());
        assertEquals("February 25, 2025", february.twentyFifth().of(2025).toString());
        assertEquals("February 26, 2025", february.twentySixth().of(2025).toString());
        assertEquals("February 27, 2025", february.twentySeventh().of(2025).toString());
        assertEquals("February 28, 2025", february.twentyEighth().of(2025).toString());
        assertEquals("February 29, 2000", february.twentyNinth().of(2000).toString());
        InstanceCreationFailedException exception = assertThrows(InstanceCreationFailedException.class,
                () -> february.twentyNinth().of(2025));
        assertEquals("29 is an invalid day number in February, 2025", exception.getMessage());
    }

    @Test
    void testInstanceCreationShortcutForJanuary() {
        assertEquals("January 1, 2025", january.first().of(2025).toString());
        assertEquals("January 2, 2025", january.second().of(2025).toString());
        assertEquals("January 3, 2025", january.third().of(2025).toString());
        assertEquals("January 4, 2025", january.fourth().of(2025).toString());
        assertEquals("January 5, 2025", january.fifth().of(2025).toString());
        assertEquals("January 6, 2025", january.sixth().of(2025).toString());
        assertEquals("January 7, 2025", january.seventh().of(2025).toString());
        assertEquals("January 8, 2025", january.eighth().of(2025).toString());
        assertEquals("January 9, 2025", january.ninth().of(2025).toString());
        assertEquals("January 10, 2025", january.tenth().of(2025).toString());
        assertEquals("January 11, 2025", january.eleventh().of(2025).toString());
        assertEquals("January 12, 2025", january.twelfth().of(2025).toString());
        assertEquals("January 13, 2025", january.thirteenth().of(2025).toString());
        assertEquals("January 14, 2025", january.fourteenth().of(2025).toString());
        assertEquals("January 15, 2025", january.fifteenth().of(2025).toString());
        assertEquals("January 16, 2025", january.sixteenth().of(2025).toString());
        assertEquals("January 17, 2025", january.seventeenth().of(2025).toString());
        assertEquals("January 18, 2025", january.eighteenth().of(2025).toString());
        assertEquals("January 19, 2025", january.nineteenth().of(2025).toString());
        assertEquals("January 20, 2025", january.twentieth().of(2025).toString());
        assertEquals("January 21, 2025", january.twentyFirst().of(2025).toString());
        assertEquals("January 22, 2025", january.twentySecond().of(2025).toString());
        assertEquals("January 23, 2025", january.twentyThird().of(2025).toString());
        assertEquals("January 24, 2025", january.twentyFourth().of(2025).toString());
        assertEquals("January 25, 2025", january.twentyFifth().of(2025).toString());
        assertEquals("January 26, 2025", january.twentySixth().of(2025).toString());
        assertEquals("January 27, 2025", january.twentySeventh().of(2025).toString());
        assertEquals("January 28, 2025", january.twentyEighth().of(2025).toString());
        assertEquals("January 29, 2025", january.twentyNinth().of(2025).toString());
        assertEquals("January 30, 2025", january.thirtieth().of(2025).toString());
        assertEquals("January 31, 2025", january.thirtyFirst().of(2025).toString());
    }

    @Test
    void testInstanceCreationShortcutForJuly() {
        assertEquals("July 1, 2025", july.first().of(2025).toString());
        assertEquals("July 2, 2025", july.second().of(2025).toString());
        assertEquals("July 3, 2025", july.third().of(2025).toString());
        assertEquals("July 4, 2025", july.fourth().of(2025).toString());
        assertEquals("July 5, 2025", july.fifth().of(2025).toString());
        assertEquals("July 6, 2025", july.sixth().of(2025).toString());
        assertEquals("July 7, 2025", july.seventh().of(2025).toString());
        assertEquals("July 8, 2025", july.eighth().of(2025).toString());
        assertEquals("July 9, 2025", july.ninth().of(2025).toString());
        assertEquals("July 10, 2025", july.tenth().of(2025).toString());
        assertEquals("July 11, 2025", july.eleventh().of(2025).toString());
        assertEquals("July 12, 2025", july.twelfth().of(2025).toString());
        assertEquals("July 13, 2025", july.thirteenth().of(2025).toString());
        assertEquals("July 14, 2025", july.fourteenth().of(2025).toString());
        assertEquals("July 15, 2025", july.fifteenth().of(2025).toString());
        assertEquals("July 16, 2025", july.sixteenth().of(2025).toString());
        assertEquals("July 17, 2025", july.seventeenth().of(2025).toString());
        assertEquals("July 18, 2025", july.eighteenth().of(2025).toString());
        assertEquals("July 19, 2025", july.nineteenth().of(2025).toString());
        assertEquals("July 20, 2025", july.twentieth().of(2025).toString());
        assertEquals("July 21, 2025", july.twentyFirst().of(2025).toString());
        assertEquals("July 22, 2025", july.twentySecond().of(2025).toString());
        assertEquals("July 23, 2025", july.twentyThird().of(2025).toString());
        assertEquals("July 24, 2025", july.twentyFourth().of(2025).toString());
        assertEquals("July 25, 2025", july.twentyFifth().of(2025).toString());
        assertEquals("July 26, 2025", july.twentySixth().of(2025).toString());
        assertEquals("July 27, 2025", july.twentySeventh().of(2025).toString());
        assertEquals("July 28, 2025", july.twentyEighth().of(2025).toString());
        assertEquals("July 29, 2025", july.twentyNinth().of(2025).toString());
        assertEquals("July 30, 2025", july.thirtieth().of(2025).toString());
        assertEquals("July 31, 2025", july.thirtyFirst().of(2025).toString());
    }

    @Test
    void testInstanceCreationShortcutForJune() {
        assertEquals("June 1, 2025", june.first().of(2025).toString());
        assertEquals("June 2, 2025", june.second().of(2025).toString());
        assertEquals("June 3, 2025", june.third().of(2025).toString());
        assertEquals("June 4, 2025", june.fourth().of(2025).toString());
        assertEquals("June 5, 2025", june.fifth().of(2025).toString());
        assertEquals("June 6, 2025", june.sixth().of(2025).toString());
        assertEquals("June 7, 2025", june.seventh().of(2025).toString());
        assertEquals("June 8, 2025", june.eighth().of(2025).toString());
        assertEquals("June 9, 2025", june.ninth().of(2025).toString());
        assertEquals("June 10, 2025", june.tenth().of(2025).toString());
        assertEquals("June 11, 2025", june.eleventh().of(2025).toString());
        assertEquals("June 12, 2025", june.twelfth().of(2025).toString());
        assertEquals("June 13, 2025", june.thirteenth().of(2025).toString());
        assertEquals("June 14, 2025", june.fourteenth().of(2025).toString());
        assertEquals("June 15, 2025", june.fifteenth().of(2025).toString());
        assertEquals("June 16, 2025", june.sixteenth().of(2025).toString());
        assertEquals("June 17, 2025", june.seventeenth().of(2025).toString());
        assertEquals("June 18, 2025", june.eighteenth().of(2025).toString());
        assertEquals("June 19, 2025", june.nineteenth().of(2025).toString());
        assertEquals("June 20, 2025", june.twentieth().of(2025).toString());
        assertEquals("June 21, 2025", june.twentyFirst().of(2025).toString());
        assertEquals("June 22, 2025", june.twentySecond().of(2025).toString());
        assertEquals("June 23, 2025", june.twentyThird().of(2025).toString());
        assertEquals("June 24, 2025", june.twentyFourth().of(2025).toString());
        assertEquals("June 25, 2025", june.twentyFifth().of(2025).toString());
        assertEquals("June 26, 2025", june.twentySixth().of(2025).toString());
        assertEquals("June 27, 2025", june.twentySeventh().of(2025).toString());
        assertEquals("June 28, 2025", june.twentyEighth().of(2025).toString());
        assertEquals("June 29, 2025", june.twentyNinth().of(2025).toString());
        assertEquals("June 30, 2025", june.thirtieth().of(2025).toString());
        assertThrows(UnsupportedOperationException.class, () -> june.thirtyFirst());
    }

    @Test
    void testInstanceCreationShortcutForMarch() {
        assertEquals("March 1, 2025", march.first().of(2025).toString());
        assertEquals("March 2, 2025", march.second().of(2025).toString());
        assertEquals("March 3, 2025", march.third().of(2025).toString());
        assertEquals("March 4, 2025", march.fourth().of(2025).toString());
        assertEquals("March 5, 2025", march.fifth().of(2025).toString());
        assertEquals("March 6, 2025", march.sixth().of(2025).toString());
        assertEquals("March 7, 2025", march.seventh().of(2025).toString());
        assertEquals("March 8, 2025", march.eighth().of(2025).toString());
        assertEquals("March 9, 2025", march.ninth().of(2025).toString());
        assertEquals("March 10, 2025", march.tenth().of(2025).toString());
        assertEquals("March 11, 2025", march.eleventh().of(2025).toString());
        assertEquals("March 12, 2025", march.twelfth().of(2025).toString());
        assertEquals("March 13, 2025", march.thirteenth().of(2025).toString());
        assertEquals("March 14, 2025", march.fourteenth().of(2025).toString());
        assertEquals("March 15, 2025", march.fifteenth().of(2025).toString());
        assertEquals("March 16, 2025", march.sixteenth().of(2025).toString());
        assertEquals("March 17, 2025", march.seventeenth().of(2025).toString());
        assertEquals("March 18, 2025", march.eighteenth().of(2025).toString());
        assertEquals("March 19, 2025", march.nineteenth().of(2025).toString());
        assertEquals("March 20, 2025", march.twentieth().of(2025).toString());
        assertEquals("March 21, 2025", march.twentyFirst().of(2025).toString());
        assertEquals("March 22, 2025", march.twentySecond().of(2025).toString());
        assertEquals("March 23, 2025", march.twentyThird().of(2025).toString());
        assertEquals("March 24, 2025", march.twentyFourth().of(2025).toString());
        assertEquals("March 25, 2025", march.twentyFifth().of(2025).toString());
        assertEquals("March 26, 2025", march.twentySixth().of(2025).toString());
        assertEquals("March 27, 2025", march.twentySeventh().of(2025).toString());
        assertEquals("March 28, 2025", march.twentyEighth().of(2025).toString());
        assertEquals("March 29, 2025", march.twentyNinth().of(2025).toString());
        assertEquals("March 30, 2025", march.thirtieth().of(2025).toString());
        assertEquals("March 31, 2025", march.thirtyFirst().of(2025).toString());
    }

    @Test
    void testInstanceCreationShortcutForMay() {
        assertEquals("May 1, 2025", may.first().of(2025).toString());
        assertEquals("May 2, 2025", may.second().of(2025).toString());
        assertEquals("May 3, 2025", may.third().of(2025).toString());
        assertEquals("May 4, 2025", may.fourth().of(2025).toString());
        assertEquals("May 5, 2025", may.fifth().of(2025).toString());
        assertEquals("May 6, 2025", may.sixth().of(2025).toString());
        assertEquals("May 7, 2025", may.seventh().of(2025).toString());
        assertEquals("May 8, 2025", may.eighth().of(2025).toString());
        assertEquals("May 9, 2025", may.ninth().of(2025).toString());
        assertEquals("May 10, 2025", may.tenth().of(2025).toString());
        assertEquals("May 11, 2025", may.eleventh().of(2025).toString());
        assertEquals("May 12, 2025", may.twelfth().of(2025).toString());
        assertEquals("May 13, 2025", may.thirteenth().of(2025).toString());
        assertEquals("May 14, 2025", may.fourteenth().of(2025).toString());
        assertEquals("May 15, 2025", may.fifteenth().of(2025).toString());
        assertEquals("May 16, 2025", may.sixteenth().of(2025).toString());
        assertEquals("May 17, 2025", may.seventeenth().of(2025).toString());
        assertEquals("May 18, 2025", may.eighteenth().of(2025).toString());
        assertEquals("May 19, 2025", may.nineteenth().of(2025).toString());
        assertEquals("May 20, 2025", may.twentieth().of(2025).toString());
        assertEquals("May 21, 2025", may.twentyFirst().of(2025).toString());
        assertEquals("May 22, 2025", may.twentySecond().of(2025).toString());
        assertEquals("May 23, 2025", may.twentyThird().of(2025).toString());
        assertEquals("May 24, 2025", may.twentyFourth().of(2025).toString());
        assertEquals("May 25, 2025", may.twentyFifth().of(2025).toString());
        assertEquals("May 26, 2025", may.twentySixth().of(2025).toString());
        assertEquals("May 27, 2025", may.twentySeventh().of(2025).toString());
        assertEquals("May 28, 2025", may.twentyEighth().of(2025).toString());
        assertEquals("May 29, 2025", may.twentyNinth().of(2025).toString());
        assertEquals("May 30, 2025", may.thirtieth().of(2025).toString());
        assertEquals("May 31, 2025", may.thirtyFirst().of(2025).toString());
    }

    @Test
    void testInstanceCreationShortcutForNovember() {
        assertEquals("November 1, 2025", november.first().of(2025).toString());
        assertEquals("November 2, 2025", november.second().of(2025).toString());
        assertEquals("November 3, 2025", november.third().of(2025).toString());
        assertEquals("November 4, 2025", november.fourth().of(2025).toString());
        assertEquals("November 5, 2025", november.fifth().of(2025).toString());
        assertEquals("November 6, 2025", november.sixth().of(2025).toString());
        assertEquals("November 7, 2025", november.seventh().of(2025).toString());
        assertEquals("November 8, 2025", november.eighth().of(2025).toString());
        assertEquals("November 9, 2025", november.ninth().of(2025).toString());
        assertEquals("November 10, 2025", november.tenth().of(2025).toString());
        assertEquals("November 11, 2025", november.eleventh().of(2025).toString());
        assertEquals("November 12, 2025", november.twelfth().of(2025).toString());
        assertEquals("November 13, 2025", november.thirteenth().of(2025).toString());
        assertEquals("November 14, 2025", november.fourteenth().of(2025).toString());
        assertEquals("November 15, 2025", november.fifteenth().of(2025).toString());
        assertEquals("November 16, 2025", november.sixteenth().of(2025).toString());
        assertEquals("November 17, 2025", november.seventeenth().of(2025).toString());
        assertEquals("November 18, 2025", november.eighteenth().of(2025).toString());
        assertEquals("November 19, 2025", november.nineteenth().of(2025).toString());
        assertEquals("November 20, 2025", november.twentieth().of(2025).toString());
        assertEquals("November 21, 2025", november.twentyFirst().of(2025).toString());
        assertEquals("November 22, 2025", november.twentySecond().of(2025).toString());
        assertEquals("November 23, 2025", november.twentyThird().of(2025).toString());
        assertEquals("November 24, 2025", november.twentyFourth().of(2025).toString());
        assertEquals("November 25, 2025", november.twentyFifth().of(2025).toString());
        assertEquals("November 26, 2025", november.twentySixth().of(2025).toString());
        assertEquals("November 27, 2025", november.twentySeventh().of(2025).toString());
        assertEquals("November 28, 2025", november.twentyEighth().of(2025).toString());
        assertEquals("November 29, 2025", november.twentyNinth().of(2025).toString());
        assertEquals("November 30, 2025", november.thirtieth().of(2025).toString());
        assertThrows(UnsupportedOperationException.class, () -> november.thirtyFirst());
    }

    @Test
    void testInstanceCreationShortcutForOctober() {
        assertEquals("October 1, 2025", october.first().of(2025).toString());
        assertEquals("October 2, 2025", october.second().of(2025).toString());
        assertEquals("October 3, 2025", october.third().of(2025).toString());
        assertEquals("October 4, 2025", october.fourth().of(2025).toString());
        assertEquals("October 5, 2025", october.fifth().of(2025).toString());
        assertEquals("October 6, 2025", october.sixth().of(2025).toString());
        assertEquals("October 7, 2025", october.seventh().of(2025).toString());
        assertEquals("October 8, 2025", october.eighth().of(2025).toString());
        assertEquals("October 9, 2025", october.ninth().of(2025).toString());
        assertEquals("October 10, 2025", october.tenth().of(2025).toString());
        assertEquals("October 11, 2025", october.eleventh().of(2025).toString());
        assertEquals("October 12, 2025", october.twelfth().of(2025).toString());
        assertEquals("October 13, 2025", october.thirteenth().of(2025).toString());
        assertEquals("October 14, 2025", october.fourteenth().of(2025).toString());
        assertEquals("October 15, 2025", october.fifteenth().of(2025).toString());
        assertEquals("October 16, 2025", october.sixteenth().of(2025).toString());
        assertEquals("October 17, 2025", october.seventeenth().of(2025).toString());
        assertEquals("October 18, 2025", october.eighteenth().of(2025).toString());
        assertEquals("October 19, 2025", october.nineteenth().of(2025).toString());
        assertEquals("October 20, 2025", october.twentieth().of(2025).toString());
        assertEquals("October 21, 2025", october.twentyFirst().of(2025).toString());
        assertEquals("October 22, 2025", october.twentySecond().of(2025).toString());
        assertEquals("October 23, 2025", october.twentyThird().of(2025).toString());
        assertEquals("October 24, 2025", october.twentyFourth().of(2025).toString());
        assertEquals("October 25, 2025", october.twentyFifth().of(2025).toString());
        assertEquals("October 26, 2025", october.twentySixth().of(2025).toString());
        assertEquals("October 27, 2025", october.twentySeventh().of(2025).toString());
        assertEquals("October 28, 2025", october.twentyEighth().of(2025).toString());
        assertEquals("October 29, 2025", october.twentyNinth().of(2025).toString());
        assertEquals("October 30, 2025", october.thirtieth().of(2025).toString());
        assertEquals("October 31, 2025", october.thirtyFirst().of(2025).toString());
    }

    @Test
    void testInstanceCreationShortcutForSeptember() {
        assertEquals("September 1, 2025", september.first().of(2025).toString());
        assertEquals("September 2, 2025", september.second().of(2025).toString());
        assertEquals("September 3, 2025", september.third().of(2025).toString());
        assertEquals("September 4, 2025", september.fourth().of(2025).toString());
        assertEquals("September 5, 2025", september.fifth().of(2025).toString());
        assertEquals("September 6, 2025", september.sixth().of(2025).toString());
        assertEquals("September 7, 2025", september.seventh().of(2025).toString());
        assertEquals("September 8, 2025", september.eighth().of(2025).toString());
        assertEquals("September 9, 2025", september.ninth().of(2025).toString());
        assertEquals("September 10, 2025", september.tenth().of(2025).toString());
        assertEquals("September 11, 2025", september.eleventh().of(2025).toString());
        assertEquals("September 12, 2025", september.twelfth().of(2025).toString());
        assertEquals("September 13, 2025", september.thirteenth().of(2025).toString());
        assertEquals("September 14, 2025", september.fourteenth().of(2025).toString());
        assertEquals("September 15, 2025", september.fifteenth().of(2025).toString());
        assertEquals("September 16, 2025", september.sixteenth().of(2025).toString());
        assertEquals("September 17, 2025", september.seventeenth().of(2025).toString());
        assertEquals("September 18, 2025", september.eighteenth().of(2025).toString());
        assertEquals("September 19, 2025", september.nineteenth().of(2025).toString());
        assertEquals("September 20, 2025", september.twentieth().of(2025).toString());
        assertEquals("September 21, 2025", september.twentyFirst().of(2025).toString());
        assertEquals("September 22, 2025", september.twentySecond().of(2025).toString());
        assertEquals("September 23, 2025", september.twentyThird().of(2025).toString());
        assertEquals("September 24, 2025", september.twentyFourth().of(2025).toString());
        assertEquals("September 25, 2025", september.twentyFifth().of(2025).toString());
        assertEquals("September 26, 2025", september.twentySixth().of(2025).toString());
        assertEquals("September 27, 2025", september.twentySeventh().of(2025).toString());
        assertEquals("September 28, 2025", september.twentyEighth().of(2025).toString());
        assertEquals("September 29, 2025", september.twentyNinth().of(2025).toString());
        assertEquals("September 30, 2025", september.thirtieth().of(2025).toString());
        assertThrows(UnsupportedOperationException.class, () -> september.thirtyFirst());
    }

    @Test
    void testPrintString() {
        assertEquals("April 1", GregorianMonth.APRIL.first().toString());
    }
}
