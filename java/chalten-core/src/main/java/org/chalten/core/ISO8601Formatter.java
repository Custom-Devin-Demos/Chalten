package org.chalten.core;

/** Smalltalk {@code ISO8601Formatter}: delegates to each calendar magnitude's {@code printAsISO8601On:}. */
public final class ISO8601Formatter {

    public String format(ISO8601Printable calendarMagnitude) {
        StringBuilder stream = new StringBuilder();
        format(calendarMagnitude, stream);
        return stream.toString();
    }

    public void format(ISO8601Printable calendarMagnitude, StringBuilder stream) {
        calendarMagnitude.printAsISO8601On(stream);
    }
}
