package org.chalten.measure;

import java.util.Objects;

/** Port of Aconcagua's {@code MultipleUnitOfMeasure}: an exact multiple of another unit. */
public final class DerivedUnit implements TimeUnit {

    private final TimeUnit unit;
    private final String name;
    private final String pluralName;
    private final String symbol;
    private final Fraction factor;

    DerivedUnit(TimeUnit unit, String name, String pluralName, String symbol, Fraction factor) {
        this.unit = Objects.requireNonNull(unit);
        this.name = Objects.requireNonNull(name);
        this.pluralName = Objects.requireNonNull(pluralName);
        this.symbol = Objects.requireNonNull(symbol);
        this.factor = Objects.requireNonNull(factor);
        if (!factor.isStrictlyPositive()) {
            throw new InstanceCreationFailedException("Unit factors must be strictly positive");
        }
    }

    /** The unit this one is a multiple of (Aconcagua {@code baseUnit}, which may itself be derived). */
    public TimeUnit unit() {
        return unit;
    }

    /** How many {@link #unit()}s one of this unit is worth. */
    public Fraction factor() {
        return factor;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String pluralName() {
        return pluralName;
    }

    @Override
    public String symbol() {
        return symbol;
    }

    @Override
    public BaseUnit baseUnit() {
        return unit.baseUnit();
    }

    @Override
    public Fraction conversionFactor() {
        return unit.conversionFactor().times(factor);
    }

    /** Aconcagua {@code Unit>>printOn:} prints the symbol. */
    @Override
    public String toString() {
        return symbol;
    }
}
