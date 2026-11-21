package org.chalten.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.chalten.testkit.ChaltenTest;
import org.junit.jupiter.api.Test;

class CoreSmokeTest extends ChaltenTest {

    @Test
    void runsOnJava21() {
        assertEquals(21, Runtime.version().feature());
    }

    @Test
    void sharedLocalesAreOnTheClasspath() {
        for (String resource : new String[] {
            "/locales/chronology.json",
            "/locales/en/chalten.json", "/locales/en/months.json", "/locales/en/units.json", "/locales/en/week-days.json",
            "/locales/es/chalten.json", "/locales/es/months.json", "/locales/es/units.json", "/locales/es/week-days.json",
        }) {
            assertNotNull(getClass().getResource(resource), resource);
        }
    }
}
