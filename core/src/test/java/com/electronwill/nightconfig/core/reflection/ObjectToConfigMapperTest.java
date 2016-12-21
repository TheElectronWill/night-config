package com.electronwill.nightconfig.core.reflection;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.MapConfig;
import com.electronwill.nightconfig.core.SimpleConfig;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

/**
 * @author TheElectronWill
 */
public class ObjectToConfigMapperTest {

	@Test
	public void testWithSimpleConfig() throws Exception {
		ObjectToConfigMapper mapper = new ObjectToConfigMapper();
		Config config = new SimpleConfig();
		MyObject object = new MyObject();
		mapper.map(object, config);

		System.out.println("MyObject mapped to a SimpleConfig:");
		System.out.println(config.asMap());

		assert config.getInt("integer") == object.integer;
		assert config.getDouble("decimal") == object.decimal;
		assert config.getString("string") == object.string;
		assert config.<String>getList("stringList") == object.stringList;
		assert config.getConfig("config") == object.config;
		assert config.getValue("subObject") instanceof Config;
		Config sub = (Config)config.getValue("subObject");
		assert sub.getInt("integer") == 1234567890;
		assert sub.getDouble("decimal") == Math.PI;
		assert sub.getString("string").equals("value");
		assert sub.getList("stringList").equals(Arrays.asList("a", "b", "c"));
		assert sub.getConfig("config").size() == 0;
		assert sub.containsValue("subObject");
		assert sub.getValue("subObject") == null;
	}

	@Test
	public void testWithMapConfig() throws Exception {
		ObjectToConfigMapper mapper = new ObjectToConfigMapper();
		Config config = new SimpleConfig(SimpleConfig.STRATEGY_SUPPORT_ALL);
		MyObject object = new MyObject();
		mapper.map(object, config);

		System.out.println("MyObject mapped to a MapConfig:");
		System.out.println(config.asMap());

		assert config.getInt("integer") == object.integer;
		assert config.getDouble("decimal") == object.decimal;
		assert config.getString("string") == object.string;
		assert config.<String>getList("stringList") == object.stringList;
		assert config.getConfig("config") == object.config;
		assert config.getValue("subObject") == object.subObject;
	}

	private static class MyObject {
		int integer = 1234567890;
		double decimal = Math.PI;
		String string = "value";
		List<String> stringList = Arrays.asList("a", "b", "c");
		Config config = new SimpleConfig(SimpleConfig.STRATEGY_SUPPORT_ALL);
		MyObject subObject;

		public MyObject(MyObject subObject) {
			this.subObject = null;
			//empty config
		}

		public MyObject() {
			this.config.setValue("a.b.c", "configValue");
			this.subObject = new MyObject(null);
		}

		@Override
		public String toString() {
			return "MyObject{" +
					"integer=" + integer +
					", decimal=" + decimal +
					", string='" + string + '\'' +
					", stringList=" + stringList +
					", config=" + config +
					", subObject=" + subObject +
					'}';
		}
	}

}