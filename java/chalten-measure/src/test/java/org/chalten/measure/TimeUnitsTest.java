package org.chalten.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/** Port of {@code TimeUnitsTest} (a plain {@code TestCase} in Smalltalk). */
class TimeUnitsTest {

    private boolean isUnitCommensurableWith(String unitSymbol, String anotherUnitSymbol) {
        return unit(unitSymbol).isCommensurableWith(unit(anotherUnitSymbol));
    }

    @Test
    void testAbsoluteUnits() {
        assertEquals(TimeQuantity.of(60, unit("second")), TimeQuantity.of(1, unit("minute")));
        assertEquals(TimeQuantity.of(90, unit("minute")), TimeQuantity.of(Fraction.of(3, 2), unit("hour")));
        assertEquals(TimeQuantity.of(72, unit("hour")), TimeQuantity.of(3, unit("day")));
        assertEquals(TimeQuantity.of(14, unit("day")), TimeQuantity.of(2, unit("week")));
    }

    @Test
    void testCommensurability() {
        assertTrue(isUnitCommensurableWith("year", "decade"));
        assertTrue(isUnitCommensurableWith("month", "year"));
        assertFalse(isUnitCommensurableWith("day", "month"));
        assertTrue(isUnitCommensurableWith("day", "week"));
    }

    @Test
    void testRelativeUnits() {
        assertEquals(TimeQuantity.of(12, unit("month")), TimeQuantity.of(1, unit("year")));
        assertEquals(TimeQuantity.of(1, unit("decade")), TimeQuantity.of(2, unit("lustrum")));
        assertEquals(TimeQuantity.of(Fraction.of(1, 2), unit("century")), TimeQuantity.of(1, unit("jubilee")));
        assertEquals(TimeQuantity.of(10, unit("century")), TimeQuantity.of(1, unit("millenium")));
    }

    private TimeUnit unit(String unitSymbol) {
        TimeUnit relative = TimeUnits.units().get(unitSymbol);
        return relative != null ? relative : TimeUnits.absoluteUnits().get(unitSymbol);
    }
}
