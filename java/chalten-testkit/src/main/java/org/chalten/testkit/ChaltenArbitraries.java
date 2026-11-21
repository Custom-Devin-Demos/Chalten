package org.chalten.testkit;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.List;
import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Combinators;
import net.jqwik.api.Tuple;
import org.chalten.core.FixedOffsetRuleset;
import org.chalten.core.GregorianDate;
import org.chalten.core.GregorianDateTime;
import org.chalten.core.GregorianMonth;
import org.chalten.core.GregorianMonthOfYear;
import org.chalten.core.GregorianYear;
import org.chalten.core.Instant;
import org.chalten.core.TimeOfDay;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;

/**
 * Shared jqwik generators for the property-based invariants of {@code PLAN.md} §6. They weigh the edge cases of
 * calendar arithmetic (year 0, negative years, century and 400-year leap rules, leap days, month starts and ends)
 * well above their uniform frequency. Expected values come from {@code java.time}, never from the code under test.
 */
public final class ChaltenArbitraries {

    /** Proleptic years around the leap-year rule boundaries, including year 0 and negative years. */
    public static final List<Long> SPECIAL_YEARS = List.of(0L, -1L, 1L, -4L, 4L, -100L, 100L, -400L, 400L, 1582L,
            1600L, 1700L, 1900L, 1970L, 2000L, 2024L, 2100L, -9999L, 9999L);

    /** IANA zones with daylight-saving, half-hour and quarter-hour offsets, negative DST and skipped days. */
    public static final List<String> ZONE_IDS = List.of("America/New_York", "Europe/London", "Europe/Dublin",
            "America/Sao_Paulo", "America/St_Johns", "Asia/Kolkata", "Asia/Kathmandu", "Australia/Lord_Howe",
            "Pacific/Chatham", "Pacific/Apia", "Africa/Casablanca", "Antarctica/Troll", "UTC");

    /**
     * The first year whose dates (from any time of day) convert through Julian day numbers like Smalltalk does
     * correctly: {@code Number>>rounded} rounds negative Julian day numbers (before -4713-11-24T12:00) away from
     * zero, one day off. See {@code DIFFERENCES.md} S1.
     */
    public static final long FIRST_JULIAN_DAY_YEAR = -4712;

    /** The first year for which {@code GregorianDate>>dayOfWeek} is correct ({@code DIFFERENCES.md} S1). */
    public static final long FIRST_DAY_OF_WEEK_YEAR = -4715;

    private ChaltenArbitraries() {
    }

    /** Year numbers in [-10000, 10000], a fifth of them from {@link #SPECIAL_YEARS}. */
    public static Arbitrary<Long> yearNumbers() {
        return Arbitraries.frequencyOf(
                Tuple.of(4, Arbitraries.longs().between(-10_000, 10_000)),
                Tuple.of(1, Arbitraries.of(SPECIAL_YEARS)));
    }

    /** Year numbers over the whole {@code java.time.Year} range. */
    public static Arbitrary<Long> wideYearNumbers() {
        return Arbitraries.frequencyOf(
                Tuple.of(4, Arbitraries.longs().between(Year.MIN_VALUE, Year.MAX_VALUE)),
                Tuple.of(1, yearNumbers()));
    }

    /** {@link #yearNumbers()} from {@code minimum} on. */
    public static Arbitrary<Long> yearNumbersFrom(long minimum) {
        return Arbitraries.frequencyOf(
                Tuple.of(4, Arbitraries.longs().between(minimum, 10_000)),
                Tuple.of(1, Arbitraries.of(SPECIAL_YEARS.stream().filter(year -> year >= minimum).toList())));
    }

    public static Arbitrary<GregorianYear> years() {
        return yearNumbers().map(GregorianYear::number);
    }

    public static Arbitrary<GregorianMonthOfYear> monthsOfYear() {
        return Combinators.combine(yearNumbers(), Arbitraries.integers().between(1, 12))
                .as((year, month) -> GregorianMonthOfYear.year(GregorianYear.number(year),
                        GregorianMonth.monthAt(month)));
    }

    /** Dates whose day is uniform, the first, the last of its month, or a leap day, in equal parts. */
    public static Arbitrary<GregorianDate> dates() {
        return datesIn(yearNumbers());
    }

    /** {@link #dates()} from the year {@code minimum} on. */
    public static Arbitrary<GregorianDate> datesFrom(long minimum) {
        return datesIn(yearNumbersFrom(minimum));
    }

    private static Arbitrary<GregorianDate> datesIn(Arbitrary<Long> yearNumbers) {
        return Combinators.combine(yearNumbers, Arbitraries.integers().between(1, 12),
                Arbitraries.integers().between(1, 31), Arbitraries.integers().between(0, 3))
                .as(ChaltenArbitraries::date);
    }

