package org.chalten.core;

/**
 * Smalltalk {@code CalendarDayOfWeek}. Smalltalk {@code name} is {@link #fullName()} because {@code Enum.name()} is
 * final.
 */
public interface CalendarDayOfWeek {

    int positionInWeek();

    /** Smalltalk {@code name}: the localized day name. */
    String fullName();

    /** Smalltalk {@code shortName}: the localized abbreviated day name. */
    String shortName();

    CalendarDayOfWeek next();

    CalendarDayOfWeek previous();

    DayOfWeek asISODayOfWeek();
}
