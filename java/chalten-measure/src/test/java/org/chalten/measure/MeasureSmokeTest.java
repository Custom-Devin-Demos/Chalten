package org.chalten.measure;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.chalten.testkit.ChaltenTest;
import org.junit.jupiter.api.Test;

class MeasureSmokeTest extends ChaltenTest {

    @Test
    void runsOnJava21() {
        assertEquals(21, Runtime.version().feature());
    }
}
