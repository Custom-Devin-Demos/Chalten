package org.chalten.testkit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.IntRange;
import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

class ChaltenTestSmokeTest extends ChaltenTest {

    @Test
    void runsOnJava21() {
        assertEquals(21, Runtime.version().feature());
    }

    @Test
    void orderingAssertionsFailWhenTheOrderDoesNotHold() {
        assertThrows(AssertionFailedError.class, () -> assertIsGreaterThan(1, 2));
        assertThrows(AssertionFailedError.class, () -> assertIsGreaterThanOrEqualTo(1, 2));
        assertThrows(AssertionFailedError.class, () -> assertIsLessThan(2, 1));
        assertThrows(AssertionFailedError.class, () -> assertIsLessThanOrEqualTo(2, 1));
        assertThrows(AssertionFailedError.class, () -> denyIsGreaterThan(2, 1));
        assertThrows(AssertionFailedError.class, () -> denyIsLessThan(1, 2));
    }

    @Property
    void orderingAssertionsHoldForASuccessor(@ForAll @IntRange(min = -1_000_000, max = 1_000_000) int number) {
        int successor = number + 1;
        assertIsGreaterThan(successor, number);
        assertIsGreaterThanOrEqualTo(successor, number);
        assertIsGreaterThanOrEqualTo(number, number);
        assertIsLessThan(number, successor);
        assertIsLessThanOrEqualTo(number, successor);
        assertIsLessThanOrEqualTo(number, number);
        denyIsGreaterThan(number, successor);
        denyIsLessThan(successor, number);
    }
}
