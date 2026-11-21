package org.chalten.measure;

/**
 * A value a {@link DiscreteInterval} can step over: the protocol Aconcagua's {@code DiscreteInterval} sends to its
 * bounds ({@code isComparableTo:}, {@code +}, {@code -} and ordering). Implemented by {@link TimeQuantity}, dates and
 * years.
 *
 * @param <T> the type of the values
 */
public interface Steppable<T extends Steppable<T>> extends Comparable<T> {

    /** Smalltalk {@code isComparableTo:}: whether {@code step} is commensurable with the distance between values. */
    boolean isComparableTo(TimeQuantity step);

    /** Smalltalk {@code + aQuantity}. */
    T plus(TimeQuantity quantity);

    /** Smalltalk {@code - anotherValue}: the distance from {@code other} to this value. */
    TimeQuantity minus(T other);
}
