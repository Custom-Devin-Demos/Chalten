package org.chalten.localization;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import org.chalten.measure.ChaltenAssertionException;

/**
 * Buoy's {@code LocaleConventions} with Chalten's {@code chronology} category: the date and time patterns and the
 * AM and PM marks of each language range, from the most specific ({@code es-AR}) to the default one ({@code *}).
 *
 * <p>{@link #current()} holds the conventions shipped in the repository-root {@code locales/chronology.json}.</p>
 */
public final class LocaleConventions {

    static final String CHRONOLOGY = "chronology";

    private static final List<String> CHRONOLOGY_PATTERN_NAMES = List.of("shortDateFormat", "mediumDateFormat",
            "longDateFormat", "fullDateFormat", "shortTimeFormat", "mediumTimeFormat", "longTimeFormat",
            "fullTimeFormat", "shortDateTimeFormat", "mediumDateTimeFormat", "longDateTimeFormat",
            "fullDateTimeFormat", "shortYearMonthFormat", "mediumYearMonthFormat", "longYearMonthFormat",
            "shortMonthDayFormat", "mediumMonthDayFormat", "longMonthDayFormat");
    private static final List<String> CHRONOLOGY_PROPERTY_NAMES = concat(CHRONOLOGY_PATTERN_NAMES,
            List.of("amMark", "pmMark"));

    private static volatile LocaleConventions current;

    private final Map<String, List<String>> propertyNamesByCategory = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Map<String, String>>> propertiesByCategory = new ConcurrentHashMap<>();

    /** Smalltalk {@code LocaleConventions current}: the conventions shipped with Chalten. */
    public static LocaleConventions current() {
        LocaleConventions conventions = current;
        if (conventions == null) {
            synchronized (LocaleConventions.class) {
                conventions = current;
                if (conventions == null) {
                    conventions = new LocaleConventions();
                    conventions.loadJSONChronologyConventionsFromString(readResource("/locales/chronology.json"));
                    current = conventions;
                }
            }
        }
        return conventions;
    }

    public static List<String> chronologyPatternNames() {
        return CHRONOLOGY_PATTERN_NAMES;
    }

    public static List<String> chronologyPropertyNames() {
        return CHRONOLOGY_PROPERTY_NAMES;
    }

    /** Smalltalk {@code chronologyPropertyNamed:for:}. */
    public String chronologyPropertyNamed(String name, String languageTag) {
        return propertyNamed(name, CHRONOLOGY, languageTag);
    }

    /**
     * Smalltalk {@code propertyNamed:of:for:}: the value in the most specific language range defining it.
     *
     * @throws NoSuchElementException {@code There's no <name> convention for <languageTag>} (Smalltalk
     *     {@code ObjectNotFound})
     */
    public String propertyNamed(String name, String category, String languageTag) {
        Map<String, Map<String, String>> propertiesByRange = propertiesByCategory.getOrDefault(category, Map.of());
        String range = languageTag;
        while (true) {
            Map<String, String> properties = propertiesByRange.get(range);
            if (properties != null && properties.containsKey(name)) {
                return properties.get(name);
            }
            if (range.equals("*")) {
                throw new NoSuchElementException("There's no " + name + " convention for " + languageTag);
            }
            int separator = range.lastIndexOf('-');
            range = separator < 0 ? "*" : range.substring(0, separator);
        }
    }

