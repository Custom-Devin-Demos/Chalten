<!-- markdownlint-disable MD013 MD033 -->

# Chalten → Java 21 port plan

Behaviour-preserving port of the Smalltalk Chalten time model (`source/`, Tonel) to Java 21 under `java/`.
The Smalltalk sources, `rowan/`, `.smalltalkci/` and the existing Pharo/GS64 workflows stay untouched so
both implementations can be compared side by side.

## 1. Inventory

Generated from `source/**/*.st` (see the appendix for every selector).

| Package | Classes | Extensions | Role | Port? |
| --- | --- | --- | --- | --- |
| `Chalten-Core` | 27 | – | Gregorian model, absolute time, offsets, ISO 8601, localization | yes |
| `Chalten-Core-Extensions` | – | 2 | `Integer>>asGregorianYear`, `Object>>isAbsolutePointInTime` | yes (static factories) |
| `Chalten-Core-Aconcagua-Extensions` | – | 1 | `Quantity` double dispatch for `Date ± quantity` | yes (overloads) |
| `Chalten-Core-Buoy-Extensions` | – | 1 | `LocaleConventions` chronology patterns loading | yes |
| `Chalten-Core-GS64-Extensions` | – | 2 | GemStone clock / binary stream shims | no (platform shim) |
| `Chalten-Timezones` | 5 | – | IANA TZif parser, `TimeZoneRuleset`, ambiguity errors | yes |
| `Chalten-Hebrew-Calendar` (+`-Extensions`) | 7 | 3 | Hebrew calendar | yes |
| `Chalten-Islamic-Calendar` (+`-Extensions`) | 7 | 3 | Tabular Islamic calendar | yes |
| `Chalten-SUnit` | 1 | – | `ChaltenTest` abstract fixture | yes (JUnit base class) |
| `Chalten-*-Tests` | 36 | 2 | SUnit tests | yes (JUnit 5) |
| `Chalten-Development-Tools` | 1 | – | Translation export script | no (dev tool) |
| `BaselineOfChalten` | 1 | – | Metacello baseline | no (replaced by Gradle) |
| **Total** | **85** | **14** | **1,316 methods, 455 SUnit tests** | |

### Findings that shape the scope

- **No business-day / holiday calendar exists in Chalten.** "Calendars" here are the Gregorian, Hebrew and
  Islamic calendar systems plus IANA timezones. A behaviour-preserving port therefore has nothing to port for
  business days or holidays; adding them would be new functionality (proposed as an optional follow-up unit, §7).
- **Spans/durations are Aconcagua measures**, not Chalten classes: `3 * month`, `2 * decade`, `1 * day` are
  `Measure`s over `TimeUnits` (relative: year, month, lustrum, decade, jubilee, century, millenium) and SI units
  (second-derived: microsecond … week). Intervals are Aconcagua `DiscreteInterval`s created by `to:`/`to:by:`.
  The Java port needs a small, exact (rational) quantity/unit library: that is unit U3's core.
- **Arithmetic is exact.** Smalltalk uses `Fraction`s (`month = 1/12 year`, Julian epoch `2440587 + 1/2`).
  Java must use exact rationals, never `double`.
- **Dates only move by day-commensurable quantities** (`CalendarDate>>isComparableTo:`). Month/year shifts live on
  `MonthOfYear` / `Year`. Chalten has no "same day next year with month-end clamping" or day-count
  conventions (the `TimeUnits` comment defers them to the caller). Policy-period helpers (term end, renewal,
  pro-rata) are therefore documented in `MIGRATION.md` as compositions, not invented as library behaviour.
- **Localization** (CLDR patterns in `locales/chronology.json`, month/weekday names in `locales/{en,es}/*.json`)
  is part of Core. The Java port reads those same JSON files as resources (no copies).
- **Timezones** parse binary TZif files from `TZDIR` or `/usr/share/zoneinfo/`; the Java port keeps that
  parser for parity and adds `java.time.ZoneId` interop on top.

## 2. Target layout

Gradle (Kotlin DSL, wrapper committed), Java 21 toolchain, JUnit 5, jqwik, JaCoCo. Base package `org.chalten`.

```text
java/
  settings.gradle.kts, build.gradle.kts, gradle/libs.versions.toml, gradlew
  build-logic/         chalten.java-library convention plugin (toolchain, JUnit 5, jqwik, JaCoCo)
  chalten-measure/     org.chalten.measure       Fraction, TimeUnit(s), TimeQuantity, DiscreteInterval, ISO8601DurationFormatter
  chalten-core/        org.chalten.core          Gregorian model, TimeOfDay, Instant, offsets, ISO8601Parser/Formatter
                       org.chalten.localization  ChronologyPattern, LocaleConventions, LocalizedChronologyFormatter
  chalten-timezones/   org.chalten.timezones     IANATimeZoneDatabase(+Parser), TimeZoneRuleset, Ambiguous/SkippedTimeException
  chalten-hebrew/      org.chalten.hebrew        Hebrew calendar
  chalten-islamic/     org.chalten.islamic       Islamic calendar
  chalten-testkit/     org.chalten.testkit       ChaltenTest base (port of Chalten-SUnit), parity-matrix checker
  PLAN.md, MIGRATION.md, DIFFERENCES.md
```

Module dependencies: `measure ← core ← {timezones, hebrew, islamic}`; `testkit` (`api` on `core` and JUnit Jupiter,
so its `ChaltenTest` can hold the Gregorian fixtures) is `testImplementation` everywhere.
`chalten-core` packages the repo-root `locales/` in place as classpath resources under `locales/`
(`processResources { from("../locales") { into("locales") } }`, path relative to `java/`), e.g. `/locales/chronology.json`.

## 3. Smalltalk idiom → Java mapping

| Smalltalk idiom | Java 21 design |
| --- | --- |
| Abstract class with `subclassResponsibility` (`CalendarDate`, `CalendarYear`, `CalendarMonthOfYear`, `CalendarDateTime`, `AbsolutePointInTime`, `TimeOffsetRuleset`) | `sealed interface` (or `sealed abstract class` when it carries shared logic, e.g. `CalendarDate.next(TimeQuantity)`) permitting the Gregorian/Hebrew/Islamic finals |
| Immutable value objects with `=`/`hash` via `equalityChecker` | `record` where the state is the identity (`GregorianDate(GregorianMonthOfYear, int)`, `TimeOfDay`, `Instant`, `TimeQuantity`); `final class` + cached fields where the object memoizes (`GregorianYear` months) |
| Invariant canonical instances (`makeInstancesInvariant`, instance registries for months, day-of-month, day-of-week) | `enum` (`GregorianMonth`, `DayOfWeek`, `HebrewMonth`, `IslamicMonth`, `HebrewDayOfWeek`, `IslamicDayOfWeek`); interned records for `CalendarDayOfMonth` |
| Class-side constructors (`GregorianYear number: 2024`, `TimeOfDay hours:minutes:`, `Instant fromUnixTime:`) | static factories with the same names: `GregorianYear.number(2024)`, `TimeOfDay.hours(h, m)`, `Instant.fromUnixTime(s)`; constructors private |
| Extension methods (`2024 asGregorianYear`, `March , 2026`, `March fifth`) | static factories (`GregorianYear.of(2024)`), `GregorianMonth.MARCH.of(2026)` (`,`), `MARCH.fifth()` |
| `Magnitude` (`< <= > >= between:and: max: min:`) | `Comparable<T>` + default methods `isBefore/isAfter/between/max/min` on `CalendarMagnitude<T>` |
| Double dispatch `+`/`-` (`additionWithCalendarDate:`, `subtractionFromCalendarDate:`) | overloads: `date.plus(TimeQuantity)`, `date.minus(TimeQuantity)`, `date.minus(GregorianDate) → TimeQuantity` (days) |
| `next`, `next:`, `previous`, `previous:` | `next()`, `next(TimeQuantity)`, `previous()`, `previous(TimeQuantity)` (same names; same zero/negative handling) |
| Aconcagua `Measure` / `Unit` / `convertTo:` / `numericalValue` | `TimeQuantity(Fraction amount, TimeUnit unit)` record, `TimeUnit` sealed (`BaseUnit`, `DerivedUnit` with exact factor), `convertTo(TimeUnit)`, `amount()`; incommensurable mixes → `CompoundTimeQuantity` (Aconcagua `MeasureBag`) |
| `Fraction`, `LargePositiveInteger` | `org.chalten.measure.Fraction` (BigInteger num/den, normalized), `long`/`BigInteger` where ranges need it |
| `DiscreteInterval from:to:by:` (`to:`, `to:by:`) | `DiscreteInterval<T extends Steppable<T>>` implementing `Iterable<T>` / `stream()`, inclusive ends, same step semantics |
| Blocks (`localizedWith: [:options \| options full]`, `ifAbsent:`, `then:otherwise:`) | `Consumer<FormatterOptions>`, `Supplier<T>`, `Optional<T>`, plain conditionals |
| Dynamic variable `CurrentLocale use:during:` | `CurrentLocale.use(tag, Supplier<T>)` backed by an inheritable `ThreadLocal` (default `en`) |
| `printOn:` / `printString` | `toString()` returning the identical string (`"March 5, 2026"`, `"2026-03-05T10:00:00.000000-03:00"`) |
| `printAsISO8601String`, `ISO8601Formatter` | `toIso8601String()`, `ISO8601Formatter.format(...)` |
| `localized`, `localizedWith:` | `localized()`, `localized(Consumer<FormatterOptions>)` |
| `AssertionChecker enforce:because:raising: InstanceCreationFailed` | `InstanceCreationFailedException extends IllegalArgumentException`, identical message text |
| `ArithmeticError` | `java.lang.ArithmeticException`, identical message text |
| `AssertionFailed` | `ChaltenAssertionException extends IllegalStateException` |
| `doesNotUnderstand:` (e.g. `February thirtieth`, `(February , 2025) twentyNinth`) | `UnsupportedOperationException` with message naming the selector (documented in `DIFFERENCES.md`) |
| `AmbiguousTimeError` (resumable, carries earlier/later) / `SkippedTimeError` | `AmbiguousTimeException` / `SkippedTimeException` (unchecked) plus overloads taking a `TimeResolutionStrategy` to mirror `on:do:` + `return:` handlers |
| `Namespace` / `TimeUnits units >> #decade` | `TimeUnits.DECADE` constants + `TimeUnits.named("decade")` |

### java.time interop (additive, never used for core semantics)

| Chalten (Java) | java.time |
| --- | --- |
| `GregorianDate` | `toLocalDate()` / `GregorianDate.from(LocalDate)` (both proleptic ISO with year 0) |
| `TimeOfDay` | `toLocalTime()` / `from(LocalTime)` (nanos truncated to micros) |
| `GregorianDateTime` | `toLocalDateTime()` / `from(LocalDateTime)` |
| `GregorianYear`, `GregorianMonthOfYear`, `GregorianMonth`, `DayOfWeek` | `Year`, `YearMonth`, `Month`, `java.time.DayOfWeek` |
| `Instant` | `toJavaInstant()` / `from(java.time.Instant)` (micros) |
| `TimeOffsetAwareGregorianDateTime` | `toOffsetDateTime()`, `toZonedDateTime()` when the ruleset is a `TimeZoneRuleset` |
| `FixedOffsetRuleset`, `TimeZoneRuleset` | `ZoneOffset`, `ZoneId` (`TimeZoneRuleset.toZoneId()`) |
| `TimeQuantity` (absolute units) | `java.time.Duration`; relative units → `java.time.Period` |

## 4. Class mapping

