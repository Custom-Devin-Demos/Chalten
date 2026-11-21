plugins {
    id("chalten.java-library")
}

description = "Shared JUnit 5 test support (port of Chalten-SUnit) used as a test dependency by every module."

dependencies {
    api(project(":chalten-core"))
    api(platform(libs.junit.bom))
    api(libs.junit.jupiter)
    api(libs.jqwik)
}

tasks.test {
    val repository = rootDir.parentFile
    systemProperty("chalten.repository", repository.absolutePath)
    inputs.files(fileTree(repository.resolve("source")) { include("**/*Test*.st") })
        .withPropertyName("sunitSources")
        .withPathSensitivity(PathSensitivity.RELATIVE)
    inputs.file(rootDir.resolve("PLAN.md")).withPropertyName("plan").withPathSensitivity(PathSensitivity.RELATIVE)
}
