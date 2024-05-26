import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.DynamicContainer.dynamicContainer;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

import org.junit.jupiter.api.*;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileNotFoundAction;
import com.electronwill.nightconfig.core.io.ParsingException;
import com.electronwill.nightconfig.json.JsonParser;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;

/**
 * Executes standard toml tests from https://github.com/toml-lang/toml-test.
 * <p>
 * The toml-test repository is cloned as a submodule, in
 * {@code src/test/resources/toml-test}.
 * Is contains two types of tests:
 * <ul>
 * <li>Invalid tests: TOML files that are invalid, the TOML parser should throw
 * an exception.</li>
 * <li>Valid tests: TOML files that are valid, the TOML parser should parse
 * them, and the result should
 * match the corresponding JSON file (same name, different suffix).</li>
 *
 * Among invalid tests are "multi" tests that must be expanded before testing.
 * Each line of "multi" tests should be parsed as one (invalid) TOML document.
 * </ul>
 */
public class StandardTomlTests {

	// Assumption: the current working directory is the gradle project dir
	// "test-multiple".
	private static final Path TOML_TEST_DIR = Path.of("src/test/resources/toml-test/tests").toAbsolutePath();
	private static final Path TEST_LIST_1_0 = TOML_TEST_DIR.resolve("files-toml-1.0.0");
	private static final Path TEST_LIST_1_1 = TOML_TEST_DIR.resolve("files-toml-1.1.0");

	private static final List<String> PARSER_BLACKLIST = List.of(
    	"invalid/inline-table/duplicate-key-3.toml",
    	"invalid/inline-table/overwrite-08.toml",
    	"invalid/array/tables-1.toml",
    	"invalid/spec/inline-table-2-0.toml",
    	"invalid/table/append-to-array-with-dotted-keys.toml",
    	"invalid/table/append-with-dotted-keys-1.toml",
    	"invalid/table/append-with-dotted-keys-2.toml",
    	"invalid/table/redefine-2.toml",
    	"invalid/table/super-twice.toml",
    	"invalid/array/extend-defined-aot.toml",
    	"invalid/control/bare-cr.toml",
    	"invalid/control/multi-cr.toml",
    	"invalid/control/rawmulti-cd.toml"
	);

	/**
	 * Standard valid and invalid tests (including "multi" tests) for the
	 * TomlParser, generated from the files in the Git submodule.
	 */
	@TestFactory
	public List<? extends DynamicNode> testParser() throws IOException {
		var validTests = new ArrayList<DynamicTest>();
		var invalidTests = new ArrayList<DynamicTest>();

		for (String testPath : Files.readAllLines(TEST_LIST_1_0)) {
			if (testPath.startsWith("invalid/")) {
				var testFile = TOML_TEST_DIR.resolve(testPath);
				var testFileName = testFile.getFileName().toString();
				var relativePath = TOML_TEST_DIR.relativize(testFile);

				if (PARSER_BLACKLIST.contains(relativePath.toString())) {
					System.err.println("skipping test " + relativePath);
					continue; // skip this test
				}

				if (testFileName.endsWith(".toml")) {
					// Regular TOML test.
					invalidTests.add(dynamicTest(relativePath.toString(), () -> {
						TomlParser parser = new TomlParser();
						assertThrows(ParsingException.class, () -> {
							parser.parse(testFile, FileNotFoundAction.THROW_ERROR);
						}, () -> String.format("invalid file '%s' should have been rejected by the parser",
								relativePath));
					}));

				} else if (testFileName.endsWith(".multi")) {
					invalidTests.add(dynamicTest(relativePath.toString(), () -> {
						TomlParser parser = new TomlParser();

						// "Multi" TOML test that contains multiple invalid key-value pairs.
						for (var line : Files.readAllLines(testFile)) {
							// skip blank lines and comments
							if (!(line.isBlank() || line.stripLeading().startsWith("#"))) {
								// we have found a key-value pair, extract the key to give a name to the test
								var key = line.substring(0, line.indexOf('=')).strip();
								var testName = relativePath + "(" + key + ")";

								System.out.println("testing " + testName);
								assertThrows(ParsingException.class, () -> {
									parser.parse(line);
								}, () -> String.format("invalid test '%s' should have failed", testName));
							}
						}
					}));
				}
			} else if (testPath.startsWith("valid/")) {
				var testFile = TOML_TEST_DIR.resolve(testPath);
				var testFileName = testFile.getFileName().toString();
				var relativePath = TOML_TEST_DIR.relativize(testFile);

				if (testFileName.endsWith(".toml")) {
					// Regular TOML test + JSON file containing the expected result.
					var expectFile = testFile.resolveSibling(testFileName.replace(".toml", ".json"));

					validTests.add(dynamicTest(relativePath.toString(), () -> {
						TomlParser tomlParser = new TomlParser();
						JsonParser jsonParser = new JsonParser();

						try {
							CommentedConfig parsed = tomlParser.parse(testFile, FileNotFoundAction.THROW_ERROR);
							Config expected = jsonParser.parse(expectFile, FileNotFoundAction.THROW_ERROR);
							assertConfigMatchesJsonExpectation(parsed, expected, relativePath.toString());
						} catch (Exception ex) {
							fail("Exception occured in test " + relativePath, ex);
						}
					}));

				}
			}
		}

		var allTests = Arrays.asList(dynamicContainer("parser valid", validTests),
				dynamicContainer("parser invalid", invalidTests));
		return allTests;
	}

