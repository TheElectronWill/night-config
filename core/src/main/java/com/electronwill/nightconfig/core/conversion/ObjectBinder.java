package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.SimpleConfig;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
	 * @param bypassTransient {@code true} to use (parse or write) a field even if it's transient
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
		final BoundConfig boundConfig = new BoundConfig(object, supportTypePredicate, bypassFinal);
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
			if (supportTypePredicate.test(field.getType())) {// Plain value
				fieldInfos = new FieldInfos(field, null);
			} else {// Subconfig
				try {
					Object fieldValue = field.get(object);
					if (fieldValue == null) {
						fieldInfos = new FieldInfos(field, null);// No value yet
					} else {
						BoundConfig subConfig = bindNotAnnotated(field.get(object), fieldType,
																 supportTypePredicate);
						fieldInfos = new FieldInfos(field, subConfig);
					}
				} catch (IllegalAccessException e) {
					throw new ReflectionException("Failed to bind field " + field, e);
				}
			}
			boundConfig.registerField(fieldInfos, path);
		}
		return boundConfig;
	}

	/**
	 * Binds an object or class that is annotated with {@link Configured}.
	 */
	private BoundConfig bindAnnotated(Object object, Class<?> clazz,
									  Predicate<Class<?>> supportTypePredicate) {
		final BoundConfig boundConfig = new BoundConfig(object, supportTypePredicate, bypassFinal);
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
			FieldInfos fieldInfos;
			if (supportTypePredicate.test(field.getType())) {// Plain value
				fieldInfos = new FieldInfos(field, null);
			} else {// Subconfig
				try {
					Object fieldValue = field.get(object);
					if (fieldValue == null) {
						fieldInfos = new FieldInfos(field, null);// No value yet
					} else {
						BoundConfig subConfig = bindAnnotated(field.get(object), fieldType,
															  supportTypePredicate);
						fieldInfos = new FieldInfos(field, subConfig);
					}
				} catch (IllegalAccessException e) {
					throw new ReflectionException("Failed to bind field " + field, e);
				}
			}
			boundConfig.registerField(fieldInfos, path);
		}
		return boundConfig;
	}

	/**
	 * A config that is bound to an object (or a class if the object is null).
	 */
	private static final class BoundConfig implements Config {
		private Object object;// may be null
		private final Map<String, Object> dataMap;// contains FieldInfos and subconfigs
		private final Predicate<Class<?>> supportPredicate;
		private final boolean bypassFinal;

		private BoundConfig(Object object, Map<String, Object> dataMap,
							Predicate<Class<?>> supportPredicate, boolean bypassFinal) {
			this.object = object;
			this.dataMap = dataMap;
			this.supportPredicate = supportPredicate;
			this.bypassFinal = bypassFinal;
		}

		/**
		 * Creates a new BoundConfig with an empty HashMap.
		 */
		private BoundConfig(Object object, Predicate<Class<?>> supportPredicate,
							boolean bypassFinal) {
			this(object, new HashMap<>(), supportPredicate, bypassFinal);
		}

		/**
		 * Adds a FieldInfos to {@link #dataMap}, at the specified path.
		 */
		private void registerField(FieldInfos fieldInfos, List<String> path) {
			final int lastIndex = path.size() - 1;
			Map<String, Object> currentMap = dataMap;
			for (String currentKey : path.subList(0, lastIndex)) {
				final Object currentValue = currentMap.get(currentKey);
				final BoundConfig config;
				if (currentValue == null) {// missing intermediary level
					config = new BoundConfig(null, new HashMap<>(1), supportPredicate, bypassFinal);
					currentMap.put(currentKey, config);
				} else if (!(currentValue instanceof BoundConfig)) {// incompatible intermediary level
					throw new IllegalArgumentException(
							"Cannot add an element to an intermediary value of type: "
							+ currentValue.getClass());
				} else {// existing intermediary level
					config = (BoundConfig)currentValue;
				}
				currentMap = config.dataMap;
			}
			String lastKey = path.get(lastIndex);
			currentMap.put(lastKey, fieldInfos);
		}

		/**
		 * Gets the data registered for the given path. Returns a BoundSearchResult that contains
		 * a FieldInfos if the path points to a plain value, or a BoundSearchResult that contains
		 * a BoundConfig if the path points to a subconfig.
		 *
		 * @return a BoundSearchResult containing either a FieldInfos or a BoundConfig
		 */
		private BoundSearchResult searchInfosOrConfig(List<String> path) {
			final int lastIndex = path.size() - 1;
			BoundConfig currentConfig = this;
			for (String key : path.subList(0, lastIndex)) {// Walks down the config hierarchy
				Object v = currentConfig.dataMap.get(key);
				if (v == null) {
					return null;
				} else if (v instanceof BoundConfig) {
					currentConfig = (BoundConfig)v;
				} else {// then (v instanceof FieldInfos) must be true
					FieldInfos fieldInfos = (FieldInfos)v;
					currentConfig = fieldInfos.getUpdatedConfig(currentConfig.object);
				}
			}
			final String lastKey = path.get(lastIndex);
			final Object data = currentConfig.dataMap.get(lastKey);
			return new BoundSearchResult(currentConfig, data);
		}

		@Override
		public <T> T getValue(List<String> path) {
			final BoundSearchResult searchResult = searchInfosOrConfig(path);
			if (searchResult == null) {
				return null;
			} else if (searchResult.hasFieldInfos()) {
				return (T)searchResult.fieldInfos.getValue(searchResult.parentConfig.object);
			} else {
				return (T)searchResult.subConfig;
			}
		}

		@Override
		public boolean containsValue(List<String> path) {
			return searchInfosOrConfig(path) != null;
		}

		@Override
		public Object setValue(List<String> path, Object value) {
			final BoundSearchResult searchResult = searchInfosOrConfig(path);
			if (searchResult == null) {
				throw new UnsupportedOperationException("Cannot add elements to a bound config");
			} else if (searchResult.hasFieldInfos()) {
				return searchResult.fieldInfos.setValue(searchResult.parentConfig.object, value,
												 bypassFinal);
			} else {
				throw new UnsupportedOperationException(
						"Cannot modify non-field elements of a bound config");
			}
		}

		@Override
		public void removeValue(List<String> path) {
			final BoundSearchResult searchResult = searchInfosOrConfig(path);
			if (searchResult == null) {
				return;// Nothing to do
			} else if (searchResult.hasFieldInfos()) {
				searchResult.fieldInfos.removeValue(searchResult.parentConfig.object, bypassFinal);
			} else {
				searchResult.subConfig.clear();
			}
		}

		@Override
		public void clear() {
			for (Map.Entry<String, Object> dataEntry : dataMap.entrySet()) {
				final Object value = dataEntry.getValue();
				if (value instanceof FieldInfos) {
					((FieldInfos)value).removeValue(object, bypassFinal);
				} else if (value instanceof BoundConfig) {
					((BoundConfig)value).clear();
				}
			}
			dataMap.clear();
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
					if (fieldInfos.boundConfig != null) {
						return fieldInfos.getUpdatedConfig(object);// Updates the object
					}
					return fieldInfos.getValue(object);
				}
				return o;
			};
			return new TransformingMap<>(dataMap, readConversion, o -> o, o -> o);
			// TODO better search conversion
		}

		@Override
		public int size() {
			return dataMap.size();
		}

		@Override
		public String toString() {
			return "BoundConfig{" + "object=" + object + ", dataMap=" + dataMap + '}';
		}
	}

	private static final class BoundSearchResult {
		final BoundConfig parentConfig;
		final FieldInfos fieldInfos;
		final BoundConfig subConfig;

		BoundSearchResult(BoundConfig parentConfig, Object data) {
			this.parentConfig = parentConfig;
			if (data instanceof FieldInfos) {
				this.fieldInfos = (FieldInfos)data;
				this.subConfig = null;
			} else {
				this.fieldInfos = null;
				this.subConfig = (BoundConfig)data;
			}
		}

		boolean hasFieldInfos() {
			return fieldInfos != null;
		}
	}

	/**
	 * Informations about a java field used by the BoundConfig.
	 */
	private static final class FieldInfos {
		final Field field;// always non-null
		final BoundConfig boundConfig;// non-null iff the field is a sub config

		FieldInfos(Field field, BoundConfig boundConfig) {
			this.field = field;
			this.boundConfig = boundConfig;
		}

		Object setValue(Object fieldObject, Object value, boolean bypassFinal) {
			if (!bypassFinal && Modifier.isFinal(field.getModifiers())) {
				throw new UnsupportedOperationException("Cannot modify the field " + field);
			}
			try {
				Object previousValue = field.get(fieldObject);
				field.set(fieldObject, value);
				return previousValue;
			} catch (IllegalAccessException e) {
				throw new ReflectionException("Failed to set field " + field, e);
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
				throw new ReflectionException("Failed to get field " + field, e);
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