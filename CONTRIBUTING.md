# Contributor's guide

## Gradle tips

Run specific tests, here is an example for the `core` module and the test `SerdeTest#testMaps`:

```sh
gradle core:test --tests '*serde.SerdeTest.testMaps'
```

- The results will be [here](./core/build/reports/tests/test/index.html).

Run code coverage:
```sh
gradle tests-all:testCodeCoverageReport
```

- The results will be [here](./tests-all/build/reports/jacoco/testCodeCoverageReport/html/index.html).

Run API compatibility check:
```sh
gradle tests-all:japicmp
```

- The results will be [here](./tests-all/build/reports/japicmp.html).
