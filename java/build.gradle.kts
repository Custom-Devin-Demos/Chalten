plugins {
    base
    `jacoco-report-aggregation`
}

val chaltenModules = listOf(
    ":chalten-measure",
    ":chalten-core",
    ":chalten-timezones",
    ":chalten-hebrew",
    ":chalten-islamic",
    ":chalten-testkit",
)

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

dependencies {
    chaltenModules.forEach { jacocoAggregation(project(it)) }
}

reporting {
    reports {
        register<JacocoCoverageReport>("testCodeCoverageReport") {
            testSuiteName = "test"
        }
    }
}

tasks.named("check") {
    dependsOn(tasks.named("testCodeCoverageReport"))
}
