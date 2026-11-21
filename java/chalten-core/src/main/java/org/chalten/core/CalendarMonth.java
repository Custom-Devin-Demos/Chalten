package org.chalten.core;

import java.util.function.Consumer;
import org.chalten.localization.FormatterOptions;
import org.chalten.localization.LocalizedChronologyFormatter;
import org.chalten.measure.TimeUnit;
import org.chalten.measure.TimeUnits;

/**
 * Smalltalk {@code CalendarMonth}: a month of a calendar, independent of any year. Smalltalk {@code name} is
 * {@link #fullName()} because {@code Enum.name()} is final.
 *
 * @param <D> the date type of the calendar
 */
public interface CalendarMonth<D extends CalendarDate<D, ?, ?>> {

    static TimeUnit day() {
        return TimeUnits.DAY;
    }

    /** Smalltalk {@code name}: the localized month name. */
    String fullName();

    /** Smalltalk {@code shortName}: the localized abbreviated month name. */
    String shortName();

    /** Smalltalk {@code , yearNumber}. */
    CalendarMonthOfYear<?, D, ?> of(long yearNumber);

    CalendarDayOfMonth<D> dayOfMonthAt(int dayNumber);

    boolean isGregorian();

    default CalendarDayOfMonth<D> first() {
        return dayOfMonthAt(1);
    }

    default CalendarDayOfMonth<D> second() {
        return dayOfMonthAt(2);
    }

    default CalendarDayOfMonth<D> third() {
        return dayOfMonthAt(3);
    }

    default CalendarDayOfMonth<D> fourth() {
        return dayOfMonthAt(4);
    }

    default CalendarDayOfMonth<D> fifth() {
        return dayOfMonthAt(5);
    }

    default CalendarDayOfMonth<D> sixth() {
        return dayOfMonthAt(6);
    }

    default CalendarDayOfMonth<D> seventh() {
        return dayOfMonthAt(7);
    }

    default CalendarDayOfMonth<D> eighth() {
        return dayOfMonthAt(8);
    }

    default CalendarDayOfMonth<D> ninth() {
        return dayOfMonthAt(9);
    }

    default CalendarDayOfMonth<D> tenth() {
        return dayOfMonthAt(10);
    }

    default CalendarDayOfMonth<D> eleventh() {
        return dayOfMonthAt(11);
    }

    default CalendarDayOfMonth<D> twelfth() {
        return dayOfMonthAt(12);
    }

    default CalendarDayOfMonth<D> thirteenth() {
        return dayOfMonthAt(13);
    }

    default CalendarDayOfMonth<D> fourteenth() {
        return dayOfMonthAt(14);
    }

    default CalendarDayOfMonth<D> fifteenth() {
        return dayOfMonthAt(15);
    }

    default CalendarDayOfMonth<D> sixteenth() {
        return dayOfMonthAt(16);
    }

    default CalendarDayOfMonth<D> seventeenth() {
        return dayOfMonthAt(17);
    }

    default CalendarDayOfMonth<D> eighteenth() {
        return dayOfMonthAt(18);
    }

    default CalendarDayOfMonth<D> nineteenth() {
        return dayOfMonthAt(19);
    }

    default CalendarDayOfMonth<D> twentieth() {
        return dayOfMonthAt(20);
    }

    default CalendarDayOfMonth<D> twentyFirst() {
        return dayOfMonthAt(21);
    }

    default CalendarDayOfMonth<D> twentySecond() {
        return dayOfMonthAt(22);
    }

    default CalendarDayOfMonth<D> twentyThird() {
        return dayOfMonthAt(23);
    }

    default CalendarDayOfMonth<D> twentyFourth() {
        return dayOfMonthAt(24);
    }

    default CalendarDayOfMonth<D> twentyFifth() {
        return dayOfMonthAt(25);
    }

    default CalendarDayOfMonth<D> twentySixth() {
        return dayOfMonthAt(26);
    }

    default CalendarDayOfMonth<D> twentySeventh() {
        return dayOfMonthAt(27);
    }

    default CalendarDayOfMonth<D> twentyEighth() {
        return dayOfMonthAt(28);
    }

    default CalendarDayOfMonth<D> twentyNinth() {
        return dayOfMonthAt(29);
    }

    /** @throws UnsupportedOperationException (Smalltalk {@code MessageNotUnderstood}) unless overridden */
    default CalendarDayOfMonth<D> thirtieth() {
        throw Smalltalk.doesNotUnderstand(this, "thirtieth");
    }

    /** @throws UnsupportedOperationException (Smalltalk {@code MessageNotUnderstood}) unless overridden */
    default CalendarDayOfMonth<D> thirtyFirst() {
        throw Smalltalk.doesNotUnderstand(this, "thirtyFirst");
    }

    /** Smalltalk {@code localized}: formatted in the medium style of {@link org.chalten.localization.CurrentLocale}. */
    default String localized() {
        return LocalizedChronologyFormatter.localized(this, options -> { });
    }

    /** Smalltalk {@code localizedWith:}: formatted in the style {@code configurer} chooses. */
    default String localized(Consumer<FormatterOptions> configurer) {
        return LocalizedChronologyFormatter.localized(this, configurer);
    }
}
