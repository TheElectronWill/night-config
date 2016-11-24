package com.electronwill.nightconfig.core.reflection;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.MapConfig;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

/**
 * Maps a config to an object.
 *
 * @author TheElectronWill
 */
public class ConfigObjectMapper {
	public static void mapConfigToObject(Config config, Object o) throws NoSuchFieldException, IllegalAccessException {
		Class<?> c = o.getClass();
		Map<String, Object> map = config.asMap();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();
			Field correspondingField = c.getField(key);
			if (value instanceof Config && !Config.class.isAssignableFrom(correspondingField.getType())) {//Config value but not Config field
				mapConfigToObject((Config)value, correspondingField.get(o));//map to object recursively
			} else {
				if (!correspondingField.isAccessible()) {
					correspondingField.setAccessible(true);
				}
				correspondingField.set(o, value);
			}
		}
	}

	public static void mapObjectToConfig(Object o, Config config) throws IllegalAccessException {
		Class<?> c = o.getClass();
		Map<String, Object> map = config.asMap();
		for (Field field : c.getFields()) {
			String name = field.getName();
			Object value = field.get(o);
			if (!config.getSupportedTypes().contains(field.getType())) {//unsupported by config => maybe a compound object?
				//TODO allow custom serializers o -> t where t is supported by the config
				Config correspondingConfig;
				if (!(map.get(name) instanceof Config)) {
					correspondingConfig = new MapConfig();
					map.put(name, correspondingConfig);
				} else {
					correspondingConfig = (Config)map.get(name);
				}
				mapObjectToConfig(value, correspondingConfig);
			} else {
				map.put(name, value);
			}
		}
	}
}

class ExampleObject {
	private String name;
	private int min, max;
	private List<String> list;
	private Params nested;

	class Params {
		int a, b, c, d;
		String p1, p2;
	}
}