	/**
	 * Standard tests for the TomlWriter, generated from the files in the Git
	 * submodule.
	 */
	@TestFactory
	public List<? extends DynamicNode> testWriter() throws IOException {
		var validTests = new ArrayList<DynamicTest>();

		for (String testPath : Files.readAllLines(TEST_LIST_1_0)) {
			if (testPath.startsWith("valid/")) {
				var testFile = TOML_TEST_DIR.resolve(testPath);
				var testFileName = testFile.getFileName().toString();
				var relativePath = TOML_TEST_DIR.relativize(testFile);

				if (testFileName.endsWith(".json")) {
					// JSON file specifying the config + TOML file containing the expected result.
					var jsonFile = testFile;
					var tomlFile = testFile.resolveSibling(testFileName.replace(".json", ".toml"));

					validTests.add(dynamicTest(relativePath.toString(), () -> {
						TomlParser tomlParser = new TomlParser();
						TomlWriter tomlWriter = new TomlWriter();
						JsonParser jsonParser = new JsonParser();

						try {
							Config config = parseJsonExpectationToConfig(jsonParser.parse(jsonFile, FileNotFoundAction.THROW_ERROR));
							String written = tomlWriter.writeToString(config);

							try {
								CommentedConfig writtenParsed = tomlParser.parse(written);
								CommentedConfig expected = tomlParser.parse(tomlFile, FileNotFoundAction.THROW_ERROR);
								assertEquals(expected, writtenParsed, String.format("Invalid output for test %s:\n%s", relativePath, written));
							} catch (Exception ex) {
								fail("Exception occured while parsing serialization of:\n" + config + "\nwhich has been written as:\n"
										+ written, ex);
							}

						} catch (Exception ex) {
							fail("Exception occured while serializing test " + relativePath, ex);
						}

					}));
				}
			}
		}
		var allTests = Arrays.asList(dynamicContainer("writer valid", validTests));
		return allTests;
	}

	private Config parseJsonExpectationToConfig(Config jsonExpect) {
		return (Config) convertJsonExpectValue(jsonExpect, List.of());
	}

