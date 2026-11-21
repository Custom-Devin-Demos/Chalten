package org.chalten.hebrew;

import java.util.List;
import org.chalten.core.CalendarDayOfMonth;
import org.chalten.core.CalendarMonth;
import org.chalten.localization.Translations;
import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;

/** Smalltalk {@code HebrewMonth}: the months of the Hebrew calendar, with Adar I / Adar II on leap years. */
public enum HebrewMonth implements CalendarMonth<HebrewDate> {
    TISHREI("Tishrei", "Tish", 1, 1, 30, 30, 30),
    CHESHVAN("Cheshvan", "Chesh", 2, 2, 29, 29, 30),
    KISLEV("Kislev", "Kis", 3, 3, 29, 30, 30),
    TEVET("Tevet", "Tev", 4, 4, 29, 29, 29),
    SHEVAT("Shevat", "Shev", 5, 5, 30, 30, 30),
    ADAR("Adar", "Ad", 6, 0, 29, 29, 29),
    ADAR_I("Adar I", "Ad I", 0, 6, 30, 30, 30),
    ADAR_II("Adar II", "Ad II", 0, 7, 29, 29, 29),
    NISAN("Nisan", "Nis", 7, 8, 30, 30, 30),
    IYAR("Iyar", "Iya", 8, 9, 29, 29, 29),
    SIVAN("Sivan", "Siv", 9, 10, 30, 30, 30),
    TAMMUZ("Tammuz", "Tam", 10, 11, 29, 29, 29),
    AV("Av", "Av", 11, 12, 30, 30, 30),
    ELUL("Elul", "Elu", 12, 13, 29, 29, 29);

    private static final List<HebrewMonth> ALL_MONTHS_IN_NON_LEAP_YEARS =
            List.of(TISHREI, CHESHVAN, KISLEV, TEVET, SHEVAT, ADAR, NISAN, IYAR, SIVAN, TAMMUZ, AV, ELUL);
    private static final List<HebrewMonth> ALL_MONTHS_IN_LEAP_YEARS =
            List.of(TISHREI, CHESHVAN, KISLEV, TEVET, SHEVAT, ADAR_I, ADAR_II, NISAN, IYAR, SIVAN, TAMMUZ, AV, ELUL);

    private final String name;
    private final String shortName;
    private final int positionInCommonYear;
    private final int positionInLeapYear;
    private final TimeQuantity numberOfDaysInDeficientYear;
    private final TimeQuantity numberOfDaysInRegularYear;
    private final TimeQuantity numberOfDaysInCompleteYear;

    HebrewMonth(String name, String shortName, int positionInCommonYear, int positionInLeapYear, int deficient,
            int regular, int complete) {
        this.name = name;
        this.shortName = shortName;
        this.positionInCommonYear = positionInCommonYear;
        this.positionInLeapYear = positionInLeapYear;
        this.numberOfDaysInDeficientYear = TimeQuantity.of(deficient, TimeUnits.DAY);
        this.numberOfDaysInRegularYear = TimeQuantity.of(regular, TimeUnits.DAY);
        this.numberOfDaysInCompleteYear = TimeQuantity.of(complete, TimeUnits.DAY);
    }

    /** Smalltalk {@code HebrewMonth class>>allOnCommonYear}. */
    public static List<HebrewMonth> allOnCommonYear() {
        return ALL_MONTHS_IN_NON_LEAP_YEARS;
    }

    /** Smalltalk {@code HebrewMonth class>>allOnLeapYear}. */
    public static List<HebrewMonth> allOnLeapYear() {
        return ALL_MONTHS_IN_LEAP_YEARS;
    }

    /** Smalltalk {@code HebrewMonth class>>monthOnCommonYearAt:} (1-based). */
    public static HebrewMonth monthOnCommonYearAt(int index) {
        return ALL_MONTHS_IN_NON_LEAP_YEARS.get(index - 1);
    }

    /** Smalltalk {@code HebrewMonth class>>monthOnLeapYearAt:} (1-based). */
    public static HebrewMonth monthOnLeapYearAt(int index) {
        return ALL_MONTHS_IN_LEAP_YEARS.get(index - 1);
    }

    /** Smalltalk {@code name}, localized. */
    @Override
    public String fullName() {
        return Translations.localized(name);
    }

    @Override
    public String shortName() {
        return Translations.localized(shortName);
    }

    /**
     * Smalltalk {@code positionInCommonYear}.
     *
     * @throws InstanceCreationFailedException {@code Adar I only exists on leap years} (and Adar II)
     */
    public int positionInCommonYear() {
        if (positionInCommonYear == 0) {
            throw new InstanceCreationFailedException(Translations.localized(name + " only exists on leap years"));
        }
        return positionInCommonYear;
    }

    /**
     * Smalltalk {@code positionInLeapYear}.
     *
     * @throws InstanceCreationFailedException {@code Adar only exists on common years}
     */
    public int positionInLeapYear() {
        if (positionInLeapYear == 0) {
            throw new InstanceCreationFailedException(Translations.localized(name + " only exists on common years"));
        }
        return positionInLeapYear;
    }

    /** Smalltalk {@code positionIn:}. */
    public int positionIn(HebrewYear year) {
        return year.isLeapYear() ? positionInLeapYear() : positionInCommonYear();
    }

    public TimeQuantity numberOfDaysInDeficientYear() {
        return numberOfDaysInDeficientYear;
    }

    public TimeQuantity numberOfDaysInRegularYear() {
        return numberOfDaysInRegularYear;
    }

    public TimeQuantity numberOfDaysInCompleteYear() {
        return numberOfDaysInCompleteYear;
    }

    public boolean isVariableLengthMonth() {
        return !(numberOfDaysInRegularYear.equals(numberOfDaysInCompleteYear)
                && numberOfDaysInCompleteYear.equals(numberOfDaysInDeficientYear));
    }

    /** Smalltalk {@code ,}: this month in the Hebrew year {@code yearNumber}. */
    @Override
    public HebrewMonthOfYear of(long yearNumber) {
        HebrewYear year = HebrewYear.of(yearNumber);
        return year.monthAt(positionIn(year));
    }

    /** Smalltalk {@code CalendarDayOfMonth class>>hebrewMonth:dayNumber:}. */
    @Override
    public CalendarDayOfMonth<HebrewDate> dayOfMonthAt(int dayNumber) {
        return new CalendarDayOfMonth<>(this, dayNumber);
    }

    @Override
    public CalendarDayOfMonth<HebrewDate> thirtieth() {
        if (numberOfDaysInCompleteYear.compareTo(TimeQuantity.of(29, TimeUnits.DAY)) > 0) {
            return dayOfMonthAt(30);
        }
        throw new UnsupportedOperationException(this + " does not understand #thirtieth");
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
