package org.chalten.localization;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.chalten.measure.InstanceCreationFailedException;

/**
 * Smalltalk {@code ChronologyPattern}: a Unicode CLDR date and time pattern, like {@code d 'de' MMMM 'de' y}.
 *
 * <p>Its elements are, in order, the literal {@link String}s to write as they are and the {@link Field}s to write
 * in their place. Text between single quotes is literal, and two single quotes stand for one. Only ASCII letters
 * are fields; any unsupported one is refused. {@code {1}} and {@code {0}} are the date and time placeholders of
 * the patterns combining both.</p>
 */
public final class ChronologyPattern {

    /** The fields of a pattern, the Smalltalk symbols of the elements. */
    public enum Field {
        YEAR, TWO_DIGIT_YEAR, MONTH, TWO_DIGIT_MONTH, MONTH_SHORT_NAME, MONTH_NAME, DAY, TWO_DIGIT_DAY,
        DAY_OF_WEEK_SHORT_NAME, DAY_OF_WEEK_NAME, HOUR_OF_12_HOUR_CLOCK, TWO_DIGIT_HOUR_OF_12_HOUR_CLOCK,
        HOUR_OF_24_HOUR_CLOCK, TWO_DIGIT_HOUR_OF_24_HOUR_CLOCK, DAY_PERIOD, MINUTE, TWO_DIGIT_MINUTE, SECOND,
        TWO_DIGIT_SECOND, SHORT_OFFSET, LONG_OFFSET, DATE, TIME
    }

    private static final Map<String, Field> FIELDS_BY_LETTERS = Map.ofEntries(
            Map.entry("y", Field.YEAR), Map.entry("yy", Field.TWO_DIGIT_YEAR),
            Map.entry("M", Field.MONTH), Map.entry("L", Field.MONTH),
            Map.entry("MM", Field.TWO_DIGIT_MONTH), Map.entry("LL", Field.TWO_DIGIT_MONTH),
            Map.entry("MMM", Field.MONTH_SHORT_NAME), Map.entry("MMMM", Field.MONTH_NAME),
            Map.entry("d", Field.DAY), Map.entry("dd", Field.TWO_DIGIT_DAY),
            Map.entry("E", Field.DAY_OF_WEEK_SHORT_NAME), Map.entry("EE", Field.DAY_OF_WEEK_SHORT_NAME),
            Map.entry("EEE", Field.DAY_OF_WEEK_SHORT_NAME), Map.entry("ccc", Field.DAY_OF_WEEK_SHORT_NAME),
            Map.entry("EEEE", Field.DAY_OF_WEEK_NAME), Map.entry("cccc", Field.DAY_OF_WEEK_NAME),
            Map.entry("h", Field.HOUR_OF_12_HOUR_CLOCK), Map.entry("hh", Field.TWO_DIGIT_HOUR_OF_12_HOUR_CLOCK),
            Map.entry("H", Field.HOUR_OF_24_HOUR_CLOCK), Map.entry("HH", Field.TWO_DIGIT_HOUR_OF_24_HOUR_CLOCK),
            Map.entry("a", Field.DAY_PERIOD),
            Map.entry("m", Field.MINUTE), Map.entry("mm", Field.TWO_DIGIT_MINUTE),
            Map.entry("s", Field.SECOND), Map.entry("ss", Field.TWO_DIGIT_SECOND),
            Map.entry("z", Field.SHORT_OFFSET), Map.entry("zz", Field.SHORT_OFFSET),
            Map.entry("zzz", Field.SHORT_OFFSET), Map.entry("O", Field.SHORT_OFFSET),
            Map.entry("zzzz", Field.LONG_OFFSET), Map.entry("OOOO", Field.LONG_OFFSET));

    private final List<Object> elements;

    private ChronologyPattern(List<Object> elements) {
        this.elements = List.copyOf(elements);
    }

    /**
     * Smalltalk {@code ChronologyPattern class>>fromString:}.
     *
     * @throws InstanceCreationFailedException for unsupported fields or placeholders, and unclosed quotes or braces
     */
    public static ChronologyPattern fromString(String pattern) {
        return new ChronologyPattern(new Parser(pattern).parse());
    }

    /** Smalltalk {@code ChronologyPattern class>>withElements:}. */
    public static ChronologyPattern withElements(List<?> elements) {
        return new ChronologyPattern(new ArrayList<>(elements));
    }

    /** The literal strings and the fields, in order. */
    public List<Object> elements() {
        return elements;
    }

    /** The pattern combining a date and a time, with its placeholders replaced by the given patterns' elements. */
    public ChronologyPattern replacingDateWith(ChronologyPattern datePattern, ChronologyPattern timePattern) {
        List<Object> combined = new ArrayList<>();
        for (Object element : elements) {
            if (element == Field.DATE) {
                combined.addAll(datePattern.elements);
            } else if (element == Field.TIME) {
                combined.addAll(timePattern.elements);
            } else {
                combined.add(element);
            }
        }
        return new ChronologyPattern(combined);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof ChronologyPattern other && elements.equals(other.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements);
    }

    @Override
    public String toString() {
        return "a ChronologyPattern" + elements;
    }

    private static final class Parser {

        private final String pattern;
        private final List<Object> elements = new ArrayList<>();
        private StringBuilder literal = new StringBuilder();
        private int position;

        Parser(String pattern) {
            this.pattern = pattern;
        }

        List<Object> parse() {
            while (!atEnd()) {
                char character = next();
                if (character == '\'') {
                    parseQuotedLiteral();
                } else if (character == '{' || isLetter(character)) {
                    flushLiteral();
                    elements.add(character == '{' ? parsePlaceholder() : parseFieldStartingWith(character));
                } else {
                    literal.append(character);
                }
            }
            flushLiteral();
            return elements;
        }

        private boolean atEnd() {
            return position >= pattern.length();
        }

        private char next() {
            return pattern.charAt(position++);
        }

        private char peek() {
            return pattern.charAt(position);
        }

        private void flushLiteral() {
            if (!literal.isEmpty()) {
                elements.add(literal.toString());
            }
            literal = new StringBuilder();
        }

        private static boolean isLetter(char character) {
            return character >= 'a' && character <= 'z' || character >= 'A' && character <= 'Z';
        }

        private Field parseFieldStartingWith(char letter) {
            StringBuilder letters = new StringBuilder().append(letter);
            while (!atEnd() && peek() == letter) {
                letters.append(next());
            }
            Field field = FIELDS_BY_LETTERS.get(letters.toString());
            if (field == null) {
                throw new InstanceCreationFailedException(
                        "The field " + letters + " is not supported in date and time patterns");
            }
            return field;
        }

        private Field parsePlaceholder() {
            StringBuilder placeholder = new StringBuilder();
            while (!atEnd() && peek() != '}') {
                placeholder.append(next());
            }
            if (atEnd()) {
                throw new InstanceCreationFailedException("Missing closing brace in date and time pattern");
            }
            next();
            return switch (placeholder.toString()) {
                case "0" -> Field.TIME;
                case "1" -> Field.DATE;
                default -> throw new InstanceCreationFailedException(
                        "The placeholder {" + placeholder + "} is not supported in date and time patterns");
            };
        }

        private void parseQuotedLiteral() {
            if (!atEnd() && peek() == '\'') {
                literal.append(next());
                return;
            }
            while (true) {
                if (atEnd()) {
                    throw new InstanceCreationFailedException("Missing closing quote in date and time pattern");
                }
                if (peek() == '\'') {
                    next();
                    if (atEnd() || peek() != '\'') {
                        return;
                    }
                }
                literal.append(next());
            }
        }
    }
}
