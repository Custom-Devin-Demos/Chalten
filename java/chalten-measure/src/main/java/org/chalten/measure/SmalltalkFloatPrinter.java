package org.chalten.measure;

import java.math.BigDecimal;

/**
 * Prints a {@code double} exactly like Pharo's {@code Float>>printOn:} ({@code absPrintExactlyOn:base:}): the
 * shortest digits that read back to the same value, in fixed notation when the decimal exponent is between -3 and 6
 * ({@code 1.52}, {@code 100.0}, {@code 0.001}) and in Smalltalk scientific notation otherwise ({@code 1.0e7}).
 */
final class SmalltalkFloatPrinter {

    private SmalltalkFloatPrinter() {
    }

    static String print(double value) {
        if (Double.isNaN(value)) {
            return "NaN";
        }
        if (Double.isInfinite(value)) {
            return value > 0 ? "Float infinity" : "Float negativeInfinity";
        }
        if (value == 0) {
            return (1 / value < 0) ? "-0.0" : "0.0";
        }
        StringBuilder out = new StringBuilder();
        if (value < 0) {
            out.append('-');
        }
        BigDecimal shortest = new BigDecimal(Double.toString(Math.abs(value))).stripTrailingZeros();
        String digits = shortest.unscaledValue().toString();
        int exponent = digits.length() - shortest.scale();
        boolean fixed = exponent >= -3 && exponent <= 6;
        int pointPosition = fixed ? exponent : 1;
        if (fixed && exponent <= 0) {
            out.append("0.").append("0".repeat(-exponent));
        }
        for (int i = 0; i < digits.length(); i++) {
            out.append(digits.charAt(i));
            if (i + 1 == pointPosition && i + 1 < digits.length()) {
                out.append('.');
            }
        }
        if (pointPosition >= digits.length()) {
            out.append("0".repeat(pointPosition - digits.length())).append(".0");
        }
        if (!fixed) {
            out.append('e').append(exponent - 1);
        }
        return out.toString();
    }
}
