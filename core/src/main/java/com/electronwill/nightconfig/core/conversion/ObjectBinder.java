package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.SimpleConfig;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Creates configurations bound to an object or class, getting its values from its fields.
 *
 * @author TheElectronWill
 */
public final class ObjectBinder {
	private final boolean bypassTransient, bypassFinal;

	/**
	 * Creates a new ObjectBinder with advanced parameters.
	 *
	 * @param bypassTransient {@code true} to use (read or write) a field even if it's transient
	 * @param bypassFinal     {@code true} to write a field even if it's final
	 */
	public ObjectBinder(boolean bypassTransient, boolean bypassFinal) {
		this.bypassTransient = bypassTransient;
		this.bypassFinal = bypassFinal;
	}

	/**
	 * Creates a new ObjectBinder with the default parameters. This is equivalent to {@code
	 * new ObjectBinder(false, true)}.
	 *
	 * @see #ObjectBinder(boolean, boolean)
	 */
	public ObjectBinder() {
		this(false, true);
	}

	/**
	 * Creates a new config bound to the static fields of a class.
	 *
	 * @param clazz the class to bind
	 * @return a config bound to the static fields of the class
	 */
	public Config bind(Class<?> clazz) {
		return bind(clazz, SimpleConfig.BASIC_SUPPORT_PREDICATE);
	}

	/**
	 * Creates a new config bound to the static fields of a class.
	 *
	 * @param clazz                the class to bind
	 * @param supportTypePredicate the Predicate that determines the supported types
	 * @return a config bound to the static fields of the class
	 */
	public Config bind(Class<?> clazz, Predicate<Class<?>> supportTypePredicate) {
		return bind(null, clazz, supportTypePredicate);
	}

	/**
	 * Creates a new config bound to the fields of a object.
	 *
	 * @param object the class to bind
	 * @return a config bound to the fields of the object
	 */
	public Config bind(Object object) {
		return bind(object, SimpleConfig.BASIC_SUPPORT_PREDICATE);
	}

	/**
	 * Creates a new config bound to the fields of a object.
	 *
	 * @param object               the class to bind
	 * @param supportTypePredicate the Predicate that determines the supported types
	 * @return a config bound to the fields of the object
	 */
	public Config bind(Object object, Predicate<Class<?>> supportTypePredicate) {
		return bind(object, object.getClass(), supportTypePredicate);
	}

	/**
	 * Creates a bound config.
	 *
	 * @param object               the object to bind, or null to bind the static fields of the
	 *                             class
	 * @param clazz                the object class, or the class to bind if the object is null
	 * @param supportTypePredicate the predicate that determines the supported types
	 * @return a config bound to the specified object or class
	 */
	private Config bind(Object object, Class<?> clazz, Predicate<Class<?>> supportTypePredicate) {
		Configured objectConf = clazz.getDeclaredAnnotation(Configured.class);
		if (objectConf == null) {
			return bindNotAnnotated(object, clazz, supportTypePredicate);
		}
		BoundConfig boundConfig = bindAnnotated(object, clazz, supportTypePredicate);
		String[] configuredPath = objectConf.path();
		if (configuredPath.length != 0) {
			Config parentConfig = new SimpleConfig(supportTypePredicate);
			parentConfig.setValue(Arrays.asList(configuredPath), boundConfig);
			return parentConfig;
		}
		return boundConfig;
	}

	/**
	 * Binds an object or class that isn't annotated with {@link Configured}.
	 */
	private BoundConfig bindNotAnnotated(Object object, Class<?> clazz,
										 Predicate<Class<?>> supportTypePredicate) {
		Config fieldInfosConfig = new SimpleConfig();
		for (Field field : clazz.getDeclaredFields()) {
			if (!field.isAccessible()) {
				field.setAccessible(true);// Enforces field access if needed
			}
			if (!bypassTransient && Modifier.isTransient(field.getModifiers())) {
				continue;// Don't process transient fields if configured so
			}
			List<String> path = Collections.singletonList(field.getName());
			Class<?> fieldType = field.getType();
			FieldInfos fieldInfos;
			if (supportTypePredicate.test(field.getType())) {
				fieldInfos = new FieldInfos(field, null);
			} else {
				try {
					BoundConfig subConfig = bindAnnotated(field.get(object), fieldType,
														  supportTypePredicate);
					fieldInfos = new FieldInfos(field, subConfig);
				} catch (IllegalAccessException e) {
					throw new RuntimeException();//TODO better exception
				}
			}
			fieldInfosConfig.setValue(path, fieldInfos);
		}
		return new BoundConfig(object, fieldInfosConfig, supportTypePredicate, bypassFinal);
	}

	/**
	 * Binds an object or class that is annotated with {@link Configured}.
	 */
	private BoundConfig bindAnnotated(Object object, Class<?> clazz,
									  Predicate<Class<?>> supportTypePredicate) {
		Config fieldInfosConfig = new SimpleConfig();
		for (Field field : clazz.getDeclaredFields()) {
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
			String[] configuredPath = fieldConf.path();// The path in @Configured
			List<String> path;
			if (configuredPath.length == 0) {
				path = Collections.singletonList(field.getName());
				// If no path has been configured, use the field's name
			} else {
				path = Arrays.asList(configuredPath);
			}
			Class<?> fieldType = field.getType();
			if (supportTypePredicate.test(fieldType)) {
				FieldInfos fieldInfos = new FieldInfos(field, null);
				fieldInfosConfig.setValue(path, fieldInfos);
			} else {
				try {
					BoundConfig subConfig = bindAnnotated(field.get(object), fieldType,
														  supportTypePredicate);
					fieldInfosConfig.setValue(path, subConfig);
				} catch (IllegalAccessException e) {
					throw new RuntimeException();//TODO better exception
				}
			}

		}
		return new BoundConfig(object, fieldInfosConfig, supportTypePredicate, bypassFinal);
	}

