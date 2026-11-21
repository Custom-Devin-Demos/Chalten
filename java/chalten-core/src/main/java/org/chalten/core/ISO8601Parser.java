package org.chalten.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.chalten.localization.Translations;
import org.chalten.measure.CompoundTimeQuantity;
import org.chalten.measure.Fraction;
import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnit;
import org.chalten.measure.TimeUnits;

/**
 * Smalltalk {@code ISO8601Parser}: parses the ISO 8601 extended format. Every failure is an
 * {@link InstanceCreationFailedException} with the Smalltalk message.
 */
public final class ISO8601Parser {

    private static final Pattern INTEGER_PREFIX = Pattern.compile("^[+-]?\\d+");
    private static final Pattern NUMBER_PREFIX = Pattern.compile("^\\d+(\\.\\d+)?");

    private Function<String, Optional<Fraction>> numberReader = ISO8601Parser::readNumber;

    /** Smalltalk {@code readNumbersUsing:}: the reader of duration amounts (Smalltalk {@code Number}). */
    public ISO8601Parser readNumbersUsing(Function<String, Optional<Fraction>> reader) {
        this.numberReader = Objects.requireNonNull(reader, "reader");
        return this;
    }

    /** Smalltalk {@code Number readFrom:ifFail:}: reads the leading decimal number, ignoring the rest. */
    static Optional<Fraction> readNumber(String string) {
        Matcher matcher = NUMBER_PREFIX.matcher(string);
        return matcher.find() ? Optional.of(Fraction.parse(matcher.group())) : Optional.empty();
    }

    /** Smalltalk {@code Integer readFrom:ifFail:}: reads the leading integer, ignoring the rest. */
    static Optional<Long> readInteger(String string) {
        Matcher matcher = INTEGER_PREFIX.matcher(string);
        return matcher.find() ? Optional.of(Long.parseLong(matcher.group())) : Optional.empty();
    }

    /** Smalltalk {@code substrings:}: splits by any of the separators, dropping empty parts. */
    static List<String> substrings(String string, String separators) {
        List<String> parts = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (char character : string.toCharArray()) {
            if (separators.indexOf(character) >= 0) {
                if (!current.isEmpty()) {
                    parts.add(current.toString());
                    current.setLength(0);
                }
            } else {
                current.append(character);
            }
        }
        if (!current.isEmpty()) {
            parts.add(current.toString());
        }
        return parts;
    }

    private static boolean isAllDigits(String string) {
        return string.chars().allMatch(character -> character >= '0' && character <= '9');
    }

    private static void enforce(boolean fact, Supplier<String> explanation) {
        if (!fact) {
            throw signal(explanation.get());
        }
    }

    private static InstanceCreationFailedException signal(String messageText) {
        return new InstanceCreationFailedException(messageText);
    }

    static String errorMessageForDateParsing(String string) {
        return Translations.localizedWithAll("The provided date '{1}' is invalid. Dates must be 4 digits (e.g., 2026)"
                + " or expanded ISO 8601 (e.g., +010000) for the year, exactly 2 digits for the month and exactly 2"
                + " digits for the day.", string);
    }

    static String errorMessageForDateTimeParsing(String string) {
        return Translations.localizedWithAll("The provided datetime '{1}' is invalid.", string);
    }

    static String errorMessageForDayNumberParsing(String string) {
        return Translations.localizedWithAll("The provided day number '{1}' is invalid. Day numbers must be 2 digits"
                + " between 01 and maximum of the month (e.g., 05).", string);
    }

    static String errorMessageForDurationParsing(String string) {
        return Translations.localizedWithAll("The provided duration '{1}' is invalid.", string);
    }

    static String errorMessageForMonthOfYearParsing(String string) {
        return Translations.localizedWithAll("The provided month of year '{1}' is invalid. Months of year must be 4"
                + " digits (e.g., 2026) or expanded ISO 8601 (e.g., +010000) for the year and exactly 2 digits for the"
                + " month.", string);
    }

    static String errorMessageForMonthParsing(String string) {
        return Translations.localizedWithAll(
                "The provided month '{1}' is invalid. Months must be 2 digits between 01 and 12 (e.g., 05).", string);
    }

    static String errorMessageForTimeParsing(String string) {
        return Translations.localizedWithAll(
                "The provided time '{1}' is invalid. Ensure it follows ISO 8601 (e.g., 'T14:30:00').", string);
    }

