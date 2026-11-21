package org.chalten.testkit.parity;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.chalten.testkit.parity.JavaTestClasses.JavaTestClass;

/**
 * Fails the build when an SUnit test of {@code source/**} has no JUnit 5 counterpart.
 *
 * <p>For every concrete SUnit test class (and test-class extension) the checker requires a row in the
 * {@code java/PLAN.md} §8 matrix with the same package, test count and selectors, a JUnit class with the same simple
 * name (extensions: {@code <Class minus Test><Package suffix>Test}) in the listed module, and one enabled JUnit test
 * method per SUnit selector, named exactly like the selector. The per-test Status column must say {@code done} for
 * ported selectors and {@code pending} for the others.
 *
 * <p>Classes listed in {@code java/test-parity-pending.txt} may still miss JUnit methods (their missing selectors are
 * reported, not failed). An entry must name an SUnit class key and must be removed once the class is fully ported.
 */
public final class TestParityChecker {

    static final String DONE = "done";
    static final String PENDING = "pending";

    /**
     * The parity of one SUnit class.
     *
     * @param key the SUnit class key
     * @param junitClass the JUnit class the matrix maps it to ({@code null} if unmapped)
     * @param owner the owning unit
     * @param selectors the SUnit test selectors
     * @param ported the selectors that have an enabled same-named JUnit method
     * @param pending whether the class is on the pending list
     */
    public record ClassParity(String key, String junitClass, String owner, List<String> selectors,
            List<String> ported, boolean pending) {

        public List<String> missing() {
            return selectors.stream().filter(each -> !ported.contains(each)).toList();
        }
    }

    /**
     * The outcome of a check.
     *
     * @param classes the parity of every SUnit class, in source order
     * @param errors the violations; the check passes when there are none
     */
    public record Result(List<ClassParity> classes, List<String> errors) {

        public boolean passed() {
            return errors.isEmpty();
        }

        public int testCount() {
            return classes.stream().mapToInt(each -> each.selectors().size()).sum();
        }

        public int portedCount() {
            return classes.stream().mapToInt(each -> each.ported().size()).sum();
        }
    }

    private static final Pattern TEST_ROW =
            Pattern.compile("(?m)^(\\| `(\\w+)` \\| `\\w+\\(\\)` \\| )(\\w+)( \\|\\s*)$");
    private static final Pattern SECTION_START =
            Pattern.compile("(?m)^<details><summary><code>([^<]+)</code>");

    private TestParityChecker() {
    }

