import me.champeau.gradle.japicmp.JapicmpTask

plugins {
	id("me.champeau.gradle.japicmp") version "0.4.2"
}

repositories {
	mavenCentral()
}

tasks.register("test-everything") {
	description = "Runs the tests of every subproject, for all Java versions."
	for (subproject in subprojects) {
		val testTasks = subproject.tasks.withType(Test::class)
		dependsOn(testTasks)
	}
}

tasks.register<JapicmpTask>("japicmp") {
	val previousJars = fileTree("${rootProject.projectDir}/japicmp-previous-version").filter { it.extension == "jar" }
	val newJars = files(
		project(":core").tasks.named("jar"),
		project(":hocon").tasks.named("jar"),
		project(":json").tasks.named("jar"),
		project(":toml").tasks.named("jar"),
		project(":yaml").tasks.named("jar"),
	)
	oldClasspath.from(previousJars)
	newClasspath.from(newJars)
	htmlOutputFile = layout.buildDirectory.file("reports/japicmp.html")
	onlyBinaryIncompatibleModified = true
	failOnSourceIncompatibility = true

	// Backward-compatible changes are OK.
	// See https://github.com/siom79/japicmp/blob/master/japicmp/src/main/java/japicmp/model/JApiCompatibilityChange.java
	val excludedChanges = japicmp.model.JApiCompatibilityChange.values().filter {
		it.isSourceCompatible() && it.isBinaryCompatible()
	}.map {
		it.name
	} + listOf("METHOD_NEW_DEFAULT")

	val excludedClasses = listOf(
		"com.electronwill.nightconfig.core.file.FileConfigBuilder",
		"com.electronwill.nightconfig.core.file.CommentedFileConfigBuilder",
	)
	logger.info("japicmp excluded changes: $excludedChanges")
	logger.info("japicmp excluded classes: $excludedClasses")
	compatibilityChangeExcludes = excludedChanges
	classExcludes = excludedClasses
}
