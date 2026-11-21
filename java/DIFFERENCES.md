<!-- markdownlint-disable MD013 MD033 -->
# Intentional behaviour differences: Smalltalk Chalten vs Java port

Every deviation of the Java port from the Smalltalk implementation is listed here with its reason. Anything not
listed is meant to behave identically; a difference found elsewhere is a bug.

## `chalten-measure` (U3)

| # | Area | Smalltalk (Chalten + Aconcagua v11) | Java | Reason |
| --- | --- | --- | --- | --- |
| M1 | Float amounts | `1.5 * day` holds a `Float`; `1.0 * day` is not an integer, so it prints `1.0 d` and formats as `P1.0D`. | There are no floating-point amounts: decimals are read exactly (`Fraction.parse("1.5")` is `3/2`, `"1.0"` is `1`). `1.0 * day` is `1 d` and formats as `P1D`. | Arithmetic must be exact. Formatting non-integer amounts still goes through `asFloat` printing, so every expected string of `ISO8601DurationFormatterTest` is unchanged. |
| M2 | `printString` of fractional quantities | A `Float` amount prints as a float (`1.5 h`); a `Fraction` amount prints as `(3/2) h`. | Every non-integer amount is a `Fraction` and prints as `(3/2) h`. | Consequence of M1. Integer amounts print identically (`365 d`, `12 mo`). |
| M3 | Quantity × quantity, quantity ÷ incommensurable quantity | Aconcagua builds derived units (`d·mo`, `d/mo`). | Not supported: `TimeQuantity` has no quantity product, and `dividedBy(TimeQuantity)` of incommensurable quantities throws `ArithmeticException("Cannot convert incommensurable units")`. Commensurable division returns the exact `Fraction` ratio, as in Aconcagua. | Chalten never uses derived time units. |
| M4 | Incommensurable sums | Aconcagua v11 raises `ArithmeticError` (`Cannot add incommensurable quantities`); the parser returns several periods as a collection. | `TimeQuantity.plus` raises the same `ArithmeticException`; several periods are kept in `CompoundTimeQuantity` (ordered `parts()`, merged `totals()`, equality by totals). Its `toString()` is `3 yr + 5 mo + 4 d`. | `CompoundTimeQuantity` is the typed Java replacement for the collection of periods (and the old `MeasureBag`); its printing is Java-only. |
| M5 | Interval indices | `DiscreteInterval at:` is one-based and raises `SubscriptOutOfBounds` (`Index out of range`). | `DiscreteInterval` is a `java.util.List`: `get(i)` is zero-based (`get(0)` is `at: 1`) and throws `IndexOutOfBoundsException("Index out of range")`. `indexOf` returns `-1` instead of evaluating an `ifAbsent:` block. | Java collection conventions. |
| M6 | Interval size limit | Unbounded (`count` is a `LargePositiveInteger` if needed). | Creation throws `ArithmeticException("Interval too large: n elements")` above `Integer.MAX_VALUE` elements. | `List.size()` is an `int`. |
| M7 | Interval steps | `DiscreteInterval` accepts any arithmetic objects (numbers, quantities). | Steps are `TimeQuantity`s and bounds implement `Steppable<T>` (`TimeQuantity`, dates, years). | Chalten only builds intervals of dates, years and quantities. |
| M8 | Unit lookup errors | `TimeUnits units >> #fortnight` raises `KeyNotFound`. | `TimeUnits.named("fortnight")` throws `IllegalArgumentException("key fortnight not found")`; `TimeUnits.find` returns an `Optional`. | Java exception idiom. |
| M9 | Localized messages | Error messages are `localized` through the image's current locale. | Messages are the English source strings. | `chalten-measure` has no localization; translated messages are U4's localization scope. |

## `chalten-core` (U2)

