# Contributor's guide

## How to run tests

You can run specific tests, here is an example for the `core` module and the test `SerdeTest#testMaps`:

```sh
gradle :core:test --tests '*serde.SerdeTest.testMaps'
```

Note that since we are building a Multi-Release JAR, we have multiple java versions, each with their own set of tests.
For instance, the sources of the Java 17 tests are in `src/test/java17`.

To **run all the tests**, do:
```sh
gradle test-everything
```

The results will be in the build directory of each module, in a subdirectory per test task:
- Results of `core:test` in [`./core/build/reports/tests/test/index.html`](./core/build/reports/tests/test/index.html).
- Results of `core:java11Test` in [`./core/build/reports/tests/java11Test/index.html`](./core/build/reports/tests/java11Test/index.html).
- Results of `core:java17Test` in [`./core/build/reports/tests/java17Test/index.html`](./core/build/reports/tests/java17Test/index.html).
- ...

## How to generate code coverage reports

Run code coverage (here for the `core` module):
```sh
gradle :core:jacocoTestReport :core:java11JacocoTestReport :core:java17JacocoTestReport
```

The results will be in the build directory of each module, in a subdirectory per report task:
- Results of [`core:jacocoTestReport`](./core/build/reports/jacoco/test/html/index.html).
- Results of [`core:java11JacocoTestReport`](./core/build/reports/jacoco/java11JacocoTestReport/html/index.html).
- Results of [`core:java17JacocoTestReport`](./core/build/reports/jacoco/java17JacocoTestReport/html/index.html).
- ...

## How to check for backward-compatibility

1. Get the JAR files of the previous release and put them into [`japicmp-previous-version`](./japicmp-previous-version).
2. Run the API compatibility check with the `japicmp` task of the root project:

```sh
gradle japicmp
```

You will find the japicmp report in [`build/reports/japicmp.html`](./build/reports/japicmp.html).

If a compatibility breaks is detected, the task will fail with an error that looks like the following:

```
> Task :japicmp FAILED

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':japicmp'.
> A failure occurred while executing me.champeau.gradle.japicmp.JApiCmpWorkAction
   > Detected binary changes.
         - current: core-3.7.0-beta.jar, hocon-3.7.0-beta.jar, json-3.7.0-beta.jar, toml-3.7.0-beta.jar, yaml-3.7.0-beta.jar
         - baseline: core-3.6.7.jar, hocon-3.6.7.jar, json-3.6.7.jar, toml-3.6.7.jar, yaml-3.6.7.jar.

     See failure report at file:///home/guillaume/Documents/Projets/night-config/build/reports/japicmp.html
```