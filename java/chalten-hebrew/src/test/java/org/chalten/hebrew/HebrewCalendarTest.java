package org.chalten.hebrew;

import java.util.List;
import java.util.function.Function;
import org.chalten.core.CalendarDayOfMonth;
import org.chalten.measure.BaseUnit;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;
import org.chalten.testkit.ChaltenTest;

/** Smalltalk {@code HebrewCalendarTest}: the fixtures shared by the Hebrew calendar tests. */
abstract class HebrewCalendarTest extends ChaltenTest {

    /** {@code first} to {@code thirtieth} of a month, by day number - 1. */
    static final List<Function<HebrewMonth, CalendarDayOfMonth<HebrewDate>>> DAYS_OF_MONTH = List.of(
            HebrewMonth::first, HebrewMonth::second, HebrewMonth::third, HebrewMonth::fourth, HebrewMonth::fifth,
            HebrewMonth::sixth, HebrewMonth::seventh, HebrewMonth::eighth, HebrewMonth::ninth, HebrewMonth::tenth,
            HebrewMonth::eleventh, HebrewMonth::twelfth, HebrewMonth::thirteenth, HebrewMonth::fourteenth,
            HebrewMonth::fifteenth, HebrewMonth::sixteenth, HebrewMonth::seventeenth, HebrewMonth::eighteenth,
            HebrewMonth::nineteenth, HebrewMonth::twentieth, HebrewMonth::twentyFirst, HebrewMonth::twentySecond,
            HebrewMonth::twentyThird, HebrewMonth::twentyFourth, HebrewMonth::twentyFifth,
            HebrewMonth::twentySixth, HebrewMonth::twentySeventh, HebrewMonth::twentyEighth,
            HebrewMonth::twentyNinth, HebrewMonth::thirtieth);

    protected final BaseUnit hebrewYear = HebrewCalendar.year();
    protected final BaseUnit hebrewMonth = HebrewCalendar.month();
    protected final HebrewMonth tishrei = HebrewMonth.TISHREI;
    protected final HebrewMonth cheshvan = HebrewMonth.CHESHVAN;
    protected final HebrewMonth kislev = HebrewMonth.KISLEV;
    protected final HebrewMonth tevet = HebrewMonth.TEVET;
    protected final HebrewMonth shevat = HebrewMonth.SHEVAT;
    protected final HebrewMonth adar = HebrewMonth.ADAR;
    protected final HebrewMonth adarI = HebrewMonth.ADAR_I;
    protected final HebrewMonth adarII = HebrewMonth.ADAR_II;
    protected final HebrewMonth nisan = HebrewMonth.NISAN;
    protected final HebrewMonth iyar = HebrewMonth.IYAR;
    protected final HebrewMonth sivan = HebrewMonth.SIVAN;
    protected final HebrewMonth tammuz = HebrewMonth.TAMMUZ;
    protected final HebrewMonth av = HebrewMonth.AV;
    protected final HebrewMonth elul = HebrewMonth.ELUL;

    /** {@code month dayNumber-th , yearNumber}, sending the ordinal selector like the Smalltalk tests. */
    static HebrewDate date(HebrewMonth month, int dayNumber, long yearNumber) {
        return DAYS_OF_MONTH.get(dayNumber - 1).apply(month).of(yearNumber);
    }

    static HebrewYear y(long number) {
        return HebrewYear.of(number);
    }

    TimeQuantity days(long number) {
        return TimeQuantity.of(number, TimeUnits.DAY);
    }

    TimeQuantity hebrewYears(long number) {
        return TimeQuantity.of(number, hebrewYear);
    }

    TimeQuantity hebrewMonths(long number) {
        return TimeQuantity.of(number, hebrewMonth);
    }

    /** Compares magnitudes of different calendars, which Java's static types reject at compile time. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static int compareUnchecked(Comparable magnitude, Object anotherMagnitude) {
        return magnitude.compareTo(anotherMagnitude);
    }
}
