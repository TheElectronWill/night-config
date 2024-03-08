plugins {
    `java-library`
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
	// implementation(project(":core"))
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(8)
    }
}
