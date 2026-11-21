package org.chalten.measure;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class SmalltalkFloatPrinterTest {

    @ParameterizedTest
    @CsvSource({
        "1.5, 1.5", "1.52, 1.52", "1.55, 1.55", "100.0, 100.0", "1.0, 1.0", "0.5, 0.5", "-1.5, -1.5",
        "0.001, 0.001", "0.0001, 0.0001", "0.00012, 0.00012", "0.00001, 1.0e-5", "0.000012, 1.2e-5", "123456.0, 123456.0", "1234567.0, 1.234567e6",
        "1.0E7, 1.0e7", "1.0E20, 1.0e20", "0.1, 0.1", "0.3333333333333333, 0.3333333333333333",
        "0.0, 0.0", "-0.0, -0.0", "12.25, 12.25", "999999.5, 999999.5"
    })
    void printsLikePharo(double value, String expected) {
        assertThat(SmalltalkFloatPrinter.print(value)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"NaN, NaN", "Infinity, Float infinity", "-Infinity, Float negativeInfinity"})
    void printsSpecialValues(double value, String expected) {
        assertThat(SmalltalkFloatPrinter.print(value)).isEqualTo(expected);
    }
}
