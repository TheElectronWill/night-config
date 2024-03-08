package com.electronwill.nightconfig.core.serde;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.serde.annotations.*;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipDeserializingIf.SkipDeIf;

public final class SerdeTestSkipDeserializingIf {
	private <T> T deserialize(Config conf, Supplier<T> sup) {
		return ObjectDeserializer.builder().build().deserializeFields(conf, sup);
	}

	static class SkipIfMissing {
		@SerdeSkipDeserializingIf(SkipDeIf.IS_MISSING)
		String name = "preserved";
	}

	@Test
	public void skipIfMissing() {
		var emptyConf = Config.inMemory();
		assertEquals("preserved", deserialize(emptyConf, SkipIfMissing::new).name);

		var conf = Config.inMemory();
		conf.set("name", "n");
		assertEquals("n", deserialize(conf, SkipIfMissing::new).name);
	}

	static class SkipIfNull {
		@SerdeSkipDeserializingIf(SkipDeIf.IS_NULL)
		String name = "preserved";
	}

	@Test
	public void skipIfNull() {
		var emptyConf = Config.inMemory();
		assertThrows(SerdeException.class, () -> deserialize(emptyConf, SkipIfNull::new));

		var nullConf = Config.inMemory();
		nullConf.set("name", null);
		assertEquals("preserved", deserialize(nullConf, SkipIfNull::new).name);

		var conf = Config.inMemory();
		conf.set("name", "n");
		assertEquals("n", deserialize(conf, SkipIfNull::new).name);
	}

	static class SkipIfEmpty {
		@SerdeSkipDeserializingIf(SkipDeIf.IS_EMPTY)
		String name = "preserved";
	}

	@Test
	public void skipIfEmpty() {
		Supplier<SkipIfEmpty> p = SkipIfEmpty::new;
		var emptyConf = Config.inMemory();
		assertThrows(SerdeException.class, () -> deserialize(emptyConf, p));

		var nullConf = Config.inMemory();
		nullConf.set("name", null);
		assertEquals(null, deserialize(nullConf, p).name);

		var eConf = Config.inMemory();
		eConf.set("name", ""); // empty string
		assertEquals("preserved", deserialize(eConf, p).name);

		var conf = Config.inMemory();
		conf.set("name", "n");
		assertEquals("n", deserialize(conf, p).name);
	}

	static class SkipIfEmptyOrNull {
		@SerdeSkipDeserializingIf({ SkipDeIf.IS_NULL, SkipDeIf.IS_EMPTY })
		String name = "preserved";
	}

	@Test
	public void skipIfEmptyOrNull() {
		Supplier<SkipIfEmptyOrNull> p = SkipIfEmptyOrNull::new;

		var emptyConf = Config.inMemory();
		assertThrows(SerdeException.class, () -> deserialize(emptyConf, p));

		var nullConf = Config.inMemory();
		nullConf.set("name", null);
		assertEquals("preserved", deserialize(nullConf, p).name);

		var eConf = Config.inMemory();
		eConf.set("name", ""); // empty string
		assertEquals("preserved", deserialize(eConf, p).name);

		var conf = Config.inMemory();
		conf.set("name", "n");
		assertEquals("n", deserialize(conf, p).name);
	}

	static class SkipCustomWrong {
		@SerdeSkipDeserializingIf(SkipDeIf.CUSTOM) // error: missing parameters
		String name;
	}

	@Test
	public void customWrong() {
		assertThrowsExactly(SerdeException.class, () -> {
			deserialize(Config.inMemory(), SkipCustomWrong::new);
		});
	}

	static class SkipIfCustomInObject1 {
		@SerdeSkipDeserializingIf(value = SkipDeIf.CUSTOM, customCheck = "skipName")
		String name = "preserved";

		@SuppressWarnings("unused")
		private boolean skipName(Object name) {
			return name != null && name.equals("skip me");
		}
	}

	static class SkipIfCustomInObject2 {
		@SerdeSkipDeserializingIf(value = SkipDeIf.CUSTOM, customCheck = "skipName()")
		String name = "preserved";

		static boolean skipName(Object name) {
			return name != null && name.equals("skip me");
		}
	}

	static class SkipIfCustomInObject3 {
		@SerdeSkipDeserializingIf(value = SkipDeIf.CUSTOM, customCheck = "skipPredicate")
		String name = "preserved";

		transient Predicate<Object> skipPredicate = name -> name != null && name.equals("skip me");
	}

	private void testSkipMe(Supplier<?> p) throws Exception {
		var emptyConf = Config.inMemory();
		Field nameField = p.get().getClass().getDeclaredField("name");
		assertThrows(SerdeException.class, () -> deserialize(emptyConf, p));

		var nullConf = Config.inMemory();
		nullConf.set("name", null);
		assertEquals(null, nameField.get(deserialize(nullConf, p)));

		var dontSkipConf = Config.inMemory();
		dontSkipConf.set("name", "don't skip me"); // empty string
		assertEquals("don't skip me",  nameField.get(deserialize(dontSkipConf, p)));

		var skipConf = Config.inMemory();
		skipConf.set("name", "skip me");
		assertEquals("preserved", nameField.get(deserialize(skipConf, p)));
	}

	@Test
	public void customInObject() throws Exception {
		testSkipMe(SkipIfCustomInObject1::new);
		testSkipMe(SkipIfCustomInObject2::new);
		testSkipMe(SkipIfCustomInObject3::new);
	}

	static class SkipIfCustomInAnotherClass1 {
		@SerdeSkipDeserializingIf(value = SkipDeIf.CUSTOM, customClass = SkipPredicates.class, customCheck = "skipMethod")
		String name = "preserved";
	}

	static class SkipIfCustomInAnotherClass2 {
		@SerdeSkipDeserializingIf(value = SkipDeIf.CUSTOM, customClass = SkipPredicates.class, customCheck = "skipField")
		String name = "preserved";
	}

	static class SkipPredicates {
		static boolean skipMethod(Object name) {
			return name != null && name.equals("skip me");
		}

		static final Predicate<Object> skipField = name -> name != null && name.equals("skip me");
	}

	@Test
	public void customInAnotherClass() throws Exception {
		testSkipMe(SkipIfCustomInAnotherClass1::new);
		testSkipMe(SkipIfCustomInAnotherClass2::new);
	}
}