| Smalltalk class | Java type | Kind | Module | Unit |
| --- | --- | --- | --- | --- |
| `TimeUnits` (+ SI time units from Aconcagua) | `TimeUnits`, `TimeUnit` | final class constants, sealed interface | measure | U3 |
| Aconcagua `Measure`, `MeasureBag` | `TimeQuantity`, `CompoundTimeQuantity` | records | measure | U3 |
| Smalltalk `Fraction` | `Fraction` | final class | measure | U3 |
| Aconcagua `DiscreteInterval` | `DiscreteInterval<T>` | final class | measure | U3 |
| `ISO8601DurationFormatter` | `ISO8601DurationFormatter` | final class | measure | U3 |
| `Quantity` extensions (Aconcagua-Extensions) | `plus/minus(TimeQuantity)` overloads on dates/years | methods | core | U2 |
| `CalendarMagnitude` | `CalendarMagnitude<T>` | interface (`Comparable` + default ordering methods) | core | U2 |
| `CalendarDate` | `CalendarDate<D, M, Y>` | interface with default methods | core | U2 |
| `CalendarDateTime` | `CalendarDateTime<T, D>` | interface with default methods | core | U2 |
| `CalendarYear` | `CalendarYear<Y, M, D>` | interface with default methods | core | U2 |
| `CalendarMonthOfYear` | `CalendarMonthOfYear<M, D, Y>` | interface with default methods | core | U2 |
| `CalendarMonth` | `CalendarMonth<D>` | interface (enums implement) | core | U2 |
| `CalendarDayOfWeek` | `CalendarDayOfWeek` | interface (enums implement) | core | U2 |
| `CalendarDayOfMonth` | `CalendarDayOfMonth<D>` | record (value equality, not interned) | core | U2 |
| `GregorianYear` | `GregorianYear` | final class | core | U2 |
| `GregorianMonth` | `GregorianMonth` | enum | core | U2 |
| `GregorianMonthOfYear` | `GregorianMonthOfYear` | final class (value equality) | core | U2 |
| `GregorianDate` | `GregorianDate` | final class (value equality) | core | U2 |
| `GregorianDateTime` | `GregorianDateTime` | final class (value equality) | core | U2 |
| `DayOfWeek` | `DayOfWeek` | enum | core | U2 |
| `TimeOfDay` | `TimeOfDay` | final class (value equality) | core | U2 |
| `EdwardGrahamRichardsAlgorithm` | `EdwardGrahamRichardsAlgorithm` | final class (singleton) | core | U2 |
| `AbsolutePointInTime` | `AbsolutePointInTime` | sealed interface | core | U2 |
| `Instant` | `Instant` | final class (exact `TimeQuantity` microseconds since the epoch) | core | U2 |
| `TimeOffsetAwareGregorianDateTime` | `TimeOffsetAwareGregorianDateTime` | final class | core | U2 |
| `TimeOffsetRuleset` | `TimeOffsetRuleset` | interface (open for the timezones module) | core | U2 |
| `FixedOffsetRuleset` | `FixedOffsetRuleset` | final class (identity equality, as in Smalltalk) | core | U2 |
| `ISO8601Parser` | `ISO8601Parser` | final class | core | U2 |
| `ISO8601Formatter` | `ISO8601Formatter` | final class | core | U2 |
| `Integer>>asGregorianYear`, `Object>>isAbsolutePointInTime` | `GregorianYear.of(long)`, `AbsolutePointInTime.isAbsolutePointInTime(Object)` | – | core | U2 |
| `ChronologyPattern` | `ChronologyPattern` | final class | core (`localization`) | U4 |
| `LocalizedChronologyFormatter` | `LocalizedChronologyFormatter`, `FormatterOptions` | final classes | core (`localization`) | U4 |
| `LocaleConventions` chronology extension (Buoy) | `LocaleConventions`, `CurrentLocale` | final classes | core (`localization`) | U4 |
| `TimeZoneRuleset` | `TimeZoneRuleset` | final class | timezones | U4 |
| `IANATimeZoneDatabase`, `IANATimeZoneDatabaseParser` | same names | final classes | timezones | U4 |
| `AmbiguousTimeError`, `SkippedTimeError` | `AmbiguousTimeException`, `SkippedTimeException` | exceptions | timezones | U4 |
| `HebrewCalendar`, `HebrewYear`, `HebrewMonth`, `HebrewMonthOfYear`, `HebrewDate`, `HebrewDateTime`, `HebrewDayOfWeek` (+ extensions) | same names (`HebrewMonth`, `HebrewDayOfWeek` enums) | final classes / records / enums | hebrew | U4 |
| `IslamicCalendar`, `IslamicYear`, `IslamicMonth`, `IslamicMonthOfYear`, `IslamicDate`, `IslamicDateTime`, `IslamicDayOfWeek` (+ extensions) | same names | final classes / records / enums | islamic | U4 |
| `ChaltenTest` (Chalten-SUnit) | `org.chalten.testkit.ChaltenTest` | abstract JUnit base | testkit | U1 stub, U2 fills |
| `BaselineOfChalten`, `Chalten-Development-Tools`, `Chalten-Core-GS64-Extensions` | – | not ported | – | – |

## 5. Units, ordering and acceptance

Each unit is one child session and one PR. The dependency graph is mostly a chain (every Chalten class sits on
the measure library and the Gregorian core), so units run in waves and PRs are stacked: each PR targets the
previous unit's branch and GitHub retargets them as the bottom merges.

```text
PLAN (this PR) ← U1 skeleton+CI ← U3 measures/intervals ← U2 core ← { U4 calendars+tz+l10n | U5 parity+properties | U6 migration guide }
```

| Wave | Unit | Scope | Done when |
| --- | --- | --- | --- |
| 1 | **U1 Skeleton + CI** | Gradle wrapper, multi-module layout of §2, Java 21 toolchain, JUnit 5 + jqwik + JaCoCo, `chalten-testkit` stub, `.github/workflows/java.yml` (build, test, JaCoCo XML/HTML artifact + job summary) triggered on `java/**` | `./gradlew build` green locally and in Actions |
| 2 | **U3 Spans, intervals, durations** | `Fraction`, `TimeUnit(s)`, `TimeQuantity`, `CompoundTimeQuantity`, `DiscreteInterval`, `ISO8601DurationFormatter`; port `TimeUnitsTest`, `ISO8601DurationFormatterTest` | ported tests + unit tests green, exact rational arithmetic |
| 3 | **U2 Core time model** | everything marked U2 in §4 incl. date/month/year arithmetic and month-end / leap-year / year-0 / negative-year edge cases; port the U2 SUnit classes | every U2 SUnit test ported and green |
| 4 | **U4 Calendars, timezones, localization** | Hebrew, Islamic, `chalten-timezones`, `org.chalten.localization`; port their SUnit classes | every U4 SUnit test ported and green |
| 4 | **U5 Test parity** | `chalten-testkit` parity checker (parses `source/**/*Test*.st`, fails the build if any SUnit selector lacks a same-named JUnit method), jqwik invariants (§6), `DIFFERENCES.md`, fills the Status column below | checker green over U2/U3 (U4 classes reported until U4 lands), properties green |
| 4 | **U6 Migration guide** | `java/MIGRATION.md`: Smalltalk → Java API table, policy-period recipes (term start/end, renewal dates with month-end handling, pro-rata day counts) as compiled, tested examples | examples compile and run as tests |

Porting rule for U2/U3/U4: a JUnit class per SUnit class with the **same class and method names**, same assertions,
same expected strings and error messages. Any deviation goes to `DIFFERENCES.md` with the reason.

## 6. Property-based invariants (U5, jqwik)

- `d.next(n days).previous(n days) == d` and `d.next(n).minus(d) == n days` for any date and `n ≥ 0`, across year 0, negative years and leap days.
- `d.next(a).next(b) == d.next(a + b)`; `d.next(-n) == d.previous(n)`.
- `GregorianDate` ↔ `java.time.LocalDate` round-trip and agreement of `dayOfWeek`, `isLeapYear`, `numberOfDays`, ordering.
- `MonthOfYear.next(k months).previous(k months)` identity; `numberOfDays ∈ {28,29,30,31}`; Feb has 29 iff leap.
- `Instant` ↔ `TimeOffsetAwareGregorianDateTime` round-trip under fixed offsets and IANA zones (non-gap times).
- `TimeQuantity` conversion round-trips are exact; `ISO8601Parser.parseDuration(format(q)) == q`.
- `DiscreteInterval` size and ordering match stepping with `next`.
- `GregorianYear.isLeapYear` agrees with `java.time.Year.isLeap`.

Generators live in `org.chalten.testkit.ChaltenArbitraries` and weight year 0, negative years, century and 400-year boundaries, month starts and ends, leap days, fixed offsets over ±16 h and DST, half-hour and quarter-hour IANA zones. Conversions through Julian day numbers are generated from the year -4712 on and `dayOfWeek` from -4715 on, because the Smalltalk arithmetic is one day off before that and the port keeps it (`DIFFERENCES.md` S1). `DiscreteInterval` holds `Steppable` values (dates, years, quantities), so interval properties step with `plus`; IANA zone properties use `java.time.zone.ZoneRules` as an in-test `TimeOffsetRuleset` until U4's rulesets land.

## 7. Open decisions / follow-ups

- **Business-day and holiday rules** are not in Chalten. Optional unit U7 (new behaviour, not parity): `BusinessDayCalendar` with weekend rules, holiday sets and modified-following adjustments on top of `GregorianDate`.
- **Policy-period conventions** (anniversary clamping of Feb 29, actual/365 vs actual/actual pro-rata) are not Chalten behaviour; U6 documents them as compositions so Masterpiece's own rules stay explicit.
- Coordinates: Maven group `org.chalten`, artifact per module, version `0.1.0-SNAPSHOT` until the user picks real coordinates.

## 8. Test-parity matrix

Status is `done` when the same-named JUnit method exists and is enabled, `pending` otherwise. `./gradlew check` (`checkTestParity`) fails if a non-pending class has a missing selector, if a Status disagrees with the code, or if this matrix drifts from `source/**/*Test*.st`; `./gradlew updateTestParityStatus` rewrites the Status column. Classes owned by U4 are listed in `java/test-parity-pending.txt` and reported without failing until U4 lands; the checker fails once a listed class is fully ported, so the entry must then be removed.