	/**
	 * A config that is bound to an object (or a class if the object is null).
	 */
	private static final class BoundConfig implements Config {
		private Object object;// may be null
		private final Config dataConfig;// contains FieldInfos and subconfigs
		private final Predicate<Class<?>> supportPredicate;
		private final boolean bypassFinal;

		private BoundConfig(Object object, Config dataConfig, Predicate<Class<?>> supportPredicate,
							boolean bypassFinal) {
			this.object = object;
			this.dataConfig = dataConfig;
			this.supportPredicate = supportPredicate;
			this.bypassFinal = bypassFinal;
		}

		@Override
		public void setValue(List<String> path, Object value) {
			Object currentData = dataConfig.getValue(path);
			if (currentData instanceof FieldInfos) {
				setFieldValue((FieldInfos)currentData, value);
			} else {
				throw new UnsupportedOperationException();//TODO msg
			}
		}
		//TODO aussi revoir les exceptions de CharacterInput et CharacterOutpout

		@Override
		public void removeValue(List<String> path) {
			Object data = dataConfig.getValue(path);
			if (data instanceof FieldInfos) {
				removeFieldValue((FieldInfos)data);
			} else if (data instanceof Config) {
				((Config)data).clear();
			}
			dataConfig.removeValue(path);
		}

		private void removeFieldValue(FieldInfos fieldInfos) {
			setFieldValue(fieldInfos, null);
			if (fieldInfos.boundConfig != null) {
				fieldInfos.boundConfig.clear();
			}
		}

		private void setFieldValue(FieldInfos fieldInfos, Object value) {
			Field field = fieldInfos.field;
			if (!bypassFinal && Modifier.isFinal(field.getModifiers())) {
				throw new UnsupportedOperationException();//TODO msg
			}
			try {
				fieldInfos.field.set(object, null);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		@Override
		public void clear() {
			for (Map.Entry<String, Object> dataEntry : dataConfig.asMap().entrySet()) {
				final Object value = dataEntry.getValue();
				if (value instanceof FieldInfos) {
					removeFieldValue((FieldInfos)value);
				}
			}
			dataConfig.clear();
		}

		@Override
		public boolean supportsType(Class<?> type) {
			return supportPredicate.test(type);
		}

		@Override
		public Map<String, Object> asMap() {
			Function<Object, Object> readConversion = o -> {
				if (o instanceof FieldInfos) {
					FieldInfos fieldInfos = (FieldInfos)o;
					try {
						if (fieldInfos.boundConfig != null) {
							BoundConfig boundConfig = fieldInfos.boundConfig;
							boundConfig.object = fieldInfos.field.get(object);// Updates the object
							return boundConfig;
						}
						return fieldInfos.field.get(object);
					} catch (IllegalAccessException e) {
						throw new RuntimeException(e);
					}
				}
				return o;
			};
			return new TransformingMap<>(dataConfig.asMap(), readConversion, o -> o, o -> o);
			// TODO better search conversion
		}

		@Override
		public <T> T getValue(List<String> path) {
			Object data = dataConfig.getValue(path);
			if (data instanceof FieldInfos) {
				FieldInfos fieldInfos = (FieldInfos)data;
				Object fieldValue;
				try {
					fieldValue = fieldInfos.field.get(object);
				} catch (IllegalAccessException e) {
					throw new RuntimeException(e);
				}
				if (fieldInfos.boundConfig != null) {
					BoundConfig boundConfig = fieldInfos.boundConfig;
					boundConfig.object = fieldValue;// Updates the object
					return (T)boundConfig;
				}
				return (T)fieldValue;
			} else {
				return (T)data;
			}
		}

		@Override
		public boolean containsValue(List<String> path) {
			return dataConfig.containsValue(path);
		}

		@Override
		public int size() {
			return dataConfig.size();
		}
	}

	/**
	 * Informations about a java field used by the BoundConfig.
	 */
	private static final class FieldInfos {
		final Field field;// always non-null
		final BoundConfig boundConfig;// non-null iff the field is a sub config

		private FieldInfos(Field field, BoundConfig boundConfig) {
			this.field = field;
			this.boundConfig = boundConfig;
		}

		void setValue(Object fieldObject, Object value, boolean bypassFinal) {
			if (!bypassFinal && Modifier.isFinal(field.getModifiers())) {
				throw new UnsupportedOperationException();// TODO msg
			}
			try {
				field.set(fieldObject, value);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		void removeValue(Object fieldObject, boolean bypassFinal) {
			if (field.getType().isPrimitive()) {
				setValue(fieldObject, (byte)0, bypassFinal);
			} else {
				setValue(fieldObject, null, bypassFinal);
				if (boundConfig != null) {
					boundConfig.clear();
				}
			}
		}

		Object getValue(Object fieldObject) {
			try {
				return field.get(fieldObject);
			} catch (IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}

		BoundConfig getUpdatedConfig(Object fieldObject) {
			boundConfig.object = getValue(fieldObject);
			return boundConfig;
		}

		@Override
		public String toString() {
			return "FieldInfos{" + "field=" + field + ", boundConfig=" + boundConfig + '}';
		}
	}
}