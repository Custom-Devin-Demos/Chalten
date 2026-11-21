package org.chalten.islamic;

import java.util.List;
import org.chalten.core.CalendarDayOfWeek;
import org.chalten.core.DayOfWeek;
import org.chalten.localization.Translations;

/** Smalltalk {@code IslamicDayOfWeek}: the days of the Islamic week, Yawn al-Ahad (Sunday, 1) to Yawn as-Sabt (7). */
public enum IslamicDayOfWeek implements CalendarDayOfWeek {
    YAWN_AL_AHAD("Yawn al-Ahad", "Ahd"),
    YAWN_AL_ITHNAYN("Yawn al-Ithnayn", "Ith"),
    YAWN_ATH_THULATHA("Yawn ath-Thulatha", "Thu"),
    YAWN_AL_ARBAA("Yawn al-Arba'a'", "Arb"),
    YAWN_AL_KHAMEES("Yawn al-Khamees", "Kha"),
    YAWN_AL_JUMUHA("Yawn al-Jumu'ah", "Jum"),
    YAWN_AS_SABT("Yawn as-Sabt", "Sab");

    private static final List<IslamicDayOfWeek> WELL_KNOWN_DAYS = List.of(values());

    private final String name;
    private final String shortName;

    IslamicDayOfWeek(String name, String shortName) {
        this.name = name;
        this.shortName = shortName;
    }

    /** Smalltalk {@code IslamicDayOfWeek class>>dayAt:} (1-based, Yawn al-Ahad first). */
    public static IslamicDayOfWeek dayAt(int positionInWeek) {
        return WELL_KNOWN_DAYS.get(positionInWeek - 1);
    }

    /** Smalltalk extension {@code DayOfWeek>>asIslamicDayOfWeek}. */
    public static IslamicDayOfWeek from(DayOfWeek dayOfWeek) {
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
    public IslamicDayOfWeek next() {
        return dayAt(positionInWeek() % 7 + 1);
    }

    @Override
    public IslamicDayOfWeek previous() {
        return dayAt((positionInWeek() - 8) % 7 + 7);
    }

    public IslamicDayOfWeek asIslamicDayOfWeek() {
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
