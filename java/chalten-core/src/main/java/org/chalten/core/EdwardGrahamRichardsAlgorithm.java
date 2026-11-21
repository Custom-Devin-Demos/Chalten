package org.chalten.core;

import java.math.RoundingMode;
import org.chalten.measure.Fraction;

/**
 * Smalltalk {@code EdwardGrahamRichardsAlgorithm}: converts a Julian day number to a proleptic Gregorian
 * day/month/year. Smalltalk {@code //} and {@code \\} floor, as {@link Math#floorDiv} and {@link Math#floorMod}.
 */
public final class EdwardGrahamRichardsAlgorithm {

    private static final EdwardGrahamRichardsAlgorithm UNIQUE_INSTANCE = new EdwardGrahamRichardsAlgorithm();

    private static final long NUMBER_OF_MONTHS_IN_YEAR = 12;
    private static final long NUMBER_OF_YEARS_IN_CYCLE = 4;
    private static final long NUMBER_OF_DAYS_IN_4_YEAR_CYCLE = 1461;
    private static final long NUMBER_OF_DAYS_IN_400_YEARS = 146097;
    private static final long MONTH_SHIFT = 2;
    private static final long NUMBER_OF_MONTHS_BETWEEN_MARCH_AND_JULY = 5;
    private static final long NUMBER_OF_DAYS_IN_MARCH_TO_JULY = 153;

    private EdwardGrahamRichardsAlgorithm() {
    }

    /** Smalltalk {@code EdwardGrahamRichardsAlgorithm new} (a singleton). */
    public static EdwardGrahamRichardsAlgorithm instance() {
        return UNIQUE_INSTANCE;
    }

    /**
     * Smalltalk {@code executeFor:}.
     *
     * @return {@code {dayNumber, monthNumber, yearNumber}}
     */
    public long[] executeFor(Fraction julianDayNumber) {
        long e = prepareForSplittingInto4YearCycles(julianDayNumber);
        long h = prepareForMonthAndDayExtraction(e);
        long dayNumber = extractDayNumber(h);
        long monthNumber = extractMonthNumber(h);
        long yearNumber = computeGregorianYearFrom(e, monthNumber);
        return new long[] {dayNumber, monthNumber, yearNumber};
    }

    /** Smalltalk {@code Number>>rounded}: half away from zero. */
    static long rounded(Fraction number) {
        return number.toBigDecimalExact().setScale(0, RoundingMode.HALF_UP).toBigIntegerExact().longValueExact();
    }

    long normalize(Fraction julianDayNumber) {
        long jdn = rounded(julianDayNumber);
        long scalingFactor = 4 * jdn + 274277;
        long epochAlignment = 38;
        return jdn + 1401 + Math.floorDiv(Math.floorDiv(scalingFactor, NUMBER_OF_DAYS_IN_400_YEARS) * 3, 4)
                - epochAlignment;
    }

    long prepareForSplittingInto4YearCycles(Fraction julianDayNumber) {
        return NUMBER_OF_YEARS_IN_CYCLE * normalize(julianDayNumber) + 3;
    }

    long splitInto4YearCycles(long e) {
        return Math.floorDiv(Math.floorMod(e, NUMBER_OF_DAYS_IN_4_YEAR_CYCLE), NUMBER_OF_YEARS_IN_CYCLE);
    }

    long prepareForMonthAndDayExtraction(long e) {
        return NUMBER_OF_MONTHS_BETWEEN_MARCH_AND_JULY * splitInto4YearCycles(e) + MONTH_SHIFT;
    }

    long extractDayNumber(long h) {
        return Math.floorDiv(Math.floorMod(h, NUMBER_OF_DAYS_IN_MARCH_TO_JULY),
                NUMBER_OF_MONTHS_BETWEEN_MARCH_AND_JULY) + 1;
    }

    long extractMonthNumber(long h) {
        return Math.floorMod(Math.floorDiv(h, NUMBER_OF_DAYS_IN_MARCH_TO_JULY) + MONTH_SHIFT,
                NUMBER_OF_MONTHS_IN_YEAR) + 1;
    }

    long computeGregorianYearFrom(long e, long monthNumber) {
        return Math.floorDiv(e, NUMBER_OF_DAYS_IN_4_YEAR_CYCLE) - 4716
                + Math.floorDiv(NUMBER_OF_MONTHS_IN_YEAR + MONTH_SHIFT - monthNumber, NUMBER_OF_MONTHS_IN_YEAR);
    }

}
