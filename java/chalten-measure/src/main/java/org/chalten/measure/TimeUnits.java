package org.chalten.measure;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * The time units Chalten uses: the absolute ones Aconcagua's {@code InternationalSystemOfUnits} defines, based on
 * the second, and the relative ones of {@code TimeUnits}, based on the year.
 *
 * <p>Relative units are calendar-dependant and never convert to absolute units: a month is not commensurable with
 * a day. Names, plurals and symbols (including the {@code millenium} spelling) are identical to the Smalltalk ones.
 */
public final class TimeUnits {

    public static final BaseUnit SECOND = TimeUnit.base("Time", "T", "second", "seconds", "s");
    public static final DerivedUnit MILLISECOND =
            TimeUnit.derived(SECOND, "millisecond", "milliseconds", "ms", Fraction.of(1, 1_000));
    public static final DerivedUnit MICROSECOND =
            TimeUnit.derived(SECOND, "microsecond", "microseconds", "µs", Fraction.of(1, 1_000_000));
    public static final DerivedUnit MINUTE = TimeUnit.derived(SECOND, "minute", "minutes", "min", Fraction.of(60));
    public static final DerivedUnit HOUR = TimeUnit.derived(SECOND, "hour", "hours", "h", Fraction.of(3_600));
    public static final DerivedUnit DAY = TimeUnit.derived(SECOND, "day", "days", "d", Fraction.of(86_400));
    public static final DerivedUnit WEEK = TimeUnit.derived(SECOND, "week", "weeks", "w", Fraction.of(604_800));

    public static final BaseUnit YEAR = TimeUnit.base("Relative time", "⏳", "year", "years", "yr");
    public static final DerivedUnit MONTH = TimeUnit.derived(YEAR, "month", "months", "mo", Fraction.of(1, 12));
    public static final DerivedUnit LUSTRUM = TimeUnit.derived(YEAR, "lustrum", "lustrums", "lus", Fraction.of(5));
    public static final DerivedUnit DECADE = TimeUnit.derived(YEAR, "decade", "decades", "dec", Fraction.of(10));
    public static final DerivedUnit JUBILEE = TimeUnit.derived(YEAR, "jubilee", "jubilees", "jub", Fraction.of(50));
    public static final DerivedUnit CENTURY = TimeUnit.derived(YEAR, "century", "centuries", "cent", Fraction.of(100));
    public static final DerivedUnit MILLENIUM =
            TimeUnit.derived(YEAR, "millenium", "milleniums", "mil", Fraction.of(1_000));

    private static final Map<String, TimeUnit> ABSOLUTE_UNITS = index(
            SECOND, MILLISECOND, MICROSECOND, MINUTE, HOUR, DAY, WEEK);
    private static final Map<String, TimeUnit> RELATIVE_UNITS = index(
            YEAR, MONTH, LUSTRUM, DECADE, JUBILEE, CENTURY, MILLENIUM);

    private TimeUnits() {
    }

    /** Smalltalk {@code TimeUnits units}: the relative units keyed by singular name. */
    public static Map<String, TimeUnit> units() {
        return RELATIVE_UNITS;
    }

    /** The absolute time units of {@code InternationalSystemOfUnits units} that Chalten uses, keyed by name. */
    public static Map<String, TimeUnit> absoluteUnits() {
        return ABSOLUTE_UNITS;
    }

    /**
     * Smalltalk {@code TimeUnits units >> #name} or {@code InternationalSystemOfUnits units >> #name}.
     *
     * @throws IllegalArgumentException if there is no such unit (Smalltalk {@code KeyNotFound})
     */
    public static TimeUnit named(String name) {
        return find(name).orElseThrow(() -> new IllegalArgumentException("key " + name + " not found"));
    }

    public static Optional<TimeUnit> find(String name) {
        TimeUnit unit = RELATIVE_UNITS.get(name);
        return Optional.ofNullable(unit != null ? unit : ABSOLUTE_UNITS.get(name));
    }

    private static Map<String, TimeUnit> index(TimeUnit... units) {
        Map<String, TimeUnit> map = new LinkedHashMap<>();
        for (TimeUnit unit : units) {
            map.put(unit.name(), unit);
        }
        return Collections.unmodifiableMap(map);
    }
}
