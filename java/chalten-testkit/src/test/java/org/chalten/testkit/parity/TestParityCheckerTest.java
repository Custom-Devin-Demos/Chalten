package org.chalten.testkit.parity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.chalten.testkit.parity.JavaTestClasses.JavaTestClass;
import org.junit.jupiter.api.Test;

class TestParityCheckerTest {

    private static final Path FILE = Path.of("Fixture.st");
    private static final Path CORE_CLASSES = Path.of("/repo/java/chalten-core/build/classes/java/test");

    private static final List<SUnitTestClass> SOURCES = List.of(
            new SUnitTestClass("Core-Tests", "DateTest", false, "ChaltenTest", false,
                    List.of("testA", "testB"), FILE),
            new SUnitTestClass("Zones-Tests", "DateTest", true, null, false, List.of("testZone"), FILE));

    private static String plan(String dateStatusA, String dateStatusB, String zoneStatus) {
        return """
                # Plan

                ## 8. Test-parity matrix

                | # | SUnit package | SUnit class | Tests | Java module | JUnit 5 class | Owner unit |
                | --- | --- | --- | --- | --- | --- | --- |
                | 1 | `Core-Tests` | `DateTest` | 2 | `chalten-core` | `org.chalten.core.DateTest` | U2 |
                | 2 | `Zones-Tests` | `DateTest (extension)` | 1 | `chalten-timezones` | `org.chalten.timezones.DateZonesTest` | U4 |
                | | | **Total** | **3** | | | |

                <details><summary><code>DateTest</code> → <code>org.chalten.core.DateTest</code> (2 tests, U2)</summary>

                | SUnit selector | JUnit method | Status |
                | --- | --- | --- |
                | `testA` | `testA()` | %s |
                | `testB` | `testB()` | %s |

                </details>

                <details><summary><code>DateTest (extension)</code> → <code>org.chalten.timezones.DateZonesTest</code> (1 test, U4)</summary>

                | SUnit selector | JUnit method | Status |
                | --- | --- | --- |
                | `testZone` | `testZone()` | %s |

                </details>

                ## 9. Next
                """.formatted(dateStatusA, dateStatusB, zoneStatus);
    }

    private static JavaTestClasses classes(JavaTestClass... classes) {
        Map<String, JavaTestClass> byName = new java.util.HashMap<>();
        for (JavaTestClass each : classes) {
            byName.put(each.name(), each);
        }
        return name -> Optional.ofNullable(byName.get(name));
    }

    private static JavaTestClass dateTest(Set<String> tests, Set<String> disabled) {
        return new JavaTestClass("org.chalten.core.DateTest", CORE_CLASSES, tests, disabled);
    }

    @Test
    void passesWhenEverySelectorIsPortedOrItsClassIsPending() {
        TestParityChecker.Result result = TestParityChecker.check(SOURCES,
                ParityMatrix.parse(plan("done", "done", "pending")), Set.of("DateTest (extension)"),
                classes(dateTest(Set.of("testA", "testB", "javaOnlyTest"), Set.of())));

        assertEquals(List.of(), result.errors());
        assertTrue(result.passed());
        assertEquals(3, result.testCount());
        assertEquals(2, result.portedCount());
        assertEquals(List.of("testZone"), result.classes().get(1).missing());
        assertTrue(TestParityChecker.report(result).contains("| `DateTest (extension)` | "
                + "`org.chalten.timezones.DateZonesTest` | U4 | 0/1 | pending (listed in test-parity-pending.txt) |"));
    }

    @Test
    void failsWhenASelectorHasNoEnabledJUnitMethod() {
        TestParityChecker.Result result = TestParityChecker.check(SOURCES,
                ParityMatrix.parse(plan("done", "pending", "pending")), Set.of("DateTest (extension)"),
                classes(dateTest(Set.of("testA", "testB"), Set.of("testB"))));

        assertEquals(List.of("DateTest>>testB has no enabled JUnit test method org.chalten.core.DateTest#testB()"),
                result.errors());
        assertTrue(TestParityChecker.report(result).contains("**Failed**"));
    }

    @Test
    void failsWhenAClassThatIsNotPendingHasNoJUnitClass() {
        TestParityChecker.Result result = TestParityChecker.check(SOURCES,
                ParityMatrix.parse(plan("pending", "pending", "pending")), Set.of(), classes());

        assertEquals(List.of("DateTest: JUnit class org.chalten.core.DateTest not found (or abstract)",
                "DateTest (extension): JUnit class org.chalten.timezones.DateZonesTest not found (or abstract)"),
                result.errors());
    }

