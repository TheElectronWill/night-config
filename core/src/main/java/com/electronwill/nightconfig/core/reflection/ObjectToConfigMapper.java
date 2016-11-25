package com.electronwill.nightconfig.core.reflection;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.MapConfig;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps objects to configurations.
 *
 * @author TheElectronWill
 */
public class ObjectToConfigMapper {

	protected final Map<Class<?>, ValueConverter<?, ?>> conversionMap;

	public ObjectToConfigMapper() {
		this(new HashMap<>());
	}

	public ObjectToConfigMapper(Map<Class<?>, ValueConverter<?, ?>> conversionMap) {
		this.conversionMap = conversionMap;
	}

	public void map(Object object, Config config) throws IllegalAccessException {
		Class<?> c = object.getClass();
		Map<String, Object> map = config.asMap();
		for (Field field : c.getDeclaredFields()) {
			String name = field.getName();
			Object value = field.get(object);//may be null!

			ValueConverter converter = conversionMap.get(field.getType());
			if (converter != null && converter.canConvert(value)) {
				value = converter.convert(value);
			}

			if (value != null && !config.supportsType(value.getClass())) {// unsupported by config => consider that it's a compound object
				Config correspondingConfig;
				if (((map.get(name) instanceof Config))) {//if a subconfig with the correct name already exists, use it.
					correspondingConfig = (Config)map.get(name);
				} else {//else, create a new one
					correspondingConfig = new MapConfig();
					map.put(name, correspondingConfig);
				}
				map(value, correspondingConfig);//recursively map the compound object to the config
			} else {//simple value
				map.put(name, value);//directly put it in the config
			}
		}
	}

	public <T> void addConversion(Class<T> typeToConvert, ConversionChecker<T> conversionChecker,
								  ConversionApplier<T, ?> conversionApplier) {
		conversionMap.put(typeToConvert, new ValueConverter<>(conversionChecker, conversionApplier));
	}

	public <T> void removeConversion(Class<T> typeToConvert, ConversionChecker<T> conversionChecker,
									 ConversionApplier<T, ?> conversionApplier) {
		ValueConverter converter = conversionMap.get(typeToConvert);
		if (converter != null && converter.checker == conversionChecker && converter.applier == conversionApplier) {
			conversionMap.remove(typeToConvert);
		}
	}
}
