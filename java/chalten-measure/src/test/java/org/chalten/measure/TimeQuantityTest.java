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

import java.time.Duration;
import java.time.Period;
import org.junit.jupiter.api.Test;

class TimeQuantityTest {

    @Test
    void printsLikeSmalltalk() {
        assertThat(DAY.times(365)).hasToString("365 d");
        assertThat(MONTH.times(12)).hasToString("12 mo");
        assertThat(HOUR.times(Fraction.of(3, 2))).hasToString("(3/2) h");
        assertThat(YEAR.times(-1)).hasToString("-1 yr");
        assertThat(TimeUnits.MICROSECOND.times(0)).hasToString("0 µs");
    }

    @Test
    void accessesItsParts() {
        TimeQuantity quantity = TimeQuantity.of(Fraction.of(3, 2), HOUR);
        assertThat(quantity.amount()).isEqualTo(Fraction.of(3, 2));
        assertThat(quantity.numericalValue()).isEqualTo(Fraction.of(3, 2));
        assertThat(quantity.unit()).isSameAs(HOUR);
        assertThat(quantity.amountInBaseUnit()).isEqualTo(Fraction.of(5400));
        assertThatThrownBy(() -> TimeQuantity.of(null, DAY)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> TimeQuantity.of(1, null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void addsAndSubtractsKeepingTheUnitWhenItIsTheSame() {
        assertThat(DAY.times(3).plus(DAY.times(4))).isEqualTo(DAY.times(7)).extracting(TimeQuantity::unit)
                .isSameAs(DAY);
        assertThat(MONTH.times(3).minus(MONTH.times(4)).unit()).isSameAs(MONTH);
        assertThat(MONTH.times(3).minus(MONTH.times(4))).hasToString("-1 mo");
    }

    @Test
    void addsAndSubtractsCommensurableQuantitiesInTheBaseUnit() {
        TimeQuantity sum = HOUR.times(1).plus(MINUTE.times(30));
        assertThat(sum).hasToString("5400 s").isEqualTo(MINUTE.times(90));
        assertThat(YEAR.times(1).plus(MONTH.times(6))).hasToString("(3/2) yr");
        assertThat(MONTH.times(18).minus(YEAR.times(1))).hasToString("(1/2) yr").isEqualTo(MONTH.times(6));
        assertThat(WEEK.times(1).minus(DAY.times(7)).isZero()).isTrue();
    }

    @Test
    void cannotMixIncommensurableQuantities() {
        assertThatThrownBy(() -> DAY.times(1).plus(MONTH.times(1))).isInstanceOf(ArithmeticException.class)
                .hasMessage("Cannot add incommensurable quantities");
        assertThatThrownBy(() -> DAY.times(1).minus(MONTH.times(1))).isInstanceOf(ArithmeticException.class)
                .hasMessage("Cannot subtract incommensurable quantities");
        assertThatThrownBy(() -> DAY.times(1).compareTo(YEAR.times(1))).isInstanceOf(ArithmeticException.class)
                .hasMessage("Incommensurable units are not comparable");
        assertThatThrownBy(() -> DAY.times(1).convertTo(MONTH)).isInstanceOf(ArithmeticException.class)
                .hasMessage("Cannot convert incommensurable units");
        assertThatThrownBy(() -> DAY.times(1).dividedBy(MONTH.times(1))).isInstanceOf(ArithmeticException.class)
                .hasMessage("Cannot convert incommensurable units");
    }

    @Test
    void scales() {
        assertThat(DAY.times(3).times(2)).isEqualTo(DAY.times(6));
        assertThat(DAY.times(3).times(Fraction.of(1, 2))).hasToString("(3/2) d");
        assertThat(DAY.times(3).dividedBy(2)).hasToString("(3/2) d");
        assertThat(DAY.times(3).dividedBy(Fraction.of(3, 4))).hasToString("4 d");
        assertThatThrownBy(() -> DAY.times(3).dividedBy(0)).isInstanceOf(ArithmeticException.class);
    }

    @Test
    void dividesCommensurableQuantitiesIntoRatios() {
        assertThat(DAY.times(3).dividedBy(HOUR.times(12))).isEqualTo(Fraction.of(6));
        assertThat(YEAR.times(1).dividedBy(MONTH.times(5))).isEqualTo(Fraction.of(12, 5));
        assertThatThrownBy(() -> DAY.times(3).dividedBy(DAY.times(0))).isInstanceOf(ArithmeticException.class);
    }

    @Test
    void convertsExactly() {
        assertThat(YEAR.times(1).convertTo(MONTH)).hasToString("12 mo");
        assertThat(MONTH.times(1).convertTo(YEAR)).hasToString("(1/12) yr");
        assertThat(DAY.times(1).convertTo(TimeUnits.MICROSECOND)).hasToString("86400000000 µs");
        assertThat(TimeUnits.MILLISECOND.times(1500).convertTo(SECOND)).hasToString("(3/2) s");
        assertThat(MINUTE.times(90).convertTo(HOUR)).hasToString("(3/2) h");
        TimeQuantity quantity = DAY.times(2);
        assertThat(quantity.convertTo(DAY)).isSameAs(quantity);
    }

    @Test
    void negatesTruncatesAndTakesAbsoluteValues() {
        assertThat(DAY.times(3).negated()).hasToString("-3 d");
        assertThat(DAY.times(-3).abs()).hasToString("3 d");
        assertThat(DAY.times(3).abs()).hasToString("3 d");
        assertThat(DAY.times(Fraction.of(7, 2)).truncated()).hasToString("3 d");
        assertThat(DAY.times(Fraction.of(-7, 2)).truncated()).hasToString("-3 d");
        assertThat(MONTH.times(Fraction.of(-1, 12)).truncated().isZero()).isTrue();
    }

    @Test
    void testingMessages() {
        assertThat(DAY.times(0).isZero()).isTrue();
        assertThat(DAY.times(0).isPositive()).isTrue();
        assertThat(DAY.times(0).isNegative()).isFalse();
        assertThat(DAY.times(0).isStrictlyPositive()).isFalse();
        assertThat(DAY.times(-1).isNegative()).isTrue();
        assertThat(DAY.times(1).isStrictlyPositive()).isTrue();
        assertThat(DAY.times(1).isCommensurableWith(WEEK)).isTrue();
        assertThat(DAY.times(1).isCommensurableWith(MONTH.times(1))).isFalse();
        assertThat(YEAR.times(1).isComparableTo(MONTH.times(1))).isTrue();
        assertThat(YEAR.times(1).isComparableTo(DAY.times(1))).isFalse();
    }

    @Test
    void equalityFollowsTheMeasuredTime() {
        assertThat(HOUR.times(1)).isEqualTo(MINUTE.times(60)).hasSameHashCodeAs(MINUTE.times(60));
        assertThat(YEAR.times(1)).isEqualTo(MONTH.times(12)).hasSameHashCodeAs(MONTH.times(12));
        assertThat(DAY.times(0)).isNotEqualTo(MONTH.times(0));
        assertThat(SECOND.times(1)).isNotEqualTo(YEAR.times(1));
        assertThat(DAY.times(1)).isNotEqualTo(DAY.times(2)).isNotEqualTo("1 d").isNotEqualTo(null);
    }

    @Test
    void comparesCommensurableQuantities() {
        assertThat(HOUR.times(25)).isGreaterThan(DAY.times(1));
        assertThat(MONTH.times(11)).isLessThan(YEAR.times(1));
        assertThat(MONTH.times(12)).isEqualByComparingTo(YEAR.times(1));
        assertThat(DAY.times(-1)).isLessThan(SECOND.times(0));
        assertThat(HOUR.times(25).min(DAY.times(1))).isEqualTo(DAY.times(1));
        assertThat(HOUR.times(25).max(DAY.times(1))).isEqualTo(HOUR.times(25));
    }

    @Test
    void createsIntervals() {
        assertThat(DAY.times(1).to(DAY.times(3))).containsExactly(DAY.times(1), DAY.times(2), DAY.times(3));
        assertThat(MONTH.times(0).to(YEAR.times(1), MONTH.times(6)))
                .containsExactly(MONTH.times(0), MONTH.times(6), MONTH.times(12));
    }

    @Test
    void interoperatesWithJavaTime() {
        assertThat(DAY.times(2).toDuration()).isEqualTo(Duration.ofDays(2));
        assertThat(TimeUnits.MICROSECOND.times(-1).toDuration()).isEqualTo(Duration.ofNanos(-1000));
        assertThat(TimeQuantity.of(Duration.ofMillis(1500))).isEqualTo(SECOND.times(Fraction.of(3, 2)));
        assertThat(TimeQuantity.of(Duration.ofSeconds(-1, 1))).isEqualTo(
                SECOND.times(Fraction.of(-999_999_999, 1_000_000_000)));
        assertThatThrownBy(() -> SECOND.times(Fraction.of(1, 3)).toDuration())
                .isInstanceOf(ArithmeticException.class).hasMessage("(1/3) s is not a whole number of nanoseconds");
        assertThatThrownBy(() -> MONTH.times(1).toDuration()).isInstanceOf(ArithmeticException.class);
        assertThat(MONTH.times(14).toPeriod()).isEqualTo(Period.of(1, 2, 0));
        assertThat(TimeUnits.DECADE.times(1).toPeriod()).isEqualTo(Period.ofYears(10));
        assertThatThrownBy(() -> YEAR.times(Fraction.of(1, 24)).toPeriod())
                .isInstanceOf(ArithmeticException.class).hasMessage("(1/24) yr is not a whole number of months");
        assertThatThrownBy(() -> DAY.times(1).toPeriod()).isInstanceOf(ArithmeticException.class);
    }
}
