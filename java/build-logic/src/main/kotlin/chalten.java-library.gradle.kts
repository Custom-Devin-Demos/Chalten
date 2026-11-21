plugins {
    `java-library`
    jacoco
}

val libs = the<VersionCatalogsExtension>().named("libs")

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

jacoco {
    toolVersion = libs.findVersion("jacoco").get().requiredVersion
}

dependencies {
    testImplementation(platform(libs.findLibrary("junit-bom").get()))
    testImplementation(libs.findLibrary("junit-jupiter").get())
    testImplementation(libs.findLibrary("jqwik").get())
    testImplementation(libs.findLibrary("assertj-core").get())
    testRuntimeOnly(libs.findLibrary("junit-platform-launcher").get())
    if (project.path != ":chalten-testkit") {
        testImplementation(project(":chalten-testkit"))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
    options.release = 21
    options.compilerArgs.addAll(listOf("-parameters", "-Xlint:all"))
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform {
        includeEngines("junit-jupiter", "jqwik")
    }
    systemProperty("jqwik.database", layout.buildDirectory.file("jqwik-database").get().asFile.path)
    testLogging {
        events("failed", "skipped")
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
    finalizedBy(tasks.named("jacocoTestReport"))
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.named("test"))
    reports {
        xml.required = true
        html.required = true
    }
}

tasks.named("check") {
    dependsOn(tasks.named("jacocoTestReport"))
}

// The compiled JUnit classes, for the root project's SUnit → JUnit test-parity check.
configurations.consumable("testParityClassesElements") {
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named("chalten-test-parity-classes"))
    }
    outgoing.artifact(tasks.named<JavaCompile>("compileTestJava").flatMap { it.destinationDirectory }) {
        type = ArtifactTypeDefinition.DIRECTORY_TYPE
    }
}
