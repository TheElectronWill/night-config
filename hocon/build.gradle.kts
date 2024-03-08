plugins {
    id("night-config-lib")
	id("me.champeau.mrjar") version "0.1.1"
}

multiRelease {
    targetVersions(8, 11, 17)
}

dependencies {
	implementation(project(":core"))
	implementation(libs.typesafeConfig)

	testImplementation(project(":test-shared"))
}
