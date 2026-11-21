plugins {
    id("chalten.java-library")
}

description = "Shared JUnit 5 test support (port of Chalten-SUnit) used as a test dependency by every module."

dependencies {
    api(project(":chalten-core"))
    api(platform(libs.junit.bom))
    api(libs.junit.jupiter)
}
