package org.chalten.measure;

/**
 * A unit of time: the port of the Aconcagua units Chalten uses ({@code BaseUnitOfMeasure} and
 * {@code MultipleUnitOfMeasure}).
 *
 * <p>Units are compared by identity, as in Aconcagua. Two units are commensurable when they share the same
 * {@linkplain #baseUnit() base unit}: every absolute unit is based on the second, every relative unit on the year.
 * Calendars that define their own base units (Hebrew, Islamic) create them with {@link #base}.
 */
public sealed interface TimeUnit permits BaseUnit, DerivedUnit {

    /** Creates a new base unit measuring its own dimension, e.g. the relative {@code year}. */
    static BaseUnit base(String dimensionName, String dimensionSymbol, String name, String pluralName, String symbol) {
        return new BaseUnit(dimensionName, dimensionSymbol, name, pluralName, symbol);
    }

    /** Creates a unit worth {@code factor} times {@code unit}, e.g. {@code month = 1/12 year}. */
    static DerivedUnit derived(TimeUnit unit, String name, String pluralName, String symbol, Fraction factor) {
        return new DerivedUnit(unit, name, pluralName, symbol, factor);
    }

    /** Aconcagua {@code nameForOne}, e.g. {@code day}. */
    String name();

    /** Aconcagua {@code nameForMany}, e.g. {@code days}. */
    String pluralName();

    /** Aconcagua {@code symbol}, e.g. {@code d}. */
    String symbol();

    /** Aconcagua {@code asCanonicalUnit}: the base unit this unit is expressed in. */
    BaseUnit baseUnit();

    /** How many {@linkplain #baseUnit() base units} one of this unit is worth (one for base units). */
    Fraction conversionFactor();

    default boolean isCommensurableWith(TimeUnit unit) {
        return baseUnit() == unit.baseUnit();
    }

    /** Aconcagua {@code valueInCanonicalUnit:}. */
    default Fraction valueInBaseUnit(Fraction amount) {
        return amount.times(conversionFactor());
    }

    /** Aconcagua {@code valueFromCanonicalUnit:}. */
    default Fraction valueFromBaseUnit(Fraction amountInBaseUnit) {
        return amountInBaseUnit.dividedBy(conversionFactor());
    }

    /** Smalltalk {@code amount * unit}. */
    default TimeQuantity times(Fraction amount) {
        return TimeQuantity.of(amount, this);
    }

    /** Smalltalk {@code amount * unit}. */
    default TimeQuantity times(long amount) {
        return TimeQuantity.of(amount, this);
    }
}
