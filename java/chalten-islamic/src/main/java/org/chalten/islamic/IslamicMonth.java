package org.chalten.islamic;

import java.util.List;
import org.chalten.core.CalendarDayOfMonth;
import org.chalten.core.CalendarMonth;
import org.chalten.localization.Translations;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;

/** Smalltalk {@code IslamicMonth}: the months of the tabular Islamic calendar. */
public enum IslamicMonth implements CalendarMonth<IslamicDate> {
    MUHARRAM("Muharram", "Muh.", 30, 30),
    SAFAR("Safar", "Saf.", 29, 29),
    RABI_AL_AWWAL("Rabi' al-Awwal", "Rab. I", 30, 30),
    RABI_AL_THANI("Rabi' al-Thani", "Rab. II", 29, 29),
    JUMADA_AL_AWWAL("Jumada al-Awwal", "Jum. I", 30, 30),
    JUMADA_AL_THANI("Jumada al-Thani", "Jum. II", 29, 29),
    RAJAB("Rajab", "Raj.", 30, 30),
    SHABAN("Sha'ban", "Sha.", 29, 29),
    RAMADAN("Ramadan", "Ram.", 30, 30),
    SHAWWAL("Shawwal", "Shaw.", 29, 29),
    DU_AL_QI_DAH("Dhu al-Qi'dah", "Dhu'l-Q.", 30, 30),
    DU_AL_HIJJAH("Dhu al-Hijjah", "Dhu'l-H.", 30, 29);

    private static final List<IslamicMonth> ALL_MONTHS = List.of(values());

    private final String name;
    private final String shortName;
    private final TimeQuantity numberOfDaysInLeapYear;
    private final TimeQuantity numberOfDaysInCommonYear;

    IslamicMonth(String name, String shortName, int leapDays, int commonDays) {
        this.name = name;
        this.shortName = shortName;
        this.numberOfDaysInLeapYear = TimeQuantity.of(leapDays, TimeUnits.DAY);
        this.numberOfDaysInCommonYear = TimeQuantity.of(commonDays, TimeUnits.DAY);
    }

    /** Smalltalk {@code IslamicMonth class>>all}. */
    public static List<IslamicMonth> all() {
        return ALL_MONTHS;
    }

    /** Smalltalk {@code IslamicMonth class>>monthAt:} (1-based). */
    public static IslamicMonth monthAt(int index) {
        return ALL_MONTHS.get(index - 1);
    }

    @Override
    public String fullName() {
        return Translations.localized(name);
    }

    @Override
    public String shortName() {
        return Translations.localized(shortName);
    }

    public int positionInYear() {
        return ordinal() + 1;
    }

    public TimeQuantity numberOfDaysInLeapYear() {
        return numberOfDaysInLeapYear;
    }

    public TimeQuantity numberOfDaysInCommonYear() {
        return numberOfDaysInCommonYear;
    }

    /** Smalltalk {@code ,}: this month in the Islamic year {@code yearNumber}. */
    @Override
    public IslamicMonthOfYear of(long yearNumber) {
        return IslamicYear.of(yearNumber).monthAt(positionInYear());
    }

    @Override
    public CalendarDayOfMonth<IslamicDate> dayOfMonthAt(int dayNumber) {
        return IslamicCalendar.islamicMonth(this, dayNumber);
    }

    @Override
    public CalendarDayOfMonth<IslamicDate> thirtieth() {
        if (numberOfDaysInLeapYear.compareTo(TimeQuantity.of(29, TimeUnits.DAY)) > 0) {
            return dayOfMonthAt(30);
        }
        throw new UnsupportedOperationException(this + " does not understand #thirtieth");
    }

    /**
     * Smalltalk {@code next:}.
     *
     * @throws ArithmeticException when {@code relativeTimeQuantity} is not measured in Islamic months or years
     */
    public IslamicMonth next(TimeQuantity relativeTimeQuantity) {
        if (relativeTimeQuantity.isZero()) {
            return this;
        }
        if (relativeTimeQuantity.isNegative()) {
            return previous(relativeTimeQuantity.abs());
        }
        int numberOfMonths = inMonths(relativeTimeQuantity);
        return monthAt((positionInYear() + numberOfMonths - 1) % 12 + 1);
    }

    /**
     * Smalltalk {@code previous:}.
     *
     * @throws ArithmeticException when {@code relativeTimeQuantity} is not measured in Islamic months or years
     */
    public IslamicMonth previous(TimeQuantity relativeTimeQuantity) {
        if (relativeTimeQuantity.isZero()) {
            return this;
        }
        if (relativeTimeQuantity.isNegative()) {
            return next(relativeTimeQuantity.abs());
        }
        int numberOfMonths = inMonths(relativeTimeQuantity);
        return monthAt(((positionInYear() - numberOfMonths - 1) % 12 + 12) % 12 + 1);
    }

    private static int inMonths(TimeQuantity quantity) {
        return quantity.convertTo(IslamicCalendar.month()).numericalValue().intValueExact();
    }

    @Override
    public boolean isGregorian() {
        return false;
    }

    @Override
    public String toString() {
        return name;
    }
}
