plugins {
    `java-library`
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
	// api(project(":core"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}
