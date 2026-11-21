package org.chalten.testkit.parity;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The test-parity matrix of {@code java/PLAN.md} §8: one summary row per SUnit class and one status row per SUnit
 * selector.
 *
 * @param rows the summary rows keyed by SUnit class key ({@code GregorianDateTimeTest (extension)} for extensions)
 * @param statuses the per-test status ({@code done} or {@code pending}) keyed by SUnit class key, then by selector
 * @param total the {@code **Total**} test count of the summary table
 */
public record ParityMatrix(Map<String, Row> rows, Map<String, Map<String, String>> statuses, int total) {

    private static final Pattern SUMMARY_ROW = Pattern.compile(
            "(?m)^\\| (\\d+) \\| `([^`]+)` \\| `([^`]+)` \\| (\\d+) \\| `([^`]+)` \\| `([^`]+)` \\| (\\w+) \\|\\s*$");
    private static final Pattern TOTAL_ROW = Pattern.compile("(?m)^\\|.*\\*\\*Total\\*\\* \\| \\*\\*(\\d+)\\*\\* \\|");
    private static final Pattern SECTION = Pattern.compile(
            "(?s)<details><summary><code>([^<]+)</code> → <code>([^<]+)</code>.*?</summary>(.*?)</details>");
    private static final Pattern TEST_ROW =
            Pattern.compile("(?m)^\\| `(\\w+)` \\| `(\\w+)\\(\\)` \\| (\\w+) \\|\\s*$");

    /**
     * A summary row.
     *
     * @param sunitPackage the SUnit package
     * @param testCount the number of SUnit tests
     * @param module the Gradle module holding the JUnit class
     * @param junitClass the fully qualified JUnit 5 class name
     * @param owner the owning unit
     */
    public record Row(String sunitPackage, int testCount, String module, String junitClass, String owner) {
    }

    public ParityMatrix {
        rows = Map.copyOf(rows);
        statuses = Map.copyOf(statuses);
    }

    /** Parses the §8 tables of {@code PLAN.md}. */
    public static ParityMatrix parse(String planText) {
        String plan = section8(planText);
        Map<String, Row> rows = new LinkedHashMap<>();
        Matcher summary = SUMMARY_ROW.matcher(plan);
        while (summary.find()) {
            Row row = new Row(summary.group(2), Integer.parseInt(summary.group(4)), summary.group(5),
                    summary.group(6), summary.group(7));
            if (rows.put(summary.group(3), row) != null) {
                throw new IllegalArgumentException("PLAN.md §8 lists " + summary.group(3) + " twice");
            }
        }
        Matcher totalRow = TOTAL_ROW.matcher(plan);
        int total = totalRow.find() ? Integer.parseInt(totalRow.group(1)) : -1;
        Map<String, Map<String, String>> statuses = new LinkedHashMap<>();
        Matcher section = SECTION.matcher(plan);
        while (section.find()) {
            String key = section.group(1);
            Map<String, String> byTest = new LinkedHashMap<>();
            Matcher test = TEST_ROW.matcher(section.group(3));
            while (test.find()) {
                if (!test.group(1).equals(test.group(2))) {
                    throw new IllegalArgumentException("PLAN.md §8 maps " + key + ">>" + test.group(1) + " to "
                            + test.group(2) + "(): JUnit methods must have the SUnit selector name");
                }
                byTest.put(test.group(1), test.group(3));
            }
            if (statuses.put(key, byTest) != null) {
                throw new IllegalArgumentException("PLAN.md §8 has two per-test sections for " + key);
            }
            Row row = rows.get(key);
            if (row != null && !row.junitClass().equals(section.group(2))) {
                throw new IllegalArgumentException("PLAN.md §8 maps " + key + " to " + row.junitClass()
                        + " in the summary but to " + section.group(2) + " in the per-test matrix");
            }
        }
        return new ParityMatrix(rows, statuses, total);
    }

    /** The text of §8, from its heading to the next {@code ## } heading. */
    static String section8(String plan) {
        int start = plan.indexOf("\n## 8.");
        if (start < 0) {
            throw new IllegalArgumentException("PLAN.md has no '## 8.' test-parity matrix section");
        }
        int end = plan.indexOf("\n## ", start + 1);
        return end < 0 ? plan.substring(start) : plan.substring(start, end);
    }

    /** The selectors listed for {@code key} in the per-test matrix. */
    public List<String> selectors(String key) {
        return List.copyOf(statuses.getOrDefault(key, Map.of()).keySet());
    }
}
