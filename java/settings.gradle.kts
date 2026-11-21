pluginManagement {
    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
    }
}

rootProject.name = "chalten"

include(
    "chalten-measure",
    "chalten-core",
    "chalten-timezones",
    "chalten-hebrew",
    "chalten-islamic",
    "chalten-testkit",
)
