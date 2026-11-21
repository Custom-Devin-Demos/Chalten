package org.chalten.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.Year;
import java.time.YearMonth;
import java.util.Set;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;
import org.chalten.testkit.ChaltenArbitraries;

/** PLAN.md §6 invariants of Gregorian dates, months of year and years, checked against {@code java.time}. */
class GregorianCalendarPropertiesTest {

    @Provide
    Arbitrary<GregorianDate> dates() {
        return ChaltenArbitraries.dates();
    }

    @Provide
    Arbitrary<GregorianDate> datesWithAKnownDayOfWeek() {
        return ChaltenArbitraries.datesFrom(ChaltenArbitraries.FIRST_DAY_OF_WEEK_YEAR);
    }

    @Provide
    Arbitrary<Long> dayCounts() {
        return ChaltenArbitraries.dayCounts();
    }

    @Provide
    Arbitrary<Long> monthCounts() {
        return ChaltenArbitraries.monthCounts();
    }

    @Provide
    Arbitrary<GregorianMonthOfYear> monthsOfYear() {
        return ChaltenArbitraries.monthsOfYear();
    }

    @Provide
    Arbitrary<Long> yearNumbers() {
        return ChaltenArbitraries.yearNumbers();
    }

    @Provide
    Arbitrary<Long> wideYearNumbers() {
        return ChaltenArbitraries.wideYearNumbers();
    }

    private static TimeQuantity days(long count) {
        return TimeQuantity.of(count, TimeUnits.DAY);
    }

    private static TimeQuantity months(long count) {
        return TimeQuantity.of(count, TimeUnits.MONTH);
    }

    @Property
    void nextAndPreviousDaysAreInverse(@ForAll("dates") GregorianDate date, @ForAll("dayCounts") long count) {
        assertEquals(date, date.next(days(count)).previous(days(count)));
        assertEquals(date, date.previous(days(count)).next(days(count)));
    }

    @Property
    void subtractingADateAnswersTheDaysAdded(@ForAll("dates") GregorianDate date, @ForAll("dayCounts") long count) {
        long n = Math.abs(count);
        assertEquals(days(n), date.next(days(n)).minus(date).convertTo(TimeUnits.DAY));
        assertEquals(days(n), date.minus(date.previous(days(n))).convertTo(TimeUnits.DAY));
    }

    @Property
    void nextDaysAgreesWithLocalDatePlusDays(@ForAll("dates") GregorianDate date, @ForAll("dayCounts") long count) {
        assertEquals(date.toLocalDate().plusDays(count), date.next(days(count)).toLocalDate());
        assertEquals(date.toLocalDate().minusDays(count), date.previous(days(count)).toLocalDate());
    }

    @Property
    void nextIsAdditive(@ForAll("dates") GregorianDate date, @ForAll("dayCounts") long a,
            @ForAll("dayCounts") long b) {
        assertEquals(date.next(days(a + b)), date.next(days(a)).next(days(b)));
        assertEquals(date.next(days(a).plus(days(b))), date.next(days(a)).next(days(b)));
    }

    @Property
    void nextOfANegativeQuantityIsPrevious(@ForAll("dates") GregorianDate date, @ForAll("dayCounts") long count) {
        assertEquals(date.previous(days(count)), date.next(days(count).negated()));
        assertEquals(date.next(days(count)), date.previous(days(count).negated()));
    }

    @Property
    void localDateRoundTrips(@ForAll("dates") GregorianDate date) {
        LocalDate localDate = date.toLocalDate();
        assertEquals(date.yearNumber(), localDate.getYear());
        assertEquals(date.monthNumber(), localDate.getMonthValue());
        assertEquals(date.dayNumber(), localDate.getDayOfMonth());
        assertEquals(date, GregorianDate.from(localDate));
        assertEquals(date, GregorianDate.of(localDate.getYear(), localDate.getMonthValue(),
                localDate.getDayOfMonth()));
    }

    @Property
    void calendarFactsAgreeWithJavaTime(@ForAll("dates") GregorianDate date) {
        LocalDate localDate = date.toLocalDate();
        assertEquals(localDate.isLeapYear(), date.year().isLeapYear());
        assertEquals(days(localDate.lengthOfYear()), date.year().numberOfDays());
        assertEquals(days(localDate.lengthOfMonth()), date.monthOfYear().numberOfDays());
        assertEquals(localDate.getDayOfMonth() == localDate.lengthOfMonth(), date.isLastDayOfMonth());
    }

