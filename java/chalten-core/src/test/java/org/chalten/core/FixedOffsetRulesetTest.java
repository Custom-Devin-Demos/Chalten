package org.chalten.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.chalten.measure.Fraction;
import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.measure.TimeQuantity;
import org.chalten.testkit.ChaltenTest;
import org.junit.jupiter.api.Test;

class FixedOffsetRulesetTest extends ChaltenTest {

    private static final List<Fraction> HOUR_COUNTS = List.of(Fraction.of(10), Fraction.of(-12), Fraction.of(-21, 2),
            Fraction.of(0), Fraction.of(23, 4), Fraction.of(14));
    private static final List<String> EXPECTED = List.of("UTC+10:00", "UTC-12:00", "UTC-10:30", "UTC+00:00",
            "UTC+05:45", "UTC+14:00");

    @Test
    void testCantCreateOutOfRange() {
        InstanceCreationFailedException exception = assertThrows(InstanceCreationFailedException.class,
                () -> FixedOffsetRuleset.of(TimeQuantity.of(Fraction.parse("-16.1"), hour)));
        // DIFFERENCES.md M2: Smalltalk's Float -16.1 prints as "-16.1"; the exact amount prints as a fraction.
        assertEquals("(-161/10) h is out of range. UTC offsets range from -16:00 to +16:00", exception.getMessage());
    }

    @Test
    void testIdentifier() {
        for (int index = 0; index < HOUR_COUNTS.size(); index++) {
            FixedOffsetRuleset ruleset = FixedOffsetRuleset.of(TimeQuantity.of(HOUR_COUNTS.get(index), hour));
            assertEquals(EXPECTED.get(index), ruleset.identifier());
        }
    }

    @Test
    void testOffsetToUniversalCoordinatedTimeAt() {
        assertEquals(TimeQuantity.of(-3, hour),
                FixedOffsetRuleset.of(TimeQuantity.of(-3, hour)).offsetToUniversalCoordinatedTimeAt(Instant.now()));
    }

    @Test
    void testPrintString() {
        for (int index = 0; index < HOUR_COUNTS.size(); index++) {
            FixedOffsetRuleset ruleset = FixedOffsetRuleset.of(TimeQuantity.of(HOUR_COUNTS.get(index), hour));
            assertEquals(EXPECTED.get(index), ruleset.toString());
        }
    }
}
