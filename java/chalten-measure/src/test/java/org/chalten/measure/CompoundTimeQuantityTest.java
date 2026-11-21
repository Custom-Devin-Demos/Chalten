package org.chalten.measure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.chalten.measure.TimeUnits.DAY;
import static org.chalten.measure.TimeUnits.HOUR;
import static org.chalten.measure.TimeUnits.MONTH;
import static org.chalten.measure.TimeUnits.SECOND;
import static org.chalten.measure.TimeUnits.YEAR;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class CompoundTimeQuantityTest {

    private final CompoundTimeQuantity threeYearsFiveMonthsFourDays =
            CompoundTimeQuantity.of(YEAR.times(3), MONTH.times(5), DAY.times(4));

    @Test
    void keepsItsPartsInOrder() {
        assertThat(threeYearsFiveMonthsFourDays.parts())
                .containsExactly(YEAR.times(3), MONTH.times(5), DAY.times(4));
        assertThat(threeYearsFiveMonthsFourDays.size()).isEqualTo(3);
        assertThat(threeYearsFiveMonthsFourDays.isEmpty()).isFalse();
        assertThat(threeYearsFiveMonthsFourDays).hasToString("3 yr + 5 mo + 4 d");
        assertThat(CompoundTimeQuantity.of()).hasToString("0");
        assertThatThrownBy(() -> threeYearsFiveMonthsFourDays.parts().add(DAY.times(1)))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void copiesTheGivenCollection() {
        List<TimeQuantity> parts = new ArrayList<>(List.of(DAY.times(1)));
        CompoundTimeQuantity compound = CompoundTimeQuantity.of(parts);
        parts.add(DAY.times(2));
        assertThat(compound.parts()).containsExactly(DAY.times(1));
    }

    @Test
    void addsQuantities() {
        CompoundTimeQuantity sum = threeYearsFiveMonthsFourDays.plus(HOUR.times(2));
        assertThat(sum).hasToString("3 yr + 5 mo + 4 d + 2 h");
        assertThat(sum.plus(CompoundTimeQuantity.of(SECOND.times(1), MONTH.times(1))))
                .hasToString("3 yr + 5 mo + 4 d + 2 h + 1 s + 1 mo");
        assertThat(threeYearsFiveMonthsFourDays.negated()).hasToString("-3 yr + -5 mo + -4 d");
    }

    @Test
    void totalsCommensurableParts() {
        CompoundTimeQuantity compound = threeYearsFiveMonthsFourDays.plus(HOUR.times(2)).plus(MONTH.times(7));
        assertThat(compound.totals()).containsExactly(YEAR.times(4), DAY.times(4).plus(HOUR.times(2)));
        assertThat(compound.totals().get(0)).hasToString("4 yr");
        assertThat(CompoundTimeQuantity.of(YEAR.times(3), MONTH.times(5), MONTH.times(7)).convertTo(MONTH))
                .hasToString("48 mo");
        assertThatThrownBy(() -> threeYearsFiveMonthsFourDays.convertTo(MONTH))
                .isInstanceOf(ArithmeticException.class).hasMessage("Cannot convert incommensurable units");
        assertThat(CompoundTimeQuantity.of().convertTo(DAY)).hasToString("0 d");
    }

    @Test
    void equalityFollowsTheTotals() {
        assertThat(threeYearsFiveMonthsFourDays)
                .isEqualTo(CompoundTimeQuantity.of(DAY.times(4), MONTH.times(41)))
                .hasSameHashCodeAs(CompoundTimeQuantity.of(DAY.times(4), MONTH.times(41)))
                .isEqualTo(threeYearsFiveMonthsFourDays.plus(SECOND.times(0)))
                .isNotEqualTo(CompoundTimeQuantity.of(YEAR.times(3), MONTH.times(5)))
                .isNotEqualTo(YEAR.times(3))
                .isNotEqualTo(null);
        assertThat(CompoundTimeQuantity.of()).isEqualTo(CompoundTimeQuantity.of(DAY.times(0)));
    }

    @Test
    void isZeroWhenEveryPartIs() {
        assertThat(CompoundTimeQuantity.of().isZero()).isTrue();
        assertThat(CompoundTimeQuantity.of(DAY.times(0), MONTH.times(0)).isZero()).isTrue();
        assertThat(CompoundTimeQuantity.of(DAY.times(1), DAY.times(-1)).isZero()).isFalse();
        assertThat(threeYearsFiveMonthsFourDays.isZero()).isFalse();
    }
}
