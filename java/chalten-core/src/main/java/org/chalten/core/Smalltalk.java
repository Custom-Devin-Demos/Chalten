package org.chalten.core;

import java.math.BigInteger;
import org.chalten.measure.Fraction;

/** Helpers reproducing Smalltalk kernel behaviour the time model depends on. */
final class Smalltalk {

    private Smalltalk() {
    }

    /** {@code anInteger printStringLength: length padded: true}. */
    static String padded(long number, int length) {
        return padded(BigInteger.valueOf(number), length);
    }

    static String padded(BigInteger number, int length) {
        String digits = number.abs().toString();
        String sign = number.signum() < 0 ? "-" : "";
        int padding = length - digits.length() - sign.length();
        return sign + "0".repeat(Math.max(0, padding)) + digits;
    }

    static String padded(Fraction number, int length) {
        return number.isInteger() ? padded(number.numerator(), length) : number.toString();
    }

    /** Smalltalk {@code doesNotUnderstand:} for selectors that only some instances answer. */
    static UnsupportedOperationException doesNotUnderstand(Object receiver, String selector) {
        return new UnsupportedOperationException(receiver + " does not understand #" + selector);
    }

    static int toIntExact(Fraction number) {
        return number.intValueExact();
    }
}
