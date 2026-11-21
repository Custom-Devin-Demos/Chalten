package org.chalten.measure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.chalten.measure.TimeUnits.DAY;
import static org.chalten.measure.TimeUnits.HOUR;
import static org.chalten.measure.TimeUnits.MONTH;
import static org.chalten.measure.TimeUnits.YEAR;

import java.util.List;
import org.junit.jupiter.api.Test;

class DiscreteIntervalTest {

    /** A date-like value: the day number since some epoch, stepped in absolute units like {@code CalendarDate}. */
    record DayNumber(long number) implements Steppable<DayNumber> {

        @Override
        public boolean isComparableTo(TimeQuantity step) {
            return step.isCommensurableWith(DAY);
        }

        @Override
        public DayNumber plus(TimeQuantity quantity) {
            return new DayNumber(number + quantity.convertTo(DAY).amount().longValueExact());
        }

        @Override
        public TimeQuantity minus(DayNumber other) {
            return DAY.times(number - other.number);
        }

        @Override
        public int compareTo(DayNumber other) {
            return Long.compare(number, other.number);
        }
    }

    /** A year-like value stepped in relative units like {@code CalendarYear}. */
    record YearNumber(long number) implements Steppable<YearNumber> {

        @Override
        public boolean isComparableTo(TimeQuantity step) {
            return step.isCommensurableWith(YEAR);
        }

        @Override
        public YearNumber plus(TimeQuantity quantity) {
            return new YearNumber(number + quantity.convertTo(YEAR).amount().longValueExact());
        }

        @Override
        public TimeQuantity minus(YearNumber other) {
            return YEAR.times(number - other.number);
        }

        @Override
        public int compareTo(YearNumber other) {
            return Long.compare(number, other.number);
        }
    }

    @Test
    void stepsLikeCalendarDateToBy() {
        DiscreteInterval<DayNumber> interval =
                DiscreteInterval.from(new DayNumber(1), new DayNumber(30), DAY.times(2));
        assertThat(interval).hasSize(15);
        assertThat(interval.first()).isEqualTo(new DayNumber(1));
        assertThat(interval.last()).isEqualTo(new DayNumber(29));
        assertThat(interval.end()).isEqualTo(new DayNumber(30));
        assertThat(interval.get(1)).isEqualTo(new DayNumber(3));
        assertThat(interval.step()).isEqualTo(DAY.times(2));
        assertThat(interval.start()).isEqualTo(new DayNumber(1));
    }

    @Test
    void stepsWithAnyCommensurableUnit() {
        DiscreteInterval<DayNumber> weeks = DiscreteInterval.from(new DayNumber(0), new DayNumber(14),
                TimeUnits.WEEK.times(1));
        assertThat(weeks).containsExactly(new DayNumber(0), new DayNumber(7), new DayNumber(14));
        assertThat(DiscreteInterval.from(new DayNumber(0), new DayNumber(1), HOUR.times(24))).hasSize(2);
    }

    @Test
    void stepsLikeCalendarYearToBy() {
        DiscreteInterval<YearNumber> interval =
                DiscreteInterval.from(new YearNumber(2006), new YearNumber(2026), YEAR.times(2));
        assertThat(interval).hasSize(11);
        assertThat(interval.last()).isEqualTo(new YearNumber(2026));
        assertThat(DiscreteInterval.from(new YearNumber(2000), new YearNumber(2100), TimeUnits.DECADE.times(1)))
                .hasSize(11);
    }

    @Test
    void stepsBackwards() {
        DiscreteInterval<DayNumber> interval =
                DiscreteInterval.from(new DayNumber(10), new DayNumber(1), DAY.times(-3));
        assertThat(interval).containsExactly(new DayNumber(10), new DayNumber(7), new DayNumber(4),
                new DayNumber(1));
        assertThat(interval.rangeIncludes(new DayNumber(5))).isTrue();
        assertThat(interval.rangeIncludes(new DayNumber(11))).isFalse();
        assertThat(interval.indexOf(new DayNumber(4))).isEqualTo(2);
        assertThat(interval.contains(new DayNumber(5))).isFalse();
    }

    @Test
    void singleElementIntervals() {
        assertThat(DiscreteInterval.from(new DayNumber(5), new DayNumber(5), DAY.times(1)))
                .containsExactly(new DayNumber(5));
        assertThat(DiscreteInterval.from(new DayNumber(5), new DayNumber(6), DAY.times(7)))
                .containsExactly(new DayNumber(5));
    }

