package org.chalten.measure;

import java.util.Objects;

/** Port of Aconcagua's {@code BaseUnitOfMeasure}: the canonical unit of its own dimension. */
public final class BaseUnit implements TimeUnit {

    private final String dimensionName;
    private final String dimensionSymbol;
    private final String name;
    private final String pluralName;
    private final String symbol;

    BaseUnit(String dimensionName, String dimensionSymbol, String name, String pluralName, String symbol) {
        this.dimensionName = Objects.requireNonNull(dimensionName);
        this.dimensionSymbol = Objects.requireNonNull(dimensionSymbol);
        this.name = Objects.requireNonNull(name);
        this.pluralName = Objects.requireNonNull(pluralName);
        this.symbol = Objects.requireNonNull(symbol);
    }

    public String dimensionName() {
        return dimensionName;
    }

    public String dimensionSymbol() {
        return dimensionSymbol;
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
        return this;
    }

    @Override
    public Fraction conversionFactor() {
        return Fraction.ONE;
    }

    /** Aconcagua {@code Unit>>printOn:} prints the symbol. */
    @Override
    public String toString() {
        return symbol;
    }
}
