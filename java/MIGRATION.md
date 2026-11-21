<!-- markdownlint-disable MD013 MD033 -->
# Migrating from Smalltalk Chalten to Java Chalten

This guide is for teams moving code that calls Chalten (Smalltalk) to the Java 21 port under `java/`. It has three parts:

1. [API mapping](#api-mapping): the Smalltalk protocol and the Java name for each message.
2. [Policy-period recipes](#policy-period-recipes): term start/end, renewals and pro-rata day counts, in Smalltalk and Java side by side.
3. Reference sections: [java.time interop](#javatime-interop), [threading and locale context](#threading-and-locale-context), [exceptions](#exceptions) and [behaviour differences](#behaviour-differences).

Every Java snippet in this guide is copied from [`MigrationGuideExamplesTest`](chalten-core/src/test/java/org/chalten/core/MigrationGuideExamplesTest.java), which asserts the values shown. [`MigrationGuideTest`](chalten-core/src/test/java/org/chalten/core/MigrationGuideTest.java) fails the build if a snippet line is not in that test, or if a Java type or member named in the API mapping table does not exist (rows marked **U4** name types in the timezones, Hebrew and Islamic modules, which `chalten-core` cannot load, so they are not checked by this test).

Conventions used in the Smalltalk snippets: `day`, `month`, `year` and `hour` are the Aconcagua units `TimeUnits units >> #day` etc., as returned by `CalendarMagnitude class >> month`. Java imports come from `org.chalten.core`, `org.chalten.measure` and `org.chalten.localization`.

## API mapping

Selectors and their Java names. `Type#member` is an instance method, `Type.member` a static method, constant or enum value. Interfaces carry the shared protocol as default methods (`CalendarDate`, `CalendarMonthOfYear`, `CalendarYear`, `CalendarMagnitude`), so `GregorianDate#next` is declared in `CalendarDate`. The **Unit** column says which Java port unit provides the API; U2, U3 and U4 are merged into this branch.

| Area | Smalltalk | Java | Unit |
| --- | --- | --- | --- |
| Constructors | `GregorianMonth march fifteenth , 2025` | `GregorianMonth.MARCH`, `CalendarMonth#fifteenth`, `CalendarDayOfMonth#of` | U2 |
| Constructors | `GregorianDate monthOfYear: aMonthOfYear dayNumber: 15` | `GregorianMonthOfYear#dateAt`, `GregorianDate.of(2025, 3, 15)` | U2 |
| Constructors | `GregorianMonth march , 2025`, `aYear march`, `aYear monthAt: 3` | `GregorianMonth#of`, `GregorianYear#march`, `GregorianYear#monthAt` | U2 |
| Constructors | `GregorianYear number: 2025`, `2025 asGregorianYear` | `GregorianYear.withNumber`, `GregorianYear.of` | U2 |
| Constructors | `GregorianMonth monthAt: 3`, `GregorianMonth all` | `GregorianMonth.monthAt`, `GregorianMonth.all` | U2 |
| Constructors | `DayOfWeek monday`, `DayOfWeek dayAt: 1` | `DayOfWeek.MONDAY`, `DayOfWeek.dayAt` | U2 |
| Constructors | `TimeOfDay hours: 0 minutes: 1`, `TimeOfDay startOfDay`, `TimeOfDay halfOfDay` | `TimeOfDay.hours`, `TimeOfDay.startOfDay`, `TimeOfDay.halfOfDay` | U2 |
| Constructors | `TimeOfDay offsetFromStartOfDay: 90 * minute` | `TimeOfDay.offsetFromStartOfDay` | U2 |
| Constructors | `GregorianDateTime on: aDate at: aTimeOfDay`, `aDate at: aTimeOfDay`, `aDate atMidnight`, `aDate atNoon` | `GregorianDateTime.on`, `GregorianDate#at`, `GregorianDate#atMidnight`, `GregorianDate#atNoon`, `GregorianDate#atStartOfDay` | U2 |
| Constructors | `Instant now`, `Instant epoch`, `Instant fromUnixTime: n`, `Instant fromJulianDayNumber: n` | `Instant.now`, `Instant.epoch`, `Instant.fromUnixTime`, `Instant.fromJulianDayNumber` | U2 |
| Constructors | `TimeOffsetAwareGregorianDateTime basedOn: anInstant accordingTo: aRuleset` | `TimeOffsetAwareGregorianDateTime.basedOn` | U2 |
| Quantities | `12 * month`, `TimeUnits units >> #month` | `TimeQuantity.of(12, TimeUnits.MONTH)`, `TimeUnits.named`, `TimeUnits.find` | U3 |
| Quantities | `aQuantity amount`, `aQuantity unit`, `aQuantity convertTo: day` | `TimeQuantity#amount`, `TimeQuantity#numericalValue`, `TimeQuantity#unit`, `TimeQuantity#convertTo` | U3 |
| Quantities | `+`, `-`, `* aNumber`, `/ aQuantity` (exact ratio), `negated`, `abs` | `TimeQuantity#plus`, `TimeQuantity#minus`, `TimeQuantity#times`, `TimeQuantity#dividedBy`, `TimeQuantity#negated`, `TimeQuantity#abs` | U3 |
| Quantities | `isZero`, `positive`, `negative` | `TimeQuantity#isZero`, `TimeQuantity#isPositive`, `TimeQuantity#isNegative` | U3 |
| Quantities | Several periods (`parseDuration: 'P1Y2M' allowingSeveralTimePeriods: true`) | `CompoundTimeQuantity`, `ISO8601Parser#parseDurationAllowingSeveralTimePeriods` | U3 |
| Quantities | `91 / 366` (`Fraction`), `+ - * /`, `numerator`, `denominator`, `floor`, `rounded` via `floor`/`ceiling` | `Fraction.of`, `Fraction.parse`, `Fraction.ONE`, `Fraction#plus`, `Fraction#minus`, `Fraction#times`, `Fraction#dividedBy`, `Fraction#numerator`, `Fraction#denominator`, `Fraction#floor`, `Fraction#ceiling`, `Fraction#toBigDecimalExact` | U3 |
| Accessors | `aDate dayNumber`, `monthNumber`, `yearNumber`, `monthOfYear`, `month`, `year`, `dayOfWeek` | `GregorianDate#dayNumber`, `GregorianDate#monthNumber`, `GregorianDate#yearNumber`, `GregorianDate#monthOfYear`, `GregorianDate#month`, `GregorianDate#year`, `GregorianDate#dayOfWeek` | U2 |
| Accessors | `aMonthOfYear numberOfDays`, `first`, `last`, `year`, `month` | `GregorianMonthOfYear#numberOfDays`, `GregorianMonthOfYear#first`, `GregorianMonthOfYear#last`, `GregorianMonthOfYear#year`, `GregorianMonthOfYear#month` | U2 |
| Accessors | `aYear number`, `isLeapYear`, `numberOfDays`, `firstDate`, `lastDate` | `GregorianYear#number`, `GregorianYear#isLeapYear`, `GregorianYear#numberOfDays`, `GregorianYear#firstDate`, `GregorianYear#lastDate` | U2 |
| Accessors | `aMonth name`, `shortName`, `positionInYear`, `aDayOfWeek name` | `GregorianMonth#fullName`, `GregorianMonth#shortName`, `GregorianMonth#positionInYear`, `DayOfWeek#fullName` | U2 |
| Accessors | `aDateTime date`, `timeOfDay`, `aTimeOfDay hours`, `minutes`, `seconds` | `GregorianDateTime#date`, `GregorianDateTime#timeOfDay`, `TimeOfDay#hours`, `TimeOfDay#minutes`, `TimeOfDay#seconds` | U2 |
| Accessors | `anInstant secondsSinceEpoch`, `microsecondsSinceEpoch`, `asUnixTime` | `Instant#secondsSinceEpoch`, `Instant#microsecondsSinceEpoch`, `Instant#asUnixTime` | U2 |
| Arithmetic | `aDate + (3 * day)`, `aDate next: 3 * day`, `aDate - (3 * day)`, `aDate previous: 3 * day` | `GregorianDate#plus`, `GregorianDate#next`, `GregorianDate#minus`, `GregorianDate#previous` (day-commensurable quantities only) | U2 |
| Arithmetic | `aDate next`, `aDate previous` | `GregorianDate#next`, `GregorianDate#previous` | U2 |
| Arithmetic | `endDate - startDate` (days) | `GregorianDate#minus(GregorianDate)` returns a `TimeQuantity` in days | U2 |
| Arithmetic | `aMonthOfYear next: 6 * month`, `previous: n * month`, `endMonth - startMonth` | `GregorianMonthOfYear#next`, `GregorianMonthOfYear#previous`, `GregorianMonthOfYear#minus` | U2 |
| Arithmetic | `aYear next: 1 * year`, `aMonth next: 1 * month` | `GregorianYear#next`, `GregorianMonth#next` | U2 |
| Arithmetic | `aTimeOfDay + (90 * minute)`, `anInstant next: 1 * hour` | `TimeOfDay#plus`, `Instant#next`, `Instant#previous` | U2 |
| Comparison | `<`, `<=`, `>`, `>=`, `between: a and: b` | `GregorianDate#compareTo`, `CalendarMagnitude#isLessThan`, `CalendarMagnitude#isLessThanOrEqualTo`, `CalendarMagnitude#isGreaterThan`, `CalendarMagnitude#isGreaterThanOrEqualTo`, `CalendarMagnitude#between` | U2 |
| Comparison | `=`, `hash`, `aDate isLastDayOfMonth` | `Object#equals`, `Object#hashCode`, `GregorianDate#isLastDayOfMonth` | U2 |
| Comparison | `aQuantity max: other`, `min:` | `TimeQuantity#max`, `TimeQuantity#min`, `TimeQuantity#compareTo` | U3 |
| Intervals | `startDate to: endDate`, `startDate to: endDate by: 7 * day` | `GregorianDate#to`, `DiscreteInterval.from` | U2, U3 |
| Intervals | `size`, `first`, `last`, `includes:`, `select:`, `reject:` | `DiscreteInterval#size`, `DiscreteInterval#first`, `DiscreteInterval#last`, `DiscreteInterval#contains`, `DiscreteInterval#select`, `DiscreteInterval#reject` | U3 |
| Intervals | `at: 1` (one-based), `indexOf:` | `DiscreteInterval#get` (zero-based), `DiscreteInterval#indexOf` | U3 |
| Parsing | `ISO8601Parser new parseDate:`, `parseDateTime:`, `parseTimeOfDay:`, `parseMonthOfYear:`, `parseYear:`, `parseDuration:` | `ISO8601Parser#parseDate`, `ISO8601Parser#parseDateTime`, `ISO8601Parser#parseTimeOfDay`, `ISO8601Parser#parseMonthOfYear`, `ISO8601Parser#parseYear`, `ISO8601Parser#parseDuration` | U2 |
| Formatting | `printString`, `printAsISO8601String`, `ISO8601DurationFormatter new format:` | `Object#toString`, `ISO8601Printable#printAsISO8601String`, `ISO8601DurationFormatter#format` | U2, U3 |
| Formatting | `localized`, `localizedWith:`, `LocalizedChronologyFormatter for: aLocale`, `ChronologyPattern` | `LocalizedChronologyFormatter`, `FormatterOptions`, `ChronologyPattern` | U4 |
| Exceptions | `InstanceCreationFailed` | `InstanceCreationFailedException` | U3 |
| Exceptions | `ArithmeticError` | `ArithmeticException` | U2, U3 |
| Exceptions | `MessageNotUnderstood` for an ordinal a month lacks (`april thirtyFirst`) | `UnsupportedOperationException` | U2 |
| Exceptions | `AssertionFailed` | `ChaltenAssertionException` | U3 |
| Exceptions | `SubscriptOutOfBounds`, `KeyNotFound` | `IndexOutOfBoundsException`, `IllegalArgumentException` | U3 |
| Exceptions | `AmbiguousTimeError`, `SkippedTimeError` | `AmbiguousTimeException`, `SkippedTimeException` | U4 |
| Timezones | `FixedOffsetRuleset for: -5 * hour`, `identifier`, `offsetToUniversalCoordinatedTimeAt: anInstant` | `FixedOffsetRuleset.of`, `FixedOffsetRuleset#identifier`, `FixedOffsetRuleset#offsetToUniversalCoordinatedTimeAt`, `TimeOffsetRuleset` | U2 |
| Timezones | `aDateTime in: aRuleset`, `inCoordinatedUniversalTime`, `asInstant` | `GregorianDateTime#in`, `GregorianDateTime#inCoordinatedUniversalTime`, `TimeOffsetAwareGregorianDateTime#asInstant`, `TimeOffsetAwareGregorianDateTime#appliedOffset` | U2 |
| Timezones | `anInstant asGregorianDateTimeIn: aRuleset`, `asGregorianDateTimeInCoordinatedUniversalTime` | `Instant#asGregorianDateTimeIn`, `Instant#asGregorianDateTimeInCoordinatedUniversalTime` | U2 |
| Timezones | `TimeZoneRuleset`, `IANATimeZoneDatabase`, `IANATimeZoneDatabaseParser` | `TimeZoneRuleset`, `IANATimeZoneDatabase`, `IANATimeZoneDatabaseParser` | U4 |
| Localization | `'text' localized`, `NaturalLanguageTranslator` scope (`asNaturalLanguageTranslatorDuring:`) | `Translations.localized`, `Translations.during`, `Translations.duringLanguage`, `Translations.setDefaultLanguage` | U2 |
| Localization | `CurrentLocale use: 'es' during: [...]`, `LocaleConventions current` | `CurrentLocale`, `LocaleConventions` | U4 |
| Calendars | `HebrewCalendar`, `HebrewYear`, `HebrewMonth`, `HebrewMonthOfYear`, `HebrewDate`, `HebrewDateTime`, `HebrewDayOfWeek` | same names in `org.chalten.hebrew` | U4 |
| Calendars | `IslamicCalendar`, `IslamicYear`, `IslamicMonth`, `IslamicMonthOfYear`, `IslamicDate`, `IslamicDateTime`, `IslamicDayOfWeek` | same names in `org.chalten.islamic` | U4 |
| java.time | none (Java only) | `GregorianDate#toLocalDate`, `GregorianDate.from`, `GregorianMonthOfYear#toYearMonth`, `GregorianYear#toYear`, `GregorianMonth#toMonth`, `DayOfWeek#toDayOfWeek` | U2 |
| java.time | none (Java only) | `TimeOfDay#toLocalTime`, `GregorianDateTime#toLocalDateTime`, `TimeOffsetAwareGregorianDateTime#toOffsetDateTime`, `Instant#toJavaInstant`, `FixedOffsetRuleset#toZoneOffset`, `TimeQuantity#toPeriod`, `TimeQuantity#toDuration` | U2, U3 |

## Policy-period recipes

### What Chalten does and does not define

Chalten defines calendar arithmetic: a date plus or minus a number of **days**, a month-of-year plus or minus a number of **months** or **years**, the exact number of days between two dates, and day intervals. Both implementations reject months or years added directly to a date (see [exceptions](#exceptions)).

Chalten defines **no month-end clamping rule and no day-count convention** (actual/actual, actual/365, 30/360, ...). The Smalltalk library has neither, so the Java port adds neither. Policy code composes them from the operations above. The recipes below are application-level compositions; copy them into the policy layer and keep the convention visible in their names. They are not library behaviour, and adding such rules to Chalten would break parity.

Recipes use a half-open term: coverage runs from the inception date (inclusive) to the expiration date (exclusive), so the last covered day is `expiration previous` and the days in the term are `expiration - inception`.

### Month shifting conventions

Two month-end conventions cover the usual policy needs. Both shift the month-of-year with Chalten, then decide what to do when the day does not exist in the target month.

- **Clamp to month end**: Jan 31 + 1 month is Feb 28 (Feb 29 in a leap year). Same result as `java.time.LocalDate#plusMonths`, which `clampingAgreesWithJavaTimePlusMonths` checks over 48 months.
- **Roll to the next month**: Jan 31 + 1 month is Mar 1.

<table>
<tr><th>Smalltalk</th><th>Java</th></tr>
<tr><td>

```smalltalk
shift: aDate clampedToMonthEndBy: aQuantity
  | target lastDay |
  target := aDate monthOfYear next: aQuantity.
  lastDay := target numberOfDays amount.
  ^ target dateAt: (aDate dayNumber min: lastDay)

shift: aDate rollingToNextMonthBy: aQuantity
  | target lastDay |
  target := aDate monthOfYear next: aQuantity.
  lastDay := target numberOfDays amount.
  ^ aDate dayNumber <= lastDay
      ifTrue: [ target dateAt: aDate dayNumber ]
      ifFalse: [ target next first ]
```

</td><td>

```java
static GregorianDate shiftClampedToMonthEnd(GregorianDate date, TimeQuantity months) {
    GregorianMonthOfYear target = date.monthOfYear().next(months);
    int lastDay = target.numberOfDays().numericalValue().intValueExact();
    return target.dateAt(Math.min(date.dayNumber(), lastDay));
}

static GregorianDate shiftRollingToNextMonth(GregorianDate date, TimeQuantity months) {
    GregorianMonthOfYear target = date.monthOfYear().next(months);
    int lastDay = target.numberOfDays().numericalValue().intValueExact();
    return date.dayNumber() <= lastDay ? target.dateAt(date.dayNumber()) : target.next().first();
}
```

</td></tr>
</table>

### Creating dates

<table>
<tr><th>Smalltalk</th><th>Java</th></tr>
<tr><td>

```smalltalk
inception := GregorianMonth march fifteenth , 2025.
sameDate := GregorianDate
  monthOfYear: GregorianMonth march , 2025
  dayNumber: 15.
parsed := ISO8601Parser new parseDate: '2025-03-15'.

inception printString. "'March 15, 2025'"
inception printAsISO8601String. "'2025-03-15'"
inception dayOfWeek. "Saturday"
```

</td><td>

```java
GregorianDate inception = GregorianMonth.MARCH.fifteenth().of(2025);
GregorianDate sameDate = GregorianDate.of(2025, 3, 15);
GregorianDate parsed = new ISO8601Parser().parseDate("2025-03-15");
GregorianDate fromJavaTime = GregorianDate.from(LocalDate.of(2025, 3, 15));

assertEquals("March 15, 2025", inception.toString());
assertEquals("2025-03-15", inception.printAsISO8601String());
assertEquals(DayOfWeek.SATURDAY, inception.dayOfWeek());
```

</td></tr>
</table>

### 12-month term

Convention: clamp to month end; days in term are actual days.

<table>
<tr><th>Smalltalk</th><th>Java</th></tr>
<tr><td>

```smalltalk
inception := GregorianMonth march fifteenth , 2025.
term := 12 * month.
expiration := self shift: inception clampedToMonthEndBy: term.
lastCoveredDay := expiration previous.
daysInTerm := expiration - inception.

expiration. "March 15, 2026"
lastCoveredDay. "March 14, 2026"
daysInTerm printString. "'365 d'"
```

</td><td>

```java
GregorianDate inception = GregorianMonth.MARCH.fifteenth().of(2025);
TimeQuantity term = TimeQuantity.of(12, TimeUnits.MONTH);
GregorianDate expiration = shiftClampedToMonthEnd(inception, term);
GregorianDate lastCoveredDay = expiration.previous();
TimeQuantity daysInTerm = expiration.minus(inception);

assertEquals(date("2026-03-15"), expiration);
assertEquals(date("2026-03-14"), lastCoveredDay);
assertEquals(TimeQuantity.of(365, TimeUnits.DAY), daysInTerm);
assertEquals("365 d", daysInTerm.toString());
```

</td></tr>
</table>

`date("...")` in the tests is `new ISO8601Parser().parseDate("...")`.

### 6-month term

Convention: clamp to month end; days in term are actual days. An Aug 31 inception expires on the last day of February.

<table>
<tr><th>Smalltalk</th><th>Java</th></tr>
<tr><td>

```smalltalk
inception := GregorianMonth march fifteenth , 2025.
monthEndInception := GregorianMonth august thirtyFirst , 2025.
term := 6 * month.
expiration := self shift: inception clampedToMonthEndBy: term.
monthEndExpiration := self
  shift: monthEndInception clampedToMonthEndBy: term.

expiration. "September 15, 2025"
expiration previous. "September 14, 2025"
expiration - inception. "184 d"
monthEndExpiration. "February 28, 2026"
monthEndExpiration previous. "February 27, 2026"
monthEndExpiration - monthEndInception. "181 d"
```

</td><td>

```java
GregorianDate inception = GregorianMonth.MARCH.fifteenth().of(2025);
GregorianDate monthEndInception = GregorianMonth.AUGUST.thirtyFirst().of(2025);
TimeQuantity term = TimeQuantity.of(6, TimeUnits.MONTH);
GregorianDate expiration = shiftClampedToMonthEnd(inception, term);
GregorianDate monthEndExpiration = shiftClampedToMonthEnd(monthEndInception, term);

assertEquals(date("2025-09-15"), expiration);
assertEquals(date("2025-09-14"), expiration.previous());
assertEquals(TimeQuantity.of(184, TimeUnits.DAY), expiration.minus(inception));
assertEquals(date("2026-02-28"), monthEndExpiration);
assertEquals(date("2026-02-27"), monthEndExpiration.previous());
assertEquals(TimeQuantity.of(181, TimeUnits.DAY), monthEndExpiration.minus(monthEndInception));
```

</td></tr>
</table>

### Renewals for a Feb 29 inception

Conventions: clamp to month end (Feb 28) or roll to the next month (Mar 1). Renewal dates anchored on the original inception (`inception + n years`) return to Feb 29 in leap years; chaining each renewal on the previous expiration stays on Feb 28. Choose one explicitly. `1 * year` is `12 * month` (exact conversion).

<table>
<tr><th>Smalltalk</th><th>Java</th></tr>
<tr><td>

```smalltalk
inception := GregorianMonth february twentyNinth , 2024.
year := 1 * year.
clamped := self shift: inception clampedToMonthEndBy: year.
rolled := self shift: inception rollingToNextMonthBy: year.
fourthAnchored := self
  shift: inception clampedToMonthEndBy: year * 4.
fourthChained := inception.
1 to: 4 do: [ :renewal |
  fourthChained := self
    shift: fourthChained clampedToMonthEndBy: year ].

clamped. "February 28, 2025"
rolled. "March 1, 2025"
clamped - inception. "365 d"
rolled - inception. "366 d"
fourthAnchored. "February 29, 2028"
fourthChained. "February 28, 2028"
```

</td><td>

```java
GregorianDate inception = GregorianMonth.FEBRUARY.twentyNinth().of(2024);
TimeQuantity year = TimeQuantity.of(1, TimeUnits.YEAR);
GregorianDate clamped = shiftClampedToMonthEnd(inception, year);
GregorianDate rolled = shiftRollingToNextMonth(inception, year);
GregorianDate fourthAnchored = shiftClampedToMonthEnd(inception, year.times(4));
GregorianDate fourthChained = inception;
for (int renewal = 1; renewal <= 4; renewal++) {
    fourthChained = shiftClampedToMonthEnd(fourthChained, year);
}

assertEquals(date("2025-02-28"), clamped);
assertEquals(date("2025-03-01"), rolled);
assertEquals(TimeQuantity.of(365, TimeUnits.DAY), clamped.minus(inception));
assertEquals(TimeQuantity.of(366, TimeUnits.DAY), rolled.minus(inception));
assertEquals(date("2028-02-29"), fourthAnchored);
assertEquals(date("2028-02-28"), fourthChained);
```

</td></tr>
</table>

### Jan 31 + 1 month

Conventions: clamp to month end or roll to the next month. As with renewals, anchored and chained schedules differ after a short month.

<table>
<tr><th>Smalltalk</th><th>Java</th></tr>
<tr><td>

```smalltalk
inception := GregorianMonth january thirtyFirst , 2025.
leapYearInception := GregorianMonth january thirtyFirst , 2024.
oneMonth := 1 * month.
clamped := self shift: inception clampedToMonthEndBy: oneMonth.
rolled := self shift: inception rollingToNextMonthBy: oneMonth.
leapYearClamped := self
  shift: leapYearInception clampedToMonthEndBy: oneMonth.
secondAnchored := self
  shift: inception clampedToMonthEndBy: oneMonth * 2.
secondChained := self shift: clamped clampedToMonthEndBy: oneMonth.

clamped. "February 28, 2025"
rolled. "March 1, 2025"
leapYearClamped. "February 29, 2024"
secondAnchored. "March 31, 2025"
secondChained. "March 28, 2025"
clamped isLastDayOfMonth. "true"
```

</td><td>

```java
GregorianDate inception = GregorianMonth.JANUARY.thirtyFirst().of(2025);
GregorianDate leapYearInception = GregorianMonth.JANUARY.thirtyFirst().of(2024);
TimeQuantity oneMonth = TimeQuantity.of(1, TimeUnits.MONTH);
GregorianDate clamped = shiftClampedToMonthEnd(inception, oneMonth);
GregorianDate rolled = shiftRollingToNextMonth(inception, oneMonth);
GregorianDate leapYearClamped = shiftClampedToMonthEnd(leapYearInception, oneMonth);
GregorianDate secondAnchored = shiftClampedToMonthEnd(inception, oneMonth.times(2));
GregorianDate secondChained = shiftClampedToMonthEnd(clamped, oneMonth);

assertEquals(date("2025-02-28"), clamped);
assertEquals(date("2025-03-01"), rolled);
assertEquals(date("2024-02-29"), leapYearClamped);
assertEquals(date("2025-03-31"), secondAnchored);
assertEquals(date("2025-03-28"), secondChained);
assertTrue(clamped.isLastDayOfMonth());
```

</td></tr>
</table>

### Monthly installment schedule

Convention: clamp to month end, each due date anchored on the inception.

<table>
<tr><th>Smalltalk</th><th>Java</th></tr>
<tr><td>

```smalltalk
inception := GregorianMonth january thirtyFirst , 2025.
dueDates := (0 to: 11) collect: [ :n |
  self shift: inception clampedToMonthEndBy: n * month ].

dueDates collect: [ :each | each printAsISO8601String ].
"#('2025-01-31' '2025-02-28' '2025-03-31' '2025-04-30'
   '2025-05-31' '2025-06-30' '2025-07-31' '2025-08-31'
   '2025-09-30' '2025-10-31' '2025-11-30' '2025-12-31')"
```

</td><td>

```java
GregorianDate inception = GregorianMonth.JANUARY.thirtyFirst().of(2025);
List<GregorianDate> dueDates = IntStream.range(0, 12)
        .mapToObj(n -> shiftClampedToMonthEnd(inception, TimeQuantity.of(n, TimeUnits.MONTH)))
        .toList();

assertEquals(List.of("2025-01-31", "2025-02-28", "2025-03-31", "2025-04-30", "2025-05-31", "2025-06-30",
        "2025-07-31", "2025-08-31", "2025-09-30", "2025-10-31", "2025-11-30", "2025-12-31"),
        dueDates.stream().map(GregorianDate::printAsISO8601String).toList());
```

</td></tr>
</table>

### Pro-rata day counts (actual/actual)

Convention: actual/actual on the term. Days elapsed and days in term are actual calendar days of the half-open term; the earned fraction is their exact ratio. Dividing two day quantities returns an exact `Fraction` in both implementations; never convert to `double`. The premium is 1200 in the policy currency, and rounding to currency units is a separate policy rule applied last.

<table>
<tr><th>Smalltalk</th><th>Java</th></tr>
<tr><td>

```smalltalk
inception := GregorianMonth january first , 2024.
expiration := self
  shift: inception clampedToMonthEndBy: 1 * year.
cancellation := GregorianMonth april first , 2024.
daysInTerm := expiration - inception.
daysElapsed := cancellation - inception.
daysRemaining := expiration - cancellation.
earned := daysElapsed / daysInTerm.
unearned := 1 - earned.
earnedPremium := earned * 1200.
returnPremium := unearned * 1200.

expiration. "January 1, 2025"
daysInTerm. "366 d"
daysElapsed. "91 d"
daysRemaining. "275 d"
earned. "(91/366)"
unearned. "(275/366)"
earnedPremium. "(18200/61)"
returnPremium. "(55000/61)"
```

</td><td>

```java
GregorianDate inception = GregorianMonth.JANUARY.first().of(2024);
GregorianDate expiration = shiftClampedToMonthEnd(inception, TimeQuantity.of(1, TimeUnits.YEAR));
GregorianDate cancellation = GregorianMonth.APRIL.first().of(2024);
TimeQuantity daysInTerm = expiration.minus(inception);
TimeQuantity daysElapsed = cancellation.minus(inception);
TimeQuantity daysRemaining = expiration.minus(cancellation);
Fraction earned = daysElapsed.dividedBy(daysInTerm);
Fraction unearned = Fraction.ONE.minus(earned);
Fraction earnedPremium = earned.times(1200);
Fraction returnPremium = unearned.times(1200);

assertEquals(date("2025-01-01"), expiration);
assertEquals(TimeQuantity.of(366, TimeUnits.DAY), daysInTerm);
assertEquals(TimeQuantity.of(91, TimeUnits.DAY), daysElapsed);
assertEquals(TimeQuantity.of(275, TimeUnits.DAY), daysRemaining);
assertEquals(Fraction.of(91, 366), earned);
assertEquals(Fraction.of(275, 366), unearned);
assertEquals("(91/366)", earned.toString());
assertEquals("(275/366)", unearned.toString());
assertEquals(Fraction.of(18_200, 61), earnedPremium);
assertEquals(Fraction.of(55_000, 61), returnPremium);
assertEquals(Fraction.of(1200), earnedPremium.plus(returnPremium));
assertEquals(daysRemaining.dividedBy(daysInTerm), unearned);
```

</td></tr>
</table>

### Pro-rata day counts (actual/365)

Convention: actual/365 fixed. Days elapsed are actual days; the denominator is always 365, even in a leap-year term.

<table>
<tr><th>Smalltalk</th><th>Java</th></tr>
<tr><td>

```smalltalk
inception := GregorianMonth january first , 2024.
cancellation := GregorianMonth april first , 2024.
daysElapsed := cancellation - inception.
earned := daysElapsed / (365 * day).

earned. "(91/365)"
earned * 1200. "(21840/73)"
```

</td><td>

```java
GregorianDate inception = GregorianMonth.JANUARY.first().of(2024);
GregorianDate cancellation = GregorianMonth.APRIL.first().of(2024);
TimeQuantity daysElapsed = cancellation.minus(inception);
Fraction earned = daysElapsed.dividedBy(TimeQuantity.of(365, TimeUnits.DAY));

assertEquals(Fraction.of(91, 365), earned);
assertEquals(Fraction.of(21_840, 73), earned.times(1200));
```

</td></tr>
</table>

### Covered days as an interval

Convention: the interval is closed on both ends, so it runs from the inception to the last covered day. Its size equals the actual/actual days in the term.

<table>
<tr><th>Smalltalk</th><th>Java</th></tr>
<tr><td>

```smalltalk
inception := GregorianMonth march fifteenth , 2025.
lastCoveredDay := GregorianMonth march fourteenth , 2026.
coveredDays := inception to: lastCoveredDay.
weeklyAudits := inception to: lastCoveredDay by: 7 * day.

coveredDays size. "365"
coveredDays first. "March 15, 2025"
coveredDays last. "March 14, 2026"
weeklyAudits size. "53"
(coveredDays select: [ :each |
  each dayOfWeek = DayOfWeek saturday ]) size. "53"
```

</td><td>

```java
GregorianDate inception = GregorianMonth.MARCH.fifteenth().of(2025);
GregorianDate lastCoveredDay = GregorianMonth.MARCH.fourteenth().of(2026);
DiscreteInterval<GregorianDate> coveredDays = inception.to(lastCoveredDay);
DiscreteInterval<GregorianDate> weeklyAudits = inception.to(lastCoveredDay, TimeQuantity.of(7, TimeUnits.DAY));

assertEquals(365, coveredDays.size());
assertEquals(inception, coveredDays.first());
assertEquals(lastCoveredDay, coveredDays.last());
assertTrue(coveredDays.contains(GregorianMonth.FEBRUARY.twentyEighth().of(2026)));
assertFalse(coveredDays.contains(GregorianMonth.MARCH.fifteenth().of(2026)));
assertEquals(53, weeklyAudits.size());
assertEquals(date("2026-03-14"), weeklyAudits.last());
assertEquals(53, coveredDays.select(d -> d.dayOfWeek() == DayOfWeek.SATURDAY).size());
```

</td></tr>
</table>

### Effective time at a fixed offset

Policies that take effect at 12:01 a.m. local time map to an absolute instant through a time-offset ruleset. A fixed offset ignores daylight saving; IANA zones use `TimeZoneRuleset` from `chalten-timezones`.

<table>
<tr><th>Smalltalk</th><th>Java</th></tr>
<tr><td>

```smalltalk
inception := GregorianMonth march fifteenth , 2025.
eastern := FixedOffsetRuleset for: -5 * hour.
effective := (inception at: (TimeOfDay hours: 0 minutes: 1))
  in: eastern.
instant := effective asInstant.
```

</td><td>

```java
GregorianDate inception = GregorianMonth.MARCH.fifteenth().of(2025);
FixedOffsetRuleset eastern = FixedOffsetRuleset.of(TimeQuantity.of(-5, TimeUnits.HOUR));
TimeOffsetAwareGregorianDateTime effective = inception.at(TimeOfDay.hours(0, 1)).in(eastern);
Instant instant = effective.asInstant();

assertEquals("2025-03-15T00:01:00.000000-05:00", effective.toString());
assertEquals("2025-03-15T05:01:00.000000Z", instant.toString());
assertEquals(java.time.Instant.parse("2025-03-15T05:01:00Z"), instant.toJavaInstant());
```

</td></tr>
</table>

### Parsing and formatting

<table>
<tr><th>Smalltalk</th><th>Java</th></tr>
<tr><td>

```smalltalk
parser := ISO8601Parser new.
effective := parser parseDateTime: '2025-03-15T00:01:00'.
term := parser parseDuration: 'P12M'.

effective printAsISO8601String.
"'2025-03-15T00:01:00.000000'"
term. "12 mo"
ISO8601DurationFormatter new format: term. "'P12M'"
```

</td><td>

```java
ISO8601Parser parser = new ISO8601Parser();
GregorianDateTime effective = parser.parseDateTime("2025-03-15T00:01:00");
TimeQuantity term = parser.parseDuration("P12M");

assertEquals("2025-03-15T00:01:00.000000", effective.printAsISO8601String());
assertEquals(TimeQuantity.of(12, TimeUnits.MONTH), term);
assertEquals("P12M", new org.chalten.measure.ISO8601DurationFormatter().format(term));
```

</td></tr>
</table>

## java.time interop

Use Chalten types inside the policy domain and convert at the edges (persistence, JSON, other libraries). Conversions are exact for dates, months and years. `toJavaInstant()`, `toLocalTime()` and `toOffsetDateTime()` truncate below what `java.time` holds; Chalten keeps exact microseconds ([DIFFERENCES.md C6](DIFFERENCES.md#chalten-core-u2)).

`TimeQuantity#toPeriod` returns a normalized `java.time.Period`, so 12 months becomes `P1Y`. `LocalDate#plus(Period)` and `plusMonths` clamp to month end, the same as the clamping recipe; Chalten itself never clamps.

```java
GregorianDate inception = GregorianMonth.MARCH.fifteenth().of(2025);
LocalDate local = inception.toLocalDate();
GregorianDate back = GregorianDate.from(local);
TimeQuantity term = TimeQuantity.of(12, TimeUnits.MONTH);
java.time.Period period = term.toPeriod();
java.time.Duration duration = TimeQuantity.of(36, TimeUnits.HOUR).toDuration();

assertEquals(LocalDate.of(2025, 3, 15), local);
assertEquals(inception, back);
assertEquals(java.time.Period.ofYears(1), period);
assertEquals(java.time.Duration.ofHours(36), duration);
assertEquals(local.plus(period), shiftClampedToMonthEnd(inception, term).toLocalDate());
```

```java
assertEquals(ZoneOffset.ofHours(-5), eastern.toZoneOffset());
assertEquals(effective.toOffsetDateTime(),
        java.time.OffsetDateTime.parse("2025-03-15T00:01-05:00"));
```

Do not do policy arithmetic in `java.time` and then convert back: `java.time` uses `long` and `int` fields, and Chalten's exact `Fraction` ratios and day counts are the parity contract.

## Threading and locale context

- **Values are immutable.** Dates, months, years, times of day, instants, quantities, fractions, rulesets and intervals are final classes, records or enums with no mutators. Share them freely across threads.
- **`ISO8601Parser` and `ISO8601DurationFormatter`** keep no state between calls; one instance per call, as in the examples, is the simplest rule.
- **Localization is thread-scoped.** Smalltalk scopes translations with dynamic variables (`asNaturalLanguageTranslatorDuring:`, `CurrentLocale use:during:`). Java scopes them per thread with `Translations.during(translator, action)` and `Translations.duringLanguage(language, action)`; the previous translator is restored when the action ends, even on an exception. The scope is an `InheritableThreadLocal`: threads created inside the scope inherit it, but pooled executor threads do not, so re-establish the scope inside tasks submitted to an executor.
- **Defaults are global.** `Translations.setDefaultLanguage` and `setDefaultTranslator` change every thread without a scope. Call them once at start-up only.
- Month and day names (`fullName()`, `shortName()`) and exception messages are translated. Locale-aware date formatting uses `CurrentLocale` and `LocalizedChronologyFormatter` in `org.chalten.localization`.

<table>
<tr><th>Smalltalk</th><th>Java</th></tr>
<tr><td>

```smalltalk
spanish := CurrentLocale use: 'es' during: [
  GregorianMonth march name ].
english := GregorianMonth march name.

spanish. "'Marzo'"
english. "'March'"
```

</td><td>

```java
String spanish = Translations.duringLanguage("es", () -> GregorianMonth.MARCH.fullName());
String english = GregorianMonth.MARCH.fullName();

assertEquals("Marzo", spanish);
assertEquals("March", english);
```

</td></tr>
</table>

## Exceptions

Java exceptions keep the Smalltalk message texts ([DIFFERENCES.md C3](DIFFERENCES.md#chalten-core-u2)). Catch the Java type that replaces the Smalltalk class:

| Smalltalk | Java | Raised when |
| --- | --- | --- |
| `InstanceCreationFailed` | `InstanceCreationFailedException` | An invalid date, month or time of day is built or parsed. |
| `ArithmeticError` | `ArithmeticException` | Subtracting a later date from an earlier one, adding incommensurable quantities, or adding months or years to a date. |
| `MessageNotUnderstood` | `UnsupportedOperationException` | Asking a month for an ordinal it lacks (`april thirtyFirst`). |
| `AssertionFailed` | `ChaltenAssertionException` | A domain rule fails, such as a fixed offset out of range. |
| `SubscriptOutOfBounds` | `IndexOutOfBoundsException` | `DiscreteInterval#get` with an index out of range (zero-based in Java). |
| `KeyNotFound` | `IllegalArgumentException` | `TimeUnits.named` with an unknown unit name. |

```java
GregorianDate inception = GregorianMonth.MARCH.fifteenth().of(2026);
GregorianDate cancellation = GregorianMonth.MARCH.first().of(2026);
ArithmeticException beforeInception =
        assertThrows(ArithmeticException.class, () -> cancellation.minus(inception));
InstanceCreationFailedException invalidDay =
        assertThrows(InstanceCreationFailedException.class, () -> GregorianMonth.FEBRUARY.of(2025).dateAt(29));
InstanceCreationFailedException invalidText =
        assertThrows(InstanceCreationFailedException.class, () -> new ISO8601Parser().parseDate("2025-02-30"));
UnsupportedOperationException missingSelector =
        assertThrows(UnsupportedOperationException.class, () -> GregorianMonth.APRIL.thirtyFirst());
ArithmeticException monthsOnADate =
        assertThrows(ArithmeticException.class, () -> inception.plus(TimeQuantity.of(1, TimeUnits.MONTH)));

assertEquals("Cannot subtract two dates when the second one is greater than the first one.",
        beforeInception.getMessage());
assertEquals("29 is an invalid day number in February, 2025", invalidDay.getMessage());
assertEquals("30 is an invalid day number in February, 2025", invalidText.getMessage());
assertEquals("April does not understand #thirtyFirst", missingSelector.getMessage());
assertEquals("Incommensurable units are not comparable", monthsOnADate.getMessage());
```

The last case is why the month-shifting recipes go through `monthOfYear()`: a date only moves by day-commensurable quantities.

## Behaviour differences

Intentional differences between the implementations are listed in [DIFFERENCES.md](DIFFERENCES.md). The ones most likely to affect migrated policy code:

- [M1/M2](DIFFERENCES.md#chalten-measure-u3): no floating-point amounts; decimals are exact `Fraction`s and print as `(3/2) h`.
- [M5](DIFFERENCES.md#chalten-measure-u3): `DiscreteInterval#get` is zero-based (`get(0)` is `at: 1`).
- [C1](DIFFERENCES.md#chalten-core-u2): renamed selectors (`fullName()`, `FixedOffsetRuleset.of`, `april.first().of(2025)`).
- [C2/C3](DIFFERENCES.md#chalten-core-u2): exception classes renamed, message texts unchanged.
- [C5](DIFFERENCES.md#chalten-core-u2): abstract Smalltalk classes are Java interfaces with default methods.
- [C6](DIFFERENCES.md#chalten-core-u2): `java.time` conversions truncate; Chalten keeps exact microseconds.
- [C8](DIFFERENCES.md#chalten-core-u2): localization scope is thread-based, see [threading and locale context](#threading-and-locale-context).

The class mapping and the SUnit-to-JUnit parity matrix are in [PLAN.md](PLAN.md).
