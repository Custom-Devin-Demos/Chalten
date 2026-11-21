package org.chalten.testkit.parity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** The real {@code source/} tree, {@code PLAN.md} §8 and pending list agree (JUnit classes are checked by Gradle). */
class RepositoryParityTest {

    private static final Path REPOSITORY = Path.of(System.getProperty("chalten.repository", "../.."));

    @Test
    void theMatrixListsEverySUnitTestOfTheSourceTree() throws IOException {
        Map<String, List<String>> source =
                SUnitSourceParser.testSelectorsByKey(SUnitSourceParser.parseTree(REPOSITORY.resolve("source")));
        ParityMatrix matrix = ParityMatrix.parse(
                Files.readString(REPOSITORY.resolve("java/PLAN.md"), StandardCharsets.UTF_8));

        Map<String, Set<String>> sourceSelectors = new LinkedHashMap<>();
        source.forEach((key, selectors) -> sourceSelectors.put(key, Set.copyOf(selectors)));
        Map<String, Set<String>> matrixSelectors = new LinkedHashMap<>();
        matrix.rows().keySet().forEach(key -> matrixSelectors.put(key, Set.copyOf(matrix.selectors(key))));

        assertEquals(36, source.size());
        assertEquals(455, source.values().stream().mapToInt(List::size).sum());
        assertEquals(455, matrix.total());
        assertEquals(sourceSelectors, matrixSelectors);
    }

    @Test
    void thePendingListOnlyNamesU4Classes() throws IOException {
        ParityMatrix matrix = ParityMatrix.parse(
                Files.readString(REPOSITORY.resolve("java/PLAN.md"), StandardCharsets.UTF_8));
        Set<String> pending = TestParityChecker.parsePending(
                Files.readString(REPOSITORY.resolve("java/test-parity-pending.txt"), StandardCharsets.UTF_8));

        for (String key : pending) {
            assertEquals("U4", matrix.rows().get(key).owner(), key);
        }
    }
}
