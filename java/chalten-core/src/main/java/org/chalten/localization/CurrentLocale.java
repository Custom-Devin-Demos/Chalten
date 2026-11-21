package org.chalten.localization;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Buoy's {@code CurrentLocale} dynamic variable: the language tag of the current thread, {@code en} by default.
 *
 * <p>Inside {@link #use(String, Supplier)} the Chalten names are also translated into that tag's language.</p>
 */
public final class CurrentLocale {

    private static final String DEFAULT_LANGUAGE_TAG = "en";
    private static final InheritableThreadLocal<String> CURRENT = new InheritableThreadLocal<>();

    private CurrentLocale() {
    }

    /** Smalltalk {@code CurrentLocale value}. */
    public static String value() {
        String languageTag = CURRENT.get();
        return languageTag == null ? DEFAULT_LANGUAGE_TAG : languageTag;
    }

    /** Smalltalk {@code CurrentLocale use:during:}. */
    public static <T> T use(String languageTag, Supplier<T> action) {
        Objects.requireNonNull(languageTag, "languageTag");
        String previous = CURRENT.get();
        CURRENT.set(languageTag);
        try {
            return Translations.duringLanguage(languageOf(languageTag), action);
        } finally {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        }
    }

    /** The primary language subtag, as {@code es} of {@code es-AR}. */
    static String languageOf(String languageTag) {
        int separator = languageTag.indexOf('-');
        return (separator < 0 ? languageTag : languageTag.substring(0, separator)).toLowerCase(java.util.Locale.ROOT);
    }
}
