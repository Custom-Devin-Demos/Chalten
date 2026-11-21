package org.chalten.timezones;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import org.chalten.localization.Translations;

/**
 * Smalltalk {@code IANATimeZoneDatabase}: access to the IANA time zone database files.
 *
 * <p>The location of the files is {@link #setZoneInfoLocation(Path)} when configured, otherwise the {@code TZDIR}
 * environment variable, otherwise {@code /usr/share/zoneinfo/}. Identifiers come from {@code zone1970.tab}.
 */
public final class IANATimeZoneDatabase {

    static final String INVALID_IDENTIFIER = "{1} is not a valid time zone identifier";

    private static volatile IANATimeZoneDatabase current;
    private static volatile Path zoneInfoLocation;

    private volatile List<String> timeZoneIdentifiers;
    private final Map<String, TimeZoneRuleset> timeZoneRulesets = new ConcurrentHashMap<>();

    private IANATimeZoneDatabase() {
        reloadTimeZoneIdentifiers();
    }

    /** Smalltalk {@code IANATimeZoneDatabase class>>current}. */
    public static IANATimeZoneDatabase current() {
        IANATimeZoneDatabase database = current;
        if (database == null) {
            synchronized (IANATimeZoneDatabase.class) {
                database = current;
                if (database == null) {
                    database = new IANATimeZoneDatabase();
                    current = database;
                }
            }
        }
        return database;
    }

    /** Smalltalk {@code IANATimeZoneDatabase class>>zoneInfoLocation}. */
    public static Path zoneInfoLocation() {
        Path location = zoneInfoLocation;
        if (location != null) {
            return location;
        }
        String directory = System.getenv("TZDIR");
        return Path.of(directory != null ? directory : "/usr/share/zoneinfo/");
    }

    /**
     * Smalltalk {@code IANATimeZoneDatabase class>>zoneInfoLocation:}, for systems without a standard location. Like
     * Smalltalk, it does not reset {@link #current()}; send {@link #reloadTimeZoneIdentifiers()} and
     * {@link #reloadAllTimeZoneRulesets()} to pick up the new location.
     */
    public static void setZoneInfoLocation(Path directory) {
        zoneInfoLocation = directory;
    }

    /**
     * Smalltalk {@code at:}.
     *
     * @throws NoSuchElementException (Smalltalk {@code ObjectNotFound}) {@code <id> is not a valid time zone
     *         identifier}
     */
    public TimeZoneRuleset at(String timeZoneIdentifier) {
        if (!timeZoneIdentifiers.contains(timeZoneIdentifier)) {
            throw new NoSuchElementException(Translations.localizedWithAll(INVALID_IDENTIFIER, timeZoneIdentifier));
        }
        return timeZoneRulesets.computeIfAbsent(timeZoneIdentifier, this::loadRulesetIdentifiedBy);
    }

    /** The ruleset with the identifier of {@code zoneId}. */
    public TimeZoneRuleset at(ZoneId zoneId) {
        return at(zoneId.getId());
    }

    /** Smalltalk {@code timeZoneIdentifiers}: sorted. */
    public List<String> timeZoneIdentifiers() {
        return timeZoneIdentifiers;
    }

    /** Smalltalk {@code reloadAllTimeZoneRulesets}. */
    public void reloadAllTimeZoneRulesets() {
        for (String identifier : timeZoneIdentifiers) {
            timeZoneRulesets.put(identifier, loadRulesetIdentifiedBy(identifier));
        }
    }

    /** Smalltalk {@code reloadTimeZoneIdentifiers}: reads the third tab-separated column of {@code zone1970.tab}. */
    public void reloadTimeZoneIdentifiers() {
        List<String> identifiers = new ArrayList<>();
        try {
            for (String line : Files.readAllLines(zoneInfoLocation().resolve("zone1970.tab"),
                    StandardCharsets.UTF_8)) {
                if (!line.startsWith("#")) {
                    List<String> columns = Arrays.stream(line.split("\t")).filter(each -> !each.isEmpty()).toList();
                    identifiers.add(columns.get(2));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        timeZoneIdentifiers = identifiers.stream().sorted().toList();
    }

    private TimeZoneRuleset loadRulesetIdentifiedBy(String identifier) {
        Path rulesetLocation = zoneInfoLocation();
        for (String each : identifier.split("/")) {
            rulesetLocation = rulesetLocation.resolve(each);
        }
        try (InputStream stream = Files.newInputStream(rulesetLocation)) {
            return loadRulesetIdentifiedBy(identifier, stream);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Smalltalk {@code loadRulesetIdentifiedBy:from:}. */
    static TimeZoneRuleset loadRulesetIdentifiedBy(String identifier, InputStream binaryStream) {
        IANATimeZoneDatabaseParser.ParsedData parsedData = new IANATimeZoneDatabaseParser().parse(binaryStream);
        return TimeZoneRuleset.identifiedBy(identifier, parsedData.rulesets(), parsedData.transitions());
    }
}
