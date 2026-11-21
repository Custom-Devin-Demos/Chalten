package org.chalten.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import net.jqwik.api.constraints.IntRange;
import org.chalten.measure.CompoundTimeQuantity;
import org.chalten.measure.DiscreteInterval;
import org.chalten.measure.Fraction;
import org.chalten.measure.ISO8601DurationFormatter;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnit;
import org.chalten.measure.TimeUnits;
import org.chalten.testkit.ChaltenArbitraries;

/**
 * PLAN.md §6: exact {@code TimeQuantity} conversions, ISO 8601 duration parse/format round trips and
 * {@code DiscreteInterval} stepping over dates and years (the core {@code Steppable} values).
 */
class DurationPropertiesTest {

    private static final List<TimeUnit> ABSOLUTE_UNITS = List.of(TimeUnits.MICROSECOND, TimeUnits.MILLISECOND,
            TimeUnits.SECOND, TimeUnits.MINUTE, TimeUnits.HOUR, TimeUnits.DAY, TimeUnits.WEEK);
    private static final List<TimeUnit> RELATIVE_UNITS = List.of(TimeUnits.MONTH, TimeUnits.YEAR, TimeUnits.LUSTRUM,
            TimeUnits.DECADE, TimeUnits.JUBILEE, TimeUnits.CENTURY, TimeUnits.MILLENIUM);
    private static final List<TimeUnit> DURATION_UNITS = List.of(TimeUnits.YEAR, TimeUnits.MONTH, TimeUnits.DAY,
            TimeUnits.HOUR, TimeUnits.MINUTE, TimeUnits.SECOND);

    private final ISO8601Parser parser = new ISO8601Parser();
    private final ISO8601DurationFormatter formatter = new ISO8601DurationFormatter();

    @Provide
    Arbitrary<Fraction> amounts() {
        return Combinators.combine(Arbitraries.longs().between(-1_000_000_000L, 1_000_000_000L),
                Arbitraries.longs().between(1, 10_000)).as(Fraction::of);
    }

    @Provide
    Arbitrary<List<TimeUnit>> unitPairs() {
        return Arbitraries.oneOf(Arbitraries.of(ABSOLUTE_UNITS).list().ofSize(2),
                Arbitraries.of(RELATIVE_UNITS).list().ofSize(2));
    }

    /** Whole amounts, or decimals with up to three digits that Smalltalk prints in fixed notation. */
    @Provide
    Arbitrary<Fraction> durationAmounts() {
        return Arbitraries.oneOf(Arbitraries.longs().between(0, 1_000_000_000L).map(Fraction::of),
                Arbitraries.longs().between(1, 99_999_999L).map(thousandths -> Fraction.of(thousandths, 1_000)));
    }

    @Provide
    Arbitrary<TimeUnit> durationUnits() {
        return Arbitraries.of(DURATION_UNITS);
    }

    @Provide
    Arbitrary<List<TimeQuantity>> durationPeriods() {
        return Arbitraries.of(DURATION_UNITS).set().ofMinSize(1).ofMaxSize(6).flatMap(units ->
                Arbitraries.longs().between(1, 100_000).list().ofSize(units.size()).map(amounts -> {
                    List<TimeQuantity> periods = new ArrayList<>();
                    for (TimeUnit unit : DURATION_UNITS) {
                        if (units.contains(unit)) {
                            periods.add(TimeQuantity.of(amounts.get(periods.size()), unit));
                        }
                    }
                    return periods;
                }));
    }

    @Provide
    Arbitrary<GregorianDate> dates() {
        return ChaltenArbitraries.dates();
    }

    @Provide
    Arbitrary<GregorianYear> years() {
        return ChaltenArbitraries.years();
    }

    @Property
    void conversionsRoundTripExactly(@ForAll("amounts") Fraction amount, @ForAll("unitPairs") List<TimeUnit> units) {
        TimeQuantity quantity = TimeQuantity.of(amount, units.get(0));
        TimeQuantity converted = quantity.convertTo(units.get(1));
        assertEquals(units.get(1), converted.unit());
        assertEquals(quantity, converted.convertTo(units.get(0)));
        assertEquals(0, quantity.compareTo(converted));
        if (!amount.isZero()) {
            assertEquals(Fraction.ONE, converted.dividedBy(quantity));
        }
    }

    @Property
    void durationsRoundTripThroughISO8601(@ForAll("durationAmounts") Fraction amount,
            @ForAll("durationUnits") TimeUnit unit) {
        TimeQuantity duration = TimeQuantity.of(amount, unit);
        assertEquals(duration, parser.parseDuration(formatter.format(duration)));
    }

    @Property
    void weeksRoundTripThroughISO8601(@ForAll @IntRange(min = 0, max = 1_000_000) int weeks) {
        TimeQuantity duration = TimeQuantity.of(weeks, TimeUnits.WEEK);
        assertEquals("P" + weeks + "W", formatter.format(duration));
        assertEquals(duration, parser.parseDuration(formatter.format(duration)));
    }

    @Property
    void severalPeriodsRoundTripThroughISO8601(@ForAll("durationPeriods") List<TimeQuantity> periods) {
        String formatted = formatter.format(periods);
        assertEquals(CompoundTimeQuantity.of(periods), parser.parseDurationAllowingSeveralTimePeriods(formatted));
        assertEquals(formatted, formatter.format(parser.parseDurationAllowingSeveralTimePeriods(formatted)));
    }

    @Property
    void dateIntervalsMatchSteppingWithPlus(@ForAll("dates") GregorianDate start,
            @ForAll @IntRange(min = 0, max = 2_000) int length, @ForAll @IntRange(min = 1, max = 400) int step) {
        TimeQuantity stepDays = TimeQuantity.of(step, TimeUnits.DAY);
        GregorianDate end = start.next(TimeQuantity.of(length, TimeUnits.DAY));
        DiscreteInterval<GregorianDate> interval = start.to(end, stepDays);
        assertEquals(length / step + 1, interval.size());
        assertSteps(interval, stepDays, end);
    }

    @Property
    void yearIntervalsMatchSteppingWithPlus(@ForAll("years") GregorianYear start,
            @ForAll @IntRange(min = 0, max = 2_000) int length, @ForAll @IntRange(min = 1, max = 100) int step) {
        TimeQuantity stepYears = TimeQuantity.of(step, TimeUnits.YEAR);
        GregorianYear end = start.next(TimeQuantity.of(length, TimeUnits.YEAR));
        DiscreteInterval<GregorianYear> interval = DiscreteInterval.from(start, end, stepYears);
        assertEquals(length / step + 1, interval.size());
        assertSteps(interval, stepYears, end);
    }

    private static <T extends org.chalten.measure.Steppable<T>> void assertSteps(DiscreteInterval<T> interval,
            TimeQuantity step, T end) {
        T expected = interval.first();
        for (int index = 0; index < interval.size(); index++) {
            assertEquals(expected, interval.get(index));
            assertTrue(interval.rangeIncludes(expected));
            if (index > 0) {
                assertTrue(interval.get(index - 1).compareTo(interval.get(index)) < 0);
            }
            expected = expected.plus(step);
        }
        assertEquals(interval.get(interval.size() - 1), interval.last());
        assertTrue(interval.last().compareTo(end) <= 0);
        assertTrue(expected.compareTo(end) > 0);
    }
}