    /** Smalltalk {@code loadJSONChronologyConventionsFrom:}. */
    public void loadJSONChronologyConventionsFrom(Path file) {
        try {
            loadJSONChronologyConventionsFromString(Files.readString(file, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    /**
     * Smalltalk {@code loadJSONChronologyConventionsFromString:}: merges the conventions property by property, so
     * the loaded ones override the ones already there. Every pattern is parsed first, so one with an unsupported
     * field is rejected before loading anything.
     *
     * @throws org.chalten.measure.InstanceCreationFailedException for unsupported patterns
     * @throws ChaltenAssertionException {@code <name> is not a convention of the chronology category}
     */
    public void loadJSONChronologyConventionsFromString(String json) {
        Map<String, Map<String, String>> conventions = new JsonReader(json).readConventions();
        conventions.values().forEach(properties -> properties.forEach((name, value) -> {
            if (CHRONOLOGY_PATTERN_NAMES.contains(name)) {
                ChronologyPattern.fromString(value);
            }
        }));
        propertyNamesByCategory.put(CHRONOLOGY, CHRONOLOGY_PROPERTY_NAMES);
        loadConventionsOf(CHRONOLOGY, conventions);
    }

    private void loadConventionsOf(String category, Map<String, Map<String, String>> conventions) {
        List<String> propertyNames = propertyNamesByCategory.get(category);
        conventions.values().forEach(properties -> properties.keySet().forEach(name -> {
            if (!propertyNames.contains(name)) {
                throw new ChaltenAssertionException(name + " is not a convention of the " + category + " category");
            }
        }));
        Map<String, Map<String, String>> propertiesByRange =
                propertiesByCategory.computeIfAbsent(category, ignored -> new ConcurrentHashMap<>());
        conventions.forEach((range, properties) ->
                propertiesByRange.computeIfAbsent(range, ignored -> new ConcurrentHashMap<>()).putAll(properties));
    }

    private static String readResource(String name) {
        try (InputStream stream = LocaleConventions.class.getResourceAsStream(name)) {
            if (stream == null) {
                throw new IllegalStateException("Missing resource " + name);
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }

    private static List<String> concat(List<String> first, List<String> second) {
        List<String> all = new java.util.ArrayList<>(first);
        all.addAll(second);
        return List.copyOf(all);
    }

    /** Reads a JSON object of language ranges to objects of string properties. */
    private static final class JsonReader {

        private final String json;
        private int position;

        JsonReader(String json) {
            this.json = json;
        }

        Map<String, Map<String, String>> readConventions() {
            Map<String, Map<String, String>> conventions = new LinkedHashMap<>();
            readObject(() -> {
                String range = readString();
                expect(':');
                Map<String, String> properties = new LinkedHashMap<>();
                readObject(() -> {
                    String name = readString();
                    expect(':');
                    properties.put(name, readString());
                });
                conventions.put(range, properties);
            });
            skipWhitespace();
            if (position != json.length()) {
                throw error();
            }
            return conventions;
        }

        private void readObject(Runnable member) {
            expect('{');
            skipWhitespace();
            if (peek() == '}') {
                position++;
                return;
            }
            while (true) {
                member.run();
                skipWhitespace();
                char next = json.charAt(position++);
                if (next == '}') {
                    return;
                }
                if (next != ',') {
                    throw error();
                }
            }
        }

        private String readString() {
            expect('"');
            StringBuilder string = new StringBuilder();
            while (true) {
                char character = json.charAt(position++);
                if (character == '"') {
                    return string.toString();
                }
                if (character == '\\') {
                    char escaped = json.charAt(position++);
                    switch (escaped) {
                        case 'u' -> {
                            string.append((char) Integer.parseInt(json.substring(position, position + 4), 16));
                            position += 4;
                        }
                        case 'n' -> string.append('\n');
                        case 't' -> string.append('\t');
                        case 'r' -> string.append('\r');
                        case 'b' -> string.append('\b');
                        case 'f' -> string.append('\f');
                        default -> string.append(escaped);
                    }
                } else {
                    string.append(character);
                }
            }
        }

        private void expect(char expected) {
            skipWhitespace();
            if (position >= json.length() || json.charAt(position) != expected) {
                throw error();
            }
            position++;
        }

        private char peek() {
            if (position >= json.length()) {
                throw error();
            }
            return json.charAt(position);
        }

        private void skipWhitespace() {
            while (position < json.length() && Character.isWhitespace(json.charAt(position))) {
                position++;
            }
        }

        private IllegalArgumentException error() {
            return new IllegalArgumentException("Invalid JSON conventions at position " + position);
        }
    }
}
