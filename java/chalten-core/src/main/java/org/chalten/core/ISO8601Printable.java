package org.chalten.core;

/** Smalltalk {@code printAsISO8601On:}, understood by the values {@link ISO8601Formatter} formats. */
public interface ISO8601Printable {

    void printAsISO8601On(StringBuilder stream);

    /** Smalltalk {@code printAsISO8601String}. */
    default String printAsISO8601String() {
        StringBuilder stream = new StringBuilder();
        printAsISO8601On(stream);
        return stream.toString();
    }
}
