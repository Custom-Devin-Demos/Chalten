package org.chalten.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.IntStream;
import org.chalten.localization.Translations;
import org.chalten.measure.DiscreteInterval;
import org.chalten.measure.Fraction;
import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;
import org.junit.jupiter.api.Test;

/**
 * Backs every Java snippet of {@code java/MIGRATION.md}: each snippet appears here line by line (checked by
 * {@link MigrationGuideTest}) and the values the guide shows are asserted. The policy-period helpers are
 * compositions of Chalten operations that belong in application code, not in Chalten.
 */
class MigrationGuideExamplesTest {

    static GregorianDate shiftClampedToMonthEnd(GregorianDate date, TimeQuantity months) {
        GregorianMonthOfYear target = date.monthOfYear().next(months);
        int lastDay = target.numberOfDays().numericalValue().intValueExact();
        return target.dateAt(Math.min(date.dayNumber(), lastDay));
    }

    static GregorianDate shiftRollingToNextMonth(GregorianDate date, TimeQuantity months) {
        GregorianMonthOfYear target = date.monthOfYear().next(months);
        int lastDay = target.numberOfDays().numericalValue().intValueExact();
        return date.dayNumber() <= lastDay ? target.dateAt(date.dayNumber()) : target.next().first();
    }

    private static GregorianDate date(String iso8601) {
        return new ISO8601Parser().parseDate(iso8601);
    }

    private static long javaTimeDays(GregorianDate start, GregorianDate end) {
        return ChronoUnit.DAYS.between(start.toLocalDate(), end.toLocalDate());
    }

    @Test
    void creatingDates() {
        GregorianDate inception = GregorianMonth.MARCH.fifteenth().of(2025);
        GregorianDate sameDate = GregorianDate.of(2025, 3, 15);
        GregorianDate parsed = new ISO8601Parser().parseDate("2025-03-15");
        GregorianDate fromJavaTime = GregorianDate.from(LocalDate.of(2025, 3, 15));

        assertEquals(inception, sameDate);
        assertEquals(inception, parsed);
        assertEquals(inception, fromJavaTime);
        assertEquals("March 15, 2025", inception.toString());
        assertEquals("2025-03-15", inception.printAsISO8601String());
        assertEquals(DayOfWeek.SATURDAY, inception.dayOfWeek());
        assertEquals(GregorianMonth.MARCH.of(2025), inception.monthOfYear());
    }

    @Test
    void twelveMonthTerm() {
        GregorianDate inception = GregorianMonth.MARCH.fifteenth().of(2025);
        TimeQuantity term = TimeQuantity.of(12, TimeUnits.MONTH);
        GregorianDate expiration = shiftClampedToMonthEnd(inception, term);
        GregorianDate lastCoveredDay = expiration.previous();
        TimeQuantity daysInTerm = expiration.minus(inception);

        assertEquals(date("2026-03-15"), expiration);
        assertEquals(date("2026-03-14"), lastCoveredDay);
        assertEquals(TimeQuantity.of(365, TimeUnits.DAY), daysInTerm);
        assertEquals("365 d", daysInTerm.toString());
        assertEquals(365, javaTimeDays(inception, expiration));
    }

    @Test
    void sixMonthTerm() {
        GregorianDate inception = GregorianMonth.MARCH.fifteenth().of(2025);
        GregorianDate monthEndInception = GregorianMonth.AUGUST.thirtyFirst().of(2025);
        TimeQuantity term = TimeQuantity.of(6, TimeUnits.MONTH);
        GregorianDate expiration = shiftClampedToMonthEnd(inception, term);
        GregorianDate monthEndExpiration = shiftClampedToMonthEnd(monthEndInception, term);

        assertEquals(date("2025-09-15"), expiration);
        assertEquals(date("2025-09-14"), expiration.previous());
        assertEquals(TimeQuantity.of(184, TimeUnits.DAY), expiration.minus(inception));
        assertEquals(date("2026-02-28"), monthEndExpiration);
        assertEquals(date("2026-02-27"), monthEndExpiration.previous());
        assertEquals(TimeQuantity.of(181, TimeUnits.DAY), monthEndExpiration.minus(monthEndInception));
        assertEquals(184, javaTimeDays(inception, expiration));
        assertEquals(181, javaTimeDays(monthEndInception, monthEndExpiration));
    }

