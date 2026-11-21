package org.chalten.measure;

import java.util.AbstractList;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;
import java.util.function.Predicate;

/**
 * An immutable, finite, inclusive sequence {@code start, start + step, ...} that never goes past {@code end}: the port
 * of Aconcagua's {@code DiscreteInterval from:to:by:}.
 *
 * <p>The size is {@code ((end - start) / step) truncated + 1}; creation fails when the step is incommensurable with
 * the values, zero, or points away from {@code end}. Java indices are zero-based ({@code get(0)} is Smalltalk's
 * {@code at: 1}).
 *
 * @param <T> the type of the values
 */
public final class DiscreteInterval<T extends Steppable<T>> extends AbstractList<T> implements RandomAccess {

    static final String INCOMMENSURABLE = "Cannot create an interval with incommensurable quantities";
    static final String INVALID_STEP = "Cannot create an interval with an invalid step";
    static final String INDEX_OUT_OF_RANGE = "Index out of range";

    private final T start;
    private final T end;
    private final TimeQuantity step;
    private final int count;

    private DiscreteInterval(T start, T end, TimeQuantity step, int count) {
        this.start = start;
        this.end = end;
        this.step = step;
        this.count = count;
    }

    /**
     * Smalltalk {@code DiscreteInterval from:to:by:}.
     *
     * @throws ArithmeticException {@code Cannot create an interval with incommensurable quantities}
     * @throws InstanceCreationFailedException {@code Cannot create an interval with an invalid step} when the step is
     *         zero or no element fits between {@code start} and {@code end}
     */
    public static <T extends Steppable<T>> DiscreteInterval<T> from(T start, T end, TimeQuantity step) {
        Objects.requireNonNull(start, "start");
        Objects.requireNonNull(end, "end");
        Objects.requireNonNull(step, "step");
        if (!start.isComparableTo(step)) {
            throw new ArithmeticException(INCOMMENSURABLE);
        }
        if (step.isZero()) {
            throw new InstanceCreationFailedException(INVALID_STEP);
        }
        Fraction count = Fraction.of(end.minus(start).dividedBy(step).truncated()).plus(1);
        if (!count.isStrictlyPositive()) {
            throw new InstanceCreationFailedException(INVALID_STEP);
        }
        if (count.compareTo(Fraction.of(Integer.MAX_VALUE)) > 0) {
            throw new ArithmeticException("Interval too large: " + count + " elements");
        }
        return new DiscreteInterval<>(start, end, step, count.intValueExact());
    }

    public T start() {
        return start;
    }

    /** The upper bound given at creation, which is not necessarily an element (see {@link #last()}). */
    public T end() {
        return end;
    }

    public TimeQuantity step() {
        return step;
    }

    public T first() {
        return start;
    }

    public T last() {
        return get(count - 1);
    }

    @Override
    public int size() {
        return count;
    }

    /**
     * Smalltalk {@code at: index + 1}.
     *
     * @throws IndexOutOfBoundsException {@code Index out of range}
     */
    @Override
    public T get(int index) {
        if (index < 0 || index >= count) {
            throw new IndexOutOfBoundsException(INDEX_OUT_OF_RANGE);
        }
        return start.plus(step.times(index));
    }

    /**
     * Aconcagua {@code rangeIncludes:}: whether {@code value} lies between the bounds, regardless of the step.
     */
    public boolean rangeIncludes(T value) {
        return step.isPositive()
                ? value.compareTo(start) >= 0 && value.compareTo(end) <= 0
                : value.compareTo(end) >= 0 && value.compareTo(start) <= 0;
    }

    @Override
    @SuppressWarnings("unchecked")
    public int indexOf(Object object) {
        if (!(object instanceof Steppable<?>)) {
            return -1;
        }
        try {
            T value = (T) object;
            if (!rangeIncludes(value)) {
                return -1;
            }
            Fraction index = value.minus(start).dividedBy(step);
            return index.isInteger() && index.compareTo(Fraction.of(count)) < 0 ? index.intValueExact() : -1;
        } catch (ClassCastException | ArithmeticException incomparable) {
            return -1;
        }
    }

    @Override
    public int lastIndexOf(Object object) {
        return indexOf(object);
    }

    @Override
    public boolean contains(Object object) {
        return indexOf(object) >= 0;
    }

    /** Smalltalk {@code reject:}. */
    public List<T> reject(Predicate<? super T> predicate) {
        return stream().filter(predicate.negate()).toList();
    }

    /** Smalltalk {@code select:}. */
    public List<T> select(Predicate<? super T> predicate) {
        return stream().filter(predicate).toList();
    }
}
