package org.chalten.measure;

import java.math.BigInteger;
import java.time.Duration;
import java.time.Period;
import java.util.Objects;

/**
 * An exact amount of a {@link TimeUnit}: the port of Aconcagua's {@code Quantity} (formerly {@code Measure}) as Chalten
 * uses it.
 *
 * <p>Quantities in commensurable units are equal when they measure the same time ({@code 1 h} equals {@code 60 min});
 * adding or subtracting them yields the unit of the receiver when both units are the same and the base unit
 * otherwise. Incommensurable quantities (for instance days and months) cannot be added, compared or converted; use
 * {@link CompoundTimeQuantity} to keep several of them together.
 *
 * @param amount the Smalltalk {@code numericalValue}
 * @param unit the unit
 */
public record TimeQuantity(Fraction amount, TimeUnit unit) implements Steppable<TimeQuantity> {

    static final String CANNOT_ADD = "Cannot add incommensurable quantities";
    static final String CANNOT_SUBTRACT = "Cannot subtract incommensurable quantities";
    static final String CANNOT_COMPARE = "Incommensurable units are not comparable";
    static final String CANNOT_CONVERT = "Cannot convert incommensurable units";

    public TimeQuantity {
        Objects.requireNonNull(amount, "amount");
        Objects.requireNonNull(unit, "unit");
    }

    public static TimeQuantity of(Fraction amount, TimeUnit unit) {
        return new TimeQuantity(amount, unit);
    }

    public static TimeQuantity of(long amount, TimeUnit unit) {
        return new TimeQuantity(Fraction.of(amount), unit);
    }

    /** The exact number of seconds in {@code duration}. */
    public static TimeQuantity of(Duration duration) {
        Fraction seconds = Fraction.of(duration.getSeconds())
                .plus(Fraction.of(duration.getNano(), 1_000_000_000L));
        return of(seconds, TimeUnits.SECOND);
    }

    /** Smalltalk {@code numericalValue}. */
    public Fraction numericalValue() {
        return amount;
    }

    /** Aconcagua {@code numericalValueInCanonicalUnit}. */
    public Fraction amountInBaseUnit() {
        return unit.valueInBaseUnit(amount);
    }

    public boolean isCommensurableWith(TimeUnit anotherUnit) {
        return unit.isCommensurableWith(anotherUnit);
    }

    public boolean isCommensurableWith(TimeQuantity quantity) {
        return isCommensurableWith(quantity.unit);
    }

    @Override
    public boolean isComparableTo(TimeQuantity quantity) {
        return isCommensurableWith(quantity);
    }

    /**
     * Smalltalk {@code +}.
     *
     * @throws ArithmeticException {@code Cannot add incommensurable quantities}
     */
    @Override
    public TimeQuantity plus(TimeQuantity addend) {
        if (unit == addend.unit) {
            return of(amount.plus(addend.amount), unit);
        }
        if (!isCommensurableWith(addend)) {
            throw new ArithmeticException(CANNOT_ADD);
        }
        return of(amountInBaseUnit().plus(addend.amountInBaseUnit()), unit.baseUnit());
    }

    /**
     * Smalltalk {@code -}.
     *
     * @throws ArithmeticException {@code Cannot subtract incommensurable quantities}
     */
    @Override
    public TimeQuantity minus(TimeQuantity subtrahend) {
        if (unit == subtrahend.unit) {
            return of(amount.minus(subtrahend.amount), unit);
        }
        if (!isCommensurableWith(subtrahend)) {
            throw new ArithmeticException(CANNOT_SUBTRACT);
        }
        return of(amountInBaseUnit().minus(subtrahend.amountInBaseUnit()), unit.baseUnit());
    }

    /** Smalltalk {@code * aNumber}. */
    public TimeQuantity times(Fraction multiplier) {
        return of(amount.times(multiplier), unit);
    }

    /** Smalltalk {@code * aNumber}. */
    public TimeQuantity times(long multiplier) {
        return times(Fraction.of(multiplier));
    }

    /** Smalltalk {@code / aNumber}; division by zero throws {@link ArithmeticException}. */
    public TimeQuantity dividedBy(Fraction divisor) {
        return of(amount.dividedBy(divisor), unit);
    }

    /** Smalltalk {@code / aNumber}; division by zero throws {@link ArithmeticException}. */
    public TimeQuantity dividedBy(long divisor) {
        return dividedBy(Fraction.of(divisor));
    }

