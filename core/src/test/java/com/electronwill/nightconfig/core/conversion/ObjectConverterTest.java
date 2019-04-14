package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.EnumGetMethod;
import com.electronwill.nightconfig.core.InMemoryFormat;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.TestEnum;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author TheElectronWill
 */
public class ObjectConverterTest {

	static final List<String> list1 = Arrays.asList("a", "b", "c");
	static final List<String> list2 = Collections.singletonList("element");
	static final List<UnmodifiableConfig> list3;
	static final List<List<UnmodifiableConfig>> nestedList2;
	static final List<List<List<UnmodifiableConfig>>> nestedList3;
	static {
		Config objRepr1 = Config.inMemory(); // object represented as a config
		objRepr1.set("a", "a");
		objRepr1.set("b", "b");

		Config objRepr2 = Config.inMemory(); // object represented as a config
		objRepr2.set("a", "A");
		objRepr2.set("b", "B");

		list3 = Arrays.asList(objRepr1, objRepr2);
		nestedList2 = Arrays.asList(list3, list3);
		nestedList3 = Arrays.asList(nestedList2, nestedList2);
	}

	static final Config config1 = Config.inMemoryUniversal();
	static final Config config2 = Config.inMemory();

	private final Config config = Config.inMemory();
	{
		config.set("parentValue", "parent");
		config.set("integer", 1234568790);
		config.set("decimal", Math.PI);
		config.set("string", "value");
		config.set("stringList", list1);
		config.set("objList", list3);
		config.set("nestedObjList2", nestedList2);
		config.set("nestedObjList3", nestedList3);
		config.set("config", config1);
        config.set("enumValue", "A");
		config.set("subObject.parentValue", "subParent");
		config.set("subObject.integer", -1);
		config.set("subObject.decimal", 0.5);
		config.set("subObject.string", "Hey!");
		config.set("subObject.stringList", list2);
		config.set("subObject.objList", list3);
		config.set("subObject.config", config2);
		config.set("subObject.subObject", null);
		config.set("subObject.nestedObjList2", null);
		config.set("subObject.nestedObjList3", null);
        config.set("subObject.enumValue", 2);
	}

	@Test
	public void configToObjectToConfigToObject() throws Exception {
		ObjectConverter converter = new ObjectConverter();
		MyObject object = converter.toObject(config, MyObject::new);
		Config newConfig = Config.inMemory();
		converter.toConfig(object, newConfig);

		MyObject newObject = new MyObject();
		converter.toObject(newConfig, newObject);

		System.out.println("Original config: " + config);
		System.out.println("New config: " + newConfig);
		System.out.println("\nOriginal object: " + object);
		System.out.println("New object: " + object);

        // test the enum values
        assertEquals(TestEnum.A, config.getEnum("enumValue", TestEnum.class));
        assertEquals(TestEnum.C, config.getEnum("subObject.enumValue", TestEnum.class, EnumGetMethod.ORDINAL_OR_NAME));

        // Replace string and integer by enum values in order to compare the configs properly
        // (the object converter should have put enum values in the new config)
        config.set("enumValue", TestEnum.A);
        config.set("subObject.enumValue", TestEnum.C);

        // ensure that the conversion was well done
        assertEquals(config, newConfig, "Invalid conversion");
		assertEquals(object, newObject, "Invalid conversion");
	}

	@Test
	public void configToObject() throws Exception {
		System.out.println("====== Test with non-final fields ======");
		{
			MyObject object = new MyObject();
			testConfigToObject(config, object);// does the mapping
			assertSame(config.get("parentValue"), object.parentValue);
			assertEquals((int)config.get("integer"), object.integer);
			assertEquals((double)config.get("decimal"), object.decimal);
            assertEquals(config.getEnum("enumValue", TestEnum.class), object.enumValue);
			assertSame(config.get("string"), object.string);
			assertSame(list1, object.stringList);
			assertSame(config1, object.config);
			assertNotNull(object.subObject);
			assertEquals((int)config.get("subObject.integer"), object.subObject.integer);
			assertEquals((double)config.get("subObject.decimal"), object.subObject.decimal);
			assertSame(config.get("subObject.string"), object.subObject.string);
			assertSame(list2, object.subObject.stringList);
			assertSame(config2, object.subObject.config);
			assertNull(object.subObject.subObject);
            assertEquals(config.getEnum("subObject.enumValue", TestEnum.class, EnumGetMethod.ORDINAL_OR_NAME), object.subObject.enumValue);

			assertTrue(object.nestedObjList3 instanceof LinkedList);
			assertTrue(object.nestedObjList3.get(0) instanceof LinkedList);
		}

		System.out.println();
		System.out.println("====== Test with final fields ======");
		{
			MyObjectFinal object = new MyObjectFinal();
			testConfigToObject(config, object);//does the mapping
			assertSame(config.get("parentValue"), object.parentValue);
			assertEquals((int)config.get("integer"), object.integer);
			assertEquals((double)config.get("decimal"), object.decimal);
			assertEquals(config.getEnum("enumValue", TestEnum.class), object.enumValue);
			assertSame(config.get("string"), object.string);
			assertSame(list1, object.stringList);
			assertSame(config1, object.config);
			assertNotNull(object.subObject);
			assertEquals((int)config.get("subObject.integer"), object.subObject.integer);
			assertEquals((double)config.get("subObject.decimal"), object.subObject.decimal);
			assertSame(config.get("subObject.string"), object.subObject.string);
			assertSame(list2, object.subObject.stringList);
			assertSame(config2, object.subObject.config);
			assertNull(object.subObject.subObject);
			assertEquals(config.getEnum("subObject.enumValue", TestEnum.class, EnumGetMethod.ORDINAL_OR_NAME),object.subObject.enumValue);
		}
	}

