package org.chalten.measure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.junit.jupiter.api.Test;

class FractionTest {

    @Test
    void normalizesSignAndCommonFactors() {
        Fraction fraction = Fraction.of(6, -4);
        assertThat(fraction.numerator()).isEqualTo(BigInteger.valueOf(-3));
        assertThat(fraction.denominator()).isEqualTo(BigInteger.TWO);
        assertThat(fraction).isEqualTo(Fraction.of(-3, 2)).hasSameHashCodeAs(Fraction.of(-3, 2));
        assertThat(Fraction.of(0, -5)).isEqualTo(Fraction.ZERO);
        assertThat(Fraction.of(10, 5)).isEqualTo(Fraction.of(2)).matches(Fraction::isInteger);
    }

    @Test
    void rejectsZeroDenominators() {
        assertThatThrownBy(() -> Fraction.of(1, 0)).isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> Fraction.ONE.dividedBy(Fraction.ZERO)).isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> Fraction.ZERO.reciprocal()).isInstanceOf(ArithmeticException.class);
    }

    @Test
    void arithmeticIsExact() {
        Fraction third = Fraction.of(1, 3);
        assertThat(third.plus(third).plus(third)).isEqualTo(Fraction.ONE);
        assertThat(Fraction.of(1, 12).times(12)).isEqualTo(Fraction.ONE);
        assertThat(Fraction.of(1, 2).minus(Fraction.of(1, 3))).isEqualTo(Fraction.of(1, 6));
        assertThat(Fraction.of(3, 4).dividedBy(Fraction.of(3, 8))).isEqualTo(Fraction.of(2));
        assertThat(Fraction.of(2440587).plus(Fraction.of(1, 2))).isEqualTo(Fraction.of(4881175, 2));
        assertThat(Fraction.of(5).plus(3).minus(2).times(2).dividedBy(4)).isEqualTo(Fraction.of(3));
        assertThat(Fraction.of(-3, 4).negated()).isEqualTo(Fraction.of(3, 4));
        assertThat(Fraction.of(-3, 4).abs()).isEqualTo(Fraction.of(3, 4));
        assertThat(Fraction.of(3, 4).abs()).isEqualTo(Fraction.of(3, 4));
        assertThat(Fraction.of(-3, 4).reciprocal()).isEqualTo(Fraction.of(-4, 3));
    }

    @Test
    void roundsLikeSmalltalk() {
        assertThat(Fraction.of(7, 2).truncated()).isEqualTo(BigInteger.valueOf(3));
        assertThat(Fraction.of(-7, 2).truncated()).isEqualTo(BigInteger.valueOf(-3));
        assertThat(Fraction.of(7, 2).floor()).isEqualTo(BigInteger.valueOf(3));
        assertThat(Fraction.of(-7, 2).floor()).isEqualTo(BigInteger.valueOf(-4));
        assertThat(Fraction.of(7, 2).ceiling()).isEqualTo(BigInteger.valueOf(4));
        assertThat(Fraction.of(-7, 2).ceiling()).isEqualTo(BigInteger.valueOf(-3));
        assertThat(Fraction.of(-4).ceiling()).isEqualTo(BigInteger.valueOf(-4));
        assertThat(Fraction.of(-4).floor()).isEqualTo(BigInteger.valueOf(-4));
    }

    @Test
    void integerDivisionAndRemaindersLikeSmalltalk() {
        Fraction seven = Fraction.of(7);
        Fraction minusSeven = Fraction.of(-7);
        Fraction two = Fraction.of(2);
        assertThat(minusSeven.floorDiv(two)).isEqualTo(BigInteger.valueOf(-4));
        assertThat(minusSeven.floorMod(two)).isEqualTo(Fraction.ONE);
        assertThat(seven.floorMod(two.negated())).isEqualTo(Fraction.of(-1));
        assertThat(minusSeven.truncatedDiv(two)).isEqualTo(BigInteger.valueOf(-3));
        assertThat(minusSeven.remainder(two)).isEqualTo(Fraction.of(-1));
        assertThat(Fraction.of(7, 2).floorMod(Fraction.ONE)).isEqualTo(Fraction.of(1, 2));
    }

    @Test
    void testingMessages() {
        assertThat(Fraction.ZERO.isZero()).isTrue();
        assertThat(Fraction.ZERO.isPositive()).isTrue();
        assertThat(Fraction.ZERO.isStrictlyPositive()).isFalse();
        assertThat(Fraction.ZERO.isNegative()).isFalse();
        assertThat(Fraction.of(-1, 2).isNegative()).isTrue();
        assertThat(Fraction.of(-1, 2).signum()).isEqualTo(-1);
        assertThat(Fraction.of(1, 2).isStrictlyPositive()).isTrue();
        assertThat(Fraction.of(1, 2).isInteger()).isFalse();
    }

    @Test
    void compares() {
        assertThat(Fraction.of(1, 3)).isLessThan(Fraction.of(1, 2));
        assertThat(Fraction.of(-1, 2)).isLessThan(Fraction.of(-1, 3));
        assertThat(Fraction.of(3)).isGreaterThan(Fraction.of(2));
        assertThat(Fraction.of(1, 3).min(Fraction.of(1, 2))).isEqualTo(Fraction.of(1, 3));
        assertThat(Fraction.of(1, 3).max(Fraction.of(1, 2))).isEqualTo(Fraction.of(1, 2));
        assertThat(Fraction.of(1, 2)).isNotEqualTo("1/2").isNotEqualTo(null);
    }

    @Test
    void printsLikeSmalltalk() {
        assertThat(Fraction.of(365)).hasToString("365");
        assertThat(Fraction.of(-2)).hasToString("-2");
        assertThat(Fraction.of(3, 2)).hasToString("(3/2)");
        assertThat(Fraction.of(-3, 2)).hasToString("(-3/2)");
    }

    @Test
    void parsesIntegersFractionsAndDecimalsExactly() {
        assertThat(Fraction.parse("1.52")).isEqualTo(Fraction.of(38, 25));
        assertThat(Fraction.parse("-0.5")).isEqualTo(Fraction.of(-1, 2));
        assertThat(Fraction.parse("100")).isEqualTo(Fraction.of(100));
        assertThat(Fraction.parse("1.0")).isEqualTo(Fraction.ONE);
        assertThat(Fraction.parse("1e3")).isEqualTo(Fraction.of(1000));
        assertThat(Fraction.parse("3/2")).isEqualTo(Fraction.of(3, 2));
        assertThat(Fraction.parse(" (-3/2) ")).isEqualTo(Fraction.of(-3, 2));
        assertThat(Fraction.of(new BigDecimal("2.50"))).isEqualTo(Fraction.of(5, 2));
        assertThatThrownBy(() -> Fraction.parse("one")).isInstanceOf(NumberFormatException.class)
                .hasMessage("Not a number: one");
    }

    @Test
    void convertsToJavaNumbers() {
        assertThat(Fraction.of(7, 2).intValue()).isEqualTo(3);
        assertThat(Fraction.of(-7, 2).longValue()).isEqualTo(-3);
        assertThat(Fraction.of(3).longValueExact()).isEqualTo(3);
        assertThat(Fraction.of(3).intValueExact()).isEqualTo(3);
        assertThatThrownBy(() -> Fraction.of(1, 2).longValueExact()).isInstanceOf(ArithmeticException.class);
        assertThatThrownBy(() -> Fraction.of(1, 2).intValueExact()).isInstanceOf(ArithmeticException.class);
        assertThat(Fraction.of(1, 4).toBigDecimalExact()).isEqualByComparingTo("0.25");
        assertThatThrownBy(() -> Fraction.of(1, 3).toBigDecimalExact()).isInstanceOf(ArithmeticException.class);
        assertThat(Fraction.of(1, 4).floatValue()).isEqualTo(0.25f);
    }

    @Test
    void convertsToTheCorrectlyRoundedDouble() {
        assertThat(Fraction.of(1, 3).doubleValue()).isEqualTo(1.0 / 3);
        assertThat(Fraction.of(-2, 3).doubleValue()).isEqualTo(-2.0 / 3);
        assertThat(Fraction.parse("1.52").doubleValue()).isEqualTo(1.52);
        assertThat(Fraction.parse("0.1").doubleValue()).isEqualTo(0.1);
        assertThat(Fraction.of(BigInteger.TEN.pow(30).add(BigInteger.ONE), BigInteger.TEN.pow(10)).doubleValue())
                .isEqualTo(1e20);
        assertThat(Fraction.of(1, 1_000_000_007).doubleValue()).isEqualTo(1.0 / 1_000_000_007);
        assertThat(Fraction.of(12).doubleValue()).isEqualTo(12.0);
    }
}
