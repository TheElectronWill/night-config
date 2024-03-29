plugins {
    id("night-config-lib")
    id("me.champeau.mrjar") version "0.1.1"
}

// Use the build script defined in buildSrc

dependencies {
	compileOnly("org.jetbrains:annotations:+")
    testImplementation(project(":test-shared"))
}

// Build a multi-release JAR with the following java versions
// (the first version is the base one, with sources located in src/java)
multiRelease {
    targetVersions(8, 11, 17)
}