    @Property
    void dayOfWeekAgreesWithJavaTime(@ForAll("datesWithAKnownDayOfWeek") GregorianDate date) {
        assertEquals(date.toLocalDate().getDayOfWeek(), date.dayOfWeek().toDayOfWeek());
    }

    @Property
    void orderingAgreesWithJavaTime(@ForAll("dates") GregorianDate one, @ForAll("dates") GregorianDate other) {
        assertEquals(Integer.signum(one.toLocalDate().compareTo(other.toLocalDate())),
                Integer.signum(one.compareTo(other)));
        assertEquals(one.toLocalDate().equals(other.toLocalDate()), one.equals(other));
    }

    @Property
    void neighbouringDatesAreOrdered(@ForAll("dates") GregorianDate date) {
        assertTrue(date.next().compareTo(date) > 0);
        assertTrue(date.previous().compareTo(date) < 0);
        assertEquals(date.next(days(1)), date.next());
    }

    @Property
    void monthsOfYearMoveLikeYearMonth(@ForAll("monthsOfYear") GregorianMonthOfYear monthOfYear,
            @ForAll("monthCounts") long count) {
        assertEquals(monthOfYear, monthOfYear.next(months(count)).previous(months(count)));
        assertEquals(monthOfYear, monthOfYear.previous(months(count)).next(months(count)));
        assertEquals(monthOfYear.toYearMonth().plusMonths(count), monthOfYear.next(months(count)).toYearMonth());
        assertEquals(monthOfYear.previous(months(count)), monthOfYear.next(months(count).negated()));
        long n = Math.abs(count);
        assertEquals(months(n), monthOfYear.next(months(n)).minus(monthOfYear).convertTo(TimeUnits.MONTH));
    }

    @Property
    void monthsOfYearMoveByYears(@ForAll("monthsOfYear") GregorianMonthOfYear monthOfYear,
            @ForAll("monthCounts") long count) {
        TimeQuantity years = TimeQuantity.of(count, TimeUnits.YEAR);
        assertEquals(monthOfYear.next(months(12 * count)), monthOfYear.next(years));
        assertEquals(monthOfYear, monthOfYear.next(years).previous(years));
    }

    @Property
    void monthLengthsAreThoseOfTheGregorianRule(@ForAll("monthsOfYear") GregorianMonthOfYear monthOfYear) {
        long length = monthOfYear.numberOfDays().convertTo(TimeUnits.DAY).amount().longValueExact();
        assertTrue(Set.of(28L, 29L, 30L, 31L).contains(length));
        assertEquals(YearMonth.of(Math.toIntExact(monthOfYear.year().number()), monthOfYear.month().positionInYear())
                .lengthOfMonth(), length);
        if (monthOfYear.month() == GregorianMonth.FEBRUARY) {
            assertEquals(monthOfYear.year().isLeapYear(), length == 29);
        }
        assertEquals(monthOfYear.numberOfDays(), monthOfYear.last().minus(monthOfYear.first()).plus(days(1)));
    }

    @Property
    void leapYearRuleAgreesWithJavaTime(@ForAll("wideYearNumbers") long number) {
        assertEquals(Year.isLeap(number), GregorianYear.number(number).isLeapYear());
    }

    @Property
    void yearsRoundTripAndMove(@ForAll("yearNumbers") long number, @ForAll("monthCounts") long count) {
        GregorianYear year = GregorianYear.number(number);
        TimeQuantity years = TimeQuantity.of(count, TimeUnits.YEAR);
        assertEquals(year, GregorianYear.from(Year.of(Math.toIntExact(number))));
        assertEquals(Year.of(Math.toIntExact(number)), year.toYear());
        assertEquals(year, year.next(years).previous(years));
        assertEquals(GregorianYear.number(number + count), year.next(years));
        assertEquals(year.previous(years), year.next(years.negated()));
        assertEquals(days(Year.of(Math.toIntExact(number)).length()), year.numberOfDays());
        assertEquals(year.numberOfDays(), year.lastDate().minus(year.firstDate()).plus(days(1)));
    }
}
