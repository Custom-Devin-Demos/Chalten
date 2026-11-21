package org.chalten.hebrew;

import org.chalten.core.CalendarDayOfMonth;
import org.chalten.measure.BaseUnit;
import org.chalten.measure.TimeUnit;

/** Smalltalk {@code HebrewCalendar}: the relative units of the Hebrew calendar time scale. */
public final class HebrewCalendar {

    private static final BaseUnit YEAR = TimeUnit.base("Relative years in the Hebrew Calendar Time escale", "✡️⏳",
            "hebrew year", "hebrew years", "hyr");
    private static final BaseUnit MONTH = TimeUnit.base("Relative months in the Hebrew Calendar Time escale", "✡️⏳",
            "hebrew month", "hebrew months", "hmo");

    private HebrewCalendar() {
    }

    /** Smalltalk {@code HebrewCalendar class>>year}. */
    public static BaseUnit year() {
        return YEAR;
    }

    /** Smalltalk {@code HebrewCalendar class>>month}. */
    public static BaseUnit month() {
        return MONTH;
    }

    /** Smalltalk extension {@code CalendarDayOfMonth class>>hebrewMonth:dayNumber:}. */
    public static CalendarDayOfMonth<HebrewDate> hebrewMonth(HebrewMonth month, int dayNumber) {
        return month.dayOfMonthAt(dayNumber);
    }
}