    @Test
    void leapDayInceptionRenewals() {
        GregorianDate inception = GregorianMonth.FEBRUARY.twentyNinth().of(2024);
        TimeQuantity year = TimeQuantity.of(1, TimeUnits.YEAR);
        GregorianDate clamped = shiftClampedToMonthEnd(inception, year);
        GregorianDate rolled = shiftRollingToNextMonth(inception, year);
        GregorianDate fourthAnchored = shiftClampedToMonthEnd(inception, year.times(4));
        GregorianDate fourthChained = inception;
        for (int renewal = 1; renewal <= 4; renewal++) {
            fourthChained = shiftClampedToMonthEnd(fourthChained, year);
        }

        assertEquals(date("2025-02-28"), clamped);
        assertEquals(date("2025-03-01"), rolled);
        assertEquals(TimeQuantity.of(365, TimeUnits.DAY), clamped.minus(inception));
        assertEquals(TimeQuantity.of(366, TimeUnits.DAY), rolled.minus(inception));
        assertEquals(date("2028-02-29"), fourthAnchored);
        assertEquals(date("2028-02-28"), fourthChained);
        assertEquals(TimeQuantity.of(12, TimeUnits.MONTH), year);
    }

    @Test
    void monthEndPlusOneMonth() {
        GregorianDate inception = GregorianMonth.JANUARY.thirtyFirst().of(2025);
        GregorianDate leapYearInception = GregorianMonth.JANUARY.thirtyFirst().of(2024);
        TimeQuantity oneMonth = TimeQuantity.of(1, TimeUnits.MONTH);
        GregorianDate clamped = shiftClampedToMonthEnd(inception, oneMonth);
        GregorianDate rolled = shiftRollingToNextMonth(inception, oneMonth);
        GregorianDate leapYearClamped = shiftClampedToMonthEnd(leapYearInception, oneMonth);
        GregorianDate secondAnchored = shiftClampedToMonthEnd(inception, oneMonth.times(2));
        GregorianDate secondChained = shiftClampedToMonthEnd(clamped, oneMonth);

        assertEquals(date("2025-02-28"), clamped);
        assertEquals(date("2025-03-01"), rolled);
        assertEquals(date("2024-02-29"), leapYearClamped);
        assertEquals(date("2025-03-31"), secondAnchored);
        assertEquals(date("2025-03-28"), secondChained);
        assertTrue(clamped.isLastDayOfMonth());
    }

    @Test
    void clampingAgreesWithJavaTimePlusMonths() {
        for (GregorianDate inception : List.of(date("2024-02-29"), date("2025-01-31"), date("2025-08-31"),
                date("2025-03-15"), date("2023-12-31"))) {
            for (int months = 0; months <= 48; months++) {
                assertEquals(inception.toLocalDate().plusMonths(months),
                        shiftClampedToMonthEnd(inception, TimeQuantity.of(months, TimeUnits.MONTH)).toLocalDate());
            }
        }
    }

    @Test
    void monthlyInstallmentSchedule() {
        GregorianDate inception = GregorianMonth.JANUARY.thirtyFirst().of(2025);
        List<GregorianDate> dueDates = IntStream.range(0, 12)
                .mapToObj(n -> shiftClampedToMonthEnd(inception, TimeQuantity.of(n, TimeUnits.MONTH)))
                .toList();

        assertEquals(List.of("2025-01-31", "2025-02-28", "2025-03-31", "2025-04-30", "2025-05-31", "2025-06-30",
                "2025-07-31", "2025-08-31", "2025-09-30", "2025-10-31", "2025-11-30", "2025-12-31"),
                dueDates.stream().map(GregorianDate::printAsISO8601String).toList());
    }

