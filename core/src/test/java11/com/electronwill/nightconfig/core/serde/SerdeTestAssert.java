package com.electronwill.nightconfig.core.serde;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.serde.annotations.*;
import com.electronwill.nightconfig.core.serde.annotations.SerdeAssert.AssertThat;

public final class SerdeTestAssert {
	private static <T> T deserialize(Config conf, Supplier<T> sup) {
		return ObjectDeserializer.builder().build().deserializeFields(conf, sup);
	}

	private static CommentedConfig serialize(Object o) {
		return ObjectSerializer.builder().build().serializeFields(o, CommentedConfig::inMemory);
	}

	static class AssertNotNull {
		@SerdeAssert(AssertThat.NOT_NULL)
		String name;
	}

	@Test
	public void assertNotNullDeserialize() {
		// Missing value => error (not because of @SerdeAssert)
		var emptyConf = Config.inMemory();
		assertThrows(SerdeException.class, () -> deserialize(emptyConf, AssertNotNull::new));

		// Null value => error because of @SerdeAssert
		var nullConf = Config.inMemory();
		nullConf.set("name", null);
		assertThrows(SerdeAssertException.class, () -> deserialize(nullConf, AssertNotNull::new));

		// Not null => ok
		var conf = Config.inMemory();
		conf.set("name", "n");
		assertEquals("n", deserialize(conf, AssertNotNull::new).name);
	}

	@Test
	public void assertNotNullSerialize() {
		// Null value => error because of @SerdeAssert
		var nullObj = new AssertNotNull();
		assertThrows(SerdeAssertException.class, () -> serialize(nullObj));

		// Not null => ok
		var obj = new AssertNotNull();
		obj.name = "n";
		assertEquals("n", serialize(obj).get("name"));
	}

	static class AssertNotEmpty {
		@SerdeAssert(AssertThat.NOT_EMPTY)
		String name = "not empty";
	}

	@Test
	public void assertNotEmptyDeserialize() {
		// Missing value => error (not because of @SerdeAssert)
		var missingConf = Config.inMemory();
		assertThrows(SerdeException.class, () -> deserialize(missingConf, AssertNotEmpty::new));

		// Null value => ok
		var conf = Config.inMemory();
		conf.set("name", null);
		assertEquals(null, deserialize(conf, AssertNotEmpty::new).name);

		// Empty value => error because of @SerdeAssert
		var emptyValConf = Config.inMemory();
		emptyValConf.set("name", ""); // empty string
		assertThrows(SerdeAssertException.class, () -> deserialize(emptyValConf, AssertNotEmpty::new));

		// Not empty => ok
		conf = Config.inMemory();
		conf.set("name", "n");
		assertEquals("n", deserialize(conf, AssertNotEmpty::new).name);
	}

	@Test
	public void assertNotEmptySerialize() {
		// Null value => ok
		var nullObj = new AssertNotEmpty();
		nullObj.name = null;
		assertNull(serialize(nullObj).get("name"));

		// Empty value => error
		var emptyValObj = new AssertNotEmpty();
		emptyValObj.name = ""; // empty string
		assertThrows(SerdeAssertException.class, () -> serialize(emptyValObj));

		// Not null => ok
		var obj = new AssertNotEmpty();
		obj.name = "n";
		assertEquals("n", serialize(obj).get("name"));
	}

	static class AssertNotEmptyNotNull {
		@SerdeAssert({ AssertThat.NOT_NULL, AssertThat.NOT_EMPTY })
		String name;
	}

	@Test
	public void assertNotEmptyNotNullDeserialize() {
		// Missing value => error (not because of @SerdeAssert)
		var emptyConf = Config.inMemory();
		assertThrows(SerdeException.class, () -> deserialize(emptyConf, AssertNotEmptyNotNull::new));

		// Null value => error because of @SerdeAssert
		var nullConf = Config.inMemory();
		nullConf.set("name", null);
		assertThrows(SerdeAssertException.class, () -> deserialize(nullConf, AssertNotEmptyNotNull::new));

		// Empty value => error because of @SerdeAssert
		var emptyValConf = Config.inMemory();
		emptyValConf.set("name", ""); // empty string
		assertThrows(SerdeAssertException.class, () -> deserialize(emptyValConf, AssertNotEmptyNotNull::new));

		// Not empty => ok
		var notEmptyConf = Config.inMemory();
		notEmptyConf.set("name", "n");
		assertEquals("n", deserialize(notEmptyConf, AssertNotEmptyNotNull::new).name);
	}

