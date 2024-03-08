package com.electronwill.nightconfig.core.serde;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.serde.annotations.*;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipSerializingIf.SkipSerIf;

public final class SerdeTestSkipSerializingIf {
	private CommentedConfig serialize(Object o) {
		return ObjectSerializer.builder().build().serializeFields(o, CommentedConfig::inMemory);
	}

	static class SkipIfNull {
		@SerdeSkipSerializingIf(SkipSerIf.IS_NULL)
		String name;
	}

	@Test
	public void skipIfNull() {
		var emptyConf = Config.inMemory();
		assertEquals(emptyConf, serialize(new SkipIfNull()));

		var conf = Config.inMemory();
		conf.set("name", "n");
		var s = new SkipIfNull();
		s.name = "n";
		assertEquals(conf, serialize(s));
	}

	static class SkipIfEmpty {
		@SerdeSkipSerializingIf(SkipSerIf.IS_EMPTY)
		String name;
	}

	@Test
	public void skipIfEmpty() {
		var nullS = new SkipIfEmpty();
		var nullConf = Config.inMemory();
		nullConf.set("name", null);
		assertEquals(nullConf, serialize(nullS));

		var emptyConf = Config.inMemory();
		var emptyS = new SkipIfEmpty();
		emptyS.name = ""; // empty string
		assertEquals(emptyConf, serialize(emptyS)); // should produce empty conf

		var conf = Config.inMemory();
		conf.set("name", "n");
		var s = new SkipIfEmpty();
		s.name = "n";
		assertEquals(conf, serialize(s));
	}

	static class SkipIfEmptyOrNull {
		@SerdeSkipSerializingIf({ SkipSerIf.IS_NULL, SkipSerIf.IS_EMPTY })
		String name;
	}

	@Test
	public void skipIfEmptyOrNull() {
		var emptyConf = Config.inMemory();

		var nullS = new SkipIfEmptyOrNull();
		assertEquals(emptyConf, serialize(nullS));

		var emptyS = new SkipIfEmptyOrNull();
		emptyS.name = ""; // empty string
		assertEquals(emptyConf, serialize(emptyS)); // should produce empty conf

		var conf = Config.inMemory();
		conf.set("name", "n");
		var s = new SkipIfEmptyOrNull();
		s.name = "n";
		assertEquals(conf, serialize(s));
	}

	static class SkipCustomWrong {
		@SerdeSkipSerializingIf(SkipSerIf.CUSTOM) // error: missing parameters
		String name;
	}

	@Test
	public void customWrong() {
		assertThrowsExactly(SerdeException.class, () -> {
			serialize(new SkipCustomWrong());
		});
	}

	static class SkipIfCustomInObject1 {
		@SerdeSkipSerializingIf(value = SkipSerIf.CUSTOM, customCheck = "skipName")
		String name;

		@SuppressWarnings("unused")
		private boolean skipName(String name) {
			return name != null && name.equals("skip me");
		}
	}

	static class SkipIfCustomInObject2 {
		@SerdeSkipSerializingIf(value = SkipSerIf.CUSTOM, customCheck = "skipName()")
		String name;

		static boolean skipName(String name) {
			return name != null && name.equals("skip me");
		}
	}

	static class SkipIfCustomInObject3 {
		@SerdeSkipSerializingIf(value = SkipSerIf.CUSTOM, customCheck = "skipPredicate")
		String name;

		transient Predicate<String> skipPredicate = name -> name != null && name.equals("skip me");
	}

	private void testSkipMe(Supplier<?> sup) throws Exception {
		Object o = sup.get();
		Field nameField = o.getClass().getDeclaredField("name");

		nameField.set(o, "don't skip me!");
		Config res = serialize(o);
		assertEquals("don't skip me!", res.get("name"));

		nameField.set(o, null);
		assertNull(serialize(o).get("name"));
		assertFalse(serialize(o).isEmpty());

		nameField.set(o, "skip me");
		assertTrue(serialize(o).isEmpty());
	}

	@Test
	public void customInObject() throws Exception {
		testSkipMe(SkipIfCustomInObject1::new);
		testSkipMe(SkipIfCustomInObject2::new);
		testSkipMe(SkipIfCustomInObject3::new);
	}

	static class SkipIfCustomInAnotherClass1 {
		@SerdeSkipSerializingIf(value = SkipSerIf.CUSTOM, customClass = SkipPredicates.class, customCheck = "skipMethod")
		String name;
	}

	static class SkipIfCustomInAnotherClass2 {
		@SerdeSkipSerializingIf(value = SkipSerIf.CUSTOM, customClass = SkipPredicates.class, customCheck = "skipField")
		String name;
	}

	static class SkipPredicates {
		static boolean skipMethod(String name) {
			return name != null && name.equals("skip me");
		}

		static final Predicate<String> skipField = name -> name != null && name.equals("skip me");
	}

	@Test
	public void customInAnotherClass() throws Exception {
		testSkipMe(SkipIfCustomInAnotherClass1::new);
		testSkipMe(SkipIfCustomInAnotherClass2::new);
	}
}
