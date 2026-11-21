package org.chalten.localization;

/**
 * The configuration messages of {@link LocalizedChronologyFormatter}, received by the configurer of
 * {@code localized(Consumer<FormatterOptions>)}: one of CLDR's four styles, {@code medium} by default.
 *
 * <p>{@code short} and {@code long} are Java keywords, so the styles are named with a {@code Style} suffix.</p>
 */
public interface FormatterOptions {

    /** Smalltalk {@code short}. */
    void shortStyle();

    /** Smalltalk {@code medium}. */
    void mediumStyle();

    /** Smalltalk {@code long}. */
    void longStyle();

    /** Smalltalk {@code full}. */
    void fullStyle();
}