	@Test
	public void assertNotEmptyNotNullSerialize() {
		// Null value => error
		var nullObj = new AssertNotEmptyNotNull();
		nullObj.name = null;
		assertThrows(SerdeAssertException.class, () -> serialize(nullObj));

		// Empty value => error
		var obj = new AssertNotEmptyNotNull();
		obj.name = ""; // empty string
		assertThrows(SerdeAssertException.class, () -> serialize(obj));

		// Not null => ok
		var nnObj = new AssertNotEmptyNotNull();
		nnObj.name = "n";
		assertEquals("n", serialize(nnObj).get("name"));
	}

	static class AssertWrong {
		@SerdeAssert(AssertThat.CUSTOM) // error: missing parameters
		String name;
	}

	static class AssertWrong2 {
		@SerdeAssert(value = AssertThat.NOT_NULL, customCheck = "wrong") // error: invalid customCheck
		String name;
	}

	@Test
	public void customWrong() {
		assertThrowsExactly(SerdeException.class, () -> {
			deserialize(Config.inMemory(), AssertWrong::new);
		});
		assertThrowsExactly(SerdeException.class, () -> {
			serialize(new AssertWrong());
		});

		assertThrowsExactly(SerdeException.class, () -> {
			deserialize(Config.inMemory(), AssertWrong2::new);
		});
		assertThrowsExactly(SerdeException.class, () -> {
			serialize(new AssertWrong2());
		});
	}

	static class AssertCustomInObject1 {
		@SerdeAssert(value = { AssertThat.NOT_NULL, AssertThat.CUSTOM }, customCheck = "isLongEnough")
		String name = "cat";

		@SuppressWarnings("unused")
		private boolean isLongEnough(String name) {
			return name.length() > 4;
		}
	}

	static class AssertCustomInObject2 {
		@SerdeAssert(value = { AssertThat.NOT_NULL, AssertThat.CUSTOM }, customCheck = "isLongEnough")
		String name = "cat";

		@SuppressWarnings("unused")
		private static boolean isLongEnough(String name) {
			return name.length() > 4;
		}
	}

	static class AssertCustomInObject3 {
		@SerdeAssert(value = { AssertThat.NOT_NULL, AssertThat.CUSTOM }, customCheck = "isLongEnough")
		String name = "cat";

		transient Predicate<String> isLongEnough = name -> name.length() > 4;
	}

	static class AssertPredicates {
		static boolean assertMethod(String name) {
			return name.length() > 4;
		}

		static transient Predicate<String> assertField = name -> name.length() > 4;
	}

	static class AssertCustomInAnotherClass1 {
		@SerdeAssert(value = { AssertThat.NOT_NULL,
				AssertThat.CUSTOM }, customClass = AssertPredicates.class, customCheck = "assertMethod")
		String name = "cat";
	}

	static class AssertCustomInAnotherClass2 {
		@SerdeAssert(value = { AssertThat.NOT_NULL,
				AssertThat.CUSTOM }, customClass = AssertPredicates.class, customCheck = "assertField")
		String name = "cat";
	}

	private void testAssertMe(Supplier<?> p) throws Exception {
		Field nameField = p.get().getClass().getDeclaredField("name");

		// deserialization
		var badConf = Config.inMemory();
		badConf.set("name", "cat");
		assertThrows(SerdeAssertException.class, () -> deserialize(badConf, p));
		// serialization
		assertThrows(SerdeAssertException.class, () -> serialize(p.get()));

		// deserialization
		var nullConf = Config.inMemory();
		nullConf.set("name", null);
		assertThrows(SerdeAssertException.class, () -> deserialize(nullConf, p));
		// serialization
		var nullObj = p.get();
		nameField.set(nullObj, null);
		assertThrows(SerdeAssertException.class, () -> serialize(nullObj));

		// deserialization
		var okConf = Config.inMemory();
		okConf.set("name", "tacocat"); // not null and long enough
		assertEquals("tacocat", nameField.get(deserialize(okConf, p)));
		// serialization
		var okObj = p.get();
		nameField.set(okObj, "tacocat");
		assertEquals("tacocat", serialize(okObj).get("name"));
	}

	@Test
	public void customInObject() throws Exception {
		testAssertMe(AssertCustomInObject1::new);
		testAssertMe(AssertCustomInObject2::new);
		testAssertMe(AssertCustomInObject3::new);
	}

	@Test
	public void customInAnotherClass() throws Exception {
		testAssertMe(AssertCustomInAnotherClass1::new);
		testAssertMe(AssertCustomInAnotherClass2::new);
	}
}
