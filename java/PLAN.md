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
| `CalendarMagnitude` | `CalendarMagnitude<T>` | sealed interface | core | U2 |
| `CalendarDate` | `CalendarDate<D>` | sealed abstract class | core | U2 |
| `CalendarDateTime` | `CalendarDateTime<D>` | sealed abstract class | core | U2 |
| `CalendarYear` | `CalendarYear<Y>` | sealed abstract class | core | U2 |
| `CalendarMonthOfYear` | `CalendarMonthOfYear<M>` | sealed abstract class | core | U2 |
| `CalendarMonth` | `CalendarMonth` | sealed interface (enums implement) | core | U2 |
| `CalendarDayOfWeek` | `CalendarDayOfWeek` | sealed interface (enums implement) | core | U2 |
| `CalendarDayOfMonth` | `CalendarDayOfMonth` | record (interned) | core | U2 |
| `GregorianYear` | `GregorianYear` | final class | core | U2 |
| `GregorianMonth` | `GregorianMonth` | enum | core | U2 |
| `GregorianMonthOfYear` | `GregorianMonthOfYear` | record | core | U2 |
| `GregorianDate` | `GregorianDate` | record | core | U2 |
| `GregorianDateTime` | `GregorianDateTime` | record | core | U2 |
| `DayOfWeek` | `DayOfWeek` | enum | core | U2 |
| `TimeOfDay` | `TimeOfDay` | record | core | U2 |
| `EdwardGrahamRichardsAlgorithm` | `EdwardGrahamRichardsAlgorithm` | final class (package-private) | core | U2 |
| `AbsolutePointInTime` | `AbsolutePointInTime` | sealed interface | core | U2 |
| `Instant` | `Instant` | record (`long microsecondsSinceEpoch`) | core | U2 |
| `TimeOffsetAwareGregorianDateTime` | `TimeOffsetAwareGregorianDateTime` | final class | core | U2 |
| `TimeOffsetRuleset` | `TimeOffsetRuleset` | sealed interface (non-sealed for timezones module) | core | U2 |
| `FixedOffsetRuleset` | `FixedOffsetRuleset` | record | core | U2 |
| `ISO8601Parser` | `ISO8601Parser` | final class | core | U2 |
| `ISO8601Formatter` | `ISO8601Formatter` | final class | core | U2 |
| `Integer>>asGregorianYear`, `Object>>isAbsolutePointInTime` | `GregorianYear.of(int)`, `instanceof AbsolutePointInTime` | – | core | U2 |
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

## 7. Open decisions / follow-ups

- **Business-day and holiday rules** are not in Chalten. Optional unit U7 (new behaviour, not parity): `BusinessDayCalendar` with weekend rules, holiday sets and modified-following adjustments on top of `GregorianDate`.
- **Policy-period conventions** (anniversary clamping of Feb 29, actual/365 vs actual/actual pro-rata) are not Chalten behaviour; U6 documents them as compositions so Masterpiece's own rules stay explicit.
- Coordinates: Maven group `org.chalten`, artifact per module, version `0.1.0-SNAPSHOT` until the user picks real coordinates.

## 8. Test-parity matrix

Status is `pending` until the owning unit lands; U5's checker keeps it honest.

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
| `testChronologyCategory` | `testChronologyCategory()` | pending |
| `testLaterLoadsOverridePropertyByProperty` | `testLaterLoadsOverridePropertyByProperty()` | pending |
| `testLoadingFromAFile` | `testLoadingFromAFile()` | pending |
| `testMarksAreNotPatterns` | `testMarksAreNotPatterns()` | pending |
| `testPropertyNames` | `testPropertyNames()` | pending |
| `testPropertyNotDefined` | `testPropertyNotDefined()` | pending |
| `testPropertyResolvedFromTheMostSpecificRange` | `testPropertyResolvedFromTheMostSpecificRange()` | pending |
| `testUnknownProperty` | `testUnknownProperty()` | pending |
| `testUnsupportedFieldsAreRejectedBeforeLoading` | `testUnsupportedFieldsAreRejectedBeforeLoading()` | pending |

</details>

