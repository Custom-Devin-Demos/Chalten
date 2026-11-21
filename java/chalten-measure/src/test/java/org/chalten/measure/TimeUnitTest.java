package org.chalten.measure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class TimeUnitTest {

    @Test
    void absoluteUnitsMatchAconcagua() {
        assertUnit(TimeUnits.SECOND, "second", "seconds", "s", Fraction.ONE);
        assertUnit(TimeUnits.MILLISECOND, "millisecond", "milliseconds", "ms", Fraction.of(1, 1000));
        assertUnit(TimeUnits.MICROSECOND, "microsecond", "microseconds", "µs", Fraction.of(1, 1_000_000));
        assertUnit(TimeUnits.MINUTE, "minute", "minutes", "min", Fraction.of(60));
        assertUnit(TimeUnits.HOUR, "hour", "hours", "h", Fraction.of(3600));
        assertUnit(TimeUnits.DAY, "day", "days", "d", Fraction.of(86400));
        assertUnit(TimeUnits.WEEK, "week", "weeks", "w", Fraction.of(604800));
        assertThat(TimeUnits.SECOND.dimensionName()).isEqualTo("Time");
        assertThat(TimeUnits.SECOND.dimensionSymbol()).isEqualTo("T");
    }

    @Test
    void relativeUnitsMatchChalten() {
        assertUnit(TimeUnits.YEAR, "year", "years", "yr", Fraction.ONE);
        assertUnit(TimeUnits.MONTH, "month", "months", "mo", Fraction.of(1, 12));
        assertUnit(TimeUnits.LUSTRUM, "lustrum", "lustrums", "lus", Fraction.of(5));
        assertUnit(TimeUnits.DECADE, "decade", "decades", "dec", Fraction.of(10));
        assertUnit(TimeUnits.JUBILEE, "jubilee", "jubilees", "jub", Fraction.of(50));
        assertUnit(TimeUnits.CENTURY, "century", "centuries", "cent", Fraction.of(100));
        assertUnit(TimeUnits.MILLENIUM, "millenium", "milleniums", "mil", Fraction.of(1000));
        assertThat(TimeUnits.YEAR.dimensionName()).isEqualTo("Relative time");
        assertThat(TimeUnits.YEAR.dimensionSymbol()).isEqualTo("⏳");
    }

    @Test
    void absoluteAndRelativeUnitsAreIncommensurable() {
        for (TimeUnit absolute : TimeUnits.absoluteUnits().values()) {
            for (TimeUnit relative : TimeUnits.units().values()) {
                assertThat(absolute.isCommensurableWith(relative)).isFalse();
                assertThat(relative.isCommensurableWith(absolute)).isFalse();
            }
            for (TimeUnit other : TimeUnits.absoluteUnits().values()) {
                assertThat(absolute.isCommensurableWith(other)).isTrue();
            }
        }
    }

    @Test
    void looksUpUnitsByName() {
        assertThat(TimeUnits.units().keySet())
                .containsExactly("year", "month", "lustrum", "decade", "jubilee", "century", "millenium");
        assertThat(TimeUnits.absoluteUnits().keySet())
                .containsExactly("second", "millisecond", "microsecond", "minute", "hour", "day", "week");
        assertThat(TimeUnits.named("decade")).isSameAs(TimeUnits.DECADE);
        assertThat(TimeUnits.named("week")).isSameAs(TimeUnits.WEEK);
        assertThat(TimeUnits.find("fortnight")).isEmpty();
        assertThatThrownBy(() -> TimeUnits.named("fortnight")).isInstanceOf(IllegalArgumentException.class)
                .hasMessage("key fortnight not found");
        assertThatThrownBy(() -> TimeUnits.units().put("x", TimeUnits.DAY))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void calendarsCanDefineTheirOwnUnits() {
        BaseUnit hebrewYear = TimeUnit.base("Hebrew years", "HY", "hebrew year", "hebrew years", "hyr");
        DerivedUnit twoYears = TimeUnit.derived(hebrewYear, "biennium", "bienniums", "bi", Fraction.of(2));
        DerivedUnit fourYears = TimeUnit.derived(twoYears, "quadrennium", "quadrenniums", "qu", Fraction.of(2));
        assertThat(fourYears.unit()).isSameAs(twoYears);
        assertThat(fourYears.factor()).isEqualTo(Fraction.of(2));
        assertThat(fourYears.baseUnit()).isSameAs(hebrewYear);
        assertThat(fourYears.conversionFactor()).isEqualTo(Fraction.of(4));
        assertThat(hebrewYear.isCommensurableWith(TimeUnits.YEAR)).isFalse();
        assertThat(fourYears.times(1)).isEqualTo(hebrewYear.times(4));
        assertThat(fourYears.valueFromBaseUnit(Fraction.of(2))).isEqualTo(Fraction.of(1, 2));
        assertThatThrownBy(() -> TimeUnit.derived(hebrewYear, "none", "nones", "n", Fraction.ZERO))
                .isInstanceOf(InstanceCreationFailedException.class)
                .hasMessage("Unit factors must be strictly positive");
    }

    @Test
    void printsItsSymbol() {
        assertThat(TimeUnits.DAY).hasToString("d");
        assertThat(TimeUnits.YEAR).hasToString("yr");
    }

    private static void assertUnit(TimeUnit unit, String name, String plural, String symbol, Fraction factor) {
        assertThat(unit.name()).isEqualTo(name);
        assertThat(unit.pluralName()).isEqualTo(plural);
        assertThat(unit.symbol()).isEqualTo(symbol);
        assertThat(unit.conversionFactor()).isEqualTo(factor);
        assertThat(unit.valueInBaseUnit(Fraction.ONE)).isEqualTo(factor);
        assertThat(TimeUnits.named(name)).isSameAs(unit);
    }
}
