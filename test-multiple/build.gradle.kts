plugins {
    `java-library`
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
	api(project(":toml"))
	api(project(":json"))
    testImplementation(project(":test-shared"))
}

// Use Java 11.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

// Get JUnit5 version from `libs.versions.toml`.
val versionCatalog = extensions.getByType<VersionCatalogsExtension>().named("libs")
val junitVersion = versionCatalog.findVersion("junit5")
    .orElseThrow{ RuntimeException("missing version in libs.versions.toml: junit5") }
    .getRequiredVersion()

// Use JUnit5.
testing {
    suites {
		val test by getting(JvmTestSuite::class) {
			useJUnitJupiter(junitVersion)
		}
	}
}
