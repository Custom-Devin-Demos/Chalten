package org.chalten.localization;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import org.chalten.core.CalendarDayOfMonth;
import org.chalten.core.CalendarDayOfWeek;
import org.chalten.core.CalendarMonth;
import org.chalten.core.GregorianDate;
import org.chalten.core.GregorianDateTime;
import org.chalten.core.GregorianMonth;
import org.chalten.core.GregorianMonthOfYear;
import org.chalten.core.GregorianYear;
import org.chalten.core.Instant;
import org.chalten.core.TimeOfDay;
import org.chalten.core.TimeOffsetAwareGregorianDateTime;
import org.chalten.localization.ChronologyPattern.Field;
import org.chalten.measure.ChaltenAssertionException;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;

/**
 * Smalltalk {@code LocalizedChronologyFormatter}: formats dates and times following the CLDR conventions of a
 * locale, in one of the {@code short}, {@code medium} (default), {@code long} and {@code full} styles.
 *
 * <p>Month and weekday names are Chalten's own translations into the formatter's locale. Numbers are written with
 * ASCII digits, without grouping, and seconds without their fraction.</p>
 */
public final class LocalizedChronologyFormatter implements FormatterOptions {

    private final String languageTag;
    private final LocaleConventions conventions;
    private final String amMark;
    private final String pmMark;
    private ChronologyPattern datePattern;
    private ChronologyPattern timePattern;
    private ChronologyPattern dateTimePattern;
    private ChronologyPattern zonedDateTimePattern;
    private ChronologyPattern yearMonthPattern;
    private ChronologyPattern monthDayPattern;
    private Function<Named, String> nameAccessor;

    private LocalizedChronologyFormatter(String languageTag, LocaleConventions conventions) {
        this.languageTag = Objects.requireNonNull(languageTag, "languageTag");
        this.conventions = Objects.requireNonNull(conventions, "conventions");
        this.amMark = conventions.chronologyPropertyNamed("amMark", languageTag);
        this.pmMark = conventions.chronologyPropertyNamed("pmMark", languageTag);
        mediumStyle();
    }

    /** Smalltalk {@code LocalizedChronologyFormatter class>>for:}. */
    public static LocalizedChronologyFormatter forLanguage(String languageTag) {
        return forLanguage(languageTag, options -> { });
    }

    /** Smalltalk {@code LocalizedChronologyFormatter class>>for:configuredBy:}. */
    public static LocalizedChronologyFormatter forLanguage(String languageTag, Consumer<FormatterOptions> configurer) {
        return forLanguage(languageTag, LocaleConventions.current(), configurer);
    }

    /** Smalltalk {@code LocalizedChronologyFormatter class>>for:in:configuredBy:}. */
    public static LocalizedChronologyFormatter forLanguage(String languageTag, LocaleConventions conventions,
            Consumer<FormatterOptions> configurer) {
        LocalizedChronologyFormatter formatter = new LocalizedChronologyFormatter(languageTag, conventions);
        configurer.accept(formatter);
        return formatter;
    }

    /** Formats with the current locale, configured by {@code configurer}: the {@code localizedWith:} protocol. */
    public static String localized(Object chronologyObject, Consumer<FormatterOptions> configurer) {
        return forLanguage(CurrentLocale.value(), configurer).format(chronologyObject);
    }

    @Override
    public void shortStyle() {
        datePattern = patternNamed("shortDateFormat");
        timePattern = patternNamed("shortTimeFormat");
        combineDateAndTimeWith("shortDateTimeFormat", timePattern);
        yearMonthPattern = patternNamed("shortYearMonthFormat");
        monthDayPattern = patternNamed("shortMonthDayFormat");
        nameAccessor = Named::shortName;
    }

    @Override
    public void mediumStyle() {
        datePattern = patternNamed("mediumDateFormat");
        timePattern = patternNamed("mediumTimeFormat");
        combineDateAndTimeWith("mediumDateTimeFormat", timePattern);
        yearMonthPattern = patternNamed("mediumYearMonthFormat");
        monthDayPattern = patternNamed("mediumMonthDayFormat");
        nameAccessor = Named::shortName;
    }

    /** Times without an offset have no zone to show, so they use the medium time pattern. */
    @Override
    public void longStyle() {
        datePattern = patternNamed("longDateFormat");
        timePattern = patternNamed("mediumTimeFormat");
        combineDateAndTimeWith("longDateTimeFormat", patternNamed("longTimeFormat"));
        yearMonthPattern = patternNamed("longYearMonthFormat");
        monthDayPattern = patternNamed("longMonthDayFormat");
        nameAccessor = Named::fullName;
    }

