package org.chalten.measure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.chalten.measure.TimeUnits.DAY;
import static org.chalten.measure.TimeUnits.HOUR;
import static org.chalten.measure.TimeUnits.MINUTE;
import static org.chalten.measure.TimeUnits.MONTH;
import static org.chalten.measure.TimeUnits.SECOND;
import static org.chalten.measure.TimeUnits.WEEK;
import static org.chalten.measure.TimeUnits.YEAR;

import java.util.List;
import org.junit.jupiter.api.Test;

class ISO8601DurationFormatterExtensionsTest {

    private final ISO8601DurationFormatter formatter = new ISO8601DurationFormatter();

    @Test
    void formatsCompoundQuantities() {
        assertThat(formatter.format(CompoundTimeQuantity.of(DAY.times(4), YEAR.times(3), MINUTE.times(1))))
                .isEqualTo("P3Y4DT1M");
        assertThatThrownBy(() -> formatter.format(CompoundTimeQuantity.of()))
                .isInstanceOf(ChaltenAssertionException.class).hasMessage("Cannot format empty time periods");
    }

    @Test
    void formatsNonDecimalFractionsAsSmalltalkPrintsTheirFloat() {
        assertThat(formatter.format(SECOND.times(Fraction.of(1, 3)))).isEqualTo("PT0.3333333333333333S");
        assertThat(formatter.format(HOUR.times(Fraction.of(-3, 2)))).isEqualTo("PT-1.5H");
        assertThat(formatter.format(DAY.times(-2))).isEqualTo("P-2D");
        assertThat(formatter.format(WEEK.times(Fraction.of(1, 7)))).isEqualTo("P0.14285714285714285W");
    }

    @Test
    void decimalFractionsAreAllowedOnTheLastComponentOnly() {
        assertThat(formatter.format(List.of(DAY.times(1), HOUR.times(Fraction.of(1, 2))))).isEqualTo("P1DT0.5H");
        assertThatThrownBy(() -> formatter.format(List.of(MONTH.times(1), YEAR.times(Fraction.of(1, 2)))))
                .isInstanceOf(ChaltenAssertionException.class)
                .hasMessage("A decimal fraction can only be applied to the lowest-order component");
    }

    @Test
    void weeksAreOnlySupportedOnTheirOwn() {
        assertThatThrownBy(() -> formatter.format(List.of(WEEK.times(1))))
                .isInstanceOf(ChaltenAssertionException.class)
                .hasMessage("Cannot format time periods including invalid units");
    }

    @Test
    void removesRepeatedPeriodsByEqualityAsSmalltalkDoes() {
        assertThatThrownBy(() -> formatter.format(List.of(HOUR.times(24), DAY.times(1))))
                .isInstanceOf(ChaltenAssertionException.class)
                .hasMessage("Cannot format time periods including invalid units");
    }

    @Test
    void emitsTheTimeDesignatorOnlyOnce() {
        assertThat(formatter.format(List.of(SECOND.times(5), MINUTE.times(4)))).isEqualTo("PT4M5S");
        assertThat(formatter.format(List.of(MONTH.times(2), SECOND.times(5)))).isEqualTo("P2MT5S");
    }
}
