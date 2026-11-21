package org.chalten.timezones;

import org.chalten.core.TimeOffsetAwareGregorianDateTime;

/**
 * Smalltalk {@code AmbiguousTimeError}: the local date-time occurs twice in a time zone because of a daylight saving
 * time change. Carries both candidate absolute date-times.
 */
public final class AmbiguousTimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final transient TimeOffsetAwareGregorianDateTime earlierDateTime;
    private final transient TimeOffsetAwareGregorianDateTime laterDateTime;

    /** Smalltalk {@code AmbiguousTimeError class>>signalFor:and:withMessageText:}. */
    public AmbiguousTimeException(TimeOffsetAwareGregorianDateTime earlierDateTime,
            TimeOffsetAwareGregorianDateTime laterDateTime, String messageText) {
        super(messageText);
        this.earlierDateTime = earlierDateTime;
        this.laterDateTime = laterDateTime;
    }

    public TimeOffsetAwareGregorianDateTime earlierDateTime() {
        return earlierDateTime;
    }

    public TimeOffsetAwareGregorianDateTime laterDateTime() {
        return laterDateTime;
    }

    /** Smalltalk {@code messageText}. */
    public String messageText() {
        return getMessage();
    }
}