    /** Checks {@code sunitClasses} against the matrix, the pending list and the compiled JUnit classes. */
    public static Result check(List<SUnitTestClass> sunitClasses, ParityMatrix matrix, Set<String> pendingKeys,
            JavaTestClasses javaTestClasses) {
        Map<String, List<String>> selectorsByKey = SUnitSourceParser.testSelectorsByKey(sunitClasses);
        Map<String, SUnitTestClass> sourceByKey = new LinkedHashMap<>();
        sunitClasses.forEach(each -> sourceByKey.putIfAbsent(each.key(), each));
        List<String> errors = new ArrayList<>();
        List<ClassParity> classes = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : selectorsByKey.entrySet()) {
            String key = entry.getKey();
            List<String> selectors = entry.getValue();
            SUnitTestClass source = sourceByKey.get(key);
            boolean pending = pendingKeys.contains(key);
            ParityMatrix.Row row = matrix.rows().get(key);
            if (row == null) {
                errors.add(key + " (" + source.file() + ") has " + selectors.size()
                        + " SUnit tests but no row in PLAN.md §8");
                classes.add(new ClassParity(key, null, null, selectors, List.of(), pending));
                continue;
            }
            checkRow(key, source, selectors, row, matrix, errors);
            Optional<JavaTestClass> javaClass = javaTestClasses.find(row.junitClass());
            if (javaClass.isPresent()) {
                checkLocation(key, row, javaClass.get(), errors);
            }
            List<String> ported = selectors.stream()
                    .filter(selector -> javaClass.map(each -> each.runs(selector)).orElse(false))
                    .toList();
            if (!pending) {
                if (javaClass.isEmpty()) {
                    errors.add(key + ": JUnit class " + row.junitClass() + " not found (or abstract)");
                } else {
                    for (String selector : selectors) {
                        if (!ported.contains(selector)) {
                            errors.add(key + ">>" + selector + " has no enabled JUnit test method "
                                    + row.junitClass() + "#" + selector + "()");
                        }
                    }
                }
            } else if (ported.size() == selectors.size()) {
                errors.add(key + " is fully ported: remove it from test-parity-pending.txt");
            }
            Map<String, String> statuses = matrix.statuses().getOrDefault(key, Map.of());
            for (String selector : selectors) {
                String expected = ported.contains(selector) ? DONE : PENDING;
                String actual = statuses.get(selector);
                if (actual != null && !actual.equals(expected)) {
                    errors.add("PLAN.md §8 says " + key + ">>" + selector + " is " + actual + " but it is "
                            + expected + " (run ./gradlew updateTestParityStatus)");
                }
            }
            classes.add(new ClassParity(key, row.junitClass(), row.owner(), selectors, ported, pending));
        }
        for (String key : matrix.rows().keySet()) {
            if (!selectorsByKey.containsKey(key)) {
                errors.add("PLAN.md §8 lists " + key + " but source/ has no such SUnit test class");
            }
        }
        for (String key : pendingKeys) {
            if (!selectorsByKey.containsKey(key)) {
                errors.add("test-parity-pending.txt lists " + key + " but source/ has no such SUnit test class");
            }
        }
        int total = selectorsByKey.values().stream().mapToInt(List::size).sum();
        if (matrix.total() != total) {
            errors.add("PLAN.md §8 total is " + matrix.total() + " but source/ has " + total + " SUnit tests");
        }
        return new Result(List.copyOf(classes), List.copyOf(errors));
    }

    private static void checkRow(String key, SUnitTestClass source, List<String> selectors, ParityMatrix.Row row,
            ParityMatrix matrix, List<String> errors) {
        if (!row.sunitPackage().equals(source.packageName())) {
            errors.add(key + " is in package " + source.packageName() + " but PLAN.md §8 says "
                    + row.sunitPackage());
        }
        if (row.testCount() != selectors.size()) {
            errors.add(key + " has " + selectors.size() + " SUnit tests but PLAN.md §8 says " + row.testCount());
        }
        Set<String> listed = new LinkedHashSet<>(matrix.selectors(key));
        for (String selector : selectors) {
            if (!listed.remove(selector)) {
                errors.add(key + ">>" + selector + " is missing from the PLAN.md §8 per-test matrix");
            }
        }
        for (String extra : listed) {
            errors.add("PLAN.md §8 lists " + key + ">>" + extra + " but source/ has no such SUnit test");
        }
        String simpleName = row.junitClass().substring(row.junitClass().lastIndexOf('.') + 1);
        String expectedName = expectedJUnitSimpleName(source);
        if (!simpleName.equals(expectedName)) {
            errors.add(key + " must be ported to a JUnit class named " + expectedName + ", not " + simpleName);
        }
    }

    /**
     * The simple name the JUnit class must have: the SUnit class name, or for an extension
     * {@code GregorianDateTimeTest} of {@code Chalten-Timezones-Tests}, {@code GregorianDateTimeTimezonesTest}.
     */
    static String expectedJUnitSimpleName(SUnitTestClass source) {
        if (!source.extension()) {
            return source.className();
        }
        String base = source.className().endsWith("Test")
                ? source.className().substring(0, source.className().length() - "Test".length())
                : source.className();
        String[] words = source.packageName().split("-");
        int last = words.length - 1;
        if (last > 0 && words[last].equals("Tests")) {
            last--;
        }
        return base + words[last] + "Test";
    }

    private static void checkLocation(String key, ParityMatrix.Row row, JavaTestClass javaClass,
            List<String> errors) {
        if (javaClass.location() == null) {
            return;
        }
        String location = javaClass.location().toString().replace('\\', '/');
        if (!location.contains("/" + row.module() + "/")) {
            errors.add(key + ": " + row.junitClass() + " was loaded from " + location + ", not from module "
                    + row.module());
        }
    }

    /** Reads a pending list: one SUnit class key per line; {@code #} starts a comment. */
    public static Set<String> parsePending(String text) {
        Set<String> keys = new LinkedHashSet<>();
        for (String line : text.split("\\R")) {
            int comment = line.indexOf('#');
            String key = (comment < 0 ? line : line.substring(0, comment)).trim();
            if (!key.isEmpty() && !keys.add(key)) {
                throw new IllegalArgumentException("test-parity-pending.txt lists " + key + " twice");
            }
        }
        return keys;
    }

    /** Rewrites the per-test Status column of §8 from {@code result}. */
    public static String updateStatuses(String plan, Result result) {
        Map<String, List<String>> portedByKey = new LinkedHashMap<>();
        result.classes().forEach(each -> portedByKey.put(each.key(), each.ported()));
        StringBuilder updated = new StringBuilder();
        String currentKey = null;
        for (String line : plan.split("(?<=\\n)")) {
            Matcher section = SECTION_START.matcher(line);
            if (section.find()) {
                currentKey = section.group(1);
            }
            Matcher test = TEST_ROW.matcher(line);
            List<String> ported = portedByKey.get(currentKey);
            if (ported != null && test.find()) {
                String status = ported.contains(test.group(2)) ? DONE : PENDING;
                line = test.replaceFirst(Matcher.quoteReplacement(test.group(1) + status + test.group(4)));
            }
            updated.append(line);
        }
        return updated.toString();
    }

    /** A Markdown report of {@code result}. */
    public static String report(Result result) {
        StringBuilder report = new StringBuilder();
        report.append("# SUnit → JUnit 5 test parity\n\n");
        report.append(result.passed() ? "Passed" : "**Failed**").append(": ").append(result.portedCount())
                .append(" of ").append(result.testCount()).append(" SUnit tests have a JUnit counterpart.\n\n");
        report.append("| SUnit class | JUnit 5 class | Owner | Ported | State |\n");
        report.append("| --- | --- | --- | --- | --- |\n");
        for (ClassParity each : result.classes()) {
            String state = each.missing().isEmpty() ? DONE
                    : each.pending() ? PENDING + " (listed in test-parity-pending.txt)" : "**missing**";
            report.append("| `").append(each.key()).append("` | `").append(each.junitClass()).append("` | ")
                    .append(each.owner()).append(" | ").append(each.ported().size()).append('/')
                    .append(each.selectors().size()).append(" | ").append(state).append(" |\n");
        }
        if (!result.passed()) {
            report.append("\n## Errors\n\n");
            result.errors().forEach(error -> report.append("- ").append(error).append('\n'));
        }
        return report.toString();
    }

    /**
     * Runs the check from Gradle: {@code <repository root> <report file> [--update-plan]}. The JUnit classes are
     * looked up on the checker's class path.
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 2) {
            throw new IllegalArgumentException("usage: TestParityChecker <repository root> <report> [--update-plan]");
        }
        Path repository = Path.of(args[0]);
        Path reportFile = Path.of(args[1]);
        boolean updatePlan = args.length > 2 && args[2].equals("--update-plan");
        Path planFile = repository.resolve("java/PLAN.md");
        String plan = Files.readString(planFile, StandardCharsets.UTF_8);
        Result result = check(SUnitSourceParser.parseTree(repository.resolve("source")), ParityMatrix.parse(plan),
                parsePending(Files.readString(repository.resolve("java/test-parity-pending.txt"),
                        StandardCharsets.UTF_8)),
                JavaTestClasses.reflective(TestParityChecker.class.getClassLoader()));
        if (updatePlan) {
            Files.writeString(planFile, updateStatuses(plan, result), StandardCharsets.UTF_8);
            System.out.println("Updated the §8 Status column of " + planFile);
            return;
        }
        Files.createDirectories(reportFile.toAbsolutePath().getParent());
        Files.writeString(reportFile, report(result), StandardCharsets.UTF_8);
        for (ClassParity each : result.classes()) {
            if (each.pending() && !each.missing().isEmpty()) {
                System.out.println("pending: " + each.key() + " " + each.ported().size() + "/"
                        + each.selectors().size() + " (" + each.owner() + ")");
            }
        }
        System.out.println("Test parity: " + result.portedCount() + "/" + result.testCount()
                + " SUnit tests ported; report: " + reportFile);
        if (!result.passed()) {
            result.errors().forEach(error -> System.err.println("ERROR: " + error));
            System.exit(1);
        }
    }
}
