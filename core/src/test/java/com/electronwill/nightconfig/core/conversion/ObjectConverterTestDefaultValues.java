package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.EnumGetMethod;
import com.electronwill.nightconfig.core.TestEnum;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class ObjectConverterTestDefaultValues {

	@Test
	public void toObjectHavingDefaultValues() throws Exception {

		TestConfig testConfig = new TestConfig();
		new ObjectConverter().toObject(Config.inMemory(), testConfig);

		Assertions.assertEquals(AnnotationUtils.getDefaultValue(TestConfig.class.getField("db")), testConfig.db);
		Assertions.assertEquals(AnnotationUtils.getDefaultValue(TestConfig.class.getField("dd")), testConfig.dd);
		Assertions.assertEquals(AnnotationUtils.getDefaultValue(TestConfig.class.getField("di")), testConfig.di);
		Assertions.assertEquals(AnnotationUtils.getDefaultValue(TestConfig.class.getField("dl")), testConfig.dl);
		Assertions.assertEquals(AnnotationUtils.getDefaultValue(TestConfig.class.getField("ds")), testConfig.ds);
		Assertions.assertNotNull(testConfig.dv);
		Assertions.assertEquals("str", testConfig.dv.getStr());
	}

	@Test
	public void toConfigHavingDefaultValues() throws Exception {

		TestConfig testConfig = new TestConfig();
		Config config = Config.inMemory();
		new ObjectConverter().toConfig(testConfig, config);

		Assertions.assertEquals(AnnotationUtils.getDefaultValue(TestConfig.class.getField("db")), config.get("DB"));
		Assertions.assertEquals(AnnotationUtils.getDefaultValue(TestConfig.class.getField("dd")), config.get("DD"));
		Assertions.assertEquals(AnnotationUtils.getDefaultValue(TestConfig.class.getField("di")), config.get("DI"));
		Assertions.assertEquals(AnnotationUtils.getDefaultValue(TestConfig.class.getField("dl")), config.get("DL"));
		Assertions.assertEquals(AnnotationUtils.getDefaultValue(TestConfig.class.getField("ds")), config.get("DS"));
		Assertions.assertNotNull(config.get("DV"));
	}


	public static class TestConfig {

		@Path("DB")
		@DefaultBoolean(true)
		public Boolean db;
		@Path("DD")
		@DefaultDouble(2D)
		public Double dd;
		@Path("DI")
		@DefaultInt(2)
		public Integer di;
		@Path("DL")
		@DefaultLong(3L)
		public Long dl;
		@Path("DS")
		@DefaultString("test")
		public String ds;

		@Path("DV")
		@DefaultValue(TODVF.class)
		public TestObject dv;
	}

	public static class TestObject {
		public final String str;

		public TestObject(String str) {
			this.str = str;
		}

		public String getStr() {
			return str;
		}
	}

	public static class TODVF implements DefaultValueFactory<TestObject> {

		@Override
		public TestObject defaultValue() {
			return new TestObject("str");
		}
	}
}