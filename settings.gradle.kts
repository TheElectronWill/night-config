plugins {
    // Apply the foojay-resolver plugin to allow automatic download of JDKs
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.7.0"
}

rootProject.name = "night-config"
include("core")
include("hocon")
include("json")
include("toml")
include("yaml")

include("test-shared")