<details><summary><code>ChronologyPatternTest</code> → <code>org.chalten.localization.ChronologyPatternTest</code> (18 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testDay` | `testDay()` | pending |
| `testDayOfWeek` | `testDayOfWeek()` | pending |
| `testDayPeriod` | `testDayPeriod()` | pending |
| `testEmptyPattern` | `testEmptyPattern()` | pending |
| `testHour` | `testHour()` | pending |
| `testLiterals` | `testLiterals()` | pending |
| `testMinute` | `testMinute()` | pending |
| `testMissingClosingQuote` | `testMissingClosingQuote()` | pending |
| `testMonth` | `testMonth()` | pending |
| `testNonASCIILettersAreLiteral` | `testNonASCIILettersAreLiteral()` | pending |
| `testOffset` | `testOffset()` | pending |
| `testPlaceholders` | `testPlaceholders()` | pending |
| `testQuotedLiterals` | `testQuotedLiterals()` | pending |
| `testReplacingDateAndTime` | `testReplacingDateAndTime()` | pending |
| `testSecond` | `testSecond()` | pending |
| `testUnsupportedFields` | `testUnsupportedFields()` | pending |
| `testUnsupportedPlaceholders` | `testUnsupportedPlaceholders()` | pending |
| `testYear` | `testYear()` | pending |

</details>

<details><summary><code>DayOfWeekTest</code> → <code>org.chalten.core.DayOfWeekTest</code> (9 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testConverting` | `testConverting()` | pending |
| `testInstanceCreation` | `testInstanceCreation()` | pending |
| `testNames` | `testNames()` | pending |
| `testNamesAreLocalized` | `testNamesAreLocalized()` | pending |
| `testNext` | `testNext()` | pending |
| `testPrevious` | `testPrevious()` | pending |
| `testPrintString` | `testPrintString()` | pending |
| `testShortNames` | `testShortNames()` | pending |
| `testShortNamesAreLocalized` | `testShortNamesAreLocalized()` | pending |

</details>

<details><summary><code>FixedOffsetRulesetTest</code> → <code>org.chalten.core.FixedOffsetRulesetTest</code> (4 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testCantCreateOutOfRange` | `testCantCreateOutOfRange()` | pending |
| `testIdentifier` | `testIdentifier()` | pending |
| `testOffsetToUniversalCoordinatedTimeAt` | `testOffsetToUniversalCoordinatedTimeAt()` | pending |
| `testPrintString` | `testPrintString()` | pending |

</details>

<details><summary><code>GregorianDateTest</code> → <code>org.chalten.core.GregorianDateTest</code> (21 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAccessing` | `testAccessing()` | pending |
| `testBetweenAnd` | `testBetweenAnd()` | pending |
| `testCannotComputeDateDistanceWhenTheSecondIsGreater` | `testCannotComputeDateDistanceWhenTheSecondIsGreater()` | pending |
| `testComparing` | `testComparing()` | pending |
| `testDayOfWeek` | `testDayOfWeek()` | pending |
| `testDaysBetweenTwoDates` | `testDaysBetweenTwoDates()` | pending |
| `testGreaterThan` | `testGreaterThan()` | pending |
| `testISO8601Printing` | `testISO8601Printing()` | pending |
| `testInstanceCreationFor30DaysMonth` | `testInstanceCreationFor30DaysMonth()` | pending |
| `testInstanceCreationFor31DaysMonth` | `testInstanceCreationFor31DaysMonth()` | pending |
| `testInstanceCreationForFebruaryOnCommonYear` | `testInstanceCreationForFebruaryOnCommonYear()` | pending |
| `testInstanceCreationForFebruaryOnLeapYear` | `testInstanceCreationForFebruaryOnLeapYear()` | pending |
| `testInterval` | `testInterval()` | pending |
| `testIntervalWithStep` | `testIntervalWithStep()` | pending |
| `testIsLastDayOfMonth` | `testIsLastDayOfMonth()` | pending |
| `testLessThan` | `testLessThan()` | pending |
| `testNext` | `testNext()` | pending |
| `testParseISO8601` | `testParseISO8601()` | pending |
| `testPrevious` | `testPrevious()` | pending |
| `testPrintString` | `testPrintString()` | pending |
| `testSubtraction` | `testSubtraction()` | pending |

</details>

<details><summary><code>GregorianDateTimeTest</code> → <code>org.chalten.core.GregorianDateTimeTest</code> (17 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAccessing` | `testAccessing()` | pending |
| `testAtMidnight` | `testAtMidnight()` | pending |
| `testAtNoon` | `testAtNoon()` | pending |
| `testAtStartOfDay` | `testAtStartOfDay()` | pending |
| `testBetweenAnd` | `testBetweenAnd()` | pending |
| `testComparing` | `testComparing()` | pending |
| `testConverting` | `testConverting()` | pending |
| `testCreationShortcut` | `testCreationShortcut()` | pending |
| `testGreaterThan` | `testGreaterThan()` | pending |
| `testISO8601Parsing` | `testISO8601Parsing()` | pending |
| `testISO8601Printing` | `testISO8601Printing()` | pending |
| `testIn` | `testIn()` | pending |
| `testInCoordinatedUniversalTime` | `testInCoordinatedUniversalTime()` | pending |
| `testJulianDayNumber` | `testJulianDayNumber()` | pending |
| `testLessThan` | `testLessThan()` | pending |
| `testPrintString` | `testPrintString()` | pending |
| `testPrintStringInISO8601` | `testPrintStringInISO8601()` | pending |

</details>

<details><summary><code>GregorianDayOfMonthTest</code> → <code>org.chalten.core.GregorianDayOfMonthTest</code> (17 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testComparing` | `testComparing()` | pending |
| `testDateCreation` | `testDateCreation()` | pending |
| `testInstanceCreation` | `testInstanceCreation()` | pending |
| `testInstanceCreationShortcut` | `testInstanceCreationShortcut()` | pending |
| `testInstanceCreationShortcutForApril` | `testInstanceCreationShortcutForApril()` | pending |
| `testInstanceCreationShortcutForAugust` | `testInstanceCreationShortcutForAugust()` | pending |
| `testInstanceCreationShortcutForDecember` | `testInstanceCreationShortcutForDecember()` | pending |
| `testInstanceCreationShortcutForFebruary` | `testInstanceCreationShortcutForFebruary()` | pending |
| `testInstanceCreationShortcutForJanuary` | `testInstanceCreationShortcutForJanuary()` | pending |
| `testInstanceCreationShortcutForJuly` | `testInstanceCreationShortcutForJuly()` | pending |
| `testInstanceCreationShortcutForJune` | `testInstanceCreationShortcutForJune()` | pending |
| `testInstanceCreationShortcutForMarch` | `testInstanceCreationShortcutForMarch()` | pending |
| `testInstanceCreationShortcutForMay` | `testInstanceCreationShortcutForMay()` | pending |
| `testInstanceCreationShortcutForNovember` | `testInstanceCreationShortcutForNovember()` | pending |
| `testInstanceCreationShortcutForOctober` | `testInstanceCreationShortcutForOctober()` | pending |
| `testInstanceCreationShortcutForSeptember` | `testInstanceCreationShortcutForSeptember()` | pending |
| `testPrintString` | `testPrintString()` | pending |

</details>

<details><summary><code>GregorianLocalizationTest</code> → <code>org.chalten.localization.GregorianLocalizationTest</code> (18 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testDateTimes` | `testDateTimes()` | pending |
| `testDates` | `testDates()` | pending |
| `testDatesPadTheirNumbers` | `testDatesPadTheirNumbers()` | pending |
| `testDaysOfMonth` | `testDaysOfMonth()` | pending |
| `testDaysOfWeek` | `testDaysOfWeek()` | pending |
| `testFormattersUseTheirOwnLanguage` | `testFormattersUseTheirOwnLanguage()` | pending |
| `testInstants` | `testInstants()` | pending |
| `testInstantsAreShownOnTheirUTCDate` | `testInstantsAreShownOnTheirUTCDate()` | pending |
| `testMidnightAndNoon` | `testMidnightAndNoon()` | pending |
| `testMonths` | `testMonths()` | pending |
| `testMonthsOfYear` | `testMonthsOfYear()` | pending |
| `testOffsetAwareDateTimes` | `testOffsetAwareDateTimes()` | pending |
| `testOffsets` | `testOffsets()` | pending |
| `testPatternsNeedingPartsTheObjectDoesNotHave` | `testPatternsNeedingPartsTheObjectDoesNotHave()` | pending |
| `testPlaceholdersOutsideCombiningPatterns` | `testPlaceholdersOutsideCombiningPatterns()` | pending |
| `testSecondsAreShownWithoutTheirFraction` | `testSecondsAreShownWithoutTheirFraction()` | pending |
| `testTimesOfDay` | `testTimesOfDay()` | pending |
| `testYears` | `testYears()` | pending |

</details>

<details><summary><code>GregorianMonthOfYearTest</code> → <code>org.chalten.core.GregorianMonthOfYearTest</code> (14 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testBetweenAnd` | `testBetweenAnd()` | pending |
| `testComparing` | `testComparing()` | pending |
| `testCreation` | `testCreation()` | pending |
| `testCreationShortcut` | `testCreationShortcut()` | pending |
| `testDateCreation` | `testDateCreation()` | pending |
| `testGreaterThan` | `testGreaterThan()` | pending |
| `testISO8601Printing` | `testISO8601Printing()` | pending |
| `testLessThan` | `testLessThan()` | pending |
| `testMonthsBetweenTwoMonthsOfYear` | `testMonthsBetweenTwoMonthsOfYear()` | pending |
| `testNext` | `testNext()` | pending |
| `testNumberOfDays` | `testNumberOfDays()` | pending |
| `testParseISO8601` | `testParseISO8601()` | pending |
| `testPrevious` | `testPrevious()` | pending |
| `testPrintString` | `testPrintString()` | pending |

</details>

<details><summary><code>GregorianMonthTest</code> → <code>org.chalten.core.GregorianMonthTest</code> (20 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testApril` | `testApril()` | pending |
| `testAugust` | `testAugust()` | pending |
| `testComparing` | `testComparing()` | pending |
| `testDecember` | `testDecember()` | pending |
| `testFebruary` | `testFebruary()` | pending |
| `testISO8601Printing` | `testISO8601Printing()` | pending |
| `testInstanceCreation` | `testInstanceCreation()` | pending |
| `testJanuary` | `testJanuary()` | pending |
| `testJuly` | `testJuly()` | pending |
| `testJune` | `testJune()` | pending |
| `testMarch` | `testMarch()` | pending |
| `testMay` | `testMay()` | pending |
| `testNamesAreLocalized` | `testNamesAreLocalized()` | pending |
| `testNext` | `testNext()` | pending |
| `testNovember` | `testNovember()` | pending |
| `testOctober` | `testOctober()` | pending |
| `testParseISO8601` | `testParseISO8601()` | pending |
| `testPrevious` | `testPrevious()` | pending |
| `testPrintString` | `testPrintString()` | pending |
| `testSeptember` | `testSeptember()` | pending |

</details>

<details><summary><code>GregorianYearTest</code> → <code>org.chalten.core.GregorianYearTest</code> (17 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testBetweenAnd` | `testBetweenAnd()` | pending |
| `testComparing` | `testComparing()` | pending |
| `testGreaterThan` | `testGreaterThan()` | pending |
| `testISO8601Printing` | `testISO8601Printing()` | pending |
| `testInstanceCreation` | `testInstanceCreation()` | pending |
| `testInstanceCreationShortcut` | `testInstanceCreationShortcut()` | pending |
| `testInterval` | `testInterval()` | pending |
| `testIntervalWithStep` | `testIntervalWithStep()` | pending |
| `testIsLeapYear` | `testIsLeapYear()` | pending |
| `testLessThan` | `testLessThan()` | pending |
| `testNext` | `testNext()` | pending |
| `testNumberOfDays` | `testNumberOfDays()` | pending |
| `testNumberOfMonths` | `testNumberOfMonths()` | pending |
| `testParseISO8601` | `testParseISO8601()` | pending |
| `testPrevious` | `testPrevious()` | pending |
| `testPrintString` | `testPrintString()` | pending |
| `testYearsBetweenTwoYears` | `testYearsBetweenTwoYears()` | pending |

</details>

<details><summary><code>ISO8601DurationFormatterTest</code> → <code>org.chalten.measure.ISO8601DurationFormatterTest</code> (13 tests, U3)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testCannotFormatEmptyMixedDuration` | `testCannotFormatEmptyMixedDuration()` | pending |
| `testCannotFormatInvalidFractions` | `testCannotFormatInvalidFractions()` | pending |
| `testCannotFormatMixedDurationWithRepeatedUnits` | `testCannotFormatMixedDurationWithRepeatedUnits()` | pending |
| `testCannotFormatMixedDurationWithWeeks` | `testCannotFormatMixedDurationWithWeeks()` | pending |
| `testCannotFormatUnsupportedUnits` | `testCannotFormatUnsupportedUnits()` | pending |
| `testFormatDayDuration` | `testFormatDayDuration()` | pending |
| `testFormatHourDuration` | `testFormatHourDuration()` | pending |
| `testFormatMinuteDuration` | `testFormatMinuteDuration()` | pending |
| `testFormatMixedDuration` | `testFormatMixedDuration()` | pending |
| `testFormatMonthDuration` | `testFormatMonthDuration()` | pending |
| `testFormatSecondDuration` | `testFormatSecondDuration()` | pending |
| `testFormatWeekDuration` | `testFormatWeekDuration()` | pending |
| `testFormatYearDuration` | `testFormatYearDuration()` | pending |

</details>

<details><summary><code>ISO8601ParserTest</code> → <code>org.chalten.core.ISO8601ParserTest</code> (10 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testCannotParseInvalidDuration` | `testCannotParseInvalidDuration()` | pending |
| `testMixedTimePeriods` | `testMixedTimePeriods()` | pending |
| `testParseDayDuration` | `testParseDayDuration()` | pending |
| `testParseDurationUsingNonDefaultNumberReader` | `testParseDurationUsingNonDefaultNumberReader()` | pending |
| `testParseHourDuration` | `testParseHourDuration()` | pending |
| `testParseMinuteDuration` | `testParseMinuteDuration()` | pending |
| `testParseMonthDuration` | `testParseMonthDuration()` | pending |
| `testParseSecondDuration` | `testParseSecondDuration()` | pending |
| `testParseWeekDuration` | `testParseWeekDuration()` | pending |
| `testParseYearDuration` | `testParseYearDuration()` | pending |

</details>

<details><summary><code>InstantTest</code> → <code>org.chalten.core.InstantTest</code> (9 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAccessing` | `testAccessing()` | pending |
| `testAsUnixTime` | `testAsUnixTime()` | pending |
| `testComparing` | `testComparing()` | pending |
| `testConverting` | `testConverting()` | pending |
| `testFromJulianDayNumber` | `testFromJulianDayNumber()` | pending |
| `testFromUnixTime` | `testFromUnixTime()` | pending |
| `testJulianDayNumber` | `testJulianDayNumber()` | pending |
| `testMagnitudeProtocol` | `testMagnitudeProtocol()` | pending |
| `testPrintString` | `testPrintString()` | pending |

</details>

<details><summary><code>ShippedChronologyConventionsTest</code> → <code>org.chalten.localization.ShippedChronologyConventionsTest</code> (5 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testArgentinianSpanish` | `testArgentinianSpanish()` | pending |
| `testBritishEnglish` | `testBritishEnglish()` | pending |
| `testDefaults` | `testDefaults()` | pending |
| `testEnglish` | `testEnglish()` | pending |
| `testSpanish` | `testSpanish()` | pending |

</details>

<details><summary><code>TimeOfDayTest</code> → <code>org.chalten.core.TimeOfDayTest</code> (12 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAccessing` | `testAccessing()` | pending |
| `testAddition` | `testAddition()` | pending |
| `testAdditionCannotExceed24Hours` | `testAdditionCannotExceed24Hours()` | pending |
| `testAdditionOfLessThanOneHour` | `testAdditionOfLessThanOneHour()` | pending |
| `testComparing` | `testComparing()` | pending |
| `testISO8601Printing` | `testISO8601Printing()` | pending |
| `testInvalidCreation` | `testInvalidCreation()` | pending |
| `testParseISO8601` | `testParseISO8601()` | pending |
| `testPrintString` | `testPrintString()` | pending |
| `testSubtraction` | `testSubtraction()` | pending |
| `testSubtractionCannotExceed0Hours` | `testSubtractionCannotExceed0Hours()` | pending |
| `testSubtractionToZero` | `testSubtractionToZero()` | pending |

</details>

<details><summary><code>TimeOffsetAwareGregorianDateTimeTest</code> → <code>org.chalten.core.TimeOffsetAwareGregorianDateTimeTest</code> (5 tests, U2)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAsRelativePointInTime` | `testAsRelativePointInTime()` | pending |
| `testCreationFromInstant` | `testCreationFromInstant()` | pending |
| `testCreationFromInstantWithFixedOffset` | `testCreationFromInstantWithFixedOffset()` | pending |
| `testIn` | `testIn()` | pending |
| `testInCoordinatedUniversalTime` | `testInCoordinatedUniversalTime()` | pending |

</details>

<details><summary><code>TimeUnitsTest</code> → <code>org.chalten.measure.TimeUnitsTest</code> (3 tests, U3)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAbsoluteUnits` | `testAbsoluteUnits()` | pending |
| `testCommensurability` | `testCommensurability()` | pending |
| `testRelativeUnits` | `testRelativeUnits()` | pending |

</details>

<details><summary><code>HebrewDateTest</code> → <code>org.chalten.hebrew.HebrewDateTest</code> (23 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAccessing` | `testAccessing()` | pending |
| `testBetweenAnd` | `testBetweenAnd()` | pending |
| `testCannotComputeDateDistanceWhenTheSecondIsGreater` | `testCannotComputeDateDistanceWhenTheSecondIsGreater()` | pending |
| `testComparing` | `testComparing()` | pending |
| `testDayOfWeek` | `testDayOfWeek()` | pending |
| `testDaysBetweenTwoDates` | `testDaysBetweenTwoDates()` | pending |
| `testGreaterThan` | `testGreaterThan()` | pending |
| `testInstanceCreationFor29DaysMonth` | `testInstanceCreationFor29DaysMonth()` | pending |
| `testInstanceCreationFor30DaysMonth` | `testInstanceCreationFor30DaysMonth()` | pending |
| `testInstanceCreationForCheshvanOnCompleteYear` | `testInstanceCreationForCheshvanOnCompleteYear()` | pending |
| `testInstanceCreationForCheshvanOnDeficientYear` | `testInstanceCreationForCheshvanOnDeficientYear()` | pending |
| `testInstanceCreationForCheshvanOnRegularYear` | `testInstanceCreationForCheshvanOnRegularYear()` | pending |
| `testInstanceCreationForKislevOnCompleteYear` | `testInstanceCreationForKislevOnCompleteYear()` | pending |
| `testInstanceCreationForKislevOnDeficientYear` | `testInstanceCreationForKislevOnDeficientYear()` | pending |
| `testInstanceCreationForKislevOnRegularYear` | `testInstanceCreationForKislevOnRegularYear()` | pending |
| `testInterval` | `testInterval()` | pending |
| `testIntervalWithStep` | `testIntervalWithStep()` | pending |
| `testIsLastDayOfMonth` | `testIsLastDayOfMonth()` | pending |
| `testLessThan` | `testLessThan()` | pending |
| `testNext` | `testNext()` | pending |
| `testPrevious` | `testPrevious()` | pending |
| `testPrintString` | `testPrintString()` | pending |
| `testSubtraction` | `testSubtraction()` | pending |

</details>

<details><summary><code>HebrewDateTimeTest</code> → <code>org.chalten.hebrew.HebrewDateTimeTest</code> (12 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAccessing` | `testAccessing()` | pending |
| `testAtMidnight` | `testAtMidnight()` | pending |
| `testAtNoon` | `testAtNoon()` | pending |
| `testAtStartOfDay` | `testAtStartOfDay()` | pending |
| `testBetweenAnd` | `testBetweenAnd()` | pending |
| `testComparing` | `testComparing()` | pending |
| `testCreationShortcut` | `testCreationShortcut()` | pending |
| `testGreaterThan` | `testGreaterThan()` | pending |
| `testIn` | `testIn()` | pending |
| `testInCoordinatedUniversalTime` | `testInCoordinatedUniversalTime()` | pending |
| `testLessThan` | `testLessThan()` | pending |
| `testPrintString` | `testPrintString()` | pending |

</details>

<details><summary><code>HebrewDayOfMonthTest</code> → <code>org.chalten.hebrew.HebrewDayOfMonthTest</code> (20 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testComparing` | `testComparing()` | pending |
| `testDateCreation` | `testDateCreation()` | pending |
| `testInstanceCreation` | `testInstanceCreation()` | pending |
| `testInstanceCreationShortcut` | `testInstanceCreationShortcut()` | pending |
| `testInstanceCreationShortcutForAdar` | `testInstanceCreationShortcutForAdar()` | pending |
| `testInstanceCreationShortcutForAdarI` | `testInstanceCreationShortcutForAdarI()` | pending |
| `testInstanceCreationShortcutForAdarII` | `testInstanceCreationShortcutForAdarII()` | pending |
| `testInstanceCreationShortcutForAv` | `testInstanceCreationShortcutForAv()` | pending |
| `testInstanceCreationShortcutForCheshvan` | `testInstanceCreationShortcutForCheshvan()` | pending |
| `testInstanceCreationShortcutForElul` | `testInstanceCreationShortcutForElul()` | pending |
| `testInstanceCreationShortcutForIyar` | `testInstanceCreationShortcutForIyar()` | pending |
| `testInstanceCreationShortcutForKislev` | `testInstanceCreationShortcutForKislev()` | pending |
| `testInstanceCreationShortcutForNisan` | `testInstanceCreationShortcutForNisan()` | pending |
| `testInstanceCreationShortcutForShevat` | `testInstanceCreationShortcutForShevat()` | pending |
| `testInstanceCreationShortcutForSivan` | `testInstanceCreationShortcutForSivan()` | pending |
| `testInstanceCreationShortcutForTammuz` | `testInstanceCreationShortcutForTammuz()` | pending |
| `testInstanceCreationShortcutForTevet` | `testInstanceCreationShortcutForTevet()` | pending |
| `testInstanceCreationShortcutForTishrei` | `testInstanceCreationShortcutForTishrei()` | pending |
| `testLocalizationIsNotSupported` | `testLocalizationIsNotSupported()` | pending |
| `testPrintString` | `testPrintString()` | pending |

</details>

<details><summary><code>HebrewDayOfWeekTest</code> → <code>org.chalten.hebrew.HebrewDayOfWeekTest</code> (10 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAsHebrewDayOfWeek` | `testAsHebrewDayOfWeek()` | pending |
| `testAsISODayOfWeek` | `testAsISODayOfWeek()` | pending |
| `testInstanceCreation` | `testInstanceCreation()` | pending |
| `testNames` | `testNames()` | pending |
| `testNamesAreLocalized` | `testNamesAreLocalized()` | pending |
| `testNext` | `testNext()` | pending |
| `testPrevious` | `testPrevious()` | pending |
| `testPrintString` | `testPrintString()` | pending |
| `testShortNames` | `testShortNames()` | pending |
| `testShortNamesAreLocalized` | `testShortNamesAreLocalized()` | pending |

</details>

<details><summary><code>HebrewMonthOfYearTest</code> → <code>org.chalten.hebrew.HebrewMonthOfYearTest</code> (16 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testBetweenAnd` | `testBetweenAnd()` | pending |
| `testComparing` | `testComparing()` | pending |
| `testCreation` | `testCreation()` | pending |
| `testCreationShortcut` | `testCreationShortcut()` | pending |
| `testCreationShortcutOnLeapYear` | `testCreationShortcutOnLeapYear()` | pending |
| `testGreaterThan` | `testGreaterThan()` | pending |
| `testLessThan` | `testLessThan()` | pending |
| `testNext` | `testNext()` | pending |
| `testNumberOfDaysOnCommonCompleteYear` | `testNumberOfDaysOnCommonCompleteYear()` | pending |
| `testNumberOfDaysOnCommonDeficientYear` | `testNumberOfDaysOnCommonDeficientYear()` | pending |
| `testNumberOfDaysOnCommonRegularYear` | `testNumberOfDaysOnCommonRegularYear()` | pending |
| `testNumberOfDaysOnLeapCompleteYear` | `testNumberOfDaysOnLeapCompleteYear()` | pending |
| `testNumberOfDaysOnLeapDeficientYear` | `testNumberOfDaysOnLeapDeficientYear()` | pending |
| `testNumberOfDaysOnLeapRegularYear` | `testNumberOfDaysOnLeapRegularYear()` | pending |
| `testPrevious` | `testPrevious()` | pending |
| `testPrintString` | `testPrintString()` | pending |

</details>

<details><summary><code>HebrewMonthTest</code> → <code>org.chalten.hebrew.HebrewMonthTest</code> (19 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAdar` | `testAdar()` | pending |
| `testAdarI` | `testAdarI()` | pending |
| `testAdarII` | `testAdarII()` | pending |
| `testAv` | `testAv()` | pending |
| `testCheshvan` | `testCheshvan()` | pending |
| `testComparing` | `testComparing()` | pending |
| `testElul` | `testElul()` | pending |
| `testInstanceCreation` | `testInstanceCreation()` | pending |
| `testIsGregorian` | `testIsGregorian()` | pending |
| `testIyar` | `testIyar()` | pending |
| `testKislev` | `testKislev()` | pending |
| `testNamesAreLocalized` | `testNamesAreLocalized()` | pending |
| `testNisan` | `testNisan()` | pending |
| `testPrintString` | `testPrintString()` | pending |
| `testShevat` | `testShevat()` | pending |
| `testSivan` | `testSivan()` | pending |
| `testTammuz` | `testTammuz()` | pending |
| `testTevet` | `testTevet()` | pending |
| `testTishrei` | `testTishrei()` | pending |

</details>

<details><summary><code>HebrewYearTest</code> → <code>org.chalten.hebrew.HebrewYearTest</code> (17 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testBetweenAnd` | `testBetweenAnd()` | pending |
| `testComparing` | `testComparing()` | pending |
| `testGreaterThan` | `testGreaterThan()` | pending |
| `testInstanceCreation` | `testInstanceCreation()` | pending |
| `testInstanceCreationShortcut` | `testInstanceCreationShortcut()` | pending |
| `testInterval` | `testInterval()` | pending |
| `testIsCompleteYear` | `testIsCompleteYear()` | pending |
| `testIsDeficientYear` | `testIsDeficientYear()` | pending |
| `testIsLeapYear` | `testIsLeapYear()` | pending |
| `testIsRegularYear` | `testIsRegularYear()` | pending |
| `testLessThan` | `testLessThan()` | pending |
| `testNext` | `testNext()` | pending |
| `testNumberOfDays` | `testNumberOfDays()` | pending |
| `testNumberOfMonths` | `testNumberOfMonths()` | pending |
| `testPrevious` | `testPrevious()` | pending |
| `testPrintString` | `testPrintString()` | pending |
| `testYearsBetweenTwoYears` | `testYearsBetweenTwoYears()` | pending |

</details>

<details><summary><code>IslamicDateTest</code> → <code>org.chalten.islamic.IslamicDateTest</code> (19 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAccessing` | `testAccessing()` | pending |
| `testBetweenAnd` | `testBetweenAnd()` | pending |
| `testCannotComputeDateDistanceWhenTheSecondIsGreater` | `testCannotComputeDateDistanceWhenTheSecondIsGreater()` | pending |
| `testComparing` | `testComparing()` | pending |
| `testDayOfWeek` | `testDayOfWeek()` | pending |
| `testDaysBetweenTwoDates` | `testDaysBetweenTwoDates()` | pending |
| `testGreaterThan` | `testGreaterThan()` | pending |
| `testInstanceCreationFor29DaysMonth` | `testInstanceCreationFor29DaysMonth()` | pending |
| `testInstanceCreationFor30DaysMonth` | `testInstanceCreationFor30DaysMonth()` | pending |
| `testInstanceCreationForDuAlhijjahOnCommonYear` | `testInstanceCreationForDuAlhijjahOnCommonYear()` | pending |
| `testInstanceCreationForDuAlhijjahOnLeapYear` | `testInstanceCreationForDuAlhijjahOnLeapYear()` | pending |
| `testInterval` | `testInterval()` | pending |
| `testIntervalWithStep` | `testIntervalWithStep()` | pending |
| `testIsLastDayOfMonth` | `testIsLastDayOfMonth()` | pending |
| `testLessThan` | `testLessThan()` | pending |
| `testNext` | `testNext()` | pending |
| `testPrevious` | `testPrevious()` | pending |
| `testPrintString` | `testPrintString()` | pending |
| `testSubtraction` | `testSubtraction()` | pending |

</details>

<details><summary><code>IslamicDateTimeTest</code> → <code>org.chalten.islamic.IslamicDateTimeTest</code> (12 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAccessing` | `testAccessing()` | pending |
| `testAtMidnight` | `testAtMidnight()` | pending |
| `testAtNoon` | `testAtNoon()` | pending |
| `testAtStartOfDay` | `testAtStartOfDay()` | pending |
| `testBetweenAnd` | `testBetweenAnd()` | pending |
| `testComparing` | `testComparing()` | pending |
| `testCreationShortcut` | `testCreationShortcut()` | pending |
| `testGreaterThan` | `testGreaterThan()` | pending |
| `testIn` | `testIn()` | pending |
| `testInCoordinatedUniversalTime` | `testInCoordinatedUniversalTime()` | pending |
| `testLessThan` | `testLessThan()` | pending |
| `testPrintString` | `testPrintString()` | pending |

</details>

<details><summary><code>IslamicDayOfMonthTest</code> → <code>org.chalten.islamic.IslamicDayOfMonthTest</code> (18 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testCannotCreateWithMonthOfAnotherCalendar` | `testCannotCreateWithMonthOfAnotherCalendar()` | pending |
| `testComparing` | `testComparing()` | pending |
| `testDateCreation` | `testDateCreation()` | pending |
| `testInstanceCreation` | `testInstanceCreation()` | pending |
| `testInstanceCreationShortcut` | `testInstanceCreationShortcut()` | pending |
| `testInstanceCreationShortcutForDuAlQiDah` | `testInstanceCreationShortcutForDuAlQiDah()` | pending |
| `testInstanceCreationShortcutForDuAlQiHijjah` | `testInstanceCreationShortcutForDuAlQiHijjah()` | pending |
| `testInstanceCreationShortcutForJumadaI` | `testInstanceCreationShortcutForJumadaI()` | pending |
| `testInstanceCreationShortcutForJumadaII` | `testInstanceCreationShortcutForJumadaII()` | pending |
| `testInstanceCreationShortcutForMuharram` | `testInstanceCreationShortcutForMuharram()` | pending |
| `testInstanceCreationShortcutForRabiI` | `testInstanceCreationShortcutForRabiI()` | pending |
| `testInstanceCreationShortcutForRabiII` | `testInstanceCreationShortcutForRabiII()` | pending |
| `testInstanceCreationShortcutForRajab` | `testInstanceCreationShortcutForRajab()` | pending |
| `testInstanceCreationShortcutForRamadan` | `testInstanceCreationShortcutForRamadan()` | pending |
| `testInstanceCreationShortcutForSafar` | `testInstanceCreationShortcutForSafar()` | pending |
| `testInstanceCreationShortcutForShaban` | `testInstanceCreationShortcutForShaban()` | pending |
| `testInstanceCreationShortcutForShawwal` | `testInstanceCreationShortcutForShawwal()` | pending |
| `testPrintString` | `testPrintString()` | pending |

</details>

<details><summary><code>IslamicDayOfWeekTest</code> → <code>org.chalten.islamic.IslamicDayOfWeekTest</code> (10 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAsISODayOfWeek` | `testAsISODayOfWeek()` | pending |
| `testAsIslamicDayOfWeek` | `testAsIslamicDayOfWeek()` | pending |
| `testInstanceCreation` | `testInstanceCreation()` | pending |
| `testNames` | `testNames()` | pending |
| `testNamesAreLocalized` | `testNamesAreLocalized()` | pending |
| `testNext` | `testNext()` | pending |
| `testPrevious` | `testPrevious()` | pending |
| `testPrintString` | `testPrintString()` | pending |
| `testShortNames` | `testShortNames()` | pending |
| `testShortNamesAreLocalized` | `testShortNamesAreLocalized()` | pending |

</details>

<details><summary><code>IslamicMonthOfYearTest</code> → <code>org.chalten.islamic.IslamicMonthOfYearTest</code> (11 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testBetweenAnd` | `testBetweenAnd()` | pending |
| `testComparing` | `testComparing()` | pending |
| `testCreation` | `testCreation()` | pending |
| `testCreationShortcut` | `testCreationShortcut()` | pending |
| `testDateCreation` | `testDateCreation()` | pending |
| `testGreaterThan` | `testGreaterThan()` | pending |
| `testLessThan` | `testLessThan()` | pending |
| `testNext` | `testNext()` | pending |
| `testNumberOfDays` | `testNumberOfDays()` | pending |
| `testPrevious` | `testPrevious()` | pending |
| `testPrintString` | `testPrintString()` | pending |

</details>

<details><summary><code>IslamicMonthTest</code> → <code>org.chalten.islamic.IslamicMonthTest</code> (18 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testComparing` | `testComparing()` | pending |
| `testDuAlHijjah` | `testDuAlHijjah()` | pending |
| `testDuAlQiDah` | `testDuAlQiDah()` | pending |
| `testInstanceCreation` | `testInstanceCreation()` | pending |
| `testJumadaAlAwwal` | `testJumadaAlAwwal()` | pending |
| `testJumadaAlThani` | `testJumadaAlThani()` | pending |
| `testMuharram` | `testMuharram()` | pending |
| `testNamesAreLocalized` | `testNamesAreLocalized()` | pending |
| `testNext` | `testNext()` | pending |
| `testPrevious` | `testPrevious()` | pending |
| `testPrintString` | `testPrintString()` | pending |
| `testRabiAlAwwal` | `testRabiAlAwwal()` | pending |
| `testRabiAlThani` | `testRabiAlThani()` | pending |
| `testRajab` | `testRajab()` | pending |
| `testRamadan` | `testRamadan()` | pending |
| `testSafar` | `testSafar()` | pending |
| `testShaban` | `testShaban()` | pending |
| `testShawwal` | `testShawwal()` | pending |

</details>

<details><summary><code>IslamicYearTest</code> → <code>org.chalten.islamic.IslamicYearTest</code> (14 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testBetweenAnd` | `testBetweenAnd()` | pending |
| `testComparing` | `testComparing()` | pending |
| `testGreaterThan` | `testGreaterThan()` | pending |
| `testInstanceCreation` | `testInstanceCreation()` | pending |
| `testInstanceCreationShortcut` | `testInstanceCreationShortcut()` | pending |
| `testInterval` | `testInterval()` | pending |
| `testIsLeapYear` | `testIsLeapYear()` | pending |
| `testLessThan` | `testLessThan()` | pending |
| `testNext` | `testNext()` | pending |
| `testNumberOfDays` | `testNumberOfDays()` | pending |
| `testNumberOfMonths` | `testNumberOfMonths()` | pending |
| `testPrevious` | `testPrevious()` | pending |
| `testPrintString` | `testPrintString()` | pending |
| `testYearsBetweenTwoYears` | `testYearsBetweenTwoYears()` | pending |

</details>

<details><summary><code>GregorianDateTimeTest (extension)</code> → <code>org.chalten.timezones.GregorianDateTimeTimezonesTest</code> (3 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testInTimeZone` | `testInTimeZone()` | pending |
| `testInTimeZoneWhenLocalTimeIsAmbiguous` | `testInTimeZoneWhenLocalTimeIsAmbiguous()` | pending |
| `testInTimeZoneWhenLocalTimeIsSkipped` | `testInTimeZoneWhenLocalTimeIsSkipped()` | pending |

</details>

<details><summary><code>IANATimeZoneDatabaseTest</code> → <code>org.chalten.timezones.IANATimeZoneDatabaseTest</code> (6 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testAt` | `testAt()` | pending |
| `testAtInvalidTimeZone` | `testAtInvalidTimeZone()` | pending |
| `testOffsetToUniversalCoordinatedTimeAt` | `testOffsetToUniversalCoordinatedTimeAt()` | pending |
| `testOffsetToUniversalCoordinatedTimeAtTransitionMoment` | `testOffsetToUniversalCoordinatedTimeAtTransitionMoment()` | pending |
| `testOffsetToUniversalCoordinatedTimeBeforeFirstTransition` | `testOffsetToUniversalCoordinatedTimeBeforeFirstTransition()` | pending |
| `testTimeZoneIdentifiers` | `testTimeZoneIdentifiers()` | pending |

</details>

<details><summary><code>TimeOffsetAwareGregorianDateTimeTest (extension)</code> → <code>org.chalten.timezones.TimeOffsetAwareGregorianDateTimeTimezonesTest</code> (2 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testInTimeZone` | `testInTimeZone()` | pending |
| `testInTimeZoneWhenLocalTimeIsAmbiguous` | `testInTimeZoneWhenLocalTimeIsAmbiguous()` | pending |

</details>

<details><summary><code>TimeZoneRulesetTest</code> → <code>org.chalten.timezones.TimeZoneRulesetTest</code> (4 tests, U4)</summary>

| SUnit selector | JUnit method | Status |
| --- | --- | --- |
| `testCantCreateWithInvalidNumberOfTransitions` | `testCantCreateWithInvalidNumberOfTransitions()` | pending |
| `testIdentifier` | `testIdentifier()` | pending |
| `testOffsetToUniversalCoordinatedTimeAt` | `testOffsetToUniversalCoordinatedTimeAt()` | pending |
| `testPrintString` | `testPrintString()` | pending |

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