    @Test
    void cannotBeCreatedWithIncommensurableSteps() {
        assertThatThrownBy(() -> DiscreteInterval.from(new DayNumber(1), new DayNumber(30), MONTH.times(1)))
                .isInstanceOf(ArithmeticException.class)
                .hasMessage("Cannot create an interval with incommensurable quantities");
        assertThatThrownBy(() -> DiscreteInterval.from(new YearNumber(1), new YearNumber(3), DAY.times(1)))
                .isInstanceOf(ArithmeticException.class)
                .hasMessage("Cannot create an interval with incommensurable quantities");
    }

    @Test
    void cannotBeCreatedWithInvalidSteps() {
        assertThatThrownBy(() -> DiscreteInterval.from(new DayNumber(1), new DayNumber(30), DAY.times(0)))
                .isInstanceOf(InstanceCreationFailedException.class)
                .hasMessage("Cannot create an interval with an invalid step");
        assertThatThrownBy(() -> DiscreteInterval.from(new DayNumber(30), new DayNumber(1), DAY.times(1)))
                .isInstanceOf(InstanceCreationFailedException.class)
                .hasMessage("Cannot create an interval with an invalid step");
        assertThatThrownBy(() -> DiscreteInterval.from(new DayNumber(1), new DayNumber(30), DAY.times(-1)))
                .isInstanceOf(InstanceCreationFailedException.class)
                .hasMessage("Cannot create an interval with an invalid step");
    }

    @Test
    void rejectsTooLargeIntervals() {
        assertThatThrownBy(() -> DiscreteInterval.from(new DayNumber(0), new DayNumber(Integer.MAX_VALUE + 1L),
                DAY.times(1))).isInstanceOf(ArithmeticException.class)
                .hasMessage("Interval too large: 2147483649 elements");
    }

    @Test
    void indexesFromZero() {
        DiscreteInterval<DayNumber> interval =
                DiscreteInterval.from(new DayNumber(1), new DayNumber(5), DAY.times(1));
        assertThatThrownBy(() -> interval.get(5)).isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessage("Index out of range");
        assertThatThrownBy(() -> interval.get(-1)).isInstanceOf(IndexOutOfBoundsException.class)
                .hasMessage("Index out of range");
        assertThat(interval.indexOf(new DayNumber(1))).isZero();
        assertThat(interval.lastIndexOf(new DayNumber(5))).isEqualTo(4);
        assertThat(interval.indexOf(new DayNumber(6))).isEqualTo(-1);
        assertThat(interval.indexOf("a day")).isEqualTo(-1);
        assertThat(interval.indexOf(new YearNumber(2))).isEqualTo(-1);
    }

    @Test
    void findsOnlyActualElements() {
        DiscreteInterval<DayNumber> interval =
                DiscreteInterval.from(new DayNumber(1), new DayNumber(10), DAY.times(3));
        assertThat(interval).containsExactly(new DayNumber(1), new DayNumber(4), new DayNumber(7),
                new DayNumber(10));
        assertThat(interval.rangeIncludes(new DayNumber(2))).isTrue();
        assertThat(interval.contains(new DayNumber(2))).isFalse();
        assertThat(interval.contains(new DayNumber(7))).isTrue();
        assertThat(interval.contains(new DayNumber(13))).isFalse();
    }

    @Test
    void stepsOverQuantities() {
        DiscreteInterval<TimeQuantity> interval = DAY.times(1).to(DAY.times(2), HOUR.times(12));
        assertThat(interval).containsExactly(DAY.times(1), HOUR.times(36), DAY.times(2));
        assertThat(interval.get(1)).hasToString("129600 s");
        assertThat(interval.contains(HOUR.times(36))).isTrue();
        assertThat(interval.contains(MONTH.times(1))).isFalse();
        assertThatThrownBy(() -> DAY.times(1).to(MONTH.times(2))).isInstanceOf(ArithmeticException.class)
                .hasMessage("Cannot subtract incommensurable quantities");
    }

    @Test
    void behavesAsAnImmutableList() {
        DiscreteInterval<DayNumber> interval =
                DiscreteInterval.from(new DayNumber(1), new DayNumber(4), DAY.times(1));
        assertThat(interval).isEqualTo(List.of(new DayNumber(1), new DayNumber(2), new DayNumber(3),
                new DayNumber(4)));
        assertThat(interval.reject(day -> day.number() % 2 == 0))
                .containsExactly(new DayNumber(1), new DayNumber(3));
        assertThat(interval.select(day -> day.number() % 2 == 0))
                .containsExactly(new DayNumber(2), new DayNumber(4));
        assertThat(interval.stream().mapToLong(DayNumber::number).sum()).isEqualTo(10);
        assertThatThrownBy(() -> interval.add(new DayNumber(5))).isInstanceOf(UnsupportedOperationException.class);
        assertThatThrownBy(() -> interval.set(0, new DayNumber(5)))
                .isInstanceOf(UnsupportedOperationException.class);
    }
}
