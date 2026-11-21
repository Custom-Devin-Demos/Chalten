package org.chalten.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Keeps {@code java/MIGRATION.md} honest: every {@code java} snippet must appear, line by line and in order, in
 * {@link MigrationGuideExamplesTest} (which asserts the values the guide shows), and every Java type and member named
 * in the API mapping table must exist, except in rows provided by unit U4.
 */
class MigrationGuideTest {

    private static final List<String> PACKAGES = List.of("org.chalten.core", "org.chalten.measure",
            "org.chalten.localization", "java.lang", "java.util", "java.util.function", "java.time");
    private static final Pattern CODE_SPAN = Pattern.compile("`([^`]+)`");
    private static final Pattern NAME = Pattern.compile(
            "(?<![A-Za-z0-9_.#])([A-Z][A-Za-z0-9]*[a-z][A-Za-z0-9]*)(?:[.#]([A-Za-z_][A-Za-z0-9_]*))?");

    private static List<String> lines(String property) throws IOException {
        return Files.readAllLines(Path.of(System.getProperty(property)));
    }

    private static List<List<String>> javaSnippets(List<String> guide) {
        List<List<String>> snippets = new ArrayList<>();
        List<String> current = null;
        for (String line : guide) {
            if (current == null && line.strip().equals("```java")) {
                current = new ArrayList<>();
            } else if (current != null && line.strip().equals("```")) {
                snippets.add(current);
                current = null;
            } else if (current != null && !line.isBlank()) {
                current.add(line.strip());
            }
        }
        return snippets;
    }

    @Test
    void everyJavaSnippetIsBackedByMigrationGuideExamplesTest() throws IOException {
        List<String> examples = lines("chalten.migrationGuideExamples").stream().map(String::strip).toList();
        List<List<String>> snippets = javaSnippets(lines("chalten.migrationGuide"));
        assertFalse(snippets.isEmpty(), "MIGRATION.md has no java snippets");
        for (List<String> snippet : snippets) {
            int position = -1;
            for (String line : snippet) {
                int found = examples.subList(position + 1, examples.size()).indexOf(line);
                assertTrue(found >= 0, () -> "Snippet line not found (in order) in MigrationGuideExamplesTest: "
                        + line + "\nSnippet:\n" + String.join("\n", snippet));
                position += found + 1;
            }
        }
    }

    private static List<String> apiTableRows(List<String> guide) {
        List<String> rows = new ArrayList<>();
        boolean inSection = false;
        for (String line : guide) {
            if (line.startsWith("## ")) {
                inSection = line.contains("API mapping");
            } else if (inSection && line.startsWith("|") && !line.startsWith("| ---")) {
                rows.add(line);
            }
        }
        return rows;
    }

    private static List<String> cells(String row) {
        return Arrays.stream(row.replace("\\|", "\u0000").split("\\|"))
                .map(cell -> cell.replace('\u0000', '|').strip())
                .toList();
    }

    private static Optional<Class<?>> resolve(String simpleName) {
        for (String packageName : PACKAGES) {
            try {
                return Optional.of(Class.forName(packageName + "." + simpleName));
            } catch (ClassNotFoundException e) {
                // try the next package
            }
        }
        return Optional.empty();
    }

    private static boolean hasPublicMember(Class<?> type, String member) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(member)) {
                return true;
            }
        }
        for (Field field : type.getFields()) {
            if (field.getName().equals(member) && Modifier.isPublic(field.getModifiers())) {
                return true;
            }
        }
        return false;
    }

    @Test
    void everyJavaNameInTheApiMappingTableExists() throws IOException {
        List<String> rows = apiTableRows(lines("chalten.migrationGuide"));
        assertTrue(rows.size() > 50, "API mapping table not found or too small: " + rows.size() + " rows");
        List<String> header = cells(rows.get(0));
        int javaColumn = header.indexOf("Java");
        int unitColumn = header.indexOf("Unit");
        assertTrue(javaColumn > 0 && unitColumn > 0, "API mapping table needs Java and Unit columns: " + header);
        List<String> missing = new ArrayList<>();
        int checked = 0;
        for (String row : rows.subList(1, rows.size())) {
            List<String> cells = cells(row);
            if (cells.get(unitColumn).contains("U4")) {
                continue;
            }
            Matcher spans = CODE_SPAN.matcher(cells.get(javaColumn));
            while (spans.find()) {
                Matcher names = NAME.matcher(spans.group(1));
                while (names.find()) {
                    String typeName = names.group(1);
                    String member = names.group(2);
                    Optional<Class<?>> type = resolve(typeName);
                    if (type.isEmpty()) {
                        missing.add(typeName);
                    } else if (member != null && !hasPublicMember(type.get(), member)) {
                        missing.add(typeName + "." + member);
                    }
                    checked++;
                }
            }
        }
        assertTrue(missing.isEmpty(), () -> "MIGRATION.md names Java APIs that do not exist: " + missing);
        assertTrue(checked > 100, "too few Java names checked: " + checked);
    }
}
