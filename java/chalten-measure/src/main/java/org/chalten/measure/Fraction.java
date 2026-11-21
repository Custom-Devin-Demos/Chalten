package org.chalten.measure;

import java.io.Serial;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Objects;

/**
 * An exact, always normalized rational number: the port of Smalltalk's {@code Integer}/{@code Fraction} arithmetic.
 *
 * <p>The denominator is always strictly positive and coprime with the numerator, so two equal values always have the
 * same representation. Integers are fractions with denominator one. Arithmetic never rounds.
 */
public final class Fraction extends Number implements Comparable<Fraction> {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final Fraction ZERO = new Fraction(BigInteger.ZERO, BigInteger.ONE);
    public static final Fraction ONE = new Fraction(BigInteger.ONE, BigInteger.ONE);

    private final BigInteger numerator;
    private final BigInteger denominator;

    private Fraction(BigInteger numerator, BigInteger denominator) {
        this.numerator = numerator;
        this.denominator = denominator;
    }

    public static Fraction of(long integer) {
        return of(BigInteger.valueOf(integer));
    }

    public static Fraction of(BigInteger integer) {
        return new Fraction(Objects.requireNonNull(integer), BigInteger.ONE);
    }

    public static Fraction of(long numerator, long denominator) {
        return of(BigInteger.valueOf(numerator), BigInteger.valueOf(denominator));
    }

    /**
     * Returns {@code numerator / denominator}, normalized.
     *
     * @throws ArithmeticException if {@code denominator} is zero (Smalltalk's {@code ZeroDivide})
     */
    public static Fraction of(BigInteger numerator, BigInteger denominator) {
        Objects.requireNonNull(numerator);
        Objects.requireNonNull(denominator);
        if (denominator.signum() == 0) {
            throw new ArithmeticException("Division by zero");
        }
        if (denominator.signum() < 0) {
            numerator = numerator.negate();
            denominator = denominator.negate();
        }
        BigInteger gcd = numerator.gcd(denominator);
        if (!gcd.equals(BigInteger.ONE)) {
            numerator = numerator.divide(gcd);
            denominator = denominator.divide(gcd);
        }
        return new Fraction(numerator, denominator);
    }

    /** Returns the exact value of {@code decimal}. */
    public static Fraction of(BigDecimal decimal) {
        BigDecimal stripped = decimal.stripTrailingZeros();
        return stripped.scale() <= 0
                ? of(stripped.toBigIntegerExact())
                : of(stripped.unscaledValue(), BigInteger.TEN.pow(stripped.scale()));
    }