| # | SUnit package | SUnit class | Tests | Java module | JUnit 5 class | Owner unit |
| --- | --- | --- | --- | --- | --- | --- |
| 1 | `Chalten-Core-Tests` | `ChronologyConventionsTest` | 9 | `chalten-core` | `org.chalten.localization.ChronologyConventionsTest` | U4 |
| 2 | `Chalten-Core-Tests` | `ChronologyPatternTest` | 18 | `chalten-core` | `org.chalten.localization.ChronologyPatternTest` | U4 |
| 3 | `Chalten-Core-Tests` | `DayOfWeekTest` | 9 | `chalten-core` | `org.chalten.core.DayOfWeekTest` | U2 |
| 4 | `Chalten-Core-Tests` | `FixedOffsetRulesetTest` | 4 | `chalten-core` | `org.chalten.core.FixedOffsetRulesetTest` | U2 |
| 5 | `Chalten-Core-Tests` | `GregorianDateTest` | 21 | `chalten-core` | `org.chalten.core.GregorianDateTest` | U2 |
| 6 | `Chalten-Core-Tests` | `GregorianDateTimeTest` | 17 | `chalten-core` | `org.chalten.core.GregorianDateTimeTest` | U2 |
| 7 | `Chalten-Core-Tests` | `GregorianDayOfMonthTest` | 17 | `chalten-core` | `org.chalten.core.GregorianDayOfMonthTest` | U2 |
| 8 | `Chalten-Core-Tests` | `GregorianLocalizationTest` | 18 | `chalten-core` | `org.chalten.localization.GregorianLocalizationTest` | U4 |
| 9 | `Chalten-Core-Tests` | `GregorianMonthOfYearTest` | 14 | `chalten-core` | `org.chalten.core.GregorianMonthOfYearTest` | U2 |
| 10 | `Chalten-Core-Tests` | `GregorianMonthTest` | 20 | `chalten-core` | `org.chalten.core.GregorianMonthTest` | U2 |
| 11 | `Chalten-Core-Tests` | `GregorianYearTest` | 17 | `chalten-core` | `org.chalten.core.GregorianYearTest` | U2 |
| 12 | `Chalten-Core-Tests` | `ISO8601DurationFormatterTest` | 13 | `chalten-measure` | `org.chalten.measure.ISO8601DurationFormatterTest` | U3 |
| 13 | `Chalten-Core-Tests` | `ISO8601ParserTest` | 10 | `chalten-core` | `org.chalten.core.ISO8601ParserTest` | U2 |
| 14 | `Chalten-Core-Tests` | `InstantTest` | 9 | `chalten-core` | `org.chalten.core.InstantTest` | U2 |
| 15 | `Chalten-Core-Tests` | `ShippedChronologyConventionsTest` | 5 | `chalten-core` | `org.chalten.localization.ShippedChronologyConventionsTest` | U4 |
| 16 | `Chalten-Core-Tests` | `TimeOfDayTest` | 12 | `chalten-core` | `org.chalten.core.TimeOfDayTest` | U2 |
| 17 | `Chalten-Core-Tests` | `TimeOffsetAwareGregorianDateTimeTest` | 5 | `chalten-core` | `org.chalten.core.TimeOffsetAwareGregorianDateTimeTest` | U2 |
| 18 | `Chalten-Core-Tests` | `TimeUnitsTest` | 3 | `chalten-measure` | `org.chalten.measure.TimeUnitsTest` | U3 |
| 19 | `Chalten-Hebrew-Calendar-Tests` | `HebrewDateTest` | 23 | `chalten-hebrew` | `org.chalten.hebrew.HebrewDateTest` | U4 |
| 20 | `Chalten-Hebrew-Calendar-Tests` | `HebrewDateTimeTest` | 12 | `chalten-hebrew` | `org.chalten.hebrew.HebrewDateTimeTest` | U4 |
| 21 | `Chalten-Hebrew-Calendar-Tests` | `HebrewDayOfMonthTest` | 20 | `chalten-hebrew` | `org.chalten.hebrew.HebrewDayOfMonthTest` | U4 |
| 22 | `Chalten-Hebrew-Calendar-Tests` | `HebrewDayOfWeekTest` | 10 | `chalten-hebrew` | `org.chalten.hebrew.HebrewDayOfWeekTest` | U4 |
| 23 | `Chalten-Hebrew-Calendar-Tests` | `HebrewMonthOfYearTest` | 16 | `chalten-hebrew` | `org.chalten.hebrew.HebrewMonthOfYearTest` | U4 |
| 24 | `Chalten-Hebrew-Calendar-Tests` | `HebrewMonthTest` | 19 | `chalten-hebrew` | `org.chalten.hebrew.HebrewMonthTest` | U4 |
| 25 | `Chalten-Hebrew-Calendar-Tests` | `HebrewYearTest` | 17 | `chalten-hebrew` | `org.chalten.hebrew.HebrewYearTest` | U4 |
| 26 | `Chalten-Islamic-Calendar-Tests` | `IslamicDateTest` | 19 | `chalten-islamic` | `org.chalten.islamic.IslamicDateTest` | U4 |
| 27 | `Chalten-Islamic-Calendar-Tests` | `IslamicDateTimeTest` | 12 | `chalten-islamic` | `org.chalten.islamic.IslamicDateTimeTest` | U4 |
| 28 | `Chalten-Islamic-Calendar-Tests` | `IslamicDayOfMonthTest` | 18 | `chalten-islamic` | `org.chalten.islamic.IslamicDayOfMonthTest` | U4 |
| 29 | `Chalten-Islamic-Calendar-Tests` | `IslamicDayOfWeekTest` | 10 | `chalten-islamic` | `org.chalten.islamic.IslamicDayOfWeekTest` | U4 |
| 30 | `Chalten-Islamic-Calendar-Tests` | `IslamicMonthOfYearTest` | 11 | `chalten-islamic` | `org.chalten.islamic.IslamicMonthOfYearTest` | U4 |
| 31 | `Chalten-Islamic-Calendar-Tests` | `IslamicMonthTest` | 18 | `chalten-islamic` | `org.chalten.islamic.IslamicMonthTest` | U4 |
| 32 | `Chalten-Islamic-Calendar-Tests` | `IslamicYearTest` | 14 | `chalten-islamic` | `org.chalten.islamic.IslamicYearTest` | U4 |
| 33 | `Chalten-Timezones-Tests` | `GregorianDateTimeTest (extension)` | 3 | `chalten-timezones` | `org.chalten.timezones.GregorianDateTimeTimezonesTest` | U4 |
| 34 | `Chalten-Timezones-Tests` | `IANATimeZoneDatabaseTest` | 6 | `chalten-timezones` | `org.chalten.timezones.IANATimeZoneDatabaseTest` | U4 |
| 35 | `Chalten-Timezones-Tests` | `TimeOffsetAwareGregorianDateTimeTest (extension)` | 2 | `chalten-timezones` | `org.chalten.timezones.TimeOffsetAwareGregorianDateTimeTimezonesTest` | U4 |
| 36 | `Chalten-Timezones-Tests` | `TimeZoneRulesetTest` | 4 | `chalten-timezones` | `org.chalten.timezones.TimeZoneRulesetTest` | U4 |
| | | **Total** | **455** | | | |

### Per-test matrix