	@SuppressWarnings("unchecked")
	private Object convertJsonExpectValue(Object jsonExpect, List<String> key) {
		if (jsonExpect instanceof List) {
			var jsonList = (List<Object>) jsonExpect;
			var res = new ArrayList<>(jsonList.size());
			for (var element : jsonList) {
				res.add(convertJsonExpectValue(element, key));
			}
			return res;
		} else if (jsonExpect instanceof Config) {
			var jsonConfig = (Config) jsonExpect;
			if (jsonConfig.contains("type") && jsonConfig.contains("value")
					&& jsonConfig.get("type") instanceof String) {
				// specification of a toml value: {"type": the_type, "value": string_of_value}.
				String valueType = jsonConfig.get("type");
				String valueStr = jsonConfig.get("value");
				switch (valueType) {
					case "float": {
						switch (valueStr) {
							case "+nan":
							case "-nan":
							case "nan": {
								return Double.NaN;
							}
							case "+inf":
							case "inf": {
								return Double.POSITIVE_INFINITY;
							}
							case "-inf": {
								return Double.NEGATIVE_INFINITY;
							}
							default: {
								return Double.parseDouble(valueStr);
							}
						}
					}
					case "bool": {
						return Boolean.parseBoolean(valueStr);
					}
					case "integer": {
						return Long.parseLong(valueStr);
					}
					case "string": {
						return valueStr;
					}
					case "datetime": {
						return OffsetDateTime.parse(valueStr, FMT_DATETIME);
					}
					case "datetime-local": {
						return LocalDateTime.parse(valueStr, FMT_DATETIME_LOCAL);
					}
					case "date-local": {
						return LocalDate.parse(valueStr, FMT_DATE_LOCAL);
					}
					case "time-local": {
						return LocalTime.parse(valueStr, FMT_TIME_LOCAL);
					}
					default: {
						fail(String.format("Unknown specified value type '%s' for key %s in JSON file", valueType,
								key));
						return null;
					}
				}
			} else {
				// the value is a config
				Config tomlConfig = TomlFormat.newConfig();

				for (var jsonEntry : jsonConfig.entrySet()) {
					var jsonEntryKey = Collections.singletonList(jsonEntry.getKey());
					var fullKey = new ArrayList<>(key);
					fullKey.add(jsonEntry.getKey());
					var jsonEntryValue = convertJsonExpectValue(jsonEntry.getValue(), fullKey);
					tomlConfig.set(jsonEntryKey, jsonEntryValue);
				}

				return tomlConfig;
			}
		} else {
			fail(String.format("Invalid specified value '%s' for key %s in JSON file", jsonExpect, key));
			return null;
		}
	}

	private void assertConfigMatchesJsonExpectation(CommentedConfig tomlConfig, Config jsonExpect, String testName) {
		// We cannot compare the configurations directly, because the expectation
		// contains entries of the form {"type": the_type, "value": string_of_value}.
		tomlConfig.clearComments(); // JSON has no comments, ignore them
		assertMatchJsonExpectValue(List.of(), tomlConfig, jsonExpect, String.format("Valid test %s failed", testName));
	}

	private static DateTimeFormatter FMT_DATETIME = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
	private static DateTimeFormatter FMT_DATETIME_LOCAL = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
	private static DateTimeFormatter FMT_DATE_LOCAL = DateTimeFormatter.ISO_LOCAL_DATE;
	private static DateTimeFormatter FMT_TIME_LOCAL = DateTimeFormatter.ISO_LOCAL_TIME;

