plugins {
    id("chalten.java-library")
}

description = "Gregorian time model, absolute time, offsets, ISO 8601 and localization."

dependencies {
    api(project(":chalten-measure"))
}

// The repo-root locales/ directory is shared with the Smalltalk implementation: read it in place, never copy it.
tasks.named<ProcessResources>("processResources") {
    from(rootProject.layout.projectDirectory.dir("../locales")) {
        into("locales")
    }
}

// MigrationGuideTest checks java/MIGRATION.md against MigrationGuideExamplesTest and the public API.
tasks.named<Test>("test") {
    val migrationGuide = rootProject.layout.projectDirectory.file("MIGRATION.md")
    inputs.file(migrationGuide).withPropertyName("migrationGuide").withPathSensitivity(PathSensitivity.RELATIVE)
    systemProperty("chalten.migrationGuide", migrationGuide.asFile.path)
    systemProperty(
        "chalten.migrationGuideExamples",
        layout.projectDirectory.file("src/test/java/org/chalten/core/MigrationGuideExamplesTest.java").asFile.path,
    )
}
