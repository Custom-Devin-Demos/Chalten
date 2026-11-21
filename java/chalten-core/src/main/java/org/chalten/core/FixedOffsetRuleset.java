package org.chalten.core;

import java.time.ZoneOffset;
import java.util.Objects;
import org.chalten.localization.Translations;
import org.chalten.measure.InstanceCreationFailedException;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;

/**
 * Smalltalk {@code FixedOffsetRuleset}: a ruleset that always answers the same offset to UTC. Like Smalltalk, it
 * has identity equality.
 */
public final class FixedOffsetRuleset implements TimeOffsetRuleset {

    static final String OUT_OF_RANGE = "{1} is out of range. UTC offsets range from -16:00 to +16:00";

    private final TimeQuantity offsetToUniversalCoordinatedTime;

    private FixedOffsetRuleset(TimeQuantity offset) {
        this.offsetToUniversalCoordinatedTime = offset;
    }

    /**
     * Smalltalk {@code FixedOffsetRuleset class>>for:} ({@code for} is a Java keyword).
     *
     * @throws InstanceCreationFailedException {@code <offset> is out of range. UTC offsets range from -16:00 to
     *         +16:00}
     */
    public static FixedOffsetRuleset of(TimeQuantity offset) {
        Objects.requireNonNull(offset, "offset");
        if (offset.compareTo(TimeQuantity.of(-16, TimeUnits.HOUR)) < 0
                || offset.compareTo(TimeQuantity.of(16, TimeUnits.HOUR)) > 0) {
            throw new InstanceCreationFailedException(Translations.localizedWithAll(OUT_OF_RANGE, offset));
        }
        return new FixedOffsetRuleset(offset);
    }

    public static FixedOffsetRuleset from(ZoneOffset offset) {
        return of(TimeQuantity.of(offset.getTotalSeconds(), TimeUnits.SECOND));
    }

    public TimeQuantity offsetToUniversalCoordinatedTime() {
        return offsetToUniversalCoordinatedTime;
    }

    @Override
    public TimeOffsetAwareGregorianDateTime convertGregorianDateTime(GregorianDateTime dateTime) {
        return convertToGregorianDate(dateTime, offsetToUniversalCoordinatedTime);
    }

    @Override
    public String identifier() {
        return toString();
    }

    @Override
    public TimeQuantity offsetToUniversalCoordinatedTimeAt(Instant instant) {
        return offsetToUniversalCoordinatedTime;
    }

    /** Requires a whole-second offset. */
    public ZoneOffset toZoneOffset() {
        return ZoneOffset.ofTotalSeconds(
                offsetToUniversalCoordinatedTime.convertTo(TimeUnits.SECOND).numericalValue().intValueExact());
    }

    @Override
    public String toString() {
        StringBuilder stream = new StringBuilder("UTC");
        printOffset(offsetToUniversalCoordinatedTime, stream);
        return stream.toString();
    }
}
