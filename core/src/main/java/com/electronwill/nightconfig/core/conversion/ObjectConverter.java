package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.SimpleConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Converts Java objects to configs and vice-versa.
 *
 * @author TheElectronWill
 */
public final class ObjectConverter {
	private final boolean bypassTransient, bypassFinal;

	/**
	 * Creates a new ObjectConverter with advanced parameters.
	 *
	 * @param bypassTransient {@code true} to use (parse or write) a field even if it's transient
	 * @param bypassFinal     {@code true} to write a field even if it's final
	 */
	public ObjectConverter(boolean bypassTransient, boolean bypassFinal) {
		this.bypassTransient = bypassTransient;
		this.bypassFinal = bypassFinal;
	}

	/**
	 * Creates a new ObjectConverter with the default parameters. This is equivalent to {@code
	 * new ObjectConverter(false, true)}.
	 *
	 * @see #ObjectConverter(boolean, boolean)
	 */
	public ObjectConverter() {
		this(false, true);
	}

	/**
	 * Converts an Object to a Config.
	 *
	 * @param o           the object to convert
	 * @param destination the Config where to put the values into
	 */
	public void toConfig(Object o, Config destination) {
		Objects.requireNonNull(o, "The object must not be null.");
		Objects.requireNonNull(destination, "The config must not be null.");
		List<String> annotatedPath = AnnotationUtils.getPath(destination.getClass());
		if (annotatedPath != null) {
			destination = destination.get(annotatedPath);
		}
		convertToConfig(o, destination);
	}

	/**
	 * Converts an Object to a Config.
	 *
	 * @param o                   the object to convert
	 * @param destinationSupplier a Supplier that provides the Config where to put the values into
	 * @param <C>                 the destination's type
	 * @return the Config obtained from the Supplier
	 */
	public <C extends Config> C toConfig(Object o, Supplier<C> destinationSupplier) {
		C destination = destinationSupplier.get();
		toConfig(o, destination);
		return destination;
	}

	/**
	 * Converts a Config to an Object.
	 *
	 * @param config      the config to convert
	 * @param destination the Object where to put the values into
	 */
	public void toObject(UnmodifiableConfig config, Object destination) {
		Objects.requireNonNull(config, "The config must not be null.");
		Objects.requireNonNull(destination, "The object must not be null.");
		List<String> annotatedPath = AnnotationUtils.getPath(destination.getClass());
		if (annotatedPath != null) {
			config = config.get(annotatedPath);
		}
		convertToObject(config, destination);
	}

	/**
	 * Converts a Config to an Object.
	 *
	 * @param config              the config to convert
	 * @param destinationSupplier a Supplier that provides the Object where to put the values into
	 * @param <O>                 the destination's type
	 * @return the object obtained from the Supplier
	 */
	public <O> O toObject(UnmodifiableConfig config, Supplier<O> destinationSupplier) {
		O destination = destinationSupplier.get();
		toObject(config, destination);
		return destination;
	}

	/**
	 * Converts an Object to a Config. The {@link #bypassTransient} setting applies.
	 */
	private void convertToConfig(Object o, Config destination) {
		for (Field field : o.getClass().getDeclaredFields()) {
			if (!field.isAccessible()) {
				field.setAccessible(true);// Enforces field access if needed
			}
			if (!bypassTransient && Modifier.isTransient(field.getModifiers())) {
				continue;// Don't process transient fields if configured so
			}
			Object value;
			try {
				value = field.get(o);
			} catch (IllegalAccessException e) {// Unexpected: setAccessible is called if needed
				throw new ReflectionException("Unable to parse the field " + field, e);
			}
			AnnotationUtils.checkField(field, value);/* Checks that the value is conform to an
															eventual @SpecSometing annotation */
			Converter<Object, Object> converter = AnnotationUtils.getConverter(field);
			if (converter != null) {
				value = converter.convertFromField(value);
			}
			List<String> path = AnnotationUtils.getPath(field);
			if (value != null && (!destination.configFormat().supportsType(value.getClass())
								  || field.isAnnotationPresent(ForceBreakdown.class))) {
				convertToConfig(value, subConfig);// Writes as a subconfig
				Config subConfig = new SimpleConfig(destination.configFormat());
				destination.set(path, subConfig);
			} else {
				destination.set(path, value);// Writes as a plain value
			}
		}
	}

	/**
	 * Converts a Config to an Object. The {@link #bypassTransient} and {@link #bypassFinal}
	 * settings apply.
	 */
	private void convertToObject(UnmodifiableConfig config, Object destination) {
		for (Field field : destination.getClass().getDeclaredFields()) {
			if (!field.isAccessible()) {
				if (bypassFinal || !Modifier.isFinal(field.getModifiers())) {
					field.setAccessible(true);// Enforces field access if needed
				} else {
					continue;// Don't process final fields if configured so
				}
			}
			if (!bypassTransient && Modifier.isTransient(field.getModifiers())) {
				continue;// Don't process transient fields if configured so
			}
			List<String> path = AnnotationUtils.getPath(field);
			Object value = config.get(path);
			Converter<Object, Object> converter = AnnotationUtils.getConverter(field);
			if (converter != null) {
				value = converter.convertToField(value);
			}
			Class<?> fieldType = field.getType();
			try {
				if (value instanceof UnmodifiableConfig && !(fieldType.isAssignableFrom(
						value.getClass()))) {
					// Reads as a sub-object
					Object fieldValue = field.get(destination);
					if (fieldValue == null) {
						fieldValue = createInstance(fieldType);
						field.set(destination, fieldValue);
					}
					convertToObject((UnmodifiableConfig)value, fieldValue);
				} else {
					// Reads as a plain value
					AnnotationUtils.checkField(field, value);// Checks that the value is conform
					field.set(destination, value);
				}
			} catch (ReflectiveOperationException ex) {
				throw new ReflectionException("Unable to work with field " + field, ex);
			}
		}
	}

	/**
	 * Creates a generic instance of the specified class, using its constructor that requires no
	 * argument.
	 *
	 * @param tClass the class to create an instance of
	 * @param <T>    the class's type
	 * @return a new instance of the class
	 *
	 * @throws ReflectionException if the class doesn't have a constructor without arguments, or if
	 *                             the constructor cannot be accessed, or for another reason.
	 */
	private <T> T createInstance(Class<T> tClass) {
		try {
			Constructor<T> constructor = tClass.getDeclaredConstructor();//constructor without parameters
			if (!constructor.isAccessible()) {
				constructor.setAccessible(true);//forces the constructor to be accessible
			}
			return constructor.newInstance();//calls the constructor
		} catch (ReflectiveOperationException ex) {
			throw new ReflectionException("Unable to create an instance of " + tClass, ex);
		}
	}
}