    @Test
    void failsWhenThePlanStatusDisagrees() {
        TestParityChecker.Result result = TestParityChecker.check(SOURCES,
                ParityMatrix.parse(plan("pending", "done", "done")), Set.of("DateTest (extension)"),
                classes(dateTest(Set.of("testA", "testB"), Set.of())));

        assertEquals(List.of(
                "PLAN.md §8 says DateTest>>testA is pending but it is done (run ./gradlew updateTestParityStatus)",
                "PLAN.md §8 says DateTest (extension)>>testZone is done but it is pending"
                        + " (run ./gradlew updateTestParityStatus)"),
                result.errors());
    }

    @Test
    void failsWhenAPendingClassIsFullyPortedOrUnknown() {
        TestParityChecker.Result result = TestParityChecker.check(SOURCES,
                ParityMatrix.parse(plan("done", "done", "pending")), Set.of("DateTest", "DateTest (extension)",
                        "GoneTest"),
                classes(dateTest(Set.of("testA", "testB"), Set.of())));

        assertEquals(List.of("DateTest is fully ported: remove it from test-parity-pending.txt",
                "test-parity-pending.txt lists GoneTest but source/ has no such SUnit test class"),
                result.errors());
    }

    @Test
    void failsWhenTheMatrixDriftsFromTheSource() {
        List<SUnitTestClass> sources = List.of(
                new SUnitTestClass("Core-Tests", "DateTest", false, "ChaltenTest", false,
                        List.of("testA", "testC"), FILE),
                new SUnitTestClass("Core-Tests", "NewTest", false, "ChaltenTest", false, List.of("testNew"), FILE));

        TestParityChecker.Result result = TestParityChecker.check(sources,
                ParityMatrix.parse(plan("done", "done", "pending")), Set.of("NewTest"),
                classes(dateTest(Set.of("testA", "testB", "testC"), Set.of())));

        assertEquals(List.of("DateTest>>testC is missing from the PLAN.md §8 per-test matrix",
                "PLAN.md §8 lists DateTest>>testB but source/ has no such SUnit test",
                "NewTest (" + FILE + ") has 1 SUnit tests but no row in PLAN.md §8",
                "PLAN.md §8 lists DateTest (extension) but source/ has no such SUnit test class"),
                result.errors());
    }

    @Test
    void failsWhenTheJUnitClassIsMisnamedOrInAnotherModule() {
        List<SUnitTestClass> sources = List.of(new SUnitTestClass("Core-Tests", "DateTest", false, "ChaltenTest",
                false, List.of("testA", "testB"), FILE));
        String plan = plan("done", "done", "pending").replace("`chalten-core`", "`chalten-measure`")
                .replace("org.chalten.core.DateTest", "org.chalten.core.DatesTest")
                .replaceAll("(?s)\\| 2 \\| `Zones.*?\\n", "").replaceAll("(?s)<details><summary><code>DateTest \\(ext.*?</details>", "")
                .replace("**3**", "**2**");

        TestParityChecker.Result result = TestParityChecker.check(sources, ParityMatrix.parse(plan), Set.of(),
                classes(new JavaTestClass("org.chalten.core.DatesTest", CORE_CLASSES, Set.of("testA", "testB"),
                        Set.of())));

        assertEquals(List.of("DateTest must be ported to a JUnit class named DateTest, not DatesTest",
                "DateTest: org.chalten.core.DatesTest was loaded from " + CORE_CLASSES.toString().replace('\\', '/')
                        + ", not from module chalten-measure"),
                result.errors());
    }

    @Test
    void extensionsArePortedToAClassNamedAfterTheirPackage() {
        assertEquals("GregorianDateTimeTimezonesTest", TestParityChecker.expectedJUnitSimpleName(
                new SUnitTestClass("Chalten-Timezones-Tests", "GregorianDateTimeTest", true, null, false,
                        List.of(), FILE)));
        assertEquals("DateTest", TestParityChecker.expectedJUnitSimpleName(SOURCES.get(0)));
    }

    @Test
    void updatesTheStatusColumnFromTheResult() {
        String plan = plan("pending", "pending", "done");
        TestParityChecker.Result result = TestParityChecker.check(SOURCES, ParityMatrix.parse(plan),
                Set.of("DateTest (extension)"), classes(dateTest(Set.of("testA", "testB"), Set.of())));

        assertFalse(result.passed());
        assertEquals(plan("done", "done", "pending"), TestParityChecker.updateStatuses(plan, result));
    }

    @Test
    void readsThePendingList() {
        assertEquals(Set.of("DateTest", "DateTest (extension)"), TestParityChecker.parsePending("""
                # comment

                DateTest
                DateTest (extension)  # U4
                """));
        assertThrows(IllegalArgumentException.class, () -> TestParityChecker.parsePending("A\nA\n"));
    }

    @Test
    void theMatrixNeedsASection8() {
        assertThrows(IllegalArgumentException.class, () -> ParityMatrix.parse("# Plan\n\n## 7. Other\n"));
    }
}
