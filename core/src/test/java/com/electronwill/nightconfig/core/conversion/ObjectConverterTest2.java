package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.SimpleConfig;
import com.electronwill.nightconfig.core.io.InMemoryFormat;
import com.electronwill.nightconfig.core.utils.StringUtils;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * @author TheElectronWill
 */
public class ObjectConverterTest2 {

	@Test
	public void testSupportBasic() throws Exception {
		ObjectConverter converter = new ObjectConverter();
		Config config = new SimpleConfig();
		MyObject object = new MyObject();
		converter.toConfig(object, config);

		System.out.println("MyObject mapped to a SimpleConfig with basic strategy:");
		System.out.println(config);

		assert config.<Integer>get("integer") == object.integer;
		assert config.<Double>get("decimal") == object.decimal;
		assert config.<String>get("string") == object.string;
		assert config.<List<String>>get("stringList") == object.stringList;
		assert config.<Config>get("config") == object.config;
		assert config.get("subObject") instanceof Config;
		Config sub = config.get("subObject");
		assert sub.<Integer>get("integer") == 1234567890;
		assert sub.<Double>get("decimal") == Math.PI;
		assert sub.<String>get("string").equals("value");
		assert sub.<List<?>>get("stringList").equals(Arrays.asList("a", "b", "c"));
		assert sub.<Config>get("config").size() == 0;
		assert sub.contains("subObject");
		assert sub.get("subObject") == null;
	}

	@Test
	public void testSupportAll() throws Exception {
		ObjectConverter converter = new ObjectConverter();
		Config config = new SimpleConfig(InMemoryFormat.withUniversalSupport());
		MyObject object = new MyObject();
		converter.toConfig(object, config);

		System.out.println("MyObject mapped to a SimpleConfig with support_all strategy:");
		System.out.println(config);

		assert config.<Integer>get("integer") == object.integer;
		assert config.<Double>get("decimal") == object.decimal;
		assert config.<String>get("string") == object.string;
		assert config.<List<String>>get("stringList") == object.stringList;
		assert config.<Config>get("config") == object.config;
		assert config.get("subObject") == object.subObject;
		assert config.get("infos.coordinates").equals(object.coords.toString());
	}

	private static class MyObject {
		int integer = 1234567890;
		double decimal = Math.PI;
		String string = "value";
		List<String> stringList = Arrays.asList("a", "b", "c");
		Config config = new SimpleConfig(InMemoryFormat.withUniversalSupport());
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
				   + ", config="
				   + config
				   + ", subObject="
				   + subObject
				   + '}';
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