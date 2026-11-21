#!/usr/bin/env python3
"""Print a Markdown test and JaCoCo coverage summary for the Gradle build (used for the GitHub job summary)."""

import pathlib
import sys
import xml.etree.ElementTree as ET

ROOT = pathlib.Path(sys.argv[1] if len(sys.argv) > 1 else ".").resolve()
COUNTERS = ("INSTRUCTION", "BRANCH", "LINE", "METHOD", "CLASS")


def counters(report):
    root = ET.parse(report).getroot()
    return {c.get("type"): (int(c.get("covered")), int(c.get("missed"))) for c in root.findall("counter")}


def percent(values, kind):
    covered, missed = values.get(kind, (0, 0))
    total = covered + missed
    return f"{100 * covered / total:.1f}% ({covered}/{total})" if total else "–"


def tests(module):
    totals = [0, 0, 0, 0]
    for report in sorted((module / "build" / "test-results" / "test").glob("TEST-*.xml")):
        suite = ET.parse(report).getroot()
        for i, key in enumerate(("tests", "failures", "errors", "skipped")):
            totals[i] += int(suite.get(key, 0))
    return totals


modules = sorted(p for p in ROOT.iterdir() if p.is_dir() and p.name.startswith("chalten-") and (p / "build.gradle.kts").exists())
print("## Java build: tests and coverage\n")
print("| Module | Tests | Failed | Skipped | " + " | ".join(k.title() for k in COUNTERS) + " |")
print("| --- | --- | --- | --- | " + " | ".join("---" for _ in COUNTERS) + " |")
for module in modules:
    run, failures, errors, skipped = tests(module)
    report = module / "build" / "reports" / "jacoco" / "test" / "jacocoTestReport.xml"
    values = counters(report) if report.exists() else {}
    cells = [percent(values, k) for k in COUNTERS] if values else ["no classes"] + ["–"] * (len(COUNTERS) - 1)
    print(f"| `{module.name}` | {run} | {failures + errors} | {skipped} | " + " | ".join(cells) + " |")

aggregate = ROOT / "build" / "reports" / "jacoco" / "testCodeCoverageReport" / "testCodeCoverageReport.xml"
if aggregate.exists():
    values = counters(aggregate)
    print("| **all modules** | | | | " + " | ".join(f"**{percent(values, k)}**" for k in COUNTERS) + " |")
else:
    print("\nAggregated JaCoCo report not found.")