    /**
     * Parses an integer ({@code "-3"}), a fraction ({@code "3/2"}, optionally parenthesized as Smalltalk prints it) or a
     * decimal literal ({@code "1.52"}, read exactly as {@code 38/25}).
     */
    public static Fraction parse(String text) {
        String trimmed = text.strip();
        if (trimmed.startsWith("(") && trimmed.endsWith(")")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1).strip();
        }
        int slash = trimmed.indexOf('/');
        try {
            if (slash >= 0) {
                return of(new BigInteger(trimmed.substring(0, slash).strip()),
                        new BigInteger(trimmed.substring(slash + 1).strip()));
            }
            return of(new BigDecimal(trimmed));
        } catch (NumberFormatException e) {
            throw new NumberFormatException("Not a number: " + text);
        }
    }

    public BigInteger numerator() {
        return numerator;
    }

    public BigInteger denominator() {
        return denominator;
    }

    public boolean isInteger() {
        return denominator.equals(BigInteger.ONE);
    }

    public boolean isZero() {
        return numerator.signum() == 0;
    }

    /** Smalltalk {@code negative}: strictly less than zero. */
    public boolean isNegative() {
        return numerator.signum() < 0;
    }

    /** Smalltalk {@code positive}: greater than or equal to zero. */
    public boolean isPositive() {
        return numerator.signum() >= 0;
    }

    /** Smalltalk {@code strictlyPositive}. */
    public boolean isStrictlyPositive() {
        return numerator.signum() > 0;
    }

    public int signum() {
        return numerator.signum();
    }

    public Fraction plus(Fraction addend) {
        if (isInteger() && addend.isInteger()) {
            return new Fraction(numerator.add(addend.numerator), BigInteger.ONE);
        }
        return of(numerator.multiply(addend.denominator).add(addend.numerator.multiply(denominator)),
                denominator.multiply(addend.denominator));
    }

    public Fraction plus(long addend) {
        return plus(of(addend));
    }

    public Fraction minus(Fraction subtrahend) {
        return plus(subtrahend.negated());
    }

    public Fraction minus(long subtrahend) {
        return minus(of(subtrahend));
    }

    public Fraction times(Fraction multiplier) {
        return of(numerator.multiply(multiplier.numerator), denominator.multiply(multiplier.denominator));
    }

    public Fraction times(long multiplier) {
        return times(of(multiplier));
    }

    /**
     * Exact division.
     *
     * @throws ArithmeticException if {@code divisor} is zero
     */
    public Fraction dividedBy(Fraction divisor) {
        return of(numerator.multiply(divisor.denominator), denominator.multiply(divisor.numerator));
    }

    public Fraction dividedBy(long divisor) {
        return dividedBy(of(divisor));
    }

    public Fraction negated() {
        return new Fraction(numerator.negate(), denominator);
    }

    public Fraction abs() {
        return isNegative() ? negated() : this;
    }

    public Fraction reciprocal() {
        return of(denominator, numerator);
    }

    /** Smalltalk {@code truncated}: the integer nearest to zero. */
    public BigInteger truncated() {
        return numerator.divide(denominator);
    }

    /** Smalltalk {@code floor}. */
    public BigInteger floor() {
        BigInteger[] qr = numerator.divideAndRemainder(denominator);
        return qr[1].signum() < 0 ? qr[0].subtract(BigInteger.ONE) : qr[0];
    }

    /** Smalltalk {@code ceiling}. */
    public BigInteger ceiling() {
        return isInteger() ? numerator : floor().add(BigInteger.ONE);
    }

    /** Smalltalk {@code //}: the quotient rounded towards negative infinity. */
    public BigInteger floorDiv(Fraction divisor) {
        return dividedBy(divisor).floor();
    }

    /** Smalltalk {@code \\}: the modulo, with the sign of {@code divisor}. */
    public Fraction floorMod(Fraction divisor) {
        return minus(divisor.times(of(floorDiv(divisor))));
    }

    /** Smalltalk {@code quo:}: the quotient truncated towards zero. */
    public BigInteger truncatedDiv(Fraction divisor) {
        return dividedBy(divisor).truncated();
    }

    /** Smalltalk {@code rem:}: the remainder, with the sign of the receiver. */
    public Fraction remainder(Fraction divisor) {
        return minus(divisor.times(of(truncatedDiv(divisor))));
    }

    public Fraction min(Fraction other) {
        return compareTo(other) <= 0 ? this : other;
    }

    public Fraction max(Fraction other) {
        return compareTo(other) >= 0 ? this : other;
    }

    /** Returns the integer value, failing if this is not an integer or does not fit in a {@code long}. */
    public long longValueExact() {
        if (!isInteger()) {
            throw new ArithmeticException(this + " is not an integer");
        }
        return numerator.longValueExact();
    }

    /** Returns the integer value, failing if this is not an integer or does not fit in an {@code int}. */
    public int intValueExact() {
        if (!isInteger()) {
            throw new ArithmeticException(this + " is not an integer");
        }
        return numerator.intValueExact();
    }

    /** Returns the exact decimal expansion, failing if it does not terminate. */
    public BigDecimal toBigDecimalExact() {
        return new BigDecimal(numerator).divide(new BigDecimal(denominator));
    }

    @Override
    public int intValue() {
        return truncated().intValue();
    }

    @Override
    public long longValue() {
        return truncated().longValue();
    }

    @Override
    public float floatValue() {
        return (float) doubleValue();
    }

    /** The correctly rounded nearest {@code double} (Smalltalk {@code asFloat}); only used for display. */
    @Override
    public double doubleValue() {
        if (isInteger()) {
            return numerator.doubleValue();
        }
        BigInteger a = numerator.abs();
        int shift = 55 - (a.bitLength() - denominator.bitLength());
        BigInteger[] qr = shift >= 0
                ? a.shiftLeft(shift).divideAndRemainder(denominator)
                : a.divideAndRemainder(denominator.shiftLeft(-shift));
        BigInteger q = qr[0].shiftLeft(1);
        if (qr[1].signum() != 0) {
            q = q.setBit(0);
        }
        double magnitude = Math.scalb(q.doubleValue(), -(shift + 1));
        return numerator.signum() < 0 ? -magnitude : magnitude;
    }

    @Override
    public int compareTo(Fraction other) {
        if (isInteger() && other.isInteger()) {
            return numerator.compareTo(other.numerator);
        }
        return numerator.multiply(other.denominator).compareTo(other.numerator.multiply(denominator));
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof Fraction other
                && numerator.equals(other.numerator) && denominator.equals(other.denominator);
    }

    @Override
    public int hashCode() {
        return 31 * numerator.hashCode() + denominator.hashCode();
    }

    /** Smalltalk {@code printString}: {@code 3}, {@code -3}, {@code (3/2)}, {@code (-3/2)}. */
    @Override
    public String toString() {
        return isInteger() ? numerator.toString() : "(" + numerator + "/" + denominator + ")";
    }
}
