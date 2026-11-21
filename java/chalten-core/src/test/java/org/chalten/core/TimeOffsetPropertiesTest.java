package org.chalten.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.zone.ZoneRules;
import java.util.List;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.Assume;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;
import org.chalten.measure.TimeQuantity;
import org.chalten.measure.TimeUnits;
import org.chalten.testkit.ChaltenArbitraries;

/**
 * PLAN.md §6: {@code Instant} ↔ {@code TimeOffsetAwareGregorianDateTime} round trips under fixed offsets and under
 * IANA zones (outside gaps). The IANA rules come from {@code java.time.zone.ZoneRules} through
 * {@link ZoneRulesRuleset}, so these properties exercise the core conversions with offsets that change over time;
 * the {@code chalten-timezones} rulesets (U4) are checked by their own module.
 */
class TimeOffsetPropertiesTest {

    /** A {@link TimeOffsetRuleset} backed by {@code java.time} zone rules, used as the oracle. */
    record ZoneRulesRuleset(ZoneId zone) implements TimeOffsetRuleset {

        ZoneRules rules() {
            return zone.getRules();
        }

        @Override
        public TimeOffsetAwareGregorianDateTime convertGregorianDateTime(GregorianDateTime dateTime) {
            List<ZoneOffset> offsets = rules().getValidOffsets(dateTime.toLocalDateTime());
            if (offsets.size() != 1) {
                throw new IllegalArgumentException(dateTime + " is in a gap or an overlap of " + zone);
            }
            return convertToGregorianDate(dateTime, seconds(offsets.get(0)));
        }

        @Override
        public String identifier() {
            return zone.getId();
        }

        @Override
        public TimeQuantity offsetToUniversalCoordinatedTimeAt(Instant instant) {
            return seconds(rules().getOffset(instant.toJavaInstant()));
        }

        static TimeQuantity seconds(ZoneOffset offset) {
            return TimeQuantity.of(offset.getTotalSeconds(), TimeUnits.SECOND);
        }
    }

    @Provide
    Arbitrary<Instant> instants() {
        return ChaltenArbitraries.instants();
    }

    @Provide
    Arbitrary<Instant> modernInstants() {
        return ChaltenArbitraries.modernInstants();
    }

    @Provide
    Arbitrary<FixedOffsetRuleset> fixedOffsets() {
        return ChaltenArbitraries.fixedOffsets();
    }

    @Provide
    Arbitrary<ZoneId> zones() {
        return ChaltenArbitraries.zones();
    }

    @Provide
    Arbitrary<GregorianDateTime> dateTimes() {
        return ChaltenArbitraries.dateTimes();
    }

    @Property
    void instantsRoundTripThroughFixedOffsetDateTimes(@ForAll("instants") Instant instant,
            @ForAll("fixedOffsets") FixedOffsetRuleset ruleset) {
        TimeOffsetAwareGregorianDateTime dateTime = instant.asGregorianDateTimeIn(ruleset);
        assertEquals(instant, dateTime.asInstant());
        assertEquals(ruleset.offsetToUniversalCoordinatedTime(), dateTime.appliedOffset());
        assertEquals(instant, dateTime.asRelativePointInTime().in(ruleset).asInstant());
        assertEquals(dateTime, dateTime.asRelativePointInTime().in(ruleset));
    }

    @Property
    void fixedOffsetDateTimesRoundTrip(@ForAll("dateTimes") GregorianDateTime dateTime,
            @ForAll("fixedOffsets") FixedOffsetRuleset ruleset) {
        TimeOffsetAwareGregorianDateTime offsetAware = dateTime.in(ruleset);
        assertEquals(dateTime, offsetAware.asRelativePointInTime());
        assertEquals(offsetAware, offsetAware.asInstant().asGregorianDateTimeIn(ruleset));
        assertEquals(dateTime.toLocalDateTime().minusSeconds(ruleset.toZoneOffset().getTotalSeconds()),
                offsetAware.inCoordinatedUniversalTime().asRelativePointInTime().toLocalDateTime());
    }

    @Property
    void fixedOffsetDateTimesAgreeWithOffsetDateTime(@ForAll("instants") Instant instant,
            @ForAll("fixedOffsets") FixedOffsetRuleset ruleset) {
        ZoneOffset offset = ruleset.toZoneOffset();
        OffsetDateTime expected = OffsetDateTime.ofInstant(instant.toJavaInstant(), offset);
        TimeOffsetAwareGregorianDateTime dateTime = instant.asGregorianDateTimeIn(ruleset);
        assertEquals(expected, dateTime.toOffsetDateTime());
        assertEquals(expected.toLocalDateTime(), dateTime.asRelativePointInTime().toLocalDateTime());
        assertEquals(dateTime, TimeOffsetAwareGregorianDateTime.from(expected));
        assertEquals(instant, Instant.from(instant.toJavaInstant()));
    }

    @Property
    void instantsRoundTripThroughZonedDateTimes(@ForAll("modernInstants") Instant instant,
            @ForAll("zones") ZoneId zone) {
        ZoneRulesRuleset ruleset = new ZoneRulesRuleset(zone);
        ZonedDateTime expected = ZonedDateTime.ofInstant(instant.toJavaInstant(), zone);
        TimeOffsetAwareGregorianDateTime dateTime = instant.asGregorianDateTimeIn(ruleset);
        assertEquals(instant, dateTime.asInstant());
        assertEquals(ZoneRulesRuleset.seconds(expected.getOffset()), dateTime.appliedOffset());
        assertEquals(expected.toLocalDateTime(), dateTime.asRelativePointInTime().toLocalDateTime());
        LocalDateTime local = expected.toLocalDateTime();
        Assume.that(ruleset.rules().getValidOffsets(local).size() == 1);
        assertEquals(instant, dateTime.asRelativePointInTime().in(ruleset).asInstant());
    }

    @Property
    void localTimesOutsideGapsRoundTripThroughZones(@ForAll("modernInstants") Instant seed,
            @ForAll("zones") ZoneId zone) {
        ZoneRulesRuleset ruleset = new ZoneRulesRuleset(zone);
        GregorianDateTime dateTime = seed.asGregorianDateTimeInCoordinatedUniversalTime().asRelativePointInTime();
        Assume.that(ruleset.rules().getValidOffsets(dateTime.toLocalDateTime()).size() == 1);
        TimeOffsetAwareGregorianDateTime zoned = dateTime.in(ruleset);
        assertEquals(dateTime, zoned.asRelativePointInTime());
        assertEquals(dateTime.toLocalDateTime().atZone(zone).toInstant(), zoned.asInstant().toJavaInstant());
        assertTrue(zoned.ruleset().isPresent());
    }
}