| # | Topic | Smalltalk | Java | Why |
| --- | --- | --- | --- | --- |
| C1 | Selector names | `GregorianMonth may name`, `printString`, `FixedOffsetRuleset for:`, `april first , 2025`, `DayOfWeek monday`. | `fullName()` (enums reserve `name()`), `toString()`, `FixedOffsetRuleset.of(...)` (`for` is a keyword), `april.first().of(2025)`, `DayOfWeek.MONDAY`. | Java naming rules. Every value and message is unchanged. |
| C2 | Missing selectors | `april thirtyFirst` raises `MessageNotUnderstood`. | `april.thirtyFirst()` throws `UnsupportedOperationException`. | Smalltalk only defines the ordinal selectors that fit each month; Java interfaces must declare them all. |
| C3 | Exception classes | `InstanceCreationFailed`, `ArithmeticError`. | `InstanceCreationFailedException`, `ArithmeticException`, with the same message texts. | Java exception idiom. |
| C4 | Day-of-month interning | `CalendarDayOfMonth` instances are interned, so equal days are identical. | `CalendarDayOfMonth` is a record compared by value. | Equality is what callers use; identity is not observable through the public API. |
| C5 | Abstract classes | `CalendarDate`, `CalendarYear`, `CalendarMonthOfYear`, `CalendarDateTime`, `CalendarMagnitude` are abstract superclasses. | Generic interfaces with default methods; Gregorian types are final classes. | Enums (`GregorianMonth`, `DayOfWeek`) cannot extend classes, and `sealed` across packages and modules (Hebrew, Islamic, timezones) would need JPMS. |
| C6 | Instant precision | `Instant` holds any quantity of microseconds. | Same (exact `TimeQuantity`); `toJavaInstant()`, `toLocalTime()` and `toOffsetDateTime()` truncate below the microsecond or second that `java.time` can hold. | `java.time` interop only; core arithmetic stays exact. |
| C7 | Float offsets in messages | `FixedOffsetRuleset for: -16.1 * hour` fails with `-16.1 h is out of range...`. | The message reads `(-161/10) h is out of range...`. | Consequence of M1/M2: decimals are exact fractions. |
| C8 | Localization hook | `NaturalLanguageTranslator` and `CurrentLocale` dynamic variables. | `org.chalten.localization.Translations` reads the shared `locales/` catalogs; `Translations.during(translator, action)` and `duringLanguage(language, action)` scope a translator. Locale-aware formatting is U4. | Thread-scoped equivalent of the Smalltalk dynamic variables. |

## Calendars, timezones and localization (U4)

| # | Topic | Smalltalk | Java | Why |
| --- | --- | --- | --- | --- |
| L1 | Style selectors | `localizedWith: [ :options \| options short ]`, `long`, `medium`, `full`. | `localized(FormatterOptions::shortStyle)`, `longStyle`, `mediumStyle`, `fullStyle`. | `short` and `long` are Java keywords. |
| L2 | Formatter dispatch | Each object answers `printLocalizedOn:using:` (double dispatch). | `LocalizedChronologyFormatter.format(Object)` switches on the object's type; other objects throw `IllegalArgumentException`. | Keeps the formatter in one class; the public `localized()` methods behave the same. |
| L3 | Pattern fields | `ChronologyPattern elements` holds symbols such as `#twoDigitDay`. | It holds `ChronologyPattern.Field` constants such as `TWO_DIGIT_DAY`, including `DATE` and `TIME` for `{1}` and `{0}`. | Java has no symbols. |
| L4 | Missing conventions | `ObjectNotFound` (`There's no <name> convention for <tag>`). | `NoSuchElementException` with the same message. | Java exception idiom. |
| L5 | Formatter constructors | `LocalizedChronologyFormatter for:`, `for:configuredBy:`, `for:in:configuredBy:`. | `forLanguage(tag)`, `forLanguage(tag, configurer)`, `forLanguage(tag, conventions, configurer)`. | `for` is a Java keyword. |
| L6 | Cross-calendar comparisons | Comparing a Hebrew or Islamic date with a Gregorian one raises `ArithmeticError` at run time. | It does not compile; the years still throw `ArithmeticException` (`Years in different time scales are incommensurable`). | Generic `Comparable` types. |
| L7 | Missing month selectors | `shawwal thirtieth`, `(duAlHijjah , 1443) thirtieth` and `cheshvan thirtieth` raise `MessageNotUnderstood`. | They throw `UnsupportedOperationException`. | Same as C2. |
| L8 | Ambiguous and skipped times | `AmbiguousTimeError` and `SkippedTimeError` are resumable: a handler can `resume:` with a chosen date-time. | `AmbiguousTimeException` and `SkippedTimeException` are unchecked exceptions; `TimeResolutionStrategy` (`strict`, `earlier`, `later`) picks the result instead of resuming. | Java exceptions cannot be resumed. |
| L9 | `java.time` interop of timezones | None. | `TimeZoneRuleset.toZoneId()`, `IANATimeZoneDatabase.at(ZoneId)`, `toZonedDateTime(...)`. The rules always come from the TZif files, never from the JDK's own tzdb. | Additive interop; the JDK and OS tzdata releases can differ. |
