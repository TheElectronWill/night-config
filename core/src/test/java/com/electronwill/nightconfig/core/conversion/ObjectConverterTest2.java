package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.InMemoryFormat;
import com.electronwill.nightconfig.core.utils.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author TheElectronWill
 */
public class ObjectConverterTest2 {

	@Test
	public void testSupportBasic() throws Exception {
		ObjectConverter converter = new ObjectConverter();
		Config config = Config.inMemory();
		MyObject object = new MyObject();
		converter.toConfig(object, config);

		System.out.println("MyObject mapped to a SimpleConfig with basic strategy:");
		System.out.println(config);

		assert config.<Integer>get("integer") == object.integer;
		assert config.<Double>get("decimal") == object.decimal;
		assert config.<String>get("string") == object.string;
		assert config.<List<String>>get("stringList") == object.stringList;
		assert config.<List<Config>>get("objList").size() == object.objList.size();
		assert config.<Config>get("config") == object.config;
		assert config.get("subObject") instanceof Config;
		Config sub = config.get("subObject");
		assert sub.<Integer>get("integer") == 1234567890;
		assert sub.<Double>get("decimal") == Math.PI;
		assert sub.<String>get("string").equals("value");
		assert sub.<List<?>>get("stringList").equals(Arrays.asList("a", "b", "c"));
		assert config.<List<?>>get("objList").equals(convertedObjects);
		assert sub.<Config>get("config").size() == 0;
		assert sub.contains("subObject");
		assert sub.get("subObject") == null;
	}

	@Test
	public void testSupportAll() throws Exception {
		ObjectConverter converter = new ObjectConverter();
		Config config = InMemoryFormat.withUniversalSupport().createConfig();
		MyObject object = new MyObject();
		converter.toConfig(object, config);

		System.out.println("MyObject mapped to a SimpleConfig with support_all strategy:");
		System.out.println(config);

		assert config.<Integer>get("integer") == object.integer;
		assert config.<Double>get("decimal") == object.decimal;
		assert config.<String>get("string") == object.string;
		assert config.<List<String>>get("stringList") == object.stringList;

		assert config.<List<SomeObject>>get("objList").equals(objects);
		// withUniversalSupport() causes the configuration to accept values of any type

		assert config.<Config>get("config") == object.config;
		assert config.get("subObject") == object.subObject;
		assert config.get("infos.coordinates").equals(object.coords.toString());
	}

	private static List<SomeObject> objects = Arrays.asList(new SomeObject("a", "b"), new SomeObject("A", "B"));
	private static final List<Config> convertedObjects;
	static {
		Config objRepr1 = Config.inMemory(); // object represented as a config
		objRepr1.set("a", "a");
		objRepr1.set("b", "b");

		Config objRepr2 = Config.inMemory(); // object represented as a config
		objRepr2.set("a", "A");
		objRepr2.set("b", "B");

		convertedObjects = Arrays.asList(objRepr1, objRepr2);
	}

	private static class MyObject {
		int integer = 1234567890;
		double decimal = Math.PI;
		String string = "value";
		List<String> stringList = Arrays.asList("a", "b", "c");
		List<SomeObject> objList = objects;
		Config config = InMemoryFormat.withUniversalSupport().createConfig();
		MyObject subObject;

		@Conversion(CoordinatesConverter.class)
		@Path("infos.coordinates")
		@SpecNotNull
		Coordinates coords = new Coordinates(1, 2, 3);

		public MyObject(MyObject subObject) {
			this.subObject = subObject;
			//empty config
		}

		public MyObject() {
			this.config.set("a.b.c", "configValue");
			this.subObject = new MyObject(null);
		}

		@Override
		public String toString() {
			return "MyObject{"
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
				   + ", config="
				   + config
				   + ", subObject="
				   + subObject
				   + '}';
		}
	}

	private static class SomeObject {
		String a;
		String b;

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

	private static class Coordinates {
		int x, y, z;

		public Coordinates(int x, int y, int z) {
			this.x = x;
			this.y = y;
			this.z = z;
		}

		@Override
		public String toString() {
			return String.format("(%d,%d,%d)", x, y, z);
		}
	}

	private static class CoordinatesConverter implements Converter<Coordinates, String> {

		@Override
		public Coordinates convertToField(String value) {
			List<String> parts = StringUtils.split(value, ',');
			int x = Integer.parseInt(parts.get(0).trim());
			int y = Integer.parseInt(parts.get(1).trim());
			int z = Integer.parseInt(parts.get(2).trim());
			return new Coordinates(x, y, z);
		}

		@Override
		public String convertFromField(Coordinates value) {
			return value.toString();
		}
	}
}