    @Test
    void proRataDayCounts() {
        GregorianDate inception = GregorianMonth.JANUARY.first().of(2024);
        GregorianDate expiration = shiftClampedToMonthEnd(inception, TimeQuantity.of(1, TimeUnits.YEAR));
        GregorianDate cancellation = GregorianMonth.APRIL.first().of(2024);
        TimeQuantity daysInTerm = expiration.minus(inception);
        TimeQuantity daysElapsed = cancellation.minus(inception);
        TimeQuantity daysRemaining = expiration.minus(cancellation);
        Fraction earned = daysElapsed.dividedBy(daysInTerm);
        Fraction unearned = Fraction.ONE.minus(earned);
        Fraction earnedPremium = earned.times(1200);
        Fraction returnPremium = unearned.times(1200);

        assertEquals(date("2025-01-01"), expiration);
        assertEquals(TimeQuantity.of(366, TimeUnits.DAY), daysInTerm);
        assertEquals(TimeQuantity.of(91, TimeUnits.DAY), daysElapsed);
        assertEquals(TimeQuantity.of(275, TimeUnits.DAY), daysRemaining);
        assertEquals(Fraction.of(91, 366), earned);
        assertEquals(Fraction.of(275, 366), unearned);
        assertEquals("(91/366)", earned.toString());
        assertEquals("(275/366)", unearned.toString());
        assertEquals(Fraction.of(18_200, 61), earnedPremium);
        assertEquals(Fraction.of(55_000, 61), returnPremium);
        assertEquals(Fraction.of(1200), earnedPremium.plus(returnPremium));
        assertEquals(daysRemaining.dividedBy(daysInTerm), unearned);
    }

    @Test
    void proRataActual365() {
        GregorianDate inception = GregorianMonth.JANUARY.first().of(2024);
        GregorianDate cancellation = GregorianMonth.APRIL.first().of(2024);
        TimeQuantity daysElapsed = cancellation.minus(inception);
        Fraction earned = daysElapsed.dividedBy(TimeQuantity.of(365, TimeUnits.DAY));

        assertEquals(Fraction.of(91, 365), earned);
        assertEquals(Fraction.of(21_840, 73), earned.times(1200));
    }

    @Test
    void coveredDaysInterval() {
        GregorianDate inception = GregorianMonth.MARCH.fifteenth().of(2025);
        GregorianDate lastCoveredDay = GregorianMonth.MARCH.fourteenth().of(2026);
        DiscreteInterval<GregorianDate> coveredDays = inception.to(lastCoveredDay);
        DiscreteInterval<GregorianDate> weeklyAudits = inception.to(lastCoveredDay, TimeQuantity.of(7, TimeUnits.DAY));

        assertEquals(365, coveredDays.size());
        assertEquals(inception, coveredDays.first());
        assertEquals(lastCoveredDay, coveredDays.last());
        assertTrue(coveredDays.contains(GregorianMonth.FEBRUARY.twentyEighth().of(2026)));
        assertFalse(coveredDays.contains(GregorianMonth.MARCH.fifteenth().of(2026)));
        assertEquals(53, weeklyAudits.size());
        assertEquals(date("2026-03-14"), weeklyAudits.last());
        assertEquals(53, coveredDays.select(d -> d.dayOfWeek() == DayOfWeek.SATURDAY).size());
    }

    @Test
    void effectiveTimeAtAFixedOffset() {
        GregorianDate inception = GregorianMonth.MARCH.fifteenth().of(2025);
        FixedOffsetRuleset eastern = FixedOffsetRuleset.of(TimeQuantity.of(-5, TimeUnits.HOUR));
        TimeOffsetAwareGregorianDateTime effective = inception.at(TimeOfDay.hours(0, 1)).in(eastern);
        Instant instant = effective.asInstant();

        assertEquals("2025-03-15T00:01:00.000000-05:00", effective.toString());
        assertEquals("2025-03-15T05:01:00.000000Z", instant.toString());
        assertEquals(java.time.Instant.parse("2025-03-15T05:01:00Z"), instant.toJavaInstant());
        assertEquals(ZoneOffset.ofHours(-5), eastern.toZoneOffset());
        assertEquals(effective.toOffsetDateTime(),
                java.time.OffsetDateTime.parse("2025-03-15T00:01-05:00"));
    }

    @Test
    void javaTimeInterop() {
        GregorianDate inception = GregorianMonth.MARCH.fifteenth().of(2025);
        LocalDate local = inception.toLocalDate();
        GregorianDate back = GregorianDate.from(local);
        TimeQuantity term = TimeQuantity.of(12, TimeUnits.MONTH);
        java.time.Period period = term.toPeriod();
        java.time.Duration duration = TimeQuantity.of(36, TimeUnits.HOUR).toDuration();

        assertEquals(LocalDate.of(2025, 3, 15), local);
        assertEquals(inception, back);
        assertEquals(java.time.Period.ofYears(1), period);
        assertEquals(java.time.Duration.ofHours(36), duration);
        assertEquals(local.plus(period), shiftClampedToMonthEnd(inception, term).toLocalDate());
    }

    @Test
    void parsingAndFormatting() {
        ISO8601Parser parser = new ISO8601Parser();
        GregorianDateTime effective = parser.parseDateTime("2025-03-15T00:01:00");
        TimeQuantity term = parser.parseDuration("P12M");

        assertEquals(GregorianMonth.MARCH.fifteenth().of(2025).at(TimeOfDay.hours(0, 1)), effective);
        assertEquals("2025-03-15T00:01:00.000000", effective.printAsISO8601String());
        assertEquals(TimeQuantity.of(12, TimeUnits.MONTH), term);
        assertEquals("P12M", new org.chalten.measure.ISO8601DurationFormatter().format(term));
    }

    @Test
    void localizedNamesInAThreadScope() {
        String spanish = Translations.duringLanguage("es", () -> GregorianMonth.MARCH.fullName());
        String english = GregorianMonth.MARCH.fullName();

        assertEquals("Marzo", spanish);
        assertEquals("March", english);
    }

    @Test
    void exceptions() {
        GregorianDate inception = GregorianMonth.MARCH.fifteenth().of(2026);
        GregorianDate cancellation = GregorianMonth.MARCH.first().of(2026);
        ArithmeticException beforeInception =
                assertThrows(ArithmeticException.class, () -> cancellation.minus(inception));
        InstanceCreationFailedException invalidDay =
                assertThrows(InstanceCreationFailedException.class, () -> GregorianMonth.FEBRUARY.of(2025).dateAt(29));
        InstanceCreationFailedException invalidText =
                assertThrows(InstanceCreationFailedException.class, () -> new ISO8601Parser().parseDate("2025-02-30"));
        UnsupportedOperationException missingSelector =
                assertThrows(UnsupportedOperationException.class, () -> GregorianMonth.APRIL.thirtyFirst());
        ArithmeticException monthsOnADate =
                assertThrows(ArithmeticException.class, () -> inception.plus(TimeQuantity.of(1, TimeUnits.MONTH)));

        assertEquals("Cannot subtract two dates when the second one is greater than the first one.",
                beforeInception.getMessage());
        assertEquals("29 is an invalid day number in February, 2025", invalidDay.getMessage());
        assertEquals("30 is an invalid day number in February, 2025", invalidText.getMessage());
        assertEquals("April does not understand #thirtyFirst", missingSelector.getMessage());
        assertEquals("Incommensurable units are not comparable", monthsOnADate.getMessage());
    }
}
