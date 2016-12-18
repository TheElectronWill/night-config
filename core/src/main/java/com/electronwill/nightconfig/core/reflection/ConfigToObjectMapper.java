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
public final class ConfigToObjectMapper {

	protected final Map<Class<?>, ValueConverter<?, ?>> conversionMap;

	/**
	 * Creates a new ConfigToObjectMapper with an initially empty conversion map.
	 */
	public ConfigToObjectMapper() {
		this(new HashMap<>());
	}

	/**
	 * Creates a new ConfigToObjectMapper with the specified conversion map.
	 *
	 * @param conversionMap the map to use for converting values
	 */
	public ConfigToObjectMapper(Map<Class<?>, ValueConverter<?, ?>> conversionMap) {
		this.conversionMap = conversionMap;
	}

	/**
	 * Maps a config to a newly created object of class {@code objectClass}. This retrieves each config's
	 * first-level value and writes it to the corresponding object's field. A field corresponds to a
	 * config's entry when it has the same name.
	 *
	 * @param config      the config to map
	 * @param objectClass the class of the object
	 * @param <T>         the object's type
	 * @return a new instance of {@code objectClass} with its fields set according to the config.
	 * @throws ReflectiveOperationException
	 */
	public <T> T map(Config config, Class<T> objectClass) throws ReflectiveOperationException {
		Constructor<T> constructor = objectClass.getDeclaredConstructor();//constructor with no parameters
		if (!constructor.isAccessible()) {
			constructor.setAccessible(true);//forces the constructor to be accessible
		}
		T object = constructor.newInstance();//call the constructor
		map(config, object);
		return object;
	}

	/**
	 * Maps a config to an object. This retrieves each config's first-level value and writes it to the
	 * corresponding object's field. A field corresponds to a config's entry when it has the same name.
	 *
	 * @param config the config to map
	 * @param object the object to map the config to
	 */
	public void map(Config config, Object object) throws ReflectiveOperationException {
		Class<?> c = object.getClass();
		Map<String, Object> map = config.asMap();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			String key = entry.getKey();
			Object value = entry.getValue();//may be null!

			// Enforces access if needed
			Field correspondingField = c.getDeclaredField(key);
			if (!correspondingField.isAccessible()) {
				correspondingField.setAccessible(true);
			}

			// If the value is null, no conversion is possible
			if (value == null) {
				correspondingField.set(object, value);
				continue;
			}

			// Converts the value if there is a converter for it
			ValueConverter converter = conversionMap.get(value.getClass());
			if (converter != null && converter.canConvert(value)) {
				value = converter.convert(value);
			}

			// Sets the field value, directly or not
			if (value instanceof Config && !Config.class.isAssignableFrom(correspondingField.getType())) {
				//Config value but not Config field => maps to object
				if (correspondingField.get(object) == null) {//field is null -> try to create it
					Object newValue = map((Config)value, object.getClass());
					correspondingField.set(object, newValue);
				} else {//field is not null -> map values
					map((Config)value, correspondingField.get(object));//map to object recursively
				}
			} else {//simple value
				correspondingField.set(object, value);
			}
		}
	}

	/**
	 * Adds a conversion to apply to objects' fields.
	 *
	 * @param typeToConvert     the class of the type of field to apply this conversion to
	 * @param conversionChecker the object that decides if the conversion applies
	 * @param conversionApplier the object that applies the conversion
	 * @param <T>               the type to convert
	 */
	public <T> void addConversion(Class<T> typeToConvert, ConversionChecker<T> conversionChecker,
								  ConversionApplier<T, ?> conversionApplier) {
		conversionMap.put(typeToConvert, new ValueConverter<>(conversionChecker, conversionApplier));
	}

	/**
	 * Removes a conversion to apply to objects' fields.
	 *
	 * @param typeToConvert     the class of the type of field to apply this conversion to
	 * @param conversionChecker the object that decides if the conversion applies
	 * @param conversionApplier the object that applies the conversion
	 * @param <T>               the type to convert
	 */
	public <T> void removeConversion(Class<T> typeToConvert, ConversionChecker<T> conversionChecker,
									 ConversionApplier<T, ?> conversionApplier) {
		ValueConverter converter = conversionMap.get(typeToConvert);
		if (converter != null && converter.checker == conversionChecker && converter.applier == conversionApplier) {
			conversionMap.remove(typeToConvert);
		}
	}
}
