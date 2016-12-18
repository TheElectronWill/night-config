package com.electronwill.nightconfig.core.reflection;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.MapConfig;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Maps objects to configurations.
 *
 * @author TheElectronWill
 */
public final class ObjectToConfigMapper {

	private final Map<Class<?>, ValueConverter<?, ?>> conversionMap;
	private final boolean respectTransientModifier;

	/**
	 * Creates a new ObjectToConfigMapper with an initially empty conversion map, and that respects the
	 * transient modified.
	 */
	public ObjectToConfigMapper() {
		this(new HashMap<>(), true);
	}

	/**
	 * Creates a new ObjectToConfigMapper with the specified conversion map.
	 *
	 * @param conversionMap            the map to use for converting values
	 * @param respectTransientModifier true to respect the transient modifiers of fields, that is, to
	 *                                 ignore the transient fields; false to ignore the transient modifier
	 *                                 and to treat all the fields
	 */
	public ObjectToConfigMapper(Map<Class<?>, ValueConverter<?, ?>> conversionMap, boolean respectTransientModifier) {
		this.conversionMap = conversionMap;
		this.respectTransientModifier = respectTransientModifier;
	}

	/**
	 * Maps an object to a config. This retrieves the value of each object's field and puts it in the
	 * config. If this ObjectToConfigMapper respects the transient modified, the fields that have a
	 * transient modifier will not be written to the config.
	 *
	 * @param object the object to map
	 * @param config the config to map the object to
	 * @throws SecurityException if a field of the object isn't accessible and the request to access it is
	 *                           denied
	 */
	public void map(Object object, Config config) {
		Class<?> c = object.getClass();
		Map<String, Object> map = config.asMap();
		for (Field field : c.getDeclaredFields()) {
			// Checks if it is a transient field
			final int modifiers = field.getModifiers();
			if (respectTransientModifier && Modifier.isTransient(modifiers)) {
				continue;//respects the transient modifier: ignore the field
			}

			// Enforces access if needed
			if (!field.isAccessible()) {
				field.setAccessible(true);//may throw a SecurityException
			}

			// Gets the field's value
			Object value;
			try {
				value = field.get(object);//may be null!
			} catch (IllegalAccessException e) {
				//This does not happen in practice, because we've done field.setAccessible(true)
				throw new IllegalStateException(e);
			}

			// Converts the value if there is a converter for it
			ValueConverter converter = conversionMap.get(field.getType());
			if (converter != null && converter.canConvert(value)) {
				value = converter.convert(value);
			}

			// Puts the value in the config, directly or field by field
			final String name = field.getName();
			if (value != null && !config.supportsType(value.getClass())) {//Unsupported by config => consider that it's a compound object
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