    static String errorMessageForYearParsing(String string) {
        return Translations.localizedWithAll("The provided year '{1}' is invalid. Years must be 4 digits (e.g., 2026)"
                + " or expanded ISO 8601 (e.g., +010000).", string);
    }

    public GregorianYear parseYear(String string) {
        enforce(string.length() >= 4, () -> errorMessageForYearParsing(string));
        String parsedString = string;
        int sign = 1;
        if (string.charAt(0) == '+') {
            parsedString = string.substring(1);
        }
        if (string.charAt(0) == '-') {
            parsedString = parsedString.substring(1);
            sign = -1;
        }
        String digits = parsedString;
        enforce(isAllDigits(digits), () -> errorMessageForYearParsing(string));
        long number = readInteger(digits).orElseThrow(() -> signal(errorMessageForYearParsing(string)));
        return GregorianYear.of(number * sign);
    }

    public GregorianMonth parseMonth(String string) {
        enforce(string.length() == 2, () -> errorMessageForMonthParsing(string));
        enforce(isAllDigits(string), () -> errorMessageForMonthParsing(string));
        long index = readInteger(string).orElseThrow(() -> signal(errorMessageForMonthParsing(string)));
        enforce(index >= 1 && index <= 12, () -> errorMessageForMonthParsing(string));
        return GregorianMonth.monthAt((int) index);
    }

    public int parseDayNumber(String string) {
        enforce(string.length() == 2, () -> errorMessageForDayNumberParsing(string));
        enforce(isAllDigits(string), () -> errorMessageForDayNumberParsing(string));
        return readInteger(string).orElseThrow(() -> signal(errorMessageForDayNumberParsing(string))).intValue();
    }

    public GregorianMonthOfYear parseMonthOfYear(String string) {
        enforce(string.length() >= 7, () -> errorMessageForMonthOfYearParsing(string));
        String sign = string.charAt(0) == '-' ? "-" : "";
        List<String> parts = substrings(string, "-");
        enforce(parts.size() == 2, () -> errorMessageForMonthOfYearParsing(string));
        GregorianYear year = parseYear(sign + parts.get(0));
        GregorianMonth month = parseMonth(parts.get(1));
        return year.monthAt(month.positionInYear());
    }

    public GregorianDate parseDate(String string) {
        enforce(string.length() >= 10, () -> errorMessageForDateParsing(string));
        String sign = string.charAt(0) == '-' ? "-" : "";
        List<String> parts = substrings(string, "-");
        enforce(parts.size() == 3, () -> errorMessageForDateParsing(string));
        GregorianYear year = parseYear(sign + parts.get(0));
        GregorianMonth month = parseMonth(parts.get(1));
        int dayNumber = parseDayNumber(parts.get(2));
        return year.monthAt(month.positionInYear()).dateAt(dayNumber);
    }

    public GregorianDateTime parseDateTime(String string) {
        List<String> parts = substrings(string, "T");
        enforce(parts.size() == 2, () -> errorMessageForDateTimeParsing(string));
        GregorianDate date = parseDate(parts.get(0));
        TimeOfDay timeOfDay = parseTimeOfDay("T" + parts.get(1));
        return date.at(timeOfDay);
    }

    public TimeOfDay parseTimeOfDay(String string) {
        enforce(string.startsWith("T"), () -> errorMessageForTimeParsing(string));
        enforce(!string.endsWith(":"), () -> errorMessageForTimeParsing(string));
        enforce(!string.endsWith("."), () -> errorMessageForTimeParsing(string));
        List<String> parts = substrings(string.substring(1), ":");
        enforce(parts.size() <= 3, () -> errorMessageForTimeParsing(string));
        enforce(!parts.isEmpty(), () -> errorMessageForTimeParsing(string));
        String hoursPart = parts.get(0);
        enforce(hoursPart.length() == 2, () -> errorMessageForTimeParsing(string));
        long hours = readInteger(hoursPart).orElseThrow(() -> signal(errorMessageForTimeParsing(string)));
        if (parts.size() == 1) {
            return TimeOfDay.hours(hours);
        }
        String minutesPart = parts.get(1);
        enforce(minutesPart.length() == 2, () -> errorMessageForTimeParsing(string));
        long minutes = readInteger(minutesPart).orElseThrow(() -> signal(errorMessageForTimeParsing(string)));
        if (parts.size() == 2) {
            return TimeOfDay.hours(hours, minutes);
        }
        List<String> secondParts = substrings(parts.get(2), ".");
        String secondsPart = secondParts.get(0);
        enforce(secondsPart.length() == 2, () -> errorMessageForTimeParsing(string));
        long seconds = readInteger(secondsPart).orElseThrow(() -> signal(errorMessageForTimeParsing(string)));
        if (secondParts.size() == 1) {
            return TimeOfDay.hours(hours, minutes, seconds);
        }
        String microsecondsPart = secondParts.get(1);
        enforce(microsecondsPart.length() <= 6, () -> errorMessageForTimeParsing(string));
        long microseconds = readInteger(microsecondsPart)
                .orElseThrow(() -> signal(errorMessageForTimeParsing(string)));
        return TimeOfDay.hours(hours, minutes, seconds, microseconds);
    }

