package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.SimpleConfig;
import com.electronwill.nightconfig.core.utils.TransformingMap;
import com.electronwill.nightconfig.core.utils.TransformingSet;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
		BoundConfig boundConfig = createBoundConfig(object, clazz, supportTypePredicate);
		List<String> annotatedPath = AnnotationUtils.getPath(clazz);
		if (annotatedPath != null) {
			Config parentConfig = new SimpleConfig(supportTypePredicate);
			parentConfig.setValue(annotatedPath, boundConfig);
			return parentConfig;
		}
		return boundConfig;
	}

	/**
	 * Binds an object or a class to a config.
	 */
	private BoundConfig createBoundConfig(Object object, Class<?> clazz,
										  Predicate<Class<?>> supportTypePredicate) {
		final BoundConfig boundConfig = new BoundConfig(object, supportTypePredicate, bypassFinal);
		for (Field field : clazz.getDeclaredFields()) {
			if (!field.isAccessible()) {
				field.setAccessible(true);// Enforces field access if needed
			}
			if (!bypassTransient && Modifier.isTransient(field.getModifiers())) {
				continue;// Don't process transient fields if configured so
			}
			List<String> path = AnnotationUtils.getPath(field);
			FieldInfos fieldInfos;
			Converter<Object, Object> converter = AnnotationUtils.getConverter(field);
			if (converter == null) {
				converter = NoOpConverter.INSTANCE;
			}
			try {
				Object value = converter.convertFromField(field.get(object));
				if (value == null || supportTypePredicate.test(value.getClass())) {
					fieldInfos = new FieldInfos(field, null, converter);
				} else {
					BoundConfig subConfig = createBoundConfig(value, field.getType(),
															  supportTypePredicate);
					fieldInfos = new FieldInfos(field, subConfig, converter);
				}
			} catch (IllegalAccessException e) {
				throw new ReflectionException("Failed to bind field " + field, e);
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
			} else if (searchResult.hasSubConfig()) {
				return (T)searchResult.subConfig;
			} else {
				return (T)searchResult.fieldInfos.getValue(searchResult.parentConfig.object);
			}
		}

		@Override
		public boolean containsValue(List<String> path) {
			return searchInfosOrConfig(path) != null;
		}

		@Override
		public <T> T setValue(List<String> path, Object value) {
			final BoundSearchResult searchResult = searchInfosOrConfig(path);
			if (searchResult == null) {
				throw new UnsupportedOperationException("Cannot add elements to a bound config");
			} else if (searchResult.hasFieldInfos()) {
				return (T)searchResult.fieldInfos.setValue(searchResult.parentConfig.object, value,
														   bypassFinal);
			} else {
				throw new UnsupportedOperationException(
						"Cannot modify non-field elements of a bound config");
			}
		}

		@Override
		public <T> T removeValue(List<String> path) {
			final BoundSearchResult searchResult = searchInfosOrConfig(path);
			if (searchResult == null) {
				return null;// Nothing to do
			} else if (searchResult.hasFieldInfos()) {
				return (T)searchResult.fieldInfos.removeValue(searchResult.parentConfig.object,
															  bypassFinal);
			} else {
				SimpleConfig copy = new SimpleConfig(searchResult.subConfig);
				searchResult.subConfig.clear();
				return (T)copy;
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
		public Map<String, Object> valueMap() {
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
		public Set<? extends Entry> entrySet() {
			Function<Map.Entry<String, Object>, Entry> readTransfo = entry -> new Entry() {
				@Override
				public <T> T setValue(Object value) {
					return BoundConfig.this.setValue(entry.getKey(), value);
				}

				@Override
				public String getKey() {
					return entry.getKey();
				}

				@Override
				public <T> T getValue() {
					return (T)entry.getValue();
				}
			};
			return new TransformingSet<>(dataMap.entrySet(), readTransfo, o -> null, o -> o);
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
				fieldInfos = (FieldInfos)data;
				if (fieldInfos.boundConfig == null) {
					subConfig = null;
				} else {
					subConfig = fieldInfos.getUpdatedConfig(parentConfig.object);
				}
			} else {
				fieldInfos = null;
				subConfig = (BoundConfig)data;
			}
		}

		boolean hasFieldInfos() {
			return fieldInfos != null;
		}

		boolean hasSubConfig() {
			return subConfig != null;
		}
	}

	/**
	 * Informations about a java field used by the BoundConfig.
	 */
	private static final class FieldInfos {
		final Field field;// always non-null
		final BoundConfig boundConfig;// non-null iff the field is a sub config
		final Converter<Object, Object> converter;

		FieldInfos(Field field, BoundConfig boundConfig, Converter<Object, Object> converter) {
			this.field = field;
			this.boundConfig = boundConfig;
			this.converter = converter;
		}

		Object setValue(Object fieldObject, Object value, boolean bypassFinal) {
			if (!bypassFinal && Modifier.isFinal(field.getModifiers())) {
				throw new UnsupportedOperationException("Cannot modify the field " + field);
			}
			try {
				Object previousValue = converter.convertFromField(field.get(fieldObject));
				Object newValue = converter.convertToField(value);
				AnnotationUtils.checkField(field, newValue);
				field.set(fieldObject, newValue);
				return previousValue;
			} catch (IllegalAccessException e) {
				throw new ReflectionException("Failed to set field " + field, e);
			}
		}

		Object removeValue(Object fieldObject, boolean bypassFinal) {
			Object previousValue = getValue(fieldObject);
			if (field.getType().isPrimitive()) {
				setValue(fieldObject, (byte)0, bypassFinal);
			} else {
				setValue(fieldObject, null, bypassFinal);
				if (boundConfig != null) {
					boundConfig.clear();
				}
			}
			return previousValue;
		}

		Object getValue(Object fieldObject) {
			try {
				return converter.convertFromField(field.get(fieldObject));
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

	private static final class NoOpConverter implements Converter<Object, Object> {
		static final NoOpConverter INSTANCE = new NoOpConverter();

		@Override
		public Object convertToField(Object value) {
			return value;
		}

		@Override
		public Object convertFromField(Object value) {
			return value;
		}
	}
}