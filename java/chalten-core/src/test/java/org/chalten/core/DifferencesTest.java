package org.chalten.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Modifier;
import java.time.LocalTime;
import org.chalten.measure.Fraction;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;
import org.junit.jupiter.api.Test;

/** Tests of the {@code chalten-core} entries of {@code DIFFERENCES.md} that no SUnit port covers. */
class DifferencesTest {

    @Test
    void daysOfMonthAreValuesNotInternedInstances() {
        CalendarDayOfMonth<GregorianDate> first = GregorianMonth.APRIL.first();
        CalendarDayOfMonth<GregorianDate> other = GregorianMonth.APRIL.dayOfMonthAt(1);
        assertEquals(first, other);
        assertEquals(first.hashCode(), other.hashCode());
        assertNotSame(first, new CalendarDayOfMonth<>(GregorianMonth.APRIL, 1));
    }

    @Test
    void abstractSmalltalkClassesAreInterfacesAndGregorianTypesAreFinal() {
        for (Class<?> type : new Class<?>[] {CalendarDate.class, CalendarYear.class, CalendarMonthOfYear.class,
                CalendarDateTime.class, CalendarMagnitude.class}) {
            assertTrue(type.isInterface(), type::getName);
        }
        for (Class<?> type : new Class<?>[] {GregorianDate.class, GregorianYear.class, GregorianMonthOfYear.class,
                GregorianDateTime.class, Instant.class}) {
            assertTrue(Modifier.isFinal(type.getModifiers()), type::getName);
        }
    }

    @Test
    void instantsStayExactAndTruncateOnlyAtJavaTimeInterop() {
        Instant instant = Instant.microsecondsSinceEpoch(TimeQuantity.of(Fraction.of(3, 2), TimeUnits.MICROSECOND));
        assertEquals(Fraction.of(3, 2),
                instant.microsecondsSinceEpoch().convertTo(TimeUnits.MICROSECOND).numericalValue());
        assertEquals(java.time.Instant.ofEpochSecond(0, 1_000), instant.toJavaInstant());
        assertEquals(LocalTime.of(0, 0, 0, 1_500),
                instant.asGregorianDateTimeInCoordinatedUniversalTime().asRelativePointInTime().timeOfDay()
                        .toLocalTime());
    }
}
