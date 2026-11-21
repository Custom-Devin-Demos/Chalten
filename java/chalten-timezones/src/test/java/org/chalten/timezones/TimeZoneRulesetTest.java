package org.chalten.timezones;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.stream.Stream;
import org.chalten.core.FixedOffsetRuleset;
import org.chalten.core.Instant;
import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.measure.TimeQuantity;
import org.chalten.testkit.ChaltenTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class TimeZoneRulesetTest extends ChaltenTest {

    private static final String INVALID = "Must be exactly one more offset ruleset than transitions between them";

    private TimeZoneRuleset ruleset;
    private Instant firstTransitionTime;
    private Instant secondTransitionTime;

    private void assertOffsetToUTCAt(Instant instant, TimeQuantity timeQuantity) {
        assertEquals(timeQuantity, ruleset.offsetToUniversalCoordinatedTimeAt(instant));
    }

    private TimeQuantity hours(long number) {
        return TimeQuantity.of(number, hour);
    }

    private TimeQuantity days(long number) {
        return TimeQuantity.of(number, day);
    }

    @BeforeEach
    void setUp() {
        firstTransitionTime = Instant.epoch();
        secondTransitionTime = firstTransitionTime.next(days(365));
        ruleset = TimeZoneRuleset.identifiedBy("Testing/TimeZone",
                Stream.of(-3, -4, -2).map(number -> FixedOffsetRuleset.of(hours(number))).toList(),
                List.of(firstTransitionTime, secondTransitionTime));
    }

    private static void assertCreationFails(Executable executable) {
        assertEquals(INVALID, assertThrows(InstanceCreationFailedException.class, executable).getMessage());
    }

    @Test
    void testCantCreateWithInvalidNumberOfTransitions() {
        assertCreationFails(() -> TimeZoneRuleset.identifiedBy("Test", List.of(), List.of()));
        assertCreationFails(() -> TimeZoneRuleset.identifiedBy("Test", List.of(), List.of(Instant.epoch())));
        assertCreationFails(() -> TimeZoneRuleset.identifiedBy("Test", List.of(FixedOffsetRuleset.of(hours(1))),
                List.of(Instant.epoch())));
        assertCreationFails(() -> TimeZoneRuleset.identifiedBy("Test",
                List.of(FixedOffsetRuleset.of(hours(1)), FixedOffsetRuleset.of(hours(2))), List.of()));
    }

    @Test
    void testIdentifier() {
        assertEquals("Testing/TimeZone", ruleset.identifier());
    }

    @Test
    void testOffsetToUniversalCoordinatedTimeAt() {
        assertOffsetToUTCAt(firstTransitionTime.previous(days(1000)), hours(-3));
        assertOffsetToUTCAt(firstTransitionTime.previous(days(1)), hours(-3));
        assertOffsetToUTCAt(firstTransitionTime, hours(-4));
        assertOffsetToUTCAt(firstTransitionTime.next(days(1)), hours(-4));
        assertOffsetToUTCAt(secondTransitionTime.previous(days(1)), hours(-4));
        assertOffsetToUTCAt(secondTransitionTime, hours(-2));
        assertOffsetToUTCAt(secondTransitionTime.next(days(1)), hours(-2));
        assertOffsetToUTCAt(secondTransitionTime.next(days(1000)), hours(-2));
    }

    @Test
    void testPrintString() {
        assertEquals("Testing/TimeZone", ruleset.toString());
    }
}