	private void testConfigToObject(Config config, Object object) throws Exception {
		System.out.println("Before: " + object);

		ObjectConverter converter = new ObjectConverter();
		converter.toObject(config, object);

		System.out.println("After: " + object);
	}

	private static class MyParent {
		String parentValue;
	}

	private static class MyObject extends MyParent {
		int integer;
		double decimal;
		String string;
		List<String> stringList;
		List<SomeObject> objList;
		List<List<SomeObject>> nestedObjList2;
		LinkedList<LinkedList<Collection<SomeObject>>> nestedObjList3;
		Config config;
		MyObject subObject;
        @SpecEnum(method=EnumGetMethod.ORDINAL_OR_NAME)
        TestEnum enumValue;

		@Override
		public String toString() {
			return "MyObject{"
					+ "parentValue="
					+ parentValue
					+ ", integer="
					+ integer
					+ ", decimal="
					+ decimal
					+ ", string='"
					+ string
					+ '\''
					+ ", stringList="
					+ stringList
					+ ", objList="
					+ objList
					+ ", nestedObjList2="
					+ nestedObjList2
					+ ", nestedObjList3="
					+ nestedObjList3
					+ ", config="
					+ config
					+ ", subObject="
					+ subObject
					+ ", enumValue="
					+ enumValue
					+ '}';
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			MyObject myObject = (MyObject)o;
			return integer == myObject.integer
                && Double.compare(myObject.decimal, decimal) == 0
				&& Objects.equals(string, myObject.string)
                && Objects.equals(stringList, myObject.stringList)
                && Objects.equals(objList, myObject.objList)
                && Objects.equals(nestedObjList2, myObject.nestedObjList2)
                && Objects.equals(nestedObjList3, myObject.nestedObjList3)
                && Objects.equals(config, myObject.config)
                && Objects.equals(subObject, myObject.subObject)
                && Objects.equals(enumValue, myObject.enumValue);
		}

		@Override
		public int hashCode() {
			return Objects.hash(integer, decimal, string, stringList,
				objList, nestedObjList2, nestedObjList3, config, subObject);
		}
	}

	private static class SomeObject {
		String a;
		String b;

		public SomeObject() {
			this(null, null);
		}

		public SomeObject(String a, String b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return String.format("SO(%s, %s)", a, b);
		}

		@Override
		public boolean equals(Object o) {
			if (this == o)
				return true;
			if (o == null || getClass() != o.getClass())
				return false;
			SomeObject that = (SomeObject)o;
			return Objects.equals(a, that.a) && Objects.equals(b, that.b);
		}

		@Override
		public int hashCode() {
			return Objects.hash(a, b);
		}
	}

	static class MyObjectFinal extends MyParent {
		final int integer;
		final double decimal;
		final String string;
		final List<String> stringList;
		final List<SomeObject> objList;
		final List<List<SomeObject>> nestedObjList2;
		final List<List<List<SomeObject>>> nestedObjList3;
		final Config config;
		final MyObjectFinal subObject;
		@SpecEnum(method=EnumGetMethod.ORDINAL_OR_NAME)
		final TestEnum enumValue;

		public MyObjectFinal() {
			this(123, 1.23, "v", null, null, null, null, null, null, null);
		}

		/*
		Not necessary for the mapper to work, but necessary to prevent the compiler from inlining the use
		of the primitive fields. This allows us to print the changes correctly.
		 */
		public MyObjectFinal(int integer, double decimal, String string, List<String> stringList,
								List<SomeObject> objList,
								List<List<SomeObject>> nestedObjList2,
								List<List<List<SomeObject>>> nestedObjList3,
								Config config,
								MyObjectFinal subObject,
								TestEnum enumValue) {
			this.integer = integer;
			this.decimal = decimal;
			this.string = string;
			this.stringList = stringList;
			this.objList = objList;
			this.nestedObjList2 = nestedObjList2;
			this.nestedObjList3 = nestedObjList3;
			this.config = config;
			this.subObject = subObject;
			this.enumValue = enumValue;
		}

		@Override
		public String toString() {
			return "MyObjectFinal{"
					+ "integer="
					+ integer
					+ ", decimal="
					+ decimal
					+ ", string='"
					+ string
					+ '\''
					+ ", stringList="
					+ stringList
					+ ", objList="
					+ objList
					+ ", nestedObjList2="
					+ nestedObjList2
					+ ", nestedObjList3="
					+ nestedObjList3
					+ ", config="
					+ config
					+ ", subObject="
					+ subObject
					+ ", enumValue="
					+ enumValue
					+ '}';
		}
	}
}