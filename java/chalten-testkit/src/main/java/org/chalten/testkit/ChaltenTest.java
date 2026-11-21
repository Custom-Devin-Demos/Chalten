package org.chalten.testkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import org.chalten.core.GregorianMonth;
import org.chalten.core.ISO8601Formatter;
import org.chalten.core.ISO8601Printable;
import org.chalten.measure.TimeUnit;
import org.chalten.measure.TimeUnits;

/**
 * Abstract JUnit 5 base class for Chalten tests, the port of {@code ChaltenTest} in {@code Chalten-SUnit}.
 *
 * <p>Holds the unit fixtures ({@code hour}, {@code day}, {@code month}, {@code year}, {@code decade}), the Gregorian
 * month fixtures ({@code january} ... {@code december}), the ordering assertions, {@code assertNextOf}/
 * {@code assertPreviousOf} and {@code printAsISO8601String}.
 */
public abstract class ChaltenTest {

    protected final TimeUnit hour = TimeUnits.HOUR;
    protected final TimeUnit day = TimeUnits.DAY;
    protected final TimeUnit month = TimeUnits.MONTH;
    protected final TimeUnit year = TimeUnits.YEAR;
    protected final TimeUnit decade = TimeUnits.DECADE;

    protected final GregorianMonth january = GregorianMonth.JANUARY;
    protected final GregorianMonth february = GregorianMonth.FEBRUARY;
    protected final GregorianMonth march = GregorianMonth.MARCH;
    protected final GregorianMonth april = GregorianMonth.APRIL;
    protected final GregorianMonth may = GregorianMonth.MAY;
    protected final GregorianMonth june = GregorianMonth.JUNE;
    protected final GregorianMonth july = GregorianMonth.JULY;
    protected final GregorianMonth august = GregorianMonth.AUGUST;
    protected final GregorianMonth september = GregorianMonth.SEPTEMBER;
    protected final GregorianMonth october = GregorianMonth.OCTOBER;
    protected final GregorianMonth november = GregorianMonth.NOVEMBER;
    protected final GregorianMonth december = GregorianMonth.DECEMBER;

    protected ChaltenTest() {
    }

    protected static <T extends Comparable<? super T>> void assertIsGreaterThan(T magnitude, T anotherMagnitude) {
        assertTrue(magnitude.compareTo(anotherMagnitude) > 0,
                () -> magnitude + " is not greater than " + anotherMagnitude);
    }

    protected static <T extends Comparable<? super T>> void assertIsGreaterThanOrEqualTo(T magnitude, T anotherMagnitude) {
        assertTrue(magnitude.compareTo(anotherMagnitude) >= 0,
                () -> magnitude + " is not greater than or equal to " + anotherMagnitude);
    }

    protected static <T extends Comparable<? super T>> void assertIsLessThan(T magnitude, T anotherMagnitude) {
        assertTrue(magnitude.compareTo(anotherMagnitude) < 0,
                () -> magnitude + " is not less than " + anotherMagnitude);
    }

    protected static <T extends Comparable<? super T>> void assertIsLessThanOrEqualTo(T magnitude, T anotherMagnitude) {
        assertTrue(magnitude.compareTo(anotherMagnitude) <= 0,
                () -> magnitude + " is not less than or equal to " + anotherMagnitude);
    }

    protected static <T extends Comparable<? super T>> void denyIsGreaterThan(T magnitude, T anotherMagnitude) {
        assertFalse(magnitude.compareTo(anotherMagnitude) > 0,
                () -> magnitude + " is greater than " + anotherMagnitude);
    }

    protected static <T extends Comparable<? super T>> void denyIsLessThan(T magnitude, T anotherMagnitude) {
        assertFalse(magnitude.compareTo(anotherMagnitude) < 0,
                () -> magnitude + " is less than " + anotherMagnitude);
    }

    /** Smalltalk {@code assertNextOf:is:}: sends {@code next} to {@code magnitude}. */
    protected static void assertNextOf(Object magnitude, Object anotherMagnitude) {
        assertEquals(anotherMagnitude, send(magnitude, "next"));
    }

    /** Smalltalk {@code assertPreviousOf:is:}: sends {@code previous} to {@code magnitude}. */
    protected static void assertPreviousOf(Object magnitude, Object anotherMagnitude) {
        assertEquals(anotherMagnitude, send(magnitude, "previous"));
    }

    /** Smalltalk {@code printAsISO8601String:}. */
    protected static String printAsISO8601String(ISO8601Printable calendarMagnitude) {
        return new ISO8601Formatter().format(calendarMagnitude);
    }

    private static Object send(Object receiver, String selector) {
        try {
            return receiver.getClass().getMethod(selector).invoke(receiver);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException runtime) {
                throw runtime;
            }
            throw new IllegalStateException(e.getCause());
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException(receiver + " does not understand #" + selector, e);
        }
    }
}
