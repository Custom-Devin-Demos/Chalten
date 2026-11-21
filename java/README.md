<!-- markdownlint-disable MD013 -->

# Chalten for Java

Java 21 port of the Smalltalk Chalten time model in [`../source`](../source). The Smalltalk sources stay the
reference implementation; [PLAN.md](PLAN.md) describes the port, its class mapping and the test-parity matrix.

## Requirements

- A JDK to run Gradle (17 or newer). The build compiles and tests with a Java 21 toolchain: Gradle uses an
  installed JDK 21 or downloads one through the Foojay toolchain resolver.
- Nothing else: the Gradle wrapper (`./gradlew`) downloads Gradle itself.

## Build and test

Run every command from this `java/` directory.

```sh
./gradlew build                        # compile, run all tests, per-module and aggregated JaCoCo reports
./gradlew test                         # tests only
./gradlew :chalten-core:test           # one module
./gradlew :chalten-core:test --tests 'org.chalten.core.CoreSmokeTest'
./gradlew testCodeCoverageReport       # aggregated coverage report only
```

| Output | Location |
| --- | --- |
| Test reports (HTML) | `<module>/build/reports/tests/test/index.html` |
| Test results (JUnit XML) | `<module>/build/test-results/test/` |
| Module coverage (HTML, XML) | `<module>/build/reports/jacoco/test/` |
| Aggregated coverage (HTML, XML) | `build/reports/jacoco/testCodeCoverageReport/` |

`python3 scripts/coverage-summary.py .` prints the Markdown test and coverage table that CI writes to the job summary.

## Modules

| Module | Packages | Depends on |
| --- | --- | --- |
| `chalten-measure` | `org.chalten.measure` | – |
| `chalten-core` | `org.chalten.core`, `org.chalten.localization` | `chalten-measure` |
| `chalten-timezones` | `org.chalten.timezones` | `chalten-core` |
| `chalten-hebrew` | `org.chalten.hebrew` | `chalten-core` |
| `chalten-islamic` | `org.chalten.islamic` | `chalten-core` |
| `chalten-testkit` | `org.chalten.testkit` | `chalten-core`, JUnit Jupiter |

Every module gets the same conventions from `build-logic` (`chalten.java-library` plugin): Java 21 toolchain,
JUnit 5 and jqwik on the test classpath (plus AssertJ and `chalten-testkit`), and a JaCoCo report after `test`.
Library versions live in [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Locales

`chalten-core` packages the repo-root [`../locales`](../locales) directory, shared with the Smalltalk
implementation, as classpath resources under `locales/` (for example `/locales/chronology.json`). The files
are read in place at build time and never copied into `java/`.

## Continuous integration

[`.github/workflows/java.yml`](../.github/workflows/java.yml) runs `./gradlew build` on Java 21 for every push and
pull request touching `java/**` or the workflow, uploads the `test-reports` and `jacoco-reports` artifacts and
writes the coverage table to the job summary.
