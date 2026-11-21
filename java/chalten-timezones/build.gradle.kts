plugins {
    id("chalten.java-library")
}

description = "IANA time zone database parser and TimeZoneRuleset."

dependencies {
    api(project(":chalten-core"))
}
