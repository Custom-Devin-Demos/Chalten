package org.chalten.core;

import java.util.function.Consumer;
import org.chalten.localization.FormatterOptions;
import org.chalten.localization.LocalizedChronologyFormatter;
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

    /** Smalltalk {@code localized}: formatted in the medium style of {@link org.chalten.localization.CurrentLocale}. */
    default String localized() {
        return LocalizedChronologyFormatter.localized(this, options -> { });
    }

    /** Smalltalk {@code localizedWith:}: formatted in the style {@code configurer} chooses. */
    default String localized(Consumer<FormatterOptions> configurer) {
        return LocalizedChronologyFormatter.localized(this, configurer);
    }
}
