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
