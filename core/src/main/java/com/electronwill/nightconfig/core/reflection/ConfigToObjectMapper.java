package com.electronwill.nightconfig.core.reflection;

import com.electronwill.nightconfig.core.Config;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps configurations to objects.
 *
 * @author TheElectronWill
 */
public class ConfigToObjectMapper {

	protected final Map<Class<?>, ValueConverter<?, ?>> conversionMap;

	public ConfigToObjectMapper() {
		this(new HashMap<>());
	}

	public ConfigToObjectMapper(Map<Class<?>, ValueConverter<?, ?>> conversionMap) {
		this.conversionMap = conversionMap;
	}

	public <T> T map(Config config, Class<T> objectClass) throws ReflectiveOperationException {
		Constructor<T> constructor = objectClass.getDeclaredConstructor();//constructor with no parameters
		if (!constructor.isAccessible()) {
			constructor.setAccessible(true);//forces the constructor to be accessible
		}
		T object = constructor.newInstance();//call the constructor
		map(config, object);
		return object;
	}

	public void map(Config config, Object object) throws ReflectiveOperationException {
		Class<?> c = object.getClass();
		Map<String, Object> map = config.asMap();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();//may be null!

			Field correspondingField = c.getDeclaredField(key);
			if (!correspondingField.isAccessible()) {
				correspondingField.setAccessible(true);
			}

			if (value == null) {
				correspondingField.set(object, value);
				continue;
			}

			ValueConverter converter = conversionMap.get(value.getClass());
			if (converter != null && converter.canConvert(value)) {
				value = converter.convert(value);
			}

			if (value instanceof Config && !Config.class.isAssignableFrom(correspondingField.getType())) {
				//Config value but not Config field => maps to object
				if (correspondingField.get(object) == null) {//field is null -> try to create it
					Object newValue = map((Config)value, object.getClass());
					correspondingField.set(object, newValue);
				} else {//field is not null -> map values
					map((Config)value, correspondingField.get(object));//map to object recursively
				}
			} else {
				correspondingField.set(object, value);
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
