package org.chalten.islamic;

import org.chalten.core.CalendarDate;
import org.chalten.core.CalendarDayOfMonth;
import org.chalten.core.CalendarMonth;
import org.chalten.localization.Translations;
import org.chalten.measure.BaseUnit;
import org.chalten.measure.ChaltenAssertionException;
import org.chalten.measure.DerivedUnit;
import org.chalten.measure.Fraction;
import org.chalten.measure.TimeUnit;

/** Smalltalk {@code IslamicCalendar}: the relative units of the tabular Islamic calendar time scale. */
public final class IslamicCalendar {

    static final String NON_ISLAMIC = "Cannot use non-islamic months for days of islamic months";

    private static final BaseUnit YEAR = TimeUnit.base("Relative time in the Islamic Calendar Time escale", "☪️⏳",
            "islamic year", "islamic years", "iyr");
    private static final DerivedUnit MONTH =
            TimeUnit.derived(YEAR, "islamic month", "islamic months", "imo", Fraction.of(1, 12));

    private IslamicCalendar() {
    }

    /** Smalltalk {@code IslamicCalendar class>>year}. */
    public static BaseUnit year() {
        return YEAR;
    }

    /** Smalltalk {@code IslamicCalendar class>>month}: a twelfth of an Islamic year. */
    public static DerivedUnit month() {
        return MONTH;
    }

    /**
     * Smalltalk extension {@code CalendarDayOfMonth class>>islamicMonth:dayNumber:}.
     *
     * @throws ChaltenAssertionException {@code Cannot use non-islamic months for days of islamic months}
     */
    public static <D extends CalendarDate<D, ?, ?>> CalendarDayOfMonth<D> islamicMonth(CalendarMonth<D> month,
            int dayNumber) {
        if (month.isGregorian()) {
            throw new ChaltenAssertionException(Translations.localized(NON_ISLAMIC));
        }
        return new CalendarDayOfMonth<>(month, dayNumber);
    }
}