    /**
     * Smalltalk {@code parseDuration:}.
     *
     * @throws InstanceCreationFailedException {@code Durations with several time periods are disallowed} for
     *         durations of several periods
     */
    public TimeQuantity parseDuration(String string) {
        List<TimeQuantity> periods = parseDurationPeriods(string);
        if (periods.size() != 1) {
            throw signal("Durations with several time periods are disallowed");
        }
        return periods.get(0);
    }

    /** Smalltalk {@code parseDuration:allowingSeveralTimePeriods: true}. */
    public CompoundTimeQuantity parseDurationAllowingSeveralTimePeriods(String string) {
        return CompoundTimeQuantity.of(parseDurationPeriods(string));
    }

    private List<TimeQuantity> parseDurationPeriods(String string) {
        enforce(string.startsWith("P"), () -> errorMessageForDurationParsing(string));
        String stringToParse = string.substring(1);
        if (stringToParse.endsWith("W")) {
            return List.of(parseWeekDuration(stringToParse.substring(0, stringToParse.length() - 1)));
        }
        List<TimeQuantity> periods = parseTimePeriodsInDuration(string);
        enforce(!periods.isEmpty(), () -> errorMessageForDurationParsing(string));
        return periods;
    }

    private TimeQuantity parseWeekDuration(String string) {
        enforce(isAllDigits(string), () -> errorMessageForDurationParsing(string));
        long number = readInteger(string).orElseThrow(() -> signal(errorMessageForDurationParsing(string)));
        return TimeQuantity.of(number, TimeUnits.WEEK);
    }

    private List<TimeQuantity> parseTimePeriodsInDuration(String string) {
        List<TimeQuantity> periods = new ArrayList<>();
        StringBuilder numberStream = new StringBuilder();
        boolean isInsideTimeSection = false;
        for (char currentChar : string.substring(1).toCharArray()) {
            if (currentChar >= '0' && currentChar <= '9' || currentChar == '.') {
                numberStream.append(currentChar);
            } else if (currentChar == 'T') {
                isInsideTimeSection = true;
            } else {
                Fraction number = numberIn(numberStream, string);
                TimeUnit unit = unitFromDesignator(currentChar, isInsideTimeSection);
                if (unit == null) {
                    throw signal(errorMessageForDurationParsing(string));
                }
                periods.add(TimeQuantity.of(number, unit));
            }
        }
        return periods;
    }

    private Fraction numberIn(StringBuilder numberStream, String string) {
        String numberString = numberStream.toString();
        enforce(!numberString.isEmpty(), () -> errorMessageForDurationParsing(string));
        Fraction number = numberReader.apply(numberString)
                .orElseThrow(() -> signal(errorMessageForDurationParsing(string)));
        numberStream.setLength(0);
        return number;
    }

    private static TimeUnit unitFromDesignator(char designator, boolean isInsideTimeSection) {
        if (isInsideTimeSection) {
            switch (designator) {
                case 'H' -> {
                    return TimeUnits.HOUR;
                }
                case 'S' -> {
                    return TimeUnits.SECOND;
                }
                case 'M' -> {
                    return TimeUnits.MINUTE;
                }
                default -> {
                }
            }
        }
        return switch (designator) {
            case 'Y' -> TimeUnits.YEAR;
            case 'D' -> TimeUnits.DAY;
            case 'M' -> TimeUnits.MONTH;
            default -> null;
        };
    }
}
