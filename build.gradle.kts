tasks.register("test-everything") {
	description = "Runs the tests of every subproject, for all Java versions."
	for (subproject in subprojects) {
		val testTasks = subproject.tasks.withType(Test::class)
		dependsOn(testTasks)
	}
}