	@SuppressWarnings("unchecked")
	private void assertMatchJsonExpectValue(List<String> key, Object tomlValue, Object jsonExpectValue, String msg) {
		if (jsonExpectValue instanceof List) {
			assertInstanceOf(List.class, tomlValue,
					String.format("%s: invalid toml value for key %s: expected list", msg, key));
			List<Object> jsonList = (List<Object>) jsonExpectValue;
			List<Object> tomlList = (List<Object>) tomlValue;
			assertEquals(jsonList.size(), tomlList.size(), String
					.format("%s: invalid toml value for key %s: expected list of size %d", msg, key, jsonList.size()));
			for (int i = 0; i < tomlList.size(); i++) {
				var tomlElement = tomlList.get(i);
				var jsonElement = jsonList.get(i);
				assertMatchJsonExpectValue(key, tomlElement, jsonElement,
						String.format("%s: invalid element %d of key %s", msg, i, key));
			}
		} else if (jsonExpectValue instanceof Config) {
			Config jsonConfig = (Config) jsonExpectValue;
			if (jsonConfig.contains("type") && jsonConfig.contains("value")
					&& jsonConfig.get("type") instanceof String) {
				// Expect value that matches the structure:
				// {"type": value_type, "value": value_string}
				var expectedType = jsonConfig.<String>get("type").toLowerCase();
				var expectedValueStr = jsonConfig.<String>get("value");
				switch (expectedType) {
					case "float": {
						if (expectedValueStr.equals("+nan") || expectedValueStr.equals("-nan")
								|| expectedValueStr.equals("nan")) {
							String err = String.format("%s: invalid value for key %s: expected NaN float, got %s", msg,
									key, tomlValue);
							if (tomlValue instanceof Double) {
								assertTrue(Double.isNaN((double) tomlValue), err);
							} else if (tomlValue instanceof Float) {
								assertTrue(Float.isNaN((float) tomlValue), err);
							} else {
								fail(err);
							}
						} else if (expectedValueStr.equals("inf") || expectedValueStr.equals("+inf")) {
							expectedValueStr = "Infinity";
						} else if (expectedValueStr.equals("-inf")) {
							expectedValueStr = "-Infinity";
						} else {
							var expectedFloat = Double.parseDouble(expectedValueStr);
							assertInstanceOf(Number.class, tomlValue,
									String.format("%s: invalid value for key %s, expected float", msg, key));
							assertEquals(expectedFloat, ((Number) tomlValue).doubleValue(),
									String.format("%s: invalid value for key %s, expected float", msg, key));
						}
						break;
					}
					case "bool": {
						var expectedBool = Boolean.parseBoolean(String.valueOf(expectedValueStr));
						assertEquals(expectedBool, tomlValue,
								String.format("%s: invalid value for key %s, expected bool", msg, key));
						break;
					}
					case "integer": {
						var expectedInt = Long.parseLong(String.valueOf(expectedValueStr));
						assertInstanceOf(Number.class, tomlValue,
								String.format("%s: invalid value for key %s, expected integer", msg, key));
						assertEquals(expectedInt, ((Number) tomlValue).longValue(),
								String.format("%s: invalid value for key %s, expected integer", msg, key));
						break;
					}
					case "string": {
						var expectedString = expectedValueStr;
						assertInstanceOf(String.class, tomlValue,
								String.format("%s: invalid value for key %s, expected string", msg, key));
						assertEquals(expectedString, tomlValue,
								String.format("%s: invalid value for key %s, expected string", msg, key));
						break;
					}
					case "datetime": {
						var expectedDateTime = OffsetDateTime.parse(expectedValueStr, FMT_DATETIME);
						assertEquals(expectedDateTime, tomlValue,
								String.format("%s: invalid value for key %s, expected datetime", msg, key));
						break;
					}
					case "datetime-local": {
						var expectedDateTime = LocalDateTime.parse(expectedValueStr, FMT_DATETIME_LOCAL);
						assertEquals(expectedDateTime, tomlValue,
								String.format("%s: invalid value for key %s, expected datetime-local", msg, key));
						break;
					}
					case "date-local": {
						var expectedDateTime = LocalDate.parse(expectedValueStr, FMT_DATE_LOCAL);
						assertEquals(expectedDateTime, tomlValue,
								String.format("%s: invalid value for key %s, expected date-local", msg, key));
						break;
					}
					case "time-local": {
						var expectedDateTime = LocalTime.parse(expectedValueStr, FMT_TIME_LOCAL);
						assertEquals(expectedDateTime, tomlValue,
								String.format("%s: invalid value for key %s, expected time-local", msg, key));
						break;
					}
					default: {
						fail(String.format("Unknown expected value type '%s' for key '%s' in JSON file", expectedType,
								key));
					}
				}
			} else {
				// expect config
				assertInstanceOf(Config.class, tomlValue,
						String.format("%s: invalid toml value for key %s: expected config", msg, key));
				Config tomlConfig = (Config) tomlValue;
				assertEquals(jsonConfig.size(), tomlConfig.size(), String.format(
						"%s: invalid toml value for key %s: expected config of size %d", msg, key, jsonConfig.size()));
				for (var jsonEntry : jsonConfig.entrySet()) {
					var jsonEntryValue = jsonEntry.getRawValue();
					var tomlEntryValue = tomlConfig.getRaw(Collections.singletonList(jsonEntry.getKey()));
					var fullKey = new ArrayList<>(key);
					fullKey.add(jsonEntry.getKey());
					assertMatchJsonExpectValue(fullKey, tomlEntryValue, jsonEntryValue, msg);
				}
			}
		} else {
			fail(String.format("Invalid expectation in JSON file: %s should be a list of config", key));
		}
	}

}
