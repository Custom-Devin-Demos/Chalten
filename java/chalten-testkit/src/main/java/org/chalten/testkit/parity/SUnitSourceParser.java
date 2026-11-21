package org.chalten.testkit.parity;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Reads the SUnit test classes of a Tonel source tree ({@code source/<Package>/<Class>.class.st} and
 * {@code <Class>.extension.st}) and computes their test selectors with the SUnit rules: a test is a unary
 * instance-side selector starting with {@code test}; abstract classes are not run; a subclass of an abstract test
 * class inherits its tests.
 */
public final class SUnitSourceParser {

    private static final Pattern CLASS_DEFINITION = Pattern.compile("(?m)^Class \\{(?s)(.*?)^}");
    private static final Pattern EXTENSION_DEFINITION = Pattern.compile("(?m)^Extension \\{\\s*#name\\s*:\\s*'(\\w+)'");
    private static final Pattern METHOD_HEADER =
            Pattern.compile("(?m)^(\\w+)( class)? >> ([^\\[\\r\\n]+?)\\s*\\[\\s*$");
    private static final Pattern EXTENSION_CATEGORY = Pattern.compile("#category\\s*:\\s*'\\*([\\w-]+)'");
    private static final Pattern UNARY_TEST_SELECTOR = Pattern.compile("test\\w*");

    private SUnitSourceParser() {
    }

    /** Parses every {@code *Test*.st} file below {@code sourceRoot}. */
    public static List<SUnitTestClass> parseTree(Path sourceRoot) {
        try (Stream<Path> files = Files.walk(sourceRoot)) {
            return files.filter(Files::isRegularFile)
                    .filter(file -> {
                        String name = file.getFileName().toString();
                        return name.endsWith(".st") && name.contains("Test");
                    })
                    .sorted()
                    .map(SUnitSourceParser::parseFile)
                    .toList();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Parses one Tonel class or extension file. */
    public static SUnitTestClass parseFile(Path file) {
        String text;
        try {
            text = Files.readString(file, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return parse(text, file);
    }

    static SUnitTestClass parse(String text, Path file) {
        String className;
        String superclassName = null;
        String packageName;
        boolean extension;
        Matcher classDefinition = CLASS_DEFINITION.matcher(text);
        Matcher extensionDefinition = EXTENSION_DEFINITION.matcher(text);
        if (classDefinition.find()) {
            String body = classDefinition.group(1);
            className = property(body, "name", file);
            superclassName = property(body, "superclass", file);
            packageName = optionalProperty(body, "package");
            if (packageName == null) {
                packageName = property(body, "category", file);
            }
            extension = false;
        } else if (extensionDefinition.find()) {
            className = extensionDefinition.group(1);
            Matcher category = EXTENSION_CATEGORY.matcher(text);
            packageName = category.find() ? category.group(1) : directoryName(file);
            extension = true;
        } else {
            throw new IllegalArgumentException(file + " is neither a Tonel class nor an extension definition");
        }
        Set<String> testSelectors = new LinkedHashSet<>();
        boolean isAbstract = false;
        Matcher method = METHOD_HEADER.matcher(text);
        while (method.find()) {
            if (!method.group(1).equals(className)) {
                throw new IllegalArgumentException(file + " defines a method of " + method.group(1)
                        + " in the file of " + className);
            }
            String selector = selector(method.group(3));
            boolean classSide = method.group(2) != null;
            if (classSide) {
                isAbstract |= selector.equals("isAbstract");
            } else if (UNARY_TEST_SELECTOR.matcher(selector).matches()) {
                testSelectors.add(selector);
            }
        }
        return new SUnitTestClass(packageName, className, extension, superclassName, isAbstract,
                new ArrayList<>(testSelectors), file);
    }

    /** {@code assert: a equals: b} → {@code assert:equals:}; unary and binary selectors are unchanged. */
    static String selector(String header) {
        String[] tokens = header.trim().split("\\s+");
        if (tokens.length == 1) {
            return tokens[0];
        }
        if (!tokens[0].endsWith(":")) {
            return tokens[0];
        }
        StringBuilder keywords = new StringBuilder();
        for (int index = 0; index < tokens.length; index += 2) {
            keywords.append(tokens[index]);
        }
        return keywords.toString();
    }

    /**
     * The selectors SUnit runs for each concrete test class, keyed by {@link SUnitTestClass#key()}: its own tests,
     * plus the tests inherited from abstract test superclasses. Abstract classes and classes without tests are
     * left out. An extension adds its tests to its own key (it is mapped to its own JUnit class).
     */
    public static Map<String, List<String>> testSelectorsByKey(List<SUnitTestClass> classes) {
        Map<String, SUnitTestClass> definitions = new LinkedHashMap<>();
        for (SUnitTestClass each : classes) {
            if (!each.extension() && definitions.put(each.className(), each) != null) {
                throw new IllegalArgumentException(each.className() + " is defined twice");
            }
        }
        Map<String, List<String>> selectorsByKey = new LinkedHashMap<>();
        for (SUnitTestClass each : classes) {
            if (each.isAbstract()) {
                continue;
            }
            Set<String> selectors = new LinkedHashSet<>(each.ownTestSelectors());
            if (!each.extension()) {
                SUnitTestClass superclass = definitions.get(each.superclassName());
                while (superclass != null && superclass.isAbstract()) {
                    selectors.addAll(superclass.ownTestSelectors());
                    superclass = definitions.get(superclass.superclassName());
                }
            }
            if (!selectors.isEmpty()) {
                if (selectorsByKey.put(each.key(), List.copyOf(selectors)) != null) {
                    throw new IllegalArgumentException(each.key() + " is defined twice");
                }
            }
        }
        return selectorsByKey;
    }

    private static String property(String body, String name, Path file) {
        String value = optionalProperty(body, name);
        if (value == null) {
            throw new IllegalArgumentException(file + " has no #" + name);
        }
        return value;
    }

    private static String optionalProperty(String body, String name) {
        Matcher matcher = Pattern.compile("#" + name + "\\s*:\\s*(?:#)?'([^']*)'").matcher(body);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String directoryName(Path file) {
        Path parent = file.toAbsolutePath().getParent();
        return parent == null ? "" : parent.getFileName().toString();
    }
}
