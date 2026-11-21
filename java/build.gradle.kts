plugins {
    base
    `jvm-toolchains`
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

// SUnit → JUnit 5 test parity (PLAN.md §8): every SUnit selector under source/ needs a same-named JUnit method.
val testParity = configurations.dependencyScope("testParity")
val testParityClassesScope = configurations.dependencyScope("testParityClassesScope")
val testParityRuntime = configurations.resolvable("testParityRuntime") {
    extendsFrom(testParity.get())
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
        attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.LIBRARY))
        attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements.JAR))
        attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        attribute(TargetJvmEnvironment.TARGET_JVM_ENVIRONMENT_ATTRIBUTE, objects.named(TargetJvmEnvironment.STANDARD_JVM))
    }
}
val testParityClasses = configurations.resolvable("testParityClasses") {
    extendsFrom(testParityClassesScope.get())
    isTransitive = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("chalten-test-parity-classes"))
    }
}

dependencies {
    chaltenModules.forEach {
        testParity(project(it))
        testParityClassesScope(project(it))
    }
    testParity(platform(libs.junit.bom))
    testParity(libs.junit.jupiter)
    testParity(libs.jqwik)
    testParity(libs.assertj.core)
}

val repositoryRoot: File = rootDir.parentFile
val testParityReport = layout.buildDirectory.file("reports/test-parity/test-parity.md")

fun JavaExec.testParityTask() {
    group = "verification"
    classpath = files(testParityClasses, testParityRuntime)
    mainClass = "org.chalten.testkit.parity.TestParityChecker"
    javaLauncher = javaToolchains.launcherFor {
        languageVersion = JavaLanguageVersion.of(21)
    }
    inputs.files(fileTree(repositoryRoot.resolve("source")) { include("**/*Test*.st") })
        .withPropertyName("sunitSources")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file("test-parity-pending.txt").withPropertyName("pendingList").withPathSensitivity(PathSensitivity.RELATIVE)
    args(repositoryRoot.absolutePath, testParityReport.get().asFile.absolutePath)
}

val checkTestParity by tasks.registering(JavaExec::class) {
    description = "Fails if an SUnit test under source/ has no same-named JUnit 5 test (PLAN.md §8)."
    testParityTask()
    inputs.file("PLAN.md").withPropertyName("plan").withPathSensitivity(PathSensitivity.RELATIVE)
    outputs.file(testParityReport)
}

tasks.register<JavaExec>("updateTestParityStatus") {
    description = "Rewrites the per-test Status column of PLAN.md §8 from the compiled JUnit tests."
    testParityTask()
    args("--update-plan")
    outputs.upToDateWhen { false }
}

tasks.named("check") {
    dependsOn(checkTestParity)
}
