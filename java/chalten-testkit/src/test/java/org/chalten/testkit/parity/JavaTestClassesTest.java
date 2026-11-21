package org.chalten.testkit.parity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;
import net.jqwik.api.Example;
import org.chalten.testkit.parity.JavaTestClasses.JavaTestClass;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class JavaTestClassesTest {

    private final JavaTestClasses classes = JavaTestClasses.reflective(getClass().getClassLoader());

    abstract static class AbstractFixture {

        @Test
        void testInherited() {
        }
    }

    static class Fixture extends AbstractFixture {

        @Test
        void testPlain() {
        }

        @Disabled("fixture")
        @Test
        void testDisabled() {
        }

        @Example
        void testJqwikExample() {
        }

        void testHelperWithoutAnnotation() {
        }
    }

    @Test
    void findsTheTestMethodsJUnitRunsIncludingInheritedOnes() {
        JavaTestClass fixture = classes.find(Fixture.class.getName()).orElseThrow();

        assertEquals(Set.of("testInherited", "testPlain", "testDisabled", "testJqwikExample"),
                fixture.testMethods());
        assertEquals(Set.of("testDisabled"), fixture.disabledMethods());
        assertTrue(fixture.runs("testInherited"));
        assertFalse(fixture.runs("testDisabled"));
        assertFalse(fixture.runs("testHelperWithoutAnnotation"));
        assertTrue(fixture.location().toString().replace('\\', '/').contains("/chalten-testkit/"));
    }

    @Test
    void abstractAndUnknownClassesAreNotTestClasses() {
        assertTrue(classes.find(AbstractFixture.class.getName()).isEmpty());
        assertTrue(classes.find("org.chalten.NoSuchTest").isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"a"})
    void parameterizedTestsTakeArgumentsAndAreNotSelectors(String value) {
        assertEquals("a", value);
    }
}
