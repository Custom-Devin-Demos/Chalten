package org.chalten.timezones;

/**
 * Smalltalk {@code SkippedTimeError}: the local time does not exist in a time zone because of a daylight saving time
 * change.
 */
public final class SkippedTimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public SkippedTimeException(String messageText) {
        super(messageText);
    }

    /** Smalltalk {@code messageText}. */
    public String messageText() {
        return getMessage();
    }
}
