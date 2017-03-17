package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.SimpleConfig;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
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

	public ObjectConverter(boolean bypassTransient, boolean bypassFinal) {
		this.bypassTransient = bypassTransient;
		this.bypassFinal = bypassFinal;
	}

	public ObjectConverter() {
		this(false, true);
	}

	public void toConfig(Object o, Config destination) {
		Objects.requireNonNull(o, "The object must not be null.");
		Objects.requireNonNull(destination, "The config must not be null.");
		Configured objectConf = o.getClass().getDeclaredAnnotation(Configured.class);
		if (objectConf == null) {
			toConfigNotAnnotated(o, destination);
		} else {
			String[] configuredPath = objectConf.path();
			if (configuredPath.length != 0) {
				destination = destination.getValue(Arrays.asList(configuredPath));
			}
			toConfigAnnotated(o, destination);
		}
	}

	public <C extends Config> C toConfig(Object o, Supplier<C> destinationSupplier) {
		C destination = destinationSupplier.get();
		toConfig(o, destination);
		return destination;
	}

	public void toObject(Config config, Object destination) {
		Objects.requireNonNull(config, "The config must not be null.");
		Objects.requireNonNull(destination, "The object must not be null.");
		Configured objectConf = destination.getClass().getDeclaredAnnotation(Configured.class);
		if (objectConf == null) {
			toObjectNotAnnotated(config, destination);
		} else {
			String[] configuredPath = objectConf.path();
			if (configuredPath.length != 0) {
				config = config.getValue(Arrays.asList(configuredPath));
			}
			toObjectAnnotated(config, destination);
		}
	}

	public <O> O toObject(Config config, Supplier<O> destinationSupplier) {
		O destination = destinationSupplier.get();
		toObject(config, destination);
		return destination;
	}

	private void toConfigAnnotated(Object o, Config destination) {
		for (Field field : o.getClass().getDeclaredFields()) {
			if (!field.isAccessible()) {
				field.setAccessible(true);// Enforces field access if needed
			}
			if (!bypassTransient && Modifier.isTransient(field.getModifiers())) {
				continue;// Don't process transient fields if configured so
			}
			Configured fieldConf = field.getDeclaredAnnotation(Configured.class);
			if (fieldConf == null) {
				continue;// only process fields annotated with @Configured
			}
			Object value;
			try {
				value = field.get(o);
			} catch (IllegalAccessException e) {// Unexpected: setAccessible is called if needed
				throw new RuntimeException("Unable to read the field " + field, e);
			}
			AnnotationSpec.checkField(field, value);/* Checks that the value is conform to an
														eventual @SpecSometing annotation */
			String[] configuredPath = fieldConf.path();// The path in @Configured
			List<String> path;
			if (configuredPath.length == 0) {
				path = Collections.singletonList(field.getName());
				// If no path has been configured, use the field's name
			} else {
				path = Arrays.asList(configuredPath);
			}
			if (value != null && !destination.supportsType(value.getClass())) {
				// TODO @ForceBreakdown to break down objects even when the config supports them
				Config subConfig = new SimpleConfig(destination::supportsType);
				toConfigAnnotated(value, subConfig);// Writes as a subconfig
				destination.setValue(path, subConfig);
			} else {
				destination.setValue(path, value);// Writes as a plain value
			}
		}
	}

	private void toConfigNotAnnotated(Object o, Config destination) {
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
				throw new RuntimeException("Unable to read the field " + field, e);
			}
			AnnotationSpec.checkField(field, value);/* Checks that the value is conform to an
														eventual @SpecSometing annotation */
			List<String> path = Collections.singletonList(field.getName());
			if (value != null && !destination.supportsType(value.getClass())) {
				Config subConfig = new SimpleConfig(destination::supportsType);
				toConfigNotAnnotated(value, subConfig);// Writes as a subconfig
				destination.setValue(path, subConfig);
			} else {
				// Writes as a plain value
				destination.setValue(path, value);
			}
		}
	}

	private void toObjectAnnotated(Config config, Object destination) {
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
			Configured fieldConf = field.getDeclaredAnnotation(Configured.class);
			if (fieldConf == null) {
				continue;// only process fields annotated with @Configured
			}
			String[] configuredPath = fieldConf.path();// The path in @Configured
			Object value;
			if (configuredPath.length == 0) {
				value = config.getValue(Collections.singletonList(field.getName()));
			} else {
				value = config.getValue(Arrays.asList(configuredPath));
			}
			Class<?> fieldType = field.getType();
			try {
				if (value instanceof Config && !(fieldType.isAssignableFrom(value.getClass()))) {
					// Reads as a sub-object
					Object fieldValue = field.get(destination);
					if (fieldValue == null) {
						fieldValue = createInstance(fieldType);
						field.set(destination, fieldValue);
					}
					toObjectAnnotated((Config)value, fieldValue);
				} else {
					// Reads as a plain value
					AnnotationSpec.checkField(field, value);// Checks that the value is conform
					field.set(destination, value);
				}
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException("Unable to work with field " + field, ex);
			}
		}
	}

	private <T> T createInstance(Class<T> tClass) {
		try {
			Constructor<T> constructor = tClass.getDeclaredConstructor();//constructor without
			// parameters
			if (!constructor.isAccessible()) {
				constructor.setAccessible(true);//forces the constructor to be accessible
			}
			return constructor.newInstance();//calls the constructor
		} catch (ReflectiveOperationException ex) {
			throw new RuntimeException("Unable to create an instance of " + tClass, ex);
		}
	}

	private void toObjectNotAnnotated(Config config, Object destination) {
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
			List<String> path = Collections.singletonList(field.getName());
			Object value = config.getValue(path);
			Class<?> fieldType = field.getType();
			System.out.printf("Field %s, path %s, got value %s\n", field.getName(), path, value);
			try {
				if (value instanceof Config && !(fieldType.isAssignableFrom(value.getClass()))) {
					// Reads as a sub-object
					Object fieldValue = field.get(destination);
					if (fieldValue == null) {
						fieldValue = createInstance(fieldType);
						field.set(destination, fieldValue);
					}
					System.out.println("subConfig: " + value);
					toObjectNotAnnotated((Config)value, fieldValue);
				} else {
					// Reads as a plain value
					AnnotationSpec.checkField(field, value);// Checks that the value is conform
					field.set(destination, value);
				}
			} catch (ReflectiveOperationException ex) {
				throw new RuntimeException("Unable to work with field " + field, ex);
			}
		}
	}
}