    /** There are no full patterns for months of a year and days of a month: they use the long ones. */
    @Override
    public void fullStyle() {
        datePattern = patternNamed("fullDateFormat");
        timePattern = patternNamed("mediumTimeFormat");
        combineDateAndTimeWith("fullDateTimeFormat", patternNamed("fullTimeFormat"));
        yearMonthPattern = patternNamed("longYearMonthFormat");
        monthDayPattern = patternNamed("longMonthDayFormat");
        nameAccessor = Named::fullName;
    }

    /**
     * Smalltalk {@code format:}: the month and weekday names are translated into the formatter's locale.
     *
     * @throws ChaltenAssertionException when a pattern needs a part the object doesn't have, or for days of
     *     non-Gregorian months
     * @throws IllegalArgumentException for objects that cannot be localized
     */
    public String format(Object chronologyObject) {
        Objects.requireNonNull(chronologyObject, "chronologyObject");
        return CurrentLocale.use(languageTag, () -> {
            StringBuilder stream = new StringBuilder();
            printLocalized(chronologyObject, stream);
            return stream.toString();
        });
    }

    private void printLocalized(Object chronologyObject, StringBuilder stream) {
        switch (chronologyObject) {
            case GregorianDate date -> print(datePattern, addPartsOfDate(date, new HashMap<>()), stream);
            case TimeOfDay timeOfDay -> print(timePattern, addPartsOfTimeOfDay(timeOfDay, new HashMap<>()), stream);
            case GregorianDateTime dateTime ->
                print(dateTimePattern, addPartsOfDateTime(dateTime, new HashMap<>()), stream);
            case TimeOffsetAwareGregorianDateTime dateTime -> {
                Map<String, Object> parts = addPartsOfDateTime(dateTime.asRelativePointInTime(), new HashMap<>());
                parts.put("offset", dateTime.appliedOffset());
                print(zonedDateTimePattern, parts, stream);
            }
            case Instant instant -> printLocalized(instant.asGregorianDateTimeInCoordinatedUniversalTime(), stream);
            case GregorianMonthOfYear monthOfYear -> print(yearMonthPattern,
                    new HashMap<>(Map.of("year", monthOfYear.year().number(), "month", monthOfYear.month())),
                    stream);
            case CalendarDayOfMonth<?> dayOfMonth -> {
                if (!dayOfMonth.month().isGregorian()) {
                    throw new ChaltenAssertionException(Translations.localized(
                            "Only days of Gregorian months can be localized"));
                }
                print(monthDayPattern,
                        new HashMap<>(Map.of("month", dayOfMonth.month(), "day", dayOfMonth.dayNumber())), stream);
            }
            case GregorianYear year -> stream.append(year.number());
            case CalendarMonth<?> month -> stream.append(nameAccessor.apply(Named.of(month)));
            case CalendarDayOfWeek dayOfWeek -> stream.append(nameAccessor.apply(Named.of(dayOfWeek)));
            default -> throw new IllegalArgumentException(chronologyObject + " cannot be localized");
        }
    }

    private void combineDateAndTimeWith(String dateTimePatternName, ChronologyPattern zonedTimePattern) {
        ChronologyPattern dateTimeFormat = patternNamed(dateTimePatternName);
        dateTimePattern = dateTimeFormat.replacingDateWith(datePattern, timePattern);
        zonedDateTimePattern = dateTimeFormat.replacingDateWith(datePattern, zonedTimePattern);
    }

    private ChronologyPattern patternNamed(String name) {
        return ChronologyPattern.fromString(conventions.chronologyPropertyNamed(name, languageTag));
    }

    private static Map<String, Object> addPartsOfDate(GregorianDate date, Map<String, Object> parts) {
        parts.put("year", date.yearNumber());
        parts.put("month", date.month());
        parts.put("day", date.dayNumber());
        parts.put("dayOfWeek", date.dayOfWeek());
        return parts;
    }

    private static Map<String, Object> addPartsOfDateTime(GregorianDateTime dateTime, Map<String, Object> parts) {
        addPartsOfDate(dateTime.date(), parts);
        return addPartsOfTimeOfDay(dateTime.timeOfDay(), parts);
    }

    private static Map<String, Object> addPartsOfTimeOfDay(TimeOfDay timeOfDay, Map<String, Object> parts) {
        parts.put("hour", timeOfDay.hours().numericalValue().longValueExact());
        parts.put("minute", timeOfDay.minutes().numericalValue().longValueExact());
        parts.put("second", timeOfDay.seconds().numericalValue().longValueExact());
        return parts;
    }

    private static Object part(String partName, Map<String, Object> parts) {
        Object part = parts.get(partName);
        if (part == null) {
            throw new ChaltenAssertionException(
                    "The pattern needs a " + partName + " the formatted object doesn't have");
        }
        return part;
    }

    private static long number(String partName, Map<String, Object> parts) {
        return ((Number) part(partName, parts)).longValue();
    }