<details><summary><code>ChronologyConventionsTest</code> → <code>org.chalten.localization.ChronologyConventionsTest</code> (9 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testChronologyCategory` | `testChronologyCategory()` | done |
| `testLaterLoadsOverridePropertyByProperty` | `testLaterLoadsOverridePropertyByProperty()` | done |
| `testLoadingFromAFile` | `testLoadingFromAFile()` | done |
| `testMarksAreNotPatterns` | `testMarksAreNotPatterns()` | done |
| `testPropertyNames` | `testPropertyNames()` | done |
| `testPropertyNotDefined` | `testPropertyNotDefined()` | done |
| `testPropertyResolvedFromTheMostSpecificRange` | `testPropertyResolvedFromTheMostSpecificRange()` | done |
| `testUnknownProperty` | `testUnknownProperty()` | done |
| `testUnsupportedFieldsAreRejectedBeforeLoading` | `testUnsupportedFieldsAreRejectedBeforeLoading()` | done |

</details>

<details><summary><code>ChronologyPatternTest</code> → <code>org.chalten.localization.ChronologyPatternTest</code> (18 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testDay` | `testDay()` | done |
| `testDayOfWeek` | `testDayOfWeek()` | done |
| `testDayPeriod` | `testDayPeriod()` | done |
| `testEmptyPattern` | `testEmptyPattern()` | done |
| `testHour` | `testHour()` | done |
| `testLiterals` | `testLiterals()` | done |
| `testMinute` | `testMinute()` | done |
| `testMissingClosingQuote` | `testMissingClosingQuote()` | done |
| `testMonth` | `testMonth()` | done |
| `testNonASCIILettersAreLiteral` | `testNonASCIILettersAreLiteral()` | done |
| `testOffset` | `testOffset()` | done |
| `testPlaceholders` | `testPlaceholders()` | done |
| `testQuotedLiterals` | `testQuotedLiterals()` | done |
| `testReplacingDateAndTime` | `testReplacingDateAndTime()` | done |
| `testSecond` | `testSecond()` | done |
| `testUnsupportedFields` | `testUnsupportedFields()` | done |
| `testUnsupportedPlaceholders` | `testUnsupportedPlaceholders()` | done |
| `testYear` | `testYear()` | done |

</details>

<details><summary><code>DayOfWeekTest</code> → <code>org.chalten.core.DayOfWeekTest</code> (9 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testConverting` | `testConverting()` | done |
| `testInstanceCreation` | `testInstanceCreation()` | done |
| `testNames` | `testNames()` | done |
| `testNamesAreLocalized` | `testNamesAreLocalized()` | done |
| `testNext` | `testNext()` | done |
| `testPrevious` | `testPrevious()` | done |
| `testPrintString` | `testPrintString()` | done |
| `testShortNames` | `testShortNames()` | done |
| `testShortNamesAreLocalized` | `testShortNamesAreLocalized()` | done |

</details>

<details><summary><code>FixedOffsetRulesetTest</code> → <code>org.chalten.core.FixedOffsetRulesetTest</code> (4 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testCantCreateOutOfRange` | `testCantCreateOutOfRange()` | done |
| `testIdentifier` | `testIdentifier()` | done |
| `testOffsetToUniversalCoordinatedTimeAt` | `testOffsetToUniversalCoordinatedTimeAt()` | done |
| `testPrintString` | `testPrintString()` | done |

</details>

<details><summary><code>GregorianDateTest</code> → <code>org.chalten.core.GregorianDateTest</code> (21 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAccessing` | `testAccessing()` | done |
| `testBetweenAnd` | `testBetweenAnd()` | done |
| `testCannotComputeDateDistanceWhenTheSecondIsGreater` | `testCannotComputeDateDistanceWhenTheSecondIsGreater()` | done |
| `testComparing` | `testComparing()` | done |
| `testDayOfWeek` | `testDayOfWeek()` | done |
| `testDaysBetweenTwoDates` | `testDaysBetweenTwoDates()` | done |
| `testGreaterThan` | `testGreaterThan()` | done |
| `testISO8601Printing` | `testISO8601Printing()` | done |
| `testInstanceCreationFor30DaysMonth` | `testInstanceCreationFor30DaysMonth()` | done |
| `testInstanceCreationFor31DaysMonth` | `testInstanceCreationFor31DaysMonth()` | done |
| `testInstanceCreationForFebruaryOnCommonYear` | `testInstanceCreationForFebruaryOnCommonYear()` | done |
| `testInstanceCreationForFebruaryOnLeapYear` | `testInstanceCreationForFebruaryOnLeapYear()` | done |
| `testInterval` | `testInterval()` | done |
| `testIntervalWithStep` | `testIntervalWithStep()` | done |
| `testIsLastDayOfMonth` | `testIsLastDayOfMonth()` | done |
| `testLessThan` | `testLessThan()` | done |
| `testNext` | `testNext()` | done |
| `testParseISO8601` | `testParseISO8601()` | done |
| `testPrevious` | `testPrevious()` | done |
| `testPrintString` | `testPrintString()` | done |
| `testSubtraction` | `testSubtraction()` | done |

</details>

<details><summary><code>GregorianDateTimeTest</code> → <code>org.chalten.core.GregorianDateTimeTest</code> (17 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAccessing` | `testAccessing()` | done |
| `testAtMidnight` | `testAtMidnight()` | done |
| `testAtNoon` | `testAtNoon()` | done |
| `testAtStartOfDay` | `testAtStartOfDay()` | done |
| `testBetweenAnd` | `testBetweenAnd()` | done |
| `testComparing` | `testComparing()` | done |
| `testConverting` | `testConverting()` | done |
| `testCreationShortcut` | `testCreationShortcut()` | done |
| `testGreaterThan` | `testGreaterThan()` | done |
| `testISO8601Parsing` | `testISO8601Parsing()` | done |
| `testISO8601Printing` | `testISO8601Printing()` | done |
| `testIn` | `testIn()` | done |
| `testInCoordinatedUniversalTime` | `testInCoordinatedUniversalTime()` | done |
| `testJulianDayNumber` | `testJulianDayNumber()` | done |
| `testLessThan` | `testLessThan()` | done |
| `testPrintString` | `testPrintString()` | done |
| `testPrintStringInISO8601` | `testPrintStringInISO8601()` | done |

</details>

<details><summary><code>GregorianDayOfMonthTest</code> → <code>org.chalten.core.GregorianDayOfMonthTest</code> (17 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testComparing` | `testComparing()` | done |
| `testDateCreation` | `testDateCreation()` | done |
| `testInstanceCreation` | `testInstanceCreation()` | done |
| `testInstanceCreationShortcut` | `testInstanceCreationShortcut()` | done |
| `testInstanceCreationShortcutForApril` | `testInstanceCreationShortcutForApril()` | done |
| `testInstanceCreationShortcutForAugust` | `testInstanceCreationShortcutForAugust()` | done |
| `testInstanceCreationShortcutForDecember` | `testInstanceCreationShortcutForDecember()` | done |
| `testInstanceCreationShortcutForFebruary` | `testInstanceCreationShortcutForFebruary()` | done |
| `testInstanceCreationShortcutForJanuary` | `testInstanceCreationShortcutForJanuary()` | done |
| `testInstanceCreationShortcutForJuly` | `testInstanceCreationShortcutForJuly()` | done |
| `testInstanceCreationShortcutForJune` | `testInstanceCreationShortcutForJune()` | done |
| `testInstanceCreationShortcutForMarch` | `testInstanceCreationShortcutForMarch()` | done |
| `testInstanceCreationShortcutForMay` | `testInstanceCreationShortcutForMay()` | done |
| `testInstanceCreationShortcutForNovember` | `testInstanceCreationShortcutForNovember()` | done |
| `testInstanceCreationShortcutForOctober` | `testInstanceCreationShortcutForOctober()` | done |
| `testInstanceCreationShortcutForSeptember` | `testInstanceCreationShortcutForSeptember()` | done |
| `testPrintString` | `testPrintString()` | done |

</details>

<details><summary><code>GregorianLocalizationTest</code> → <code>org.chalten.localization.GregorianLocalizationTest</code> (18 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testDateTimes` | `testDateTimes()` | done |
| `testDates` | `testDates()` | done |
| `testDatesPadTheirNumbers` | `testDatesPadTheirNumbers()` | done |
| `testDaysOfMonth` | `testDaysOfMonth()` | done |
| `testDaysOfWeek` | `testDaysOfWeek()` | done |
| `testFormattersUseTheirOwnLanguage` | `testFormattersUseTheirOwnLanguage()` | done |
| `testInstants` | `testInstants()` | done |
| `testInstantsAreShownOnTheirUTCDate` | `testInstantsAreShownOnTheirUTCDate()` | done |
| `testMidnightAndNoon` | `testMidnightAndNoon()` | done |
| `testMonths` | `testMonths()` | done |
| `testMonthsOfYear` | `testMonthsOfYear()` | done |
| `testOffsetAwareDateTimes` | `testOffsetAwareDateTimes()` | done |
| `testOffsets` | `testOffsets()` | done |
| `testPatternsNeedingPartsTheObjectDoesNotHave` | `testPatternsNeedingPartsTheObjectDoesNotHave()` | done |
| `testPlaceholdersOutsideCombiningPatterns` | `testPlaceholdersOutsideCombiningPatterns()` | done |
| `testSecondsAreShownWithoutTheirFraction` | `testSecondsAreShownWithoutTheirFraction()` | done |
| `testTimesOfDay` | `testTimesOfDay()` | done |
| `testYears` | `testYears()` | done |

</details>

<details><summary><code>GregorianMonthOfYearTest</code> → <code>org.chalten.core.GregorianMonthOfYearTest</code> (14 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testBetweenAnd` | `testBetweenAnd()` | done |
| `testComparing` | `testComparing()` | done |
| `testCreation` | `testCreation()` | done |
| `testCreationShortcut` | `testCreationShortcut()` | done |
| `testDateCreation` | `testDateCreation()` | done |
| `testGreaterThan` | `testGreaterThan()` | done |
| `testISO8601Printing` | `testISO8601Printing()` | done |
| `testLessThan` | `testLessThan()` | done |
| `testMonthsBetweenTwoMonthsOfYear` | `testMonthsBetweenTwoMonthsOfYear()` | done |
| `testNext` | `testNext()` | done |
| `testNumberOfDays` | `testNumberOfDays()` | done |
| `testParseISO8601` | `testParseISO8601()` | done |
| `testPrevious` | `testPrevious()` | done |
| `testPrintString` | `testPrintString()` | done |

</details>

<details><summary><code>GregorianMonthTest</code> → <code>org.chalten.core.GregorianMonthTest</code> (20 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testApril` | `testApril()` | done |
| `testAugust` | `testAugust()` | done |
| `testComparing` | `testComparing()` | done |
| `testDecember` | `testDecember()` | done |
| `testFebruary` | `testFebruary()` | done |
| `testISO8601Printing` | `testISO8601Printing()` | done |
| `testInstanceCreation` | `testInstanceCreation()` | done |
| `testJanuary` | `testJanuary()` | done |
| `testJuly` | `testJuly()` | done |
| `testJune` | `testJune()` | done |
| `testMarch` | `testMarch()` | done |
| `testMay` | `testMay()` | done |
| `testNamesAreLocalized` | `testNamesAreLocalized()` | done |
| `testNext` | `testNext()` | done |
| `testNovember` | `testNovember()` | done |
| `testOctober` | `testOctober()` | done |
| `testParseISO8601` | `testParseISO8601()` | done |
| `testPrevious` | `testPrevious()` | done |
| `testPrintString` | `testPrintString()` | done |
| `testSeptember` | `testSeptember()` | done |

</details>

<details><summary><code>GregorianYearTest</code> → <code>org.chalten.core.GregorianYearTest</code> (17 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testBetweenAnd` | `testBetweenAnd()` | done |
| `testComparing` | `testComparing()` | done |
| `testGreaterThan` | `testGreaterThan()` | done |
| `testISO8601Printing` | `testISO8601Printing()` | done |
| `testInstanceCreation` | `testInstanceCreation()` | done |
| `testInstanceCreationShortcut` | `testInstanceCreationShortcut()` | done |
| `testInterval` | `testInterval()` | done |
| `testIntervalWithStep` | `testIntervalWithStep()` | done |
| `testIsLeapYear` | `testIsLeapYear()` | done |
| `testLessThan` | `testLessThan()` | done |
| `testNext` | `testNext()` | done |
| `testNumberOfDays` | `testNumberOfDays()` | done |
| `testNumberOfMonths` | `testNumberOfMonths()` | done |
| `testParseISO8601` | `testParseISO8601()` | done |
| `testPrevious` | `testPrevious()` | done |
| `testPrintString` | `testPrintString()` | done |
| `testYearsBetweenTwoYears` | `testYearsBetweenTwoYears()` | done |

</details>

<details><summary><code>ISO8601DurationFormatterTest</code> → <code>org.chalten.measure.ISO8601DurationFormatterTest</code> (13 tests, U3)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testCannotFormatEmptyMixedDuration` | `testCannotFormatEmptyMixedDuration()` | done |
| `testCannotFormatInvalidFractions` | `testCannotFormatInvalidFractions()` | done |
| `testCannotFormatMixedDurationWithRepeatedUnits` | `testCannotFormatMixedDurationWithRepeatedUnits()` | done |
| `testCannotFormatMixedDurationWithWeeks` | `testCannotFormatMixedDurationWithWeeks()` | done |
| `testCannotFormatUnsupportedUnits` | `testCannotFormatUnsupportedUnits()` | done |
| `testFormatDayDuration` | `testFormatDayDuration()` | done |
| `testFormatHourDuration` | `testFormatHourDuration()` | done |
| `testFormatMinuteDuration` | `testFormatMinuteDuration()` | done |
| `testFormatMixedDuration` | `testFormatMixedDuration()` | done |
| `testFormatMonthDuration` | `testFormatMonthDuration()` | done |
| `testFormatSecondDuration` | `testFormatSecondDuration()` | done |
| `testFormatWeekDuration` | `testFormatWeekDuration()` | done |
| `testFormatYearDuration` | `testFormatYearDuration()` | done |

</details>

<details><summary><code>ISO8601ParserTest</code> → <code>org.chalten.core.ISO8601ParserTest</code> (10 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testCannotParseInvalidDuration` | `testCannotParseInvalidDuration()` | done |
| `testMixedTimePeriods` | `testMixedTimePeriods()` | done |
| `testParseDayDuration` | `testParseDayDuration()` | done |
| `testParseDurationUsingNonDefaultNumberReader` | `testParseDurationUsingNonDefaultNumberReader()` | done |
| `testParseHourDuration` | `testParseHourDuration()` | done |
| `testParseMinuteDuration` | `testParseMinuteDuration()` | done |
| `testParseMonthDuration` | `testParseMonthDuration()` | done |
| `testParseSecondDuration` | `testParseSecondDuration()` | done |
| `testParseWeekDuration` | `testParseWeekDuration()` | done |
| `testParseYearDuration` | `testParseYearDuration()` | done |

</details>

<details><summary><code>InstantTest</code> → <code>org.chalten.core.InstantTest</code> (9 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAccessing` | `testAccessing()` | done |
| `testAsUnixTime` | `testAsUnixTime()` | done |
| `testComparing` | `testComparing()` | done |
| `testConverting` | `testConverting()` | done |
| `testFromJulianDayNumber` | `testFromJulianDayNumber()` | done |
| `testFromUnixTime` | `testFromUnixTime()` | done |
| `testJulianDayNumber` | `testJulianDayNumber()` | done |
| `testMagnitudeProtocol` | `testMagnitudeProtocol()` | done |
| `testPrintString` | `testPrintString()` | done |

</details>

<details><summary><code>ShippedChronologyConventionsTest</code> → <code>org.chalten.localization.ShippedChronologyConventionsTest</code> (5 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testArgentinianSpanish` | `testArgentinianSpanish()` | done |
| `testBritishEnglish` | `testBritishEnglish()` | done |
| `testDefaults` | `testDefaults()` | done |
| `testEnglish` | `testEnglish()` | done |
| `testSpanish` | `testSpanish()` | done |

</details>

<details><summary><code>TimeOfDayTest</code> → <code>org.chalten.core.TimeOfDayTest</code> (12 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAccessing` | `testAccessing()` | done |
| `testAddition` | `testAddition()` | done |
| `testAdditionCannotExceed24Hours` | `testAdditionCannotExceed24Hours()` | done |
| `testAdditionOfLessThanOneHour` | `testAdditionOfLessThanOneHour()` | done |
| `testComparing` | `testComparing()` | done |
| `testISO8601Printing` | `testISO8601Printing()` | done |
| `testInvalidCreation` | `testInvalidCreation()` | done |
| `testParseISO8601` | `testParseISO8601()` | done |
| `testPrintString` | `testPrintString()` | done |
| `testSubtraction` | `testSubtraction()` | done |
| `testSubtractionCannotExceed0Hours` | `testSubtractionCannotExceed0Hours()` | done |
| `testSubtractionToZero` | `testSubtractionToZero()` | done |

</details>

<details><summary><code>TimeOffsetAwareGregorianDateTimeTest</code> → <code>org.chalten.core.TimeOffsetAwareGregorianDateTimeTest</code> (5 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAsRelativePointInTime` | `testAsRelativePointInTime()` | done |
| `testCreationFromInstant` | `testCreationFromInstant()` | done |
| `testCreationFromInstantWithFixedOffset` | `testCreationFromInstantWithFixedOffset()` | done |
| `testIn` | `testIn()` | done |
| `testInCoordinatedUniversalTime` | `testInCoordinatedUniversalTime()` | done |

</details>

<details><summary><code>TimeUnitsTest</code> → <code>org.chalten.measure.TimeUnitsTest</code> (3 tests, U3)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAbsoluteUnits` | `testAbsoluteUnits()` | done |
| `testCommensurability` | `testCommensurability()` | done |
| `testRelativeUnits` | `testRelativeUnits()` | done |

</details>

<details><summary><code>HebrewDateTest</code> → <code>org.chalten.hebrew.HebrewDateTest</code> (23 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAccessing` | `testAccessing()` | done |
| `testBetweenAnd` | `testBetweenAnd()` | done |
| `testCannotComputeDateDistanceWhenTheSecondIsGreater` | `testCannotComputeDateDistanceWhenTheSecondIsGreater()` | done |
| `testComparing` | `testComparing()` | done |
| `testDayOfWeek` | `testDayOfWeek()` | done |
| `testDaysBetweenTwoDates` | `testDaysBetweenTwoDates()` | done |
| `testGreaterThan` | `testGreaterThan()` | done |
| `testInstanceCreationFor29DaysMonth` | `testInstanceCreationFor29DaysMonth()` | done |
| `testInstanceCreationFor30DaysMonth` | `testInstanceCreationFor30DaysMonth()` | done |
| `testInstanceCreationForCheshvanOnCompleteYear` | `testInstanceCreationForCheshvanOnCompleteYear()` | done |
| `testInstanceCreationForCheshvanOnDeficientYear` | `testInstanceCreationForCheshvanOnDeficientYear()` | done |
| `testInstanceCreationForCheshvanOnRegularYear` | `testInstanceCreationForCheshvanOnRegularYear()` | done |
| `testInstanceCreationForKislevOnCompleteYear` | `testInstanceCreationForKislevOnCompleteYear()` | done |
| `testInstanceCreationForKislevOnDeficientYear` | `testInstanceCreationForKislevOnDeficientYear()` | done |
| `testInstanceCreationForKislevOnRegularYear` | `testInstanceCreationForKislevOnRegularYear()` | done |
| `testInterval` | `testInterval()` | done |
| `testIntervalWithStep` | `testIntervalWithStep()` | done |
| `testIsLastDayOfMonth` | `testIsLastDayOfMonth()` | done |
| `testLessThan` | `testLessThan()` | done |
| `testNext` | `testNext()` | done |
| `testPrevious` | `testPrevious()` | done |
| `testPrintString` | `testPrintString()` | done |
| `testSubtraction` | `testSubtraction()` | done |

</details>

<details><summary><code>HebrewDateTimeTest</code> → <code>org.chalten.hebrew.HebrewDateTimeTest</code> (12 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAccessing` | `testAccessing()` | done |
| `testAtMidnight` | `testAtMidnight()` | done |
| `testAtNoon` | `testAtNoon()` | done |
| `testAtStartOfDay` | `testAtStartOfDay()` | done |
| `testBetweenAnd` | `testBetweenAnd()` | done |
| `testComparing` | `testComparing()` | done |
| `testCreationShortcut` | `testCreationShortcut()` | done |
| `testGreaterThan` | `testGreaterThan()` | done |
| `testIn` | `testIn()` | done |
| `testInCoordinatedUniversalTime` | `testInCoordinatedUniversalTime()` | done |
| `testLessThan` | `testLessThan()` | done |
| `testPrintString` | `testPrintString()` | done |

</details>

<details><summary><code>HebrewDayOfMonthTest</code> → <code>org.chalten.hebrew.HebrewDayOfMonthTest</code> (20 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testComparing` | `testComparing()` | done |
| `testDateCreation` | `testDateCreation()` | done |
| `testInstanceCreation` | `testInstanceCreation()` | done |
| `testInstanceCreationShortcut` | `testInstanceCreationShortcut()` | done |
| `testInstanceCreationShortcutForAdar` | `testInstanceCreationShortcutForAdar()` | done |
| `testInstanceCreationShortcutForAdarI` | `testInstanceCreationShortcutForAdarI()` | done |
| `testInstanceCreationShortcutForAdarII` | `testInstanceCreationShortcutForAdarII()` | done |
| `testInstanceCreationShortcutForAv` | `testInstanceCreationShortcutForAv()` | done |
| `testInstanceCreationShortcutForCheshvan` | `testInstanceCreationShortcutForCheshvan()` | done |
| `testInstanceCreationShortcutForElul` | `testInstanceCreationShortcutForElul()` | done |
| `testInstanceCreationShortcutForIyar` | `testInstanceCreationShortcutForIyar()` | done |
| `testInstanceCreationShortcutForKislev` | `testInstanceCreationShortcutForKislev()` | done |
| `testInstanceCreationShortcutForNisan` | `testInstanceCreationShortcutForNisan()` | done |
| `testInstanceCreationShortcutForShevat` | `testInstanceCreationShortcutForShevat()` | done |
| `testInstanceCreationShortcutForSivan` | `testInstanceCreationShortcutForSivan()` | done |
| `testInstanceCreationShortcutForTammuz` | `testInstanceCreationShortcutForTammuz()` | done |
| `testInstanceCreationShortcutForTevet` | `testInstanceCreationShortcutForTevet()` | done |
| `testInstanceCreationShortcutForTishrei` | `testInstanceCreationShortcutForTishrei()` | done |
| `testLocalizationIsNotSupported` | `testLocalizationIsNotSupported()` | done |
| `testPrintString` | `testPrintString()` | done |

</details>

<details><summary><code>HebrewDayOfWeekTest</code> → <code>org.chalten.hebrew.HebrewDayOfWeekTest</code> (10 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAsHebrewDayOfWeek` | `testAsHebrewDayOfWeek()` | done |
| `testAsISODayOfWeek` | `testAsISODayOfWeek()` | done |
| `testInstanceCreation` | `testInstanceCreation()` | done |
| `testNames` | `testNames()` | done |
| `testNamesAreLocalized` | `testNamesAreLocalized()` | done |
| `testNext` | `testNext()` | done |
| `testPrevious` | `testPrevious()` | done |
| `testPrintString` | `testPrintString()` | done |
| `testShortNames` | `testShortNames()` | done |
| `testShortNamesAreLocalized` | `testShortNamesAreLocalized()` | done |

</details>

<details><summary><code>HebrewMonthOfYearTest</code> → <code>org.chalten.hebrew.HebrewMonthOfYearTest</code> (16 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testBetweenAnd` | `testBetweenAnd()` | done |
| `testComparing` | `testComparing()` | done |
| `testCreation` | `testCreation()` | done |
| `testCreationShortcut` | `testCreationShortcut()` | done |
| `testCreationShortcutOnLeapYear` | `testCreationShortcutOnLeapYear()` | done |
| `testGreaterThan` | `testGreaterThan()` | done |
| `testLessThan` | `testLessThan()` | done |
| `testNext` | `testNext()` | done |
| `testNumberOfDaysOnCommonCompleteYear` | `testNumberOfDaysOnCommonCompleteYear()` | done |
| `testNumberOfDaysOnCommonDeficientYear` | `testNumberOfDaysOnCommonDeficientYear()` | done |
| `testNumberOfDaysOnCommonRegularYear` | `testNumberOfDaysOnCommonRegularYear()` | done |
| `testNumberOfDaysOnLeapCompleteYear` | `testNumberOfDaysOnLeapCompleteYear()` | done |
| `testNumberOfDaysOnLeapDeficientYear` | `testNumberOfDaysOnLeapDeficientYear()` | done |
| `testNumberOfDaysOnLeapRegularYear` | `testNumberOfDaysOnLeapRegularYear()` | done |
| `testPrevious` | `testPrevious()` | done |
| `testPrintString` | `testPrintString()` | done |

</details>

<details><summary><code>HebrewMonthTest</code> → <code>org.chalten.hebrew.HebrewMonthTest</code> (19 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAdar` | `testAdar()` | done |
| `testAdarI` | `testAdarI()` | done |
| `testAdarII` | `testAdarII()` | done |
| `testAv` | `testAv()` | done |
| `testCheshvan` | `testCheshvan()` | done |
| `testComparing` | `testComparing()` | done |
| `testElul` | `testElul()` | done |
| `testInstanceCreation` | `testInstanceCreation()` | done |
| `testIsGregorian` | `testIsGregorian()` | done |
| `testIyar` | `testIyar()` | done |
| `testKislev` | `testKislev()` | done |
| `testNamesAreLocalized` | `testNamesAreLocalized()` | done |
| `testNisan` | `testNisan()` | done |
| `testPrintString` | `testPrintString()` | done |
| `testShevat` | `testShevat()` | done |
| `testSivan` | `testSivan()` | done |
| `testTammuz` | `testTammuz()` | done |
| `testTevet` | `testTevet()` | done |
| `testTishrei` | `testTishrei()` | done |

</details>

<details><summary><code>HebrewYearTest</code> → <code>org.chalten.hebrew.HebrewYearTest</code> (17 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testBetweenAnd` | `testBetweenAnd()` | done |
| `testComparing` | `testComparing()` | done |
| `testGreaterThan` | `testGreaterThan()` | done |
| `testInstanceCreation` | `testInstanceCreation()` | done |
| `testInstanceCreationShortcut` | `testInstanceCreationShortcut()` | done |
| `testInterval` | `testInterval()` | done |
| `testIsCompleteYear` | `testIsCompleteYear()` | done |
| `testIsDeficientYear` | `testIsDeficientYear()` | done |
| `testIsLeapYear` | `testIsLeapYear()` | done |
| `testIsRegularYear` | `testIsRegularYear()` | done |
| `testLessThan` | `testLessThan()` | done |
| `testNext` | `testNext()` | done |
| `testNumberOfDays` | `testNumberOfDays()` | done |
| `testNumberOfMonths` | `testNumberOfMonths()` | done |
| `testPrevious` | `testPrevious()` | done |
| `testPrintString` | `testPrintString()` | done |
| `testYearsBetweenTwoYears` | `testYearsBetweenTwoYears()` | done |

</details>

<details><summary><code>IslamicDateTest</code> → <code>org.chalten.islamic.IslamicDateTest</code> (19 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAccessing` | `testAccessing()` | done |
| `testBetweenAnd` | `testBetweenAnd()` | done |
| `testCannotComputeDateDistanceWhenTheSecondIsGreater` | `testCannotComputeDateDistanceWhenTheSecondIsGreater()` | done |
| `testComparing` | `testComparing()` | done |
| `testDayOfWeek` | `testDayOfWeek()` | done |
| `testDaysBetweenTwoDates` | `testDaysBetweenTwoDates()` | done |
| `testGreaterThan` | `testGreaterThan()` | done |
| `testInstanceCreationFor29DaysMonth` | `testInstanceCreationFor29DaysMonth()` | done |
| `testInstanceCreationFor30DaysMonth` | `testInstanceCreationFor30DaysMonth()` | done |
| `testInstanceCreationForDuAlhijjahOnCommonYear` | `testInstanceCreationForDuAlhijjahOnCommonYear()` | done |
| `testInstanceCreationForDuAlhijjahOnLeapYear` | `testInstanceCreationForDuAlhijjahOnLeapYear()` | done |
| `testInterval` | `testInterval()` | done |
| `testIntervalWithStep` | `testIntervalWithStep()` | done |
| `testIsLastDayOfMonth` | `testIsLastDayOfMonth()` | done |
| `testLessThan` | `testLessThan()` | done |
| `testNext` | `testNext()` | done |
| `testPrevious` | `testPrevious()` | done |
| `testPrintString` | `testPrintString()` | done |
| `testSubtraction` | `testSubtraction()` | done |

</details>

<details><summary><code>IslamicDateTimeTest</code> → <code>org.chalten.islamic.IslamicDateTimeTest</code> (12 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAccessing` | `testAccessing()` | done |
| `testAtMidnight` | `testAtMidnight()` | done |
| `testAtNoon` | `testAtNoon()` | done |
| `testAtStartOfDay` | `testAtStartOfDay()` | done |
| `testBetweenAnd` | `testBetweenAnd()` | done |
| `testComparing` | `testComparing()` | done |
| `testCreationShortcut` | `testCreationShortcut()` | done |
| `testGreaterThan` | `testGreaterThan()` | done |
| `testIn` | `testIn()` | done |
| `testInCoordinatedUniversalTime` | `testInCoordinatedUniversalTime()` | done |
| `testLessThan` | `testLessThan()` | done |
| `testPrintString` | `testPrintString()` | done |

</details>

<details><summary><code>IslamicDayOfMonthTest</code> → <code>org.chalten.islamic.IslamicDayOfMonthTest</code> (18 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testCannotCreateWithMonthOfAnotherCalendar` | `testCannotCreateWithMonthOfAnotherCalendar()` | done |
| `testComparing` | `testComparing()` | done |
| `testDateCreation` | `testDateCreation()` | done |
| `testInstanceCreation` | `testInstanceCreation()` | done |
| `testInstanceCreationShortcut` | `testInstanceCreationShortcut()` | done |
| `testInstanceCreationShortcutForDuAlQiDah` | `testInstanceCreationShortcutForDuAlQiDah()` | done |
| `testInstanceCreationShortcutForDuAlQiHijjah` | `testInstanceCreationShortcutForDuAlQiHijjah()` | done |
| `testInstanceCreationShortcutForJumadaI` | `testInstanceCreationShortcutForJumadaI()` | done |
| `testInstanceCreationShortcutForJumadaII` | `testInstanceCreationShortcutForJumadaII()` | done |
| `testInstanceCreationShortcutForMuharram` | `testInstanceCreationShortcutForMuharram()` | done |
| `testInstanceCreationShortcutForRabiI` | `testInstanceCreationShortcutForRabiI()` | done |
| `testInstanceCreationShortcutForRabiII` | `testInstanceCreationShortcutForRabiII()` | done |
| `testInstanceCreationShortcutForRajab` | `testInstanceCreationShortcutForRajab()` | done |
| `testInstanceCreationShortcutForRamadan` | `testInstanceCreationShortcutForRamadan()` | done |
| `testInstanceCreationShortcutForSafar` | `testInstanceCreationShortcutForSafar()` | done |
| `testInstanceCreationShortcutForShaban` | `testInstanceCreationShortcutForShaban()` | done |
| `testInstanceCreationShortcutForShawwal` | `testInstanceCreationShortcutForShawwal()` | done |
| `testPrintString` | `testPrintString()` | done |

</details>

<details><summary><code>IslamicDayOfWeekTest</code> → <code>org.chalten.islamic.IslamicDayOfWeekTest</code> (10 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAsISODayOfWeek` | `testAsISODayOfWeek()` | done |
| `testAsIslamicDayOfWeek` | `testAsIslamicDayOfWeek()` | done |
| `testInstanceCreation` | `testInstanceCreation()` | done |
| `testNames` | `testNames()` | done |
| `testNamesAreLocalized` | `testNamesAreLocalized()` | done |
| `testNext` | `testNext()` | done |
| `testPrevious` | `testPrevious()` | done |
| `testPrintString` | `testPrintString()` | done |
| `testShortNames` | `testShortNames()` | done |
| `testShortNamesAreLocalized` | `testShortNamesAreLocalized()` | done |

</details>

<details><summary><code>IslamicMonthOfYearTest</code> → <code>org.chalten.islamic.IslamicMonthOfYearTest</code> (11 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testBetweenAnd` | `testBetweenAnd()` | done |
| `testComparing` | `testComparing()` | done |
| `testCreation` | `testCreation()` | done |
| `testCreationShortcut` | `testCreationShortcut()` | done |
| `testDateCreation` | `testDateCreation()` | done |
| `testGreaterThan` | `testGreaterThan()` | done |
| `testLessThan` | `testLessThan()` | done |
| `testNext` | `testNext()` | done |
| `testNumberOfDays` | `testNumberOfDays()` | done |
| `testPrevious` | `testPrevious()` | done |
| `testPrintString` | `testPrintString()` | done |

</details>

<details><summary><code>IslamicMonthTest</code> → <code>org.chalten.islamic.IslamicMonthTest</code> (18 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testComparing` | `testComparing()` | done |
| `testDuAlHijjah` | `testDuAlHijjah()` | done |
| `testDuAlQiDah` | `testDuAlQiDah()` | done |
| `testInstanceCreation` | `testInstanceCreation()` | done |
| `testJumadaAlAwwal` | `testJumadaAlAwwal()` | done |
| `testJumadaAlThani` | `testJumadaAlThani()` | done |
| `testMuharram` | `testMuharram()` | done |
| `testNamesAreLocalized` | `testNamesAreLocalized()` | done |
| `testNext` | `testNext()` | done |
| `testPrevious` | `testPrevious()` | done |
| `testPrintString` | `testPrintString()` | done |
| `testRabiAlAwwal` | `testRabiAlAwwal()` | done |
| `testRabiAlThani` | `testRabiAlThani()` | done |
| `testRajab` | `testRajab()` | done |
| `testRamadan` | `testRamadan()` | done |
| `testSafar` | `testSafar()` | done |
| `testShaban` | `testShaban()` | done |
| `testShawwal` | `testShawwal()` | done |

</details>

<details><summary><code>IslamicYearTest</code> → <code>org.chalten.islamic.IslamicYearTest</code> (14 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testBetweenAnd` | `testBetweenAnd()` | done |
| `testComparing` | `testComparing()` | done |
| `testGreaterThan` | `testGreaterThan()` | done |
| `testInstanceCreation` | `testInstanceCreation()` | done |
| `testInstanceCreationShortcut` | `testInstanceCreationShortcut()` | done |
| `testInterval` | `testInterval()` | done |
| `testIsLeapYear` | `testIsLeapYear()` | done |
| `testLessThan` | `testLessThan()` | done |
| `testNext` | `testNext()` | done |
| `testNumberOfDays` | `testNumberOfDays()` | done |
| `testNumberOfMonths` | `testNumberOfMonths()` | done |
| `testPrevious` | `testPrevious()` | done |
| `testPrintString` | `testPrintString()` | done |
| `testYearsBetweenTwoYears` | `testYearsBetweenTwoYears()` | done |

</details>

<details><summary><code>GregorianDateTimeTest (extension)</code> → <code>org.chalten.timezones.GregorianDateTimeTimezonesTest</code> (3 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testInTimeZone` | `testInTimeZone()` | done |
| `testInTimeZoneWhenLocalTimeIsAmbiguous` | `testInTimeZoneWhenLocalTimeIsAmbiguous()` | done |
| `testInTimeZoneWhenLocalTimeIsSkipped` | `testInTimeZoneWhenLocalTimeIsSkipped()` | done |

</details>

<details><summary><code>IANATimeZoneDatabaseTest</code> → <code>org.chalten.timezones.IANATimeZoneDatabaseTest</code> (6 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAt` | `testAt()` | done |
| `testAtInvalidTimeZone` | `testAtInvalidTimeZone()` | done |
| `testOffsetToUniversalCoordinatedTimeAt` | `testOffsetToUniversalCoordinatedTimeAt()` | done |
| `testOffsetToUniversalCoordinatedTimeAtTransitionMoment` | `testOffsetToUniversalCoordinatedTimeAtTransitionMoment()` | done |
| `testOffsetToUniversalCoordinatedTimeBeforeFirstTransition` | `testOffsetToUniversalCoordinatedTimeBeforeFirstTransition()` | done |
| `testTimeZoneIdentifiers` | `testTimeZoneIdentifiers()` | done |

</details>

<details><summary><code>TimeOffsetAwareGregorianDateTimeTest (extension)</code> → <code>org.chalten.timezones.TimeOffsetAwareGregorianDateTimeTimezonesTest</code> (2 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testInTimeZone` | `testInTimeZone()` | done |
| `testInTimeZoneWhenLocalTimeIsAmbiguous` | `testInTimeZoneWhenLocalTimeIsAmbiguous()` | done |

</details>

<details><summary><code>TimeZoneRulesetTest</code> → <code>org.chalten.timezones.TimeZoneRulesetTest</code> (4 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testCantCreateWithInvalidNumberOfTransitions` | `testCantCreateWithInvalidNumberOfTransitions()` | done |
| `testIdentifier` | `testIdentifier()` | done |
| `testOffsetToUniversalCoordinatedTimeAt` | `testOffsetToUniversalCoordinatedTimeAt()` | done |
| `testPrintString` | `testPrintString()` | done |

</details>

## Appendix: Smalltalk method inventory

Every class and extension with its selectors (test classes show counts only; their selectors are in §8).

### `BaselineOfChalten`

| Smalltalk | Kind | Superclass | Instance-side selectors | Class-side selectors |
| --- | --- | --- | --- | --- |
| `BaselineOfChalten` | Class | `BaselineOf` | `baseline:` `configureCore` `loadLocaleConventions` `loadTranslationUnits` `projectClass` `setUpCorePackages:` `setUpDependencies:` `setUpDeploymentPackages:` `setUpHebrewPackages:` `setUpIslamicPackages:` `setUpTestPackages:` `setUpToolsPackages:` | — |

### `Chalten-Core`

| Smalltalk | Kind | Superclass | Instance-side selectors | Class-side selectors |
| --- | --- | --- | --- | --- |
| `AbsolutePointInTime` | Class | `CalendarMagnitude` | `<` `=` `asInstant` `equalityChecker` `hash` `isAbsolutePointInTime` `julianDayNumber` `microsecondsSinceEpoch` `printTimeOfDayOn:` `printYearMonthDateOn:` `withGregorianDayMonthYearDo:` `withTimeOfDayDo:` | `isAbstract` |
| `CalendarDate` | Class | `CalendarMagnitude` | `+` `-` `at:` `atMidnight` `atNoon` `atStartOfDay` `dayNumber` `dayOfWeek` `isComparableTo:` `isLastDayOfMonth` `month` `monthNumber` `monthOfYear` `next` `next:` `onSameYearNext:` `onSameYearPrevious:` `oneDay` `previous` `previous:` `subtractionFromCalendarDate:` `to:` `to:by:` `year` `yearNumber` | `assert:isValidIn:` `isAbstract` |
| `CalendarDateTime` | Class | `CalendarMagnitude` | `<` `=` `asGregorianDateTime` `date` `dayNumber` `hash` `in:` `inCoordinatedUniversalTime` `month` `monthNumber` `monthOfYear` `printOn:` `timeOfDay` `year` `yearNumber` | `convert:toGregorianUsing:asReferencePointTo:` `isAbstract` |
| `CalendarDayOfMonth` | Class | `Object` | `,` `=` `dayNumber` `hash` `initalizeMonth:dayNumber:` `localized` `localizedWith:` `month` `printLocalizedOn:using:` `printOn:` | `gregorianMonth:dayNumber:` `initialize` `instanceRegistryFor:` |
| `CalendarDayOfWeek` | Class | `Object` | `asISODayOfWeek` `initializeNamed:shortNamed:positionInWeeek:` `localized` `localizedWith:` `name` `next` `positionInWeek` `previous` `printLocalizedOn:using:` `printOn:` `shortName` | `dayAt:` `isAbstract` `named:shortNamed:positionInWeeek:` |
| `CalendarMagnitude` | Class | `Magnitude` | — | `day` `isAbstract` `month` `year` |
| `CalendarMonth` | Class | `Object` | `,` `dayOfMonthAt:` `eighteenth` `eighth` `eleventh` `fifteenth` `fifth` `first` `fourteenth` `fourth` `isGregorian` `localized` `localizedWith:` `name` `nineteenth` `ninth` `printLocalizedOn:using:` `printOn:` `second` `seventeenth` `seventh` `shortName` `sixteenth` `sixth` `tenth` `third` `thirteenth` `twelfth` `twentieth` `twentyEighth` `twentyFifth` `twentyFirst` `twentyFourth` `twentyNinth` `twentySecond` `twentySeventh` `twentySixth` `twentyThird` | `day` |
| `CalendarMonthOfYear` | Class | `CalendarMagnitude` | `dateAt:` `eighteenth` `eighth` `eleventh` `fifteenth` `fifth` `first` `fourteenth` `fourth` `last` `month` `next` `next:` `nineteenth` `ninth` `numberOfDays` `previous` `previous:` `second` `seventeenth` `seventh` `sixteenth` `sixth` `tenth` `third` `thirteenth` `thirtieth` `twelfth` `twentieth` `twentyEighth` `twentyFifth` `twentyFirst` `twentyFourth` `twentySecond` `twentySeventh` `twentySixth` `twentyThird` `year` | `isAbstract` |
| `CalendarYear` | Class | `CalendarMagnitude` | `+` `-` `<` `assertIsCommensurableWith:` `firstDate` `isComparableTo:` `isLeapYear` `lastDate` `next` `next:` `number` `numberOfDays` `numberOfMonths` `previous` `previous:` `subtractionFromCalendarYear:` `to:` `to:by:` | `assertIsValid:` `calendarName` `isAbstract` |
| `ChronologyPattern` | Class | `Object` | `elements` `enforce:because:` `initializeFromString:` `initializeWithElements:` `isLetter:` `parseFieldStartingWith:from:using:` `parsePlaceholderFrom:` `parseQuotedLiteralFrom:on:` `replacingDateWith:andTimeWith:` | `fieldNamesByLetters` `fromString:` `withElements:` |
| `DayOfWeek` | Class | `CalendarDayOfWeek` | `asISODayOfWeek` | `dayAt:` `friday` `initialize` `monday` `saturday` `sunday` `thursday` `tuesday` `wednesday` |
| `EdwardGrahamRichardsAlgorithm` | Class | `Object` | `computeGregorianYearFrom:and:` `executeFor:` `extractDayNumber:` `extractMonthNumber:` `initialize` `normalize:` `prepareForMonthAndDayExtraction:` `prepareForSplittingInto4YearCycles:` `splitInto4YearCycles:` | `new` |
| `FixedOffsetRuleset` | Class | `TimeOffsetRuleset` | `convertGregorianDateTime:` `identifier` `initializeFor:` `offsetToUniversalCoordinatedTimeAt:` `printOn:` | `for:` |
| `GregorianDate` | Class | `CalendarDate` | `<` `=` `at:` `atMidnight` `atNoon` `dayNumber` `dayOfWeek` `hash` `initalizeMonthOfYear:dayNumber:` `localized` `localizedWith:` `monthNumber` `monthOfYear` `printAsISO8601On:` `printLocalizedOn:using:` `printOn:` | `monthOfYear:dayNumber:` |
| `GregorianDateTime` | Class | `CalendarDateTime` | `asGregorianDateTime` `date` `in:` `inCoordinatedUniversalTime` `initializeOn:at:` `julianDayNumber` `localized` `localizedWith:` `printAsISO8601On:` `printAsISO8601String` `printLocalizedOn:using:` `timeOfDay` | `on:at:` |
| `GregorianMonth` | Class | `CalendarMonth` | `,` `dayOfMonthAt:` `initializeNamed:shortNamed:positionInYear:numberOfDaysInLeapYear:andInCommonYear:` `isGregorian` `next` `next:` `numberOfDaysInCommonYear` `numberOfDaysInLeapYear` `positionInYear` `previous:` `printAsISO8601On:` `thirtieth` `thirtyFirst` `whenNumberOfDaysInLeapYearGreaterThan:do:otherwise:` | `all` `april` `august` `december` `february` `initialize` `january` `july` `june` `march` `may` `monthAt:` `named:shortNamed:positionInYear:numberOfDaysInLeapYear:andInCommonYear:` `named:shortNamed:positionInYear:numberOfDaysInYear:` `november` `october` `september` |
| `GregorianMonthOfYear` | Class | `CalendarMonthOfYear` | `-` `<` `=` `dateAt:` `hash` `initializeYear:month:` `localized` `localizedWith:` `month` `next:` `numberOfDays` `previous:` `printAsISO8601On:` `printLocalizedOn:using:` `printOn:` `subtractionFromGregorianMonthOfYear:` `thirtyFirst` `twentyNinth` `when:isWholeYearsDo:otherwise:` `year` | `year:month:` |
| `GregorianYear` | Class | `CalendarYear` | `=` `april` `august` `december` `february` `firstDate` `hash` `initializeNumber:` `isCentennialYear` `isLeapYear` `isYearNumberDivisibleByFour` `isYearNumberDivisibleByFourHundred` `january` `july` `june` `lastDate` `localized` `localizedWith:` `march` `may` `monthAt:` `november` `number` `numberOfDays` `numberOfMonths` `october` `printAsISO8601On:` `printLocalizedOn:using:` `printOn:` `september` | `calendarName` `initialize` `number:` |
| `ISO8601DurationFormatter` | Class | `Formatter` | `assertHasIntegerValue:unlessIsTheLastIn:` `assertNotEmpty:` `assertThereAreNotRemainingPeriodsIn:` `day` `format:on:` `formatSingleTimePeriod:on:` `formatTimePeriods:on:` `hour` `minute` `month` `second` `supportedUnitsInOrder` `week` `year` | — |
| `ISO8601Formatter` | Class | `Formatter` | `format:on:` | — |
| `ISO8601Parser` | Class | `Object` | `enforce:because:` `errorMessageForDateParsing:` `errorMessageForDateTimeParsing:` `errorMessageForDayNumberParsing:` `errorMessageForDurationParsing:` `errorMessageForMonthOfYearParsing:` `errorMessageForMonthParsing:` `errorMessageForTimeParsing:` `errorMessageForYearParsing:` `initialize` `numberIn:parsing:` `parseDate:` `parseDateTime:` `parseDayNumber:` `parseDuration:` `parseDuration:allowingSeveralTimePeriods:` `parseMonth:` `parseMonthOfYear:` `parseTimeOfDay:` `parseTimePeriodsInDuration:` `parseWeekDuration:` `parseYear:` `readNumbersUsing:` `refuse:because:` `signal:` `unitFromDesignator:insideTimeSection:` | — |
| `Instant` | Class | `AbsolutePointInTime` | `asGregorianDateTimeIn:` `asGregorianDateTimeInCoordinatedUniversalTime` `asInstant` `asUnixTime` `daysSinceEpoch` `initializeMicrosecondsSinceEpoch:` `julianDayNumber` `julianDayNumberOfEpoch` `localized` `localizedWith:` `microsecondsSinceEpoch` `milisecondsSinceEpoch` `next:` `previous:` `printLocalizedOn:using:` `printOn:` `secondsSinceEpoch` `withTimeOfDayDo:` | `daysSinceEpoch:` `epoch` `fromJulianDayNumber:` `fromUnixTime:` `initialize` `julianDayNumberOfEpoch` `microsecond` `microsecondsSinceEpoch:` `millisecond` `now` `second` `secondsSinceEpoch:` |
| `LocalizedChronologyFormatter` | Class | `Formatter` | `addPartsOfDate:to:` `addPartsOfDateTime:to:` `addPartsOfTimeOfDay:to:` `combineDateAndTimeWith:zonedTime:` `dayPeriodIn:` `format:on:` `full` `hourOf12HourClockIn:` `initializeFieldWriters` `initializeFor:in:` `long` `medium` `part:of:` `patternNamed:` `print:with:on:` `printDate:on:` `printDateTime:offsetBy:on:` `printDateTime:on:` `printDayOfMonth:on:` `printDayOfWeek:on:` `printLongOffset:on:` `printMonth:on:` `printMonthOfYear:on:` `printOffsetSeconds:on:` `printShortOffset:on:` `printTimeOfDay:on:` `printYear:on:` `short` `withPartsOfOffset:on:do:` | `for:` `for:configuredBy:` `for:in:configuredBy:` |
| `TimeOfDay` | Class | `Magnitude` | `+` `-` `<` `=` `hash` `hours` `initializeOffsetFromStartOfDay:` `localized` `localizedWith:` `microseconds` `minutes` `printAsISO8601On:` `printLocalizedOn:using:` `printOn:` `seconds` `timeFromStartOfDay` | `halfOfDay` `hour` `hours:` `hours:minutes:` `hours:minutes:seconds:` `hours:minutes:seconds:microseconds:` `microsecond` `minute` `offsetFromStartOfDay:` `second` `startOfDay` `unit:` |
| `TimeOffsetAwareGregorianDateTime` | Class | `AbsolutePointInTime` | `asInstant` `asRelativePointInTime` `in:` `inCoordinatedUniversalTime` `initializeAt:accordingTo:shiftedBy:` `julianDayNumber` `localized` `localizedWith:` `microsecondsSinceEpoch` `printLocalizedOn:using:` `printOn:` `withTimeOfDayDo:` | `at:` `at:accordingTo:shiftedBy:` `basedOn:accordingTo:` |
| `TimeOffsetRuleset` | Class | `Object` | `convertGregorianDateTime:` `convertToGregorianDate:withOffset:` `identifier` `offsetToUniversalCoordinatedTimeAt:` `printOffset:on:` | — |
| `TimeUnits` | Class | `Object` | — | `initialize` `initializeRelativeUnits` `initializeUnits` `units` |

### `Chalten-Core-Aconcagua-Extensions`

| Smalltalk | Kind | Superclass | Instance-side selectors | Class-side selectors |
| --- | --- | --- | --- | --- |
| `Quantity` | Extension | `` | `additionWithCalendarDate:` `subtractionFromCalendarDate:` `subtractionFromCalendarYear:` | — |

### `Chalten-Core-Buoy-Extensions`

| Smalltalk | Kind | Superclass | Instance-side selectors | Class-side selectors |
| --- | --- | --- | --- | --- |
| `LocaleConventions` | Extension | `` | `chronologyPropertyNamed:for:` `loadJSONChronologyConventionsFrom:` `loadJSONChronologyConventionsFromString:` | `chronologyPatternNames` `chronologyPropertyNames` |

### `Chalten-Core-Extensions`

| Smalltalk | Kind | Superclass | Instance-side selectors | Class-side selectors |
| --- | --- | --- | --- | --- |
| `Integer` | Extension | `` | `asGregorianYear` | — |
| `Object` | Extension | `` | `isAbsolutePointInTime` | — |

### `Chalten-Core-GS64-Extensions`

| Smalltalk | Kind | Superclass | Instance-side selectors | Class-side selectors |
| --- | --- | --- | --- | --- |
| `FsBinaryFileStream` | Extension | `` | `int32` `uint32` | — |
| `Time` | Extension | `` | — | `microsecondClockValue` |

### `Chalten-Core-Tests`

| Smalltalk | Kind | Superclass | Instance-side selectors | Class-side selectors |
| --- | --- | --- | --- | --- |
| `ChronologyConventionsTest` | Class | `TestCase` | 13 (9 tests) | 0 |
| `ChronologyPatternTest` | Class | `TestCase` | 21 (18 tests) | 0 |
| `DayOfWeekTest` | Class | `TestCase` | 10 (9 tests) | 0 |
| `FixedOffsetRulesetTest` | Class | `ChaltenTest` | 4 (4 tests) | 0 |
| `GregorianDateTest` | Class | `ChaltenTest` | 24 (21 tests) | 0 |
| `GregorianDateTimeTest` | Class | `ChaltenTest` | 18 (17 tests) | 0 |
| `GregorianDayOfMonthTest` | Class | `ChaltenTest` | 17 (17 tests) | 0 |
| `GregorianLocalizationTest` | Class | `ChaltenTest` | 25 (18 tests) | 0 |
| `GregorianMonthOfYearTest` | Class | `ChaltenTest` | 15 (14 tests) | 0 |
| `GregorianMonthTest` | Class | `ChaltenTest` | 21 (20 tests) | 0 |
| `GregorianYearTest` | Class | `ChaltenTest` | 18 (17 tests) | 0 |
| `ISO8601DurationFormatterTest` | Class | `ChaltenTest` | 17 (13 tests) | 0 |
| `ISO8601ParserTest` | Class | `ChaltenTest` | 13 (10 tests) | 0 |
| `InstantTest` | Class | `TestCase` | 15 (9 tests) | 0 |
| `ShippedChronologyConventionsTest` | Class | `TestCase` | 10 (5 tests) | 0 |
| `TimeOfDayTest` | Class | `TestCase` | 15 (12 tests) | 0 |
| `TimeOffsetAwareGregorianDateTimeTest` | Class | `ChaltenTest` | 6 (5 tests) | 0 |
| `TimeUnitsTest` | Class | `TestCase` | 5 (3 tests) | 0 |

### `Chalten-Development-Tools`

| Smalltalk | Kind | Superclass | Instance-side selectors | Class-side selectors |
| --- | --- | --- | --- | --- |
| `ChaltenTranslationUnitsExport` | Class | `Object` | — | `monthStringsToTranslateByCategory` `run` `timeUnitStringsToTranslateByCategory` `weekDayStringsToTranslateByCategory` |

### `Chalten-Hebrew-Calendar`

| Smalltalk | Kind | Superclass | Instance-side selectors | Class-side selectors |
| --- | --- | --- | --- | --- |
| `CalendarDayOfMonth` | Extension | `` | — | `hebrewMonth:dayNumber:` |
| `DayOfWeek` | Extension | `` | `asHebrewDayOfWeek` | — |
| `HebrewCalendar` | Class | `Object` | — | `initialize` `initializeUnits` `month` `year` |
| `HebrewDate` | Class | `CalendarDate` | `<` `=` `at:` `atMidnight` `atNoon` `dayNumber` `dayOfWeek` `hash` `initalizeMonthOfYear:dayNumber:` `monthNumber` `monthOfYear` `printOn:` | `monthOfYear:dayNumber:` |
| `HebrewDateTime` | Class | `CalendarDateTime` | `asGregorianDateTime` `date` `initializeOn:at:` `timeOfDay` | `on:at:` |
| `HebrewDayOfWeek` | Class | `CalendarDayOfWeek` | `asHebrewDayOfWeek` `asISODayOfWeek` | `dayAt:` `initialize` `yomHamishi` `yomRevii` `yomRishon` `yomShabbat` `yomSheni` `yomShishi` `yomShlishi` |
| `HebrewMonth` | Class | `CalendarMonth` | `,` `dayOfMonthAt:` `initializeNamed:shortNamed:positionInNonLeapYear:inLeapYear:numberOfDaysInDeficientYear:numberOfDaysInRegularYear:numberOfDaysInCompleteYear:` `isGregorian` `isVariableLengthMonth` `numberOfDaysInCompleteYear` `numberOfDaysInDeficientYear` `numberOfDaysInRegularYear` `positionIn:` `positionInCommonYear` `positionInLeapYear` `thirtieth` | `adar` `adarI` `adarII` `allOnCommonYear` `allOnLeapYear` `av` `cheshvan` `elul` `initialize` `iyar` `kislev` `monthOnCommonYearAt:` `monthOnLeapYearAt:` `named:shortNamed:positionInCommonYear:inLeapYear:numberOfDaysInDeficientYear:numberOfDaysInRegularYear:numberOfDaysInCompleteYear:` `named:shortNamed:positionInNonLeapYear:inLeapYear:numberOfDaysInYear:` `named:shortNamed:positionInYear:numberOfDaysInDeficientYear:numberOfDaysInRegularYear:numberOfDaysInCompleteYear:` `named:shortNamed:positionInYear:numberOfDaysInYear:` `nisan` `shevat` `sivan` `tammuz` `tevet` `tishrei` |
| `HebrewMonthOfYear` | Class | `CalendarMonthOfYear` | `<` `=` `dateAt:` `hash` `initializeYear:month:` `month` `next:` `numberOfDays` `previous:` `printOn:` `twentyNinth` `year` | `month` `year` `year:month:` |
| `HebrewYear` | Class | `CalendarYear` | `=` `adar` `adarI` `adarII` `av` `cheshvan` `elul` `firstDate` `hash` `initializeNumber:` `isCompleteYear` `isDeficientYear` `isLeapYear` `isRegularYear` `iyar` `kislev` `lastDate` `monthAt:` `nisan` `number` `numberOfDays` `numberOfMonths` `partsPerHour` `printOn:` `roshHashanahDayNumber` `roshHashanahPostponementOffsetFor:and:` `shevat` `sivan` `tammuz` `tevet` `tishrei` | `assertIsValid:` `calendarName` `initialize` `month` `number:` `year` |

### `Chalten-Hebrew-Calendar-Extensions`

| Smalltalk | Kind | Superclass | Instance-side selectors | Class-side selectors |
| --- | --- | --- | --- | --- |
| `Integer` | Extension | `` | `asHebrewYear` | — |

### `Chalten-Hebrew-Calendar-Tests`

| Smalltalk | Kind | Superclass | Instance-side selectors | Class-side selectors |
| --- | --- | --- | --- | --- |
| `HebrewCalendarTest` | Class | `ChaltenTest` | 1 (0 tests) | 1 |
| `HebrewDateTest` | Class | `HebrewCalendarTest` | 25 (23 tests) | 0 |
| `HebrewDateTimeTest` | Class | `HebrewCalendarTest` | 12 (12 tests) | 0 |
| `HebrewDayOfMonthTest` | Class | `HebrewCalendarTest` | 20 (20 tests) | 0 |
| `HebrewDayOfWeekTest` | Class | `TestCase` | 11 (10 tests) | 0 |
| `HebrewMonthOfYearTest` | Class | `HebrewCalendarTest` | 16 (16 tests) | 0 |
| `HebrewMonthTest` | Class | `HebrewCalendarTest` | 19 (19 tests) | 0 |
| `HebrewYearTest` | Class | `HebrewCalendarTest` | 17 (17 tests) | 0 |

### `Chalten-Islamic-Calendar`

| Smalltalk | Kind | Superclass | Instance-side selectors | Class-side selectors |
| --- | --- | --- | --- | --- |
| `CalendarDayOfMonth` | Extension | `` | — | `islamicMonth:dayNumber:` |
| `DayOfWeek` | Extension | `` | `asIslamicDayOfWeek` | — |
| `IslamicCalendar` | Class | `Object` | — | `initialize` `initializeUnits` `month` `year` |
| `IslamicDate` | Class | `CalendarDate` | `<` `=` `at:` `atMidnight` `atNoon` `dayNumber` `dayOfWeek` `hash` `initalizeMonthOfYear:dayNumber:` `monthNumber` `monthOfYear` `printOn:` | `monthOfYear:dayNumber:` |
| `IslamicDateTime` | Class | `CalendarDateTime` | `asGregorianDateTime` `date` `initializeOn:at:` `timeOfDay` | `on:at:` |
| `IslamicDayOfWeek` | Class | `CalendarDayOfWeek` | `asISODayOfWeek` `asIslamicDayOfWeek` | `dayAt:` `initialize` `yawnAlAhad` `yawnAlArbaa` `yawnAlIthnayn` `yawnAlJumuha` `yawnAlKhamees` `yawnAsSabt` `yawnAthThulatha` |
| `IslamicMonth` | Class | `CalendarMonth` | `,` `dayOfMonthAt:` `initializeNamed:shortNamed:positionInYear:numberOfDaysInLeapYear:andInCommonYear:` `isGregorian` `next:` `numberOfDaysInCommonYear` `numberOfDaysInLeapYear` `positionInYear` `previous:` `thirtieth` `whenNumberOfDaysInLeapYearGreaterThan:do:otherwise:` | `all` `duAlHijjah` `duAlQiDah` `initialize` `jumadaAlAwwal` `jumadaAlThani` `monthAt:` `muharram` `named:shortNamed:positionInYear:numberOfDaysInLeapYear:andInCommonYear:` `named:shortNamed:positionInYear:numberOfDaysInYear:` `rabiAlAwwal` `rabiAlThani` `rajab` `ramadan` `safar` `shaban` `shawwal` |
| `IslamicMonthOfYear` | Class | `CalendarMonthOfYear` | `<` `=` `dateAt:` `hash` `initializeYear:month:` `month` `next:` `next:and:` `numberOfDays` `previous:` `previous:and:` `printOn:` `twentyNinth` `when:isWholeYearsDo:otherwise:` `year` | `month` `year` `year:month:` |
| `IslamicYear` | Class | `CalendarYear` | `=` `duAlHijjah` `duAlQiDah` `firstDate` `hash` `initializeNumber:` `isLeapYear` `jumadaAlAwwal` `jumadaAlThani` `lastDate` `monthAt:` `muharram` `number` `numberOfDays` `numberOfMonths` `printOn:` `rabiAlAwwal` `rabiAlThani` `rajab` `ramadan` `safar` `shaban` `shawwal` | `assertIsValid:` `calendarName` `initialize` `month` `number:` `year` |

### `Chalten-Islamic-Calendar-Extensions`

| Smalltalk | Kind | Superclass | Instance-side selectors | Class-side selectors |
| --- | --- | --- | --- | --- |
| `Integer` | Extension | `` | `asIslamicYear` | — |

### `Chalten-Islamic-Calendar-Tests`

| Smalltalk | Kind | Superclass | Instance-side selectors | Class-side selectors |
| --- | --- | --- | --- | --- |
| `IslamicCalendarTest` | Class | `ChaltenTest` | 1 (0 tests) | 1 |
| `IslamicDateTest` | Class | `IslamicCalendarTest` | 21 (19 tests) | 0 |
| `IslamicDateTimeTest` | Class | `IslamicCalendarTest` | 12 (12 tests) | 0 |
| `IslamicDayOfMonthTest` | Class | `IslamicCalendarTest` | 18 (18 tests) | 0 |
| `IslamicDayOfWeekTest` | Class | `TestCase` | 11 (10 tests) | 0 |
| `IslamicMonthOfYearTest` | Class | `IslamicCalendarTest` | 11 (11 tests) | 0 |
| `IslamicMonthTest` | Class | `IslamicCalendarTest` | 18 (18 tests) | 0 |
| `IslamicYearTest` | Class | `IslamicCalendarTest` | 14 (14 tests) | 0 |

### `Chalten-SUnit`

| Smalltalk | Kind | Superclass | Instance-side selectors | Class-side selectors |
| --- | --- | --- | --- | --- |
| `ChaltenTest` | Class | `TestCase` | 11 (0 tests) | 1 |

### `Chalten-Timezones`

| Smalltalk | Kind | Superclass | Instance-side selectors | Class-side selectors |
| --- | --- | --- | --- | --- |
| `AmbiguousTimeError` | Class | `Error` | `earlierDateTime` `earlierDateTime:laterDateTime:` `laterDateTime` | `signalFor:and:withMessageText:` |
| `IANATimeZoneDatabase` | Class | `Object` | `at:` `initialize` `loadRulesetIdentifiedBy:` `loadRulesetIdentifiedBy:from:` `reloadAllTimeZoneRulesets` `reloadTimeZoneIdentifiers` `timeZoneIdentifiers` | `current` `zoneInfoLocation` `zoneInfoLocation:` |
| `IANATimeZoneDatabaseParser` | Class | `Object` | `assertIsExpectedVersion:` `assertIsMagicConstant:` `parse:` `parseHeaderFrom:` `parseOffsetIndicesIn:accordingTo:` `parseOffsetRulesetsIn:accordingTo:` `parseRulesetsIn:accordingTo:` `parseTransitionTimesIn:accordingTo:` | — |
| `SkippedTimeError` | Class | `Error` | — | — |
| `TimeZoneRuleset` | Class | `TimeOffsetRuleset` | `convertGregorianDateTime:` `identifiedBy:withAll:transitioningOn:` `identifier` `offsetToUniversalCoordinatedTimeAt:` `oneDay` `printOn:` `signalAmbiguousTimeErrorFor:between:and:` `signalSkippedTimeError` | `identifiedBy:withAll:transitioningOn:` |

### `Chalten-Timezones-Tests`

| Smalltalk | Kind | Superclass | Instance-side selectors | Class-side selectors |
| --- | --- | --- | --- | --- |
| `GregorianDateTimeTest` | Extension | `` | 3 (3 tests) | 0 |
| `IANATimeZoneDatabaseTest` | Class | `ChaltenTest` | 8 (6 tests) | 0 |
| `TimeOffsetAwareGregorianDateTimeTest` | Extension | `` | 2 (2 tests) | 0 |
| `TimeZoneRulesetTest` | Class | `ChaltenTest` | 6 (4 tests) | 0 |
