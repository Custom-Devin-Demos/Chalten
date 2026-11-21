package org.chalten.localization;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

/**
 * Hook for Buoy's {@code String>>localized}: translates a source string through the translator installed for the
 * current thread. The default translator reads the repo-root {@code locales/<language>/*.json} catalogs, whose keys
 * are the SHA-256 hex digests of the source strings, for {@link #defaultLanguage()} (initially {@code en}) and falls
 * back to the source string itself.
 *
 * <p>The localization module ({@code CurrentLocale}, {@code LocalizedChronologyFormatter}) replaces the default
 * language or installs its own translator through {@link #setDefaultTranslator(UnaryOperator)}.
 */
public final class Translations {

    private static final List<String> CATALOGS = List.of("chalten", "months", "units", "week-days");
    private static final Map<String, Map<String, String>> CATALOGS_BY_LANGUAGE = new ConcurrentHashMap<>();
    private static final InheritableThreadLocal<UnaryOperator<String>> CURRENT = new InheritableThreadLocal<>();
    private static volatile String defaultLanguage = "en";
    private static volatile UnaryOperator<String> defaultTranslator = Translations::translateInDefaultLanguage;

    private Translations() {
    }

    /** Smalltalk {@code aString localized}. */
    public static String localized(String source) {
        UnaryOperator<String> translator = CURRENT.get();
        return (translator == null ? defaultTranslator : translator).apply(source);
    }

    /** {@code '{1} ... {2}' localizedWithAll: { a . b }}: localizes the template, then expands the placeholders. */
    public static String localizedWithAll(String template, Object... arguments) {
        String result = localized(template);
        for (int index = 0; index < arguments.length; index++) {
            result = result.replace("{" + (index + 1) + "}", String.valueOf(arguments[index]));
        }
        return result;
    }

    /** Evaluates {@code action} with {@code translator} installed for the current thread (Buoy's translator scope). */
    public static <T> T during(UnaryOperator<String> translator, Supplier<T> action) {
        Objects.requireNonNull(translator, "translator");
        UnaryOperator<String> previous = CURRENT.get();
        CURRENT.set(translator);
        try {
            return action.get();
        } finally {
            if (previous == null) {
                CURRENT.remove();
            } else {
                CURRENT.set(previous);
            }
        }
    }

    /** Evaluates {@code action} translating through the catalogs of {@code language}. */
    public static <T> T duringLanguage(String language, Supplier<T> action) {
        return during(source -> translate(language, source), action);
    }

    /** Translates {@code source} with the {@code locales/<language>} catalogs, falling back to {@code source}. */
    public static String translate(String language, String source) {
        return CATALOGS_BY_LANGUAGE.computeIfAbsent(language, Translations::load).getOrDefault(digest(source), source);
    }

    public static String defaultLanguage() {
        return defaultLanguage;
    }

    public static void setDefaultLanguage(String language) {
        defaultLanguage = Objects.requireNonNull(language, "language");
    }

    public static void setDefaultTranslator(UnaryOperator<String> translator) {
        defaultTranslator = Objects.requireNonNull(translator, "translator");
    }

    private static String translateInDefaultLanguage(String source) {
        return translate(defaultLanguage, source);
    }

    static String digest(String source) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(sha256.digest(source.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }

    private static Map<String, String> load(String language) {
        Map<String, String> translations = new HashMap<>();
        for (String catalog : CATALOGS) {
            String resource = "/locales/" + language + "/" + catalog + ".json";
            try (InputStream input = Translations.class.getResourceAsStream(resource)) {
                if (input != null) {
                    collectStrings(new String(input.readAllBytes(), StandardCharsets.UTF_8), translations);
                }
            } catch (IOException e) {
                throw new IllegalStateException("Cannot read " + resource, e);
            }
        }
        return Map.copyOf(translations);
    }

    /** Collects every {@code "key" : "value"} pair of the nested catalog objects. */
    private static void collectStrings(String json, Map<String, String> into) {
        int[] position = {0};
        String pendingKey = null;
        while (position[0] < json.length()) {
            char character = json.charAt(position[0]);
            if (character == '"') {
                String text = readString(json, position);
                int next = skipWhitespace(json, position[0]);
                if (next < json.length() && json.charAt(next) == ':') {
                    pendingKey = text;
                    position[0] = next + 1;
                } else if (pendingKey != null) {
                    into.put(pendingKey, text);
                    pendingKey = null;
                }
            } else {
                if (character == '{') {
                    pendingKey = null;
                }
                position[0]++;
            }
        }
    }

    private static int skipWhitespace(String json, int from) {
        int index = from;
        while (index < json.length() && Character.isWhitespace(json.charAt(index))) {
            index++;
        }
        return index;
    }

    private static String readString(String json, int[] position) {
        StringBuilder text = new StringBuilder();
        int index = position[0] + 1;
        while (json.charAt(index) != '"') {
            char character = json.charAt(index);
            if (character == '\\') {
                char escaped = json.charAt(++index);
                switch (escaped) {
                    case 'n' -> text.append('\n');
                    case 't' -> text.append('\t');
                    case 'r' -> text.append('\r');
                    case 'b' -> text.append('\b');
                    case 'f' -> text.append('\f');
                    case 'u' -> {
                        text.append((char) Integer.parseInt(json.substring(index + 1, index + 5), 16));
                        index += 4;
                    }
                    default -> text.append(escaped);
                }
            } else {
                text.append(character);
            }
            index++;
        }
        position[0] = index + 1;
        return text.toString();
    }
}
