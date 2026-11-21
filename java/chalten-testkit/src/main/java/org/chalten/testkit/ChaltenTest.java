package org.chalten.testkit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Abstract JUnit 5 base class for Chalten tests, the port of {@code ChaltenTest} in {@code Chalten-SUnit}.
 *
 * <p>Only the calendar-independent ordering assertions live here so far. The unit fixtures ({@code day},
 * {@code month}, {@code year}, ...) and the Gregorian month fixtures ({@code january} ... {@code december}),
 * {@code assertNextOf}/{@code assertPreviousOf} and {@code printAsISO8601String} are added by the core time-model
 * port together with the types they reference.
 */
public abstract class ChaltenTest {

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
}
