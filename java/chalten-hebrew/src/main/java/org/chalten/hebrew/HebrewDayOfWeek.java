package org.chalten.hebrew;

import java.util.List;
import org.chalten.core.CalendarDayOfWeek;
import org.chalten.core.DayOfWeek;
import org.chalten.localization.Translations;

/** Smalltalk {@code HebrewDayOfWeek}: the days of the Hebrew week, Yom Rishon (Sunday, 1) to Yom Shabbat (7). */
public enum HebrewDayOfWeek implements CalendarDayOfWeek {
    YOM_RISHON("Yom Rishon", "Rish"),
    YOM_SHENI("Yom Sheni", "She"),
    YOM_SHLISHI("Yom Shlishi", "Shli"),
    YOM_REVII("Yom Revii", "Rev"),
    YOM_HAMISHI("Yom Hamishi", "Ham"),
    YOM_SHISHI("Yom Shishi", "Shi"),
    YOM_SHABBAT("Yom Shabbat", "Shab");

    private static final List<HebrewDayOfWeek> WELL_KNOWN_DAYS = List.of(values());

    private final String name;
    private final String shortName;

    HebrewDayOfWeek(String name, String shortName) {
        this.name = name;
        this.shortName = shortName;
    }

    /** Smalltalk {@code HebrewDayOfWeek class>>dayAt:} (1-based, Yom Rishon first). */
    public static HebrewDayOfWeek dayAt(int positionInWeek) {
        return WELL_KNOWN_DAYS.get(positionInWeek - 1);
    }

    /** Smalltalk extension {@code DayOfWeek>>asHebrewDayOfWeek}. */
    public static HebrewDayOfWeek from(DayOfWeek dayOfWeek) {
        return dayAt(dayOfWeek.next().positionInWeek());
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
    public HebrewDayOfWeek next() {
        return dayAt(positionInWeek() % 7 + 1);
    }

    @Override
    public HebrewDayOfWeek previous() {
        return dayAt((positionInWeek() - 8) % 7 + 7);
    }

    public HebrewDayOfWeek asHebrewDayOfWeek() {
        return this;
    }

    @Override
    public DayOfWeek asISODayOfWeek() {
        return DayOfWeek.dayAt(previous().positionInWeek());
    }

    @Override
    public String toString() {
        return name;
    }
}
