package org.chalten.measure;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

class MeasurePropertiesTest {

    private static final List<TimeUnit> ABSOLUTE = List.copyOf(TimeUnits.absoluteUnits().values());
    private static final List<TimeUnit> RELATIVE = List.copyOf(TimeUnits.units().values());

    @Provide
    Arbitrary<Fraction> fractions() {
        return Combinators.combine(Arbitraries.longs().between(-1_000_000, 1_000_000),
                Arbitraries.longs().between(1, 10_000)).as(Fraction::of);
    }

    @Provide
    Arbitrary<TimeQuantity> absoluteQuantities() {
        return Combinators.combine(fractions(), Arbitraries.of(ABSOLUTE)).as(TimeQuantity::of);
    }

    @Provide
    Arbitrary<TimeUnit> absoluteUnits() {
        return Arbitraries.of(ABSOLUTE);
    }

    @Provide
    Arbitrary<TimeQuantity> relativeQuantities() {
        return Combinators.combine(fractions(), Arbitraries.of(RELATIVE)).as(TimeQuantity::of);
    }

    @Property
    void fractionAdditionIsInvertible(@ForAll("fractions") Fraction a, @ForAll("fractions") Fraction b) {
        assertThat(a.plus(b).minus(b)).isEqualTo(a);
        assertThat(a.plus(b)).isEqualTo(b.plus(a));
    }

    @Property
    void fractionDivisionIsInvertible(@ForAll("fractions") Fraction a, @ForAll("fractions") Fraction b) {
        if (!b.isZero()) {
            assertThat(a.dividedBy(b).times(b)).isEqualTo(a);
            assertThat(Fraction.of(a.floorDiv(b)).times(b).plus(a.floorMod(b))).isEqualTo(a);
            assertThat(Fraction.of(a.truncatedDiv(b)).times(b).plus(a.remainder(b))).isEqualTo(a);
        }
    }

    @Property
    void fractionsReadBackFromTheirPrintString(@ForAll("fractions") Fraction a) {
        assertThat(Fraction.parse(a.toString())).isEqualTo(a);
        assertThat(a.floor()).isLessThanOrEqualTo(a.ceiling());
        assertThat(Fraction.of(a.truncated()).abs()).isLessThanOrEqualTo(a.abs());
    }

    @Property
    void conversionRoundTripsExactly(@ForAll("absoluteQuantities") TimeQuantity quantity,
            @ForAll("absoluteUnits") TimeUnit unit) {
        TimeQuantity converted = quantity.convertTo(unit);
        assertThat(converted).isEqualTo(quantity);
        assertThat(converted.convertTo(quantity.unit()).amount()).isEqualTo(quantity.amount());
    }

    @Property
    void additionIsCommutativeAndInvertible(@ForAll("relativeQuantities") TimeQuantity a,
            @ForAll("relativeQuantities") TimeQuantity b) {
        assertThat(a.plus(b)).isEqualTo(b.plus(a));
        assertThat(a.plus(b).minus(b)).isEqualTo(a);
        assertThat(a.minus(a).isZero()).isTrue();
        assertThat(a.plus(a.negated()).isZero()).isTrue();
    }

    @Property
    void orderingIsConsistentWithEquality(@ForAll("absoluteQuantities") TimeQuantity a,
            @ForAll("absoluteQuantities") TimeQuantity b) {
        assertThat(a.compareTo(b) == 0).isEqualTo(a.equals(b));
        assertThat(Integer.signum(a.compareTo(b))).isEqualTo(-Integer.signum(b.compareTo(a)));
        assertThat(a.abs().isNegative()).isFalse();
    }

    @Property(tries = 200)
    void intervalSizeMatchesSteppingWithPlus(@ForAll("absoluteQuantities") TimeQuantity start,
            @ForAll("absoluteQuantities") TimeQuantity step) {
        if (step.isZero()) {
            return;
        }
        TimeQuantity end = start.plus(step.times(37)).plus(step.dividedBy(2));
        DiscreteInterval<TimeQuantity> interval = start.to(end, step);
        assertThat(interval).hasSize(38);
        TimeQuantity expected = start;
        for (TimeQuantity each : interval) {
            assertThat(each).isEqualTo(expected);
            assertThat(interval.rangeIncludes(each)).isTrue();
            expected = expected.plus(step);
        }
        assertThat(interval.indexOf(interval.get(20))).isEqualTo(20);
    }
}
