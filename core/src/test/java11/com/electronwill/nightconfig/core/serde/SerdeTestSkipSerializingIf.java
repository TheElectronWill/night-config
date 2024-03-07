package com.electronwill.nightconfig.core.serde;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryFormat;
import com.electronwill.nightconfig.core.serde.annotations.*;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipSerializingIf.SkipIf;

public final class SerdeTestSkipSerializingIf {
	static class SkipIfNull {
		@SerdeSkipSerializingIf(SkipIf.IS_NULL)
		String name;
	}

	static class SkipIfEmpty {
		@SerdeSkipSerializingIf(SkipIf.IS_EMPTY)
		String name;
	}

	static class SkipCustomWrong {
		@SerdeSkipSerializingIf(SkipIf.CUSTOM) // error: missing parameters
		String name;
	}

	static class SkipIfCustomInObject1 {
		@SerdeSkipSerializingIf(value = SkipIf.CUSTOM, customCheck = "skipName")
		String name;

		@SuppressWarnings("unused")
		private boolean skipName(String name) {
			return name.equals("skip me");
		}
	}

	static class SkipIfCustomInObject2 {
		@SerdeSkipSerializingIf(value = SkipIf.CUSTOM, customCheck = "skipName")
		String name;

		static boolean skipName(String name) {
			return name.equals("skip me");
		}
	}

	static class SkipIfCustomInObject3 {
		@SerdeSkipSerializingIf(value = SkipIf.CUSTOM, customCheck = "skipPredicate")
		String name;

		transient Predicate<String> skipPredicate = name -> name.equals("skip me");

		@SuppressWarnings("unused")
		private boolean skipName(String name) {
			return name.equals("skip me");
		}
	}

	static class SkipIfCustomInAnotherClass1 {
		@SerdeSkipSerializingIf(value = SkipIf.CUSTOM, customClass = SkipPredicates.class, customCheck = "skipPredicate")
		String name;
	}

	static class SkipIfCustomInAnotherClass2 {
		@SerdeSkipSerializingIf(value = SkipIf.CUSTOM, customClass = SkipPredicates.class, customCheck = "skipPredicate")
		String name;
	}

	static class SkipPredicates {
		static boolean skipMethod(String name) {
			return name.equals("skip me");
		}

		static final Predicate<String> skipField = name -> name.equals("skip me");
	}

	// TODO the tests
}
