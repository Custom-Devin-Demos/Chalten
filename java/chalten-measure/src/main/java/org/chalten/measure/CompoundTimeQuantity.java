package org.chalten.measure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Several time quantities kept together because they cannot be added into one, e.g. {@code 3 yr + 5 mo + 4 d}: the
 * port of Aconcagua's {@code MeasureBag}. It is what {@code ISO8601Parser parseDuration:allowingSeveralTimePeriods:}
 * returns and what {@link ISO8601DurationFormatter} formats.
 *
 * <p>{@link #parts()} keeps the quantities in the order they were given, without merging them; {@link #totals()}
 * merges the commensurable ones. Two compound quantities are equal when their non-zero totals are equal.
 *
 * @param parts the quantities, in the given order
 */
public record CompoundTimeQuantity(List<TimeQuantity> parts) {

    public CompoundTimeQuantity {
        parts = List.copyOf(parts);
    }

    public static CompoundTimeQuantity of(TimeQuantity... parts) {
        return new CompoundTimeQuantity(List.of(parts));
    }

    public static CompoundTimeQuantity of(Collection<TimeQuantity> parts) {
        return new CompoundTimeQuantity(List.copyOf(parts));
    }

    public CompoundTimeQuantity plus(TimeQuantity quantity) {
        List<TimeQuantity> sum = new ArrayList<>(parts);
        sum.add(Objects.requireNonNull(quantity));
        return new CompoundTimeQuantity(sum);
    }

    public CompoundTimeQuantity plus(CompoundTimeQuantity quantity) {
        List<TimeQuantity> sum = new ArrayList<>(parts);
        sum.addAll(quantity.parts);
        return new CompoundTimeQuantity(sum);
    }

    public CompoundTimeQuantity negated() {
        return new CompoundTimeQuantity(parts.stream().map(TimeQuantity::negated).toList());
    }

    public boolean isEmpty() {
        return parts.isEmpty();
    }

    public int size() {
        return parts.size();
    }

    public boolean isZero() {
        return parts.stream().allMatch(TimeQuantity::isZero);
    }

    /**
     * The sum of the commensurable parts, one quantity per base unit in order of first appearance, added with
     * {@link TimeQuantity#plus} semantics.
     */
    public List<TimeQuantity> totals() {
        List<TimeQuantity> totals = new ArrayList<>();
        for (TimeQuantity part : parts) {
            int index = indexOfCommensurable(totals, part);
            if (index < 0) {
                totals.add(part);
            } else {
                totals.set(index, totals.get(index).plus(part));
            }
        }
        return List.copyOf(totals);
    }

    /**
     * The sum of every part, expressed in {@code unit}.
     *
     * @throws ArithmeticException {@code Cannot convert incommensurable units} if any part is incommensurable
     */
    public TimeQuantity convertTo(TimeUnit unit) {
        TimeQuantity sum = TimeQuantity.of(0, unit);
        for (TimeQuantity part : parts) {
            sum = sum.plus(part.convertTo(unit));
        }
        return sum;
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof CompoundTimeQuantity other
                && new HashSet<>(nonZeroTotals()).equals(new HashSet<>(other.nonZeroTotals()));
    }

    @Override
    public int hashCode() {
        return new HashSet<>(nonZeroTotals()).hashCode();
    }

    /** The parts joined by {@code " + "}, e.g. {@code 3 yr + 5 mo + 4 d}; {@code 0} when empty. */
    @Override
    public String toString() {
        return parts.isEmpty() ? "0" : parts.stream().map(TimeQuantity::toString).collect(Collectors.joining(" + "));
    }

    private List<TimeQuantity> nonZeroTotals() {
        return totals().stream().filter(total -> !total.isZero()).toList();
    }

    private static int indexOfCommensurable(List<TimeQuantity> quantities, TimeQuantity quantity) {
        for (int i = 0; i < quantities.size(); i++) {
            if (quantities.get(i).isCommensurableWith(quantity)) {
                return i;
            }
        }
        return -1;
    }
}
