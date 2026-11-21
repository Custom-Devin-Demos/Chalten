package org.chalten.islamic;

import java.util.List;
import java.util.function.Function;
import org.chalten.core.CalendarDayOfMonth;
import org.chalten.measure.BaseUnit;
import org.chalten.measure.DerivedUnit;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;
import org.chalten.testkit.ChaltenTest;

/** Smalltalk {@code IslamicCalendarTest}: the fixtures shared by the Islamic calendar tests. */
abstract class IslamicCalendarTest extends ChaltenTest {

    /** {@code first} to {@code thirtieth} of a month, by day number - 1. */
    static final List<Function<IslamicMonth, CalendarDayOfMonth<IslamicDate>>> DAYS_OF_MONTH = List.of(
            IslamicMonth::first, IslamicMonth::second, IslamicMonth::third, IslamicMonth::fourth, IslamicMonth::fifth,
            IslamicMonth::sixth, IslamicMonth::seventh, IslamicMonth::eighth, IslamicMonth::ninth, IslamicMonth::tenth,
            IslamicMonth::eleventh, IslamicMonth::twelfth, IslamicMonth::thirteenth, IslamicMonth::fourteenth,
            IslamicMonth::fifteenth, IslamicMonth::sixteenth, IslamicMonth::seventeenth, IslamicMonth::eighteenth,
            IslamicMonth::nineteenth, IslamicMonth::twentieth, IslamicMonth::twentyFirst, IslamicMonth::twentySecond,
            IslamicMonth::twentyThird, IslamicMonth::twentyFourth, IslamicMonth::twentyFifth,
            IslamicMonth::twentySixth, IslamicMonth::twentySeventh, IslamicMonth::twentyEighth,
            IslamicMonth::twentyNinth, IslamicMonth::thirtieth);

    protected final BaseUnit islamicYear = IslamicCalendar.year();
    protected final DerivedUnit islamicMonth = IslamicCalendar.month();
    protected final IslamicMonth muharram = IslamicMonth.MUHARRAM;
    protected final IslamicMonth safar = IslamicMonth.SAFAR;
    protected final IslamicMonth rabiAlAwwal = IslamicMonth.RABI_AL_AWWAL;
    protected final IslamicMonth rabiAlThani = IslamicMonth.RABI_AL_THANI;
    protected final IslamicMonth jumadaAlAwwal = IslamicMonth.JUMADA_AL_AWWAL;
    protected final IslamicMonth jumadaAlThani = IslamicMonth.JUMADA_AL_THANI;
    protected final IslamicMonth rajab = IslamicMonth.RAJAB;
    protected final IslamicMonth shaban = IslamicMonth.SHABAN;
    protected final IslamicMonth ramadan = IslamicMonth.RAMADAN;
    protected final IslamicMonth shawwal = IslamicMonth.SHAWWAL;
    protected final IslamicMonth duAlQiDah = IslamicMonth.DU_AL_QI_DAH;
    protected final IslamicMonth duAlHijjah = IslamicMonth.DU_AL_HIJJAH;

    /** {@code month dayNumber-th , yearNumber}, sending the ordinal selector like the Smalltalk tests. */
    static IslamicDate date(IslamicMonth month, int dayNumber, long yearNumber) {
        return DAYS_OF_MONTH.get(dayNumber - 1).apply(month).of(yearNumber);
    }

    static IslamicYear y(long number) {
        return IslamicYear.of(number);
    }

    TimeQuantity days(long number) {
        return TimeQuantity.of(number, TimeUnits.DAY);
    }

    TimeQuantity islamicYears(long number) {
        return TimeQuantity.of(number, islamicYear);
    }

    TimeQuantity islamicMonths(long number) {
        return TimeQuantity.of(number, islamicMonth);
    }

    /** Compares magnitudes of different calendars, which Java's static types reject at compile time. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    static int compareUnchecked(Comparable magnitude, Object anotherMagnitude) {
        return magnitude.compareTo(anotherMagnitude);
    }
}