    private static GregorianMonth month(Map<String, Object> parts) {
        return (GregorianMonth) part("month", parts);
    }

    private static String padded(long number) {
        return number < 10 ? "0" + number : Long.toString(number);
    }

    private long hourOf12HourClock(Map<String, Object> parts) {
        return Math.floorMod(number("hour", parts) - 1, 12) + 1;
    }

    private void print(ChronologyPattern pattern, Map<String, Object> parts, StringBuilder stream) {
        for (Object element : pattern.elements()) {
            if (element instanceof Field field) {
                write(field, parts, stream);
            } else {
                stream.append(element);
            }
        }
    }

    private void write(Field field, Map<String, Object> parts, StringBuilder stream) {
        switch (field) {
            case YEAR -> stream.append(number("year", parts));
            case TWO_DIGIT_YEAR -> stream.append(padded(Math.floorMod(number("year", parts), 100)));
            case MONTH -> stream.append(month(parts).positionInYear());
            case TWO_DIGIT_MONTH -> stream.append(padded(month(parts).positionInYear()));
            case MONTH_SHORT_NAME -> stream.append(month(parts).shortName());
            case MONTH_NAME -> stream.append(month(parts).fullName());
            case DAY -> stream.append(number("day", parts));
            case TWO_DIGIT_DAY -> stream.append(padded(number("day", parts)));
            case DAY_OF_WEEK_SHORT_NAME -> stream.append(((CalendarDayOfWeek) part("dayOfWeek", parts)).shortName());
            case DAY_OF_WEEK_NAME -> stream.append(((CalendarDayOfWeek) part("dayOfWeek", parts)).fullName());
            case HOUR_OF_12_HOUR_CLOCK -> stream.append(hourOf12HourClock(parts));
            case TWO_DIGIT_HOUR_OF_12_HOUR_CLOCK -> stream.append(padded(hourOf12HourClock(parts)));
            case HOUR_OF_24_HOUR_CLOCK -> stream.append(number("hour", parts));
            case TWO_DIGIT_HOUR_OF_24_HOUR_CLOCK -> stream.append(padded(number("hour", parts)));
            case DAY_PERIOD -> stream.append(number("hour", parts) < 12 ? amMark : pmMark);
            case MINUTE -> stream.append(number("minute", parts));
            case TWO_DIGIT_MINUTE -> stream.append(padded(number("minute", parts)));
            case SECOND -> stream.append(number("second", parts));
            case TWO_DIGIT_SECOND -> stream.append(padded(number("second", parts)));
            case SHORT_OFFSET -> printOffset((TimeQuantity) part("offset", parts), false, stream);
            case LONG_OFFSET -> printOffset((TimeQuantity) part("offset", parts), true, stream);
            case DATE, TIME -> throw new ChaltenAssertionException("The " + field.name().toLowerCase(
                    java.util.Locale.ROOT) + " placeholder can only be used in a pattern combining a date and a time");
        }
    }

    /**
     * Writes {@code UTC} and, for an offset other than zero, its sign, hours and minutes: {@code UTC-3} in the short
     * form, which drops the leading zero and zero minutes, {@code UTC-03:00} in the long one. Seconds are only shown
     * when there are any.
     */
    private static void printOffset(TimeQuantity offset, boolean isLong, StringBuilder stream) {
        stream.append("UTC");
        long totalSeconds = new java.math.BigDecimal(offset.convertTo(TimeUnits.SECOND).numericalValue().truncated())
                .longValueExact();
        if (totalSeconds == 0) {
            return;
        }
        stream.append(totalSeconds < 0 ? '-' : '+');
        long absoluteSeconds = Math.abs(totalSeconds);
        long hours = absoluteSeconds / 3600;
        long minutes = absoluteSeconds % 3600 / 60;
        long seconds = absoluteSeconds % 60;
        if (isLong) {
            stream.append(padded(hours)).append(':').append(padded(minutes));
        } else {
            stream.append(hours);
            if (minutes > 0 || seconds > 0) {
                stream.append(':').append(padded(minutes));
            }
        }
        if (seconds > 0) {
            stream.append(':').append(padded(seconds));
        }
    }

    /** The months and days of the week, which the styles show by their short or full names. */
    private interface Named {

        String shortName();

        String fullName();

        static Named of(CalendarMonth<?> month) {
            return new Named() {
                @Override
                public String shortName() {
                    return month.shortName();
                }

                @Override
                public String fullName() {
                    return month.fullName();
                }
            };
        }

        static Named of(CalendarDayOfWeek dayOfWeek) {
            return new Named() {
                @Override
                public String shortName() {
                    return dayOfWeek.shortName();
                }

                @Override
                public String fullName() {
                    return dayOfWeek.fullName();
                }
            };
        }
    }
}
