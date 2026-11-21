package org.chalten.islamic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.chalten.testkit.ChaltenTest;
import org.junit.jupiter.api.Test;

class IslamicSmokeTest extends ChaltenTest {

    @Test
    void runsOnJava21() {
        assertEquals(21, Runtime.version().feature());
    }

    @Test
    void seesTheCoreModuleResources() {
        assertNotNull(getClass().getResource("/locales/chronology.json"));
    }
}
