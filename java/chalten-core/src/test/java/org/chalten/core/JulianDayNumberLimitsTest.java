package org.chalten.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/**
 * Pins the Smalltalk results outside the range where Chalten's Julian day number arithmetic is correct
 * ({@code DIFFERENCES.md} S1). The Java port keeps them on purpose: these are the values the Smalltalk expressions
 * evaluate to, found by the properties of {@link GregorianCalendarPropertiesTest} and
 * {@link TimeOffsetPropertiesTest}.
 */
class JulianDayNumberLimitsTest {

    @Test
    void dayOfWeekIsOneDayOffBeforeMarchOfTheYearMinus4716() {
        // GregorianDateTime>>julianDayNumber truncates 36525/100 * (yearNumber + 4716) toward zero, so a negative
        // factor (January and February count in the previous year) adds one day.
        assertEquals(java.time.DayOfWeek.SATURDAY, LocalDate.of(-4716, 1, 1).getDayOfWeek());
        assertEquals(DayOfWeek.SUNDAY, GregorianDate.of(-4716, 1, 1).dayOfWeek());
        assertEquals(LocalDate.of(-4716, 3, 1).getDayOfWeek(),
                GregorianDate.of(-4716, 3, 1).dayOfWeek().toDayOfWeek());
    }

    @Test
    void dateTimesBeforeJulianDayZeroConvertOneDayEarlier() {
        // EdwardGrahamRichardsAlgorithm>>normalize: uses Number>>rounded, which rounds the negative Julian day
        // number -327.5 of January 1, -4713 at midnight away from zero, to the day before.
        GregorianDateTime midnight = GregorianDate.of(-4713, 1, 1).atMidnight();
        assertEquals(GregorianDate.of(-4714, 12, 31).atMidnight(),
                midnight.inCoordinatedUniversalTime().asRelativePointInTime());
        GregorianDateTime noon = GregorianDate.of(-4713, 1, 1).atNoon();
        assertEquals(noon, noon.inCoordinatedUniversalTime().asRelativePointInTime());
    }

    @Test
    void dateTimesFromJulianDayZeroOnConvertExactly() {
        GregorianDateTime dateTime = GregorianDate.of(-4712, 1, 1).atMidnight();
        assertEquals(dateTime, dateTime.inCoordinatedUniversalTime().asRelativePointInTime());
        Instant instant = Instant.fromJulianDayNumber(org.chalten.measure.Fraction.of(1, 2));
        assertEquals(GregorianDate.of(-4713, 11, 25).atMidnight(),
                instant.asGregorianDateTimeInCoordinatedUniversalTime().asRelativePointInTime());
    }
}
