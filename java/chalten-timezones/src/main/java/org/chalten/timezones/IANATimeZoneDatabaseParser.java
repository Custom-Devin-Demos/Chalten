package org.chalten.timezones;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.chalten.core.FixedOffsetRuleset;
import org.chalten.core.Instant;
import org.chalten.localization.Translations;
import org.chalten.measure.ChaltenAssertionException;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;

/**
 * Smalltalk {@code IANATimeZoneDatabaseParser}: parses the version 1 (32-bit) data block of a binary TZif file into
 * offset rulesets and transition instants.
 */
public final class IANATimeZoneDatabaseParser {

    static final String INVALID_MAGIC = "Invalid timezone information file. Expected TZif constant";
    static final String INVALID_VERSION = "Invalid timezone information file version. Expected \"2\" or \"3\"";

    /** The parsed data: Smalltalk's {@code #rulesets} and {@code #transitions} dictionary entries. */
    public record ParsedData(List<FixedOffsetRuleset> rulesets, List<Instant> transitions) {
    }

    /** Smalltalk's header dictionary. */
    record Header(long utcIndicatorCount, long wallIndicatorCount, long leapSecondsCount, long transitionCount,
            long localTimeTypeCount, long abbreviationByteCount) {
    }

    /** Smalltalk {@code parse:}. */
    public ParsedData parse(InputStream binaryStream) {
        DataInputStream stream = new DataInputStream(binaryStream);
        try {
            Header header = parseHeaderFrom(stream);
            List<Instant> transitions = parseTransitionTimesIn(stream, header);
            List<FixedOffsetRuleset> rulesets = parseRulesetsIn(stream, header);
            return new ParsedData(rulesets, transitions);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Header parseHeaderFrom(DataInputStream stream) throws IOException {
        assertIsMagicConstant(stream.readNBytes(4));
        assertIsExpectedVersion(stream.read());
        skipFully(stream, 15);
        return new Header(uint32(stream), uint32(stream), uint32(stream), uint32(stream), uint32(stream),
                uint32(stream));
    }

    private static void assertIsMagicConstant(byte[] bytes) {
        if (!Arrays.equals(bytes, "TZif".getBytes(StandardCharsets.UTF_8))) {
            throw new ChaltenAssertionException(Translations.localized(INVALID_MAGIC));
        }
    }

    private static void assertIsExpectedVersion(int versionByte) {
        if (versionByte != '2' && versionByte != '3') {
            throw new ChaltenAssertionException(Translations.localized(INVALID_VERSION));
        }
    }

    private static List<Instant> parseTransitionTimesIn(DataInputStream stream, Header header) throws IOException {
        List<Instant> transitions = new ArrayList<>();
        for (long index = 0; index < header.transitionCount(); index++) {
            transitions.add(Instant.fromUnixTime(stream.readInt()));
        }
        return transitions;
    }

    private static List<FixedOffsetRuleset> parseRulesetsIn(DataInputStream stream, Header header)
            throws IOException {
        int[] offsetIndices = new int[Math.toIntExact(header.transitionCount())];
        for (int index = 0; index < offsetIndices.length; index++) {
            offsetIndices[index] = stream.readUnsignedByte();
        }
        List<FixedOffsetRuleset> offsetRulesets = new ArrayList<>();
        for (long index = 0; index < header.localTimeTypeCount(); index++) {
            int offset = stream.readInt();
            offsetRulesets.add(FixedOffsetRuleset.of(TimeQuantity.of(offset, TimeUnits.SECOND)));
            skipFully(stream, 2);
        }
        List<FixedOffsetRuleset> rulesets = new ArrayList<>();
        rulesets.add(offsetRulesets.get(0));
        for (int zeroBasedIndex : offsetIndices) {
            rulesets.add(offsetRulesets.get(zeroBasedIndex));
        }
        return rulesets;
    }

    private static long uint32(DataInputStream stream) throws IOException {
        return Integer.toUnsignedLong(stream.readInt());
    }

    private static void skipFully(DataInputStream stream, int count) throws IOException {
        if (stream.skipBytes(count) != count) {
            throw new EOFException();
        }
    }
}