    private static GregorianDate date(long year, int month, int day, int kind) {
        if (kind == 3) {
            long leapYear = year;
            while (!Year.isLeap(leapYear)) {
                leapYear++;
            }
            return GregorianDate.of(leapYear, 2, 29);
        }
        int length = YearMonth.of(Math.toIntExact(year), month).lengthOfMonth();
        int dayNumber = switch (kind) {
            case 0 -> Math.min(day, length);
            case 1 -> 1;
            default -> length;
        };
        return GregorianDate.of(year, month, dayNumber);
    }

    /** Times of day with microseconds, a third of them at midnight or one microsecond before it. */
    public static Arbitrary<TimeOfDay> timesOfDay() {
        return Arbitraries.frequencyOf(
                Tuple.of(4, Arbitraries.longs().between(0, 86_400_000_000L - 1)
                        .map(micros -> TimeOfDay.offsetFromStartOfDay(TimeQuantity.of(micros, TimeUnits.MICROSECOND)))),
                Tuple.of(1, Arbitraries.of(TimeOfDay.startOfDay(), TimeOfDay.hours(23, 59, 59, 999_999))));
    }

    /** Date-times from {@link #FIRST_JULIAN_DAY_YEAR} on. */
    public static Arbitrary<GregorianDateTime> dateTimes() {
        return Combinators.combine(datesFrom(FIRST_JULIAN_DAY_YEAR), timesOfDay()).as(GregorianDateTime::on);
    }

    /** Day counts in [-200000, 200000] (about ±547 years), weighted to 0, ±1 and year, 4-year and 400-year cycles. */
    public static Arbitrary<Long> dayCounts() {
        return Arbitraries.frequencyOf(
                Tuple.of(4, Arbitraries.longs().between(-200_000, 200_000)),
                Tuple.of(1, Arbitraries.of(0L, 1L, -1L, 28L, -28L, 365L, -365L, 366L, -366L, 1_461L, -1_461L,
                        36_524L, -36_524L, 146_097L, -146_097L)));
    }

    public static Arbitrary<TimeQuantity> days() {
        return dayCounts().map(count -> TimeQuantity.of(count, TimeUnits.DAY));
    }

    /** Month counts in [-2400, 2400], weighted to 0, ±1, ±11, ±12 and ±13. */
    public static Arbitrary<Long> monthCounts() {
        return Arbitraries.frequencyOf(
                Tuple.of(4, Arbitraries.longs().between(-2_400, 2_400)),
                Tuple.of(1, Arbitraries.of(0L, 1L, -1L, 11L, -11L, 12L, -12L, 13L, -13L)));
    }

    /** Fixed offsets over the whole ±16:00 range, half of them whole quarter hours. */
    public static Arbitrary<FixedOffsetRuleset> fixedOffsets() {
        return Arbitraries.frequencyOf(
                Tuple.of(1, Arbitraries.longs().between(-16 * 3_600, 16 * 3_600)),
                Tuple.of(1, Arbitraries.longs().between(-64, 64).map(quarters -> quarters * 900)))
                .map(seconds -> FixedOffsetRuleset.of(TimeQuantity.of(seconds, TimeUnits.SECOND)));
    }

    /** Instants with microsecond precision from {@link #FIRST_JULIAN_DAY_YEAR} to 9999, weighted to the epoch. */
    public static Arbitrary<Instant> instants() {
        long first = LocalDate.of((int) FIRST_JULIAN_DAY_YEAR, 1, 1).toEpochDay() * 86_400_000_000L;
        return Arbitraries.frequencyOf(
                Tuple.of(4, Arbitraries.longs().between(first, 253_402_300_799_999_999L)),
                Tuple.of(1, Arbitraries.longs().between(-86_400_000_000L, 86_400_000_000L)),
                Tuple.of(1, Arbitraries.of(0L, -1L, 1L)))
                .map(micros -> Instant.microsecondsSinceEpoch(TimeQuantity.of(micros, TimeUnits.MICROSECOND)));
    }

    /** Instants between 1850 and 2100, where the IANA rules of {@link #ZONE_IDS} change. */
    public static Arbitrary<Instant> modernInstants() {
        return Arbitraries.longs().between(-3_786_825_600_000_000L, 4_102_444_799_999_999L)
                .map(micros -> Instant.microsecondsSinceEpoch(TimeQuantity.of(micros, TimeUnits.MICROSECOND)));
    }

    public static Arbitrary<ZoneId> zones() {
        return Arbitraries.of(ZONE_IDS).map(ZoneId::of);
    }
}
