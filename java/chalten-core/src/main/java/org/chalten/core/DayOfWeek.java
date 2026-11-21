package org.chalten.core;

import java.util.List;
import org.chalten.localization.Translations;

/** Smalltalk {@code DayOfWeek}: the ISO days of the week, Monday (1) to Sunday (7). */
public enum DayOfWeek implements CalendarDayOfWeek {
    MONDAY("Monday", "Mo"),
    TUESDAY("Tuesday", "Tu"),
    WEDNESDAY("Wednesday", "We"),
    THURSDAY("Thursday", "Th"),
    FRIDAY("Friday", "Fr"),
    SATURDAY("Saturday", "Sa"),
    SUNDAY("Sunday", "Su");

    private static final List<DayOfWeek> WELL_KNOWN_DAYS = List.of(values());

    private final String name;
    private final String shortName;

    DayOfWeek(String name, String shortName) {
        this.name = name;
        this.shortName = shortName;
    }

    /** Smalltalk {@code DayOfWeek class>>dayAt:} (1-based, Monday first). */
    public static DayOfWeek dayAt(int positionInWeek) {
        return WELL_KNOWN_DAYS.get(positionInWeek - 1);
    }

    public static DayOfWeek from(java.time.DayOfWeek dayOfWeek) {
        return dayAt(dayOfWeek.getValue());
    }

    @Override
    public int positionInWeek() {
        return ordinal() + 1;
    }

    @Override
    public String fullName() {
        return Translations.localized(name);
    }

    @Override
    public String shortName() {
        return Translations.localized(shortName);
    }

    @Override
    public DayOfWeek next() {
        return dayAt(positionInWeek() % 7 + 1);
    }

    @Override
    public DayOfWeek previous() {
        return dayAt((positionInWeek() - 8) % 7 + 7);
    }

    @Override
    public DayOfWeek asISODayOfWeek() {
        return this;
    }

    public java.time.DayOfWeek toDayOfWeek() {
        return java.time.DayOfWeek.of(positionInWeek());
    }

    @Override
    public String toString() {
        return name;
    }
}