    /**
     * Smalltalk {@code / aQuantity} for commensurable quantities: the dimensionless ratio, e.g.
     * {@code 3 d / 12 h = 6}.
     *
     * @throws ArithmeticException if the quantities are incommensurable or the divisor is zero
     */
    public Fraction dividedBy(TimeQuantity divisor) {
        return convertTo(divisor.unit).amount.dividedBy(divisor.amount);
    }

    public TimeQuantity negated() {
        return of(amount.negated(), unit);
    }

    public TimeQuantity abs() {
        return of(amount.abs(), unit);
    }

    /** Smalltalk {@code truncated}: the amount truncated towards zero, in the same unit. */
    public TimeQuantity truncated() {
        return of(Fraction.of(amount.truncated()), unit);
    }

    public boolean isZero() {
        return amount.isZero();
    }

    /** Smalltalk {@code negative}. */
    public boolean isNegative() {
        return amount.isNegative();
    }

    /** Smalltalk {@code positive}: zero or more. */
    public boolean isPositive() {
        return amount.isPositive();
    }

    /** Smalltalk {@code strictlyPositive}. */
    public boolean isStrictlyPositive() {
        return amount.isStrictlyPositive();
    }

    /**
     * Smalltalk {@code convertTo:}; returns this same quantity when it is already expressed in {@code targetUnit}.
     *
     * @throws ArithmeticException {@code Cannot convert incommensurable units}
     */
    public TimeQuantity convertTo(TimeUnit targetUnit) {
        if (unit == targetUnit) {
            return this;
        }
        if (!isCommensurableWith(targetUnit)) {
            throw new ArithmeticException(CANNOT_CONVERT);
        }
        return of(targetUnit.valueFromBaseUnit(amountInBaseUnit()), targetUnit);
    }

    /** Smalltalk {@code to:}: the interval stepping by one of this quantity's unit. */
    public DiscreteInterval<TimeQuantity> to(TimeQuantity end) {
        return to(end, of(1, unit));
    }

    /** Smalltalk {@code to:by:}. */
    public DiscreteInterval<TimeQuantity> to(TimeQuantity end, TimeQuantity step) {
        return DiscreteInterval.from(this, end, step);
    }

    /**
     * Converts an absolute quantity to a {@link Duration}.
     *
     * @throws ArithmeticException if the unit is relative or the amount is not a whole number of nanoseconds
     */
    public Duration toDuration() {
        Fraction nanos = convertTo(TimeUnits.SECOND).amount.times(1_000_000_000L);
        if (!nanos.isInteger()) {
            throw new ArithmeticException(this + " is not a whole number of nanoseconds");
        }
        BigInteger[] secondsAndNanos = nanos.numerator().divideAndRemainder(BigInteger.valueOf(1_000_000_000L));
        return Duration.ofSeconds(secondsAndNanos[0].longValueExact(), secondsAndNanos[1].longValue());
    }

    /**
     * Converts a relative quantity to a normalized {@link Period} of years and months.
     *
     * @throws ArithmeticException if the unit is absolute or the amount is not a whole number of months
     */
    public Period toPeriod() {
        Fraction months = convertTo(TimeUnits.MONTH).amount;
        if (!months.isInteger()) {
            throw new ArithmeticException(this + " is not a whole number of months");
        }
        return Period.ofMonths(months.intValueExact()).normalized();
    }

    /**
     * Compares commensurable quantities by the time they measure.
     *
     * @throws ArithmeticException {@code Incommensurable units are not comparable}
     */
    @Override
    public int compareTo(TimeQuantity other) {
        if (!isCommensurableWith(other)) {
            throw new ArithmeticException(CANNOT_COMPARE);
        }
        return amountInBaseUnit().compareTo(other.amountInBaseUnit());
    }

    public TimeQuantity min(TimeQuantity other) {
        return compareTo(other) <= 0 ? this : other;
    }

    public TimeQuantity max(TimeQuantity other) {
        return compareTo(other) >= 0 ? this : other;
    }

    /** Commensurable quantities measuring the same time are equal; incommensurable ones never are. */
    @Override
    public boolean equals(Object object) {
        return object instanceof TimeQuantity other
                && isCommensurableWith(other)
                && amountInBaseUnit().equals(other.amountInBaseUnit());
    }

    @Override
    public int hashCode() {
        return amountInBaseUnit().hashCode();
    }

    /** Smalltalk {@code printString}: the amount, a space and the unit symbol ({@code 365 d}, {@code (3/2) h}). */
    @Override
    public String toString() {
        return amount + " " + unit.symbol();
    }
}
