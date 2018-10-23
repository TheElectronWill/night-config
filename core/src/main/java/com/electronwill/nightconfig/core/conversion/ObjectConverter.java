package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

import java.lang.reflect.*;
import java.util.*;
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
		Class<?> clazz = o.getClass();
		List<String> annotatedPath = AnnotationUtils.getPath(clazz);
		if (annotatedPath != null) {
			destination = destination.getRaw(annotatedPath);
		}
		convertToConfig(o, clazz, destination);
	}

	public void toConfig(Class<?> clazz, Config destination) {
		Objects.requireNonNull(destination, "The config must not be null.");
		List<String> annotatedPath = AnnotationUtils.getPath(clazz);
		if (annotatedPath != null) {
			destination = destination.getRaw(annotatedPath);
		}
		convertToConfig(null, clazz, destination);
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

	public <C extends Config> C toConfig(Class<?> clazz, Supplier<C> destinationSupplier) {
		C destination = destinationSupplier.get();
		toConfig(clazz, destination);
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
		Class<?> clazz = destination.getClass();
		List<String> annotatedPath = AnnotationUtils.getPath(clazz);
		if (annotatedPath != null) {
			config = config.getRaw(annotatedPath);
		}
		convertToObject(config, destination, clazz);
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
	private void convertToConfig(Object object, Class<?> clazz, Config destination) {
		while (clazz != Object.class) {
			for (Field field : clazz.getDeclaredFields()) {
				final int fieldModifiers = field.getModifiers();
				if (object == null && Modifier.isStatic(fieldModifiers)) {
					continue;// Don't process static fields of object instances
				}
				if (!bypassTransient && Modifier.isTransient(fieldModifiers)) {
					continue;// Don't process transient fields if configured so
				}
				if (!field.isAccessible()) {
					field.setAccessible(true);// Enforces field access if needed
				}
				Object value;
				try {
					value = field.get(object);
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
				if (value != null && (!destination.configFormat().accepts(value)
									  || field.isAnnotationPresent(ForceBreakdown.class))) {
					if (value instanceof List) {
						// Convert each element
						final List<?> listValue = (List<?>)value;
						final List<Config> configList = new ArrayList<>(listValue.size());
	
						for (Object element : listValue) {
							Config elementConf = destination.createSubConfig();
							convertToConfig(element, element.getClass(), elementConf);
							configList.add(elementConf);
						}
						destination.set(path, configList);
					} else {
						Config subConfig = destination.createSubConfig();
						convertToConfig(value, field.getType(), subConfig);// Writes as a subconfig
						destination.set(path, subConfig);
					}
				} else {
					destination.set(path, value);// Writes as a plain value
				}
			}
			clazz = clazz.getSuperclass();
		}
	}

	/**
	 * Converts a Config to an Object. The {@link #bypassTransient} and {@link #bypassFinal}
	 * settings apply.
	 */
	private void convertToObject(UnmodifiableConfig config, Object object, Class<?> clazz) {
		while (clazz != Object.class) {
			for (Field field : clazz.getDeclaredFields()) {
				final int fieldModifiers = field.getModifiers();
				if (object == null && Modifier.isStatic(fieldModifiers)) {
					continue;// Don't process static fields of object instances
				}
				if (bypassFinal || !Modifier.isFinal(fieldModifiers)) {
					field.setAccessible(true);// Enforces field access if needed
				} else {
					continue;// Don't process final fields if configured so
				}
				if (!bypassTransient && Modifier.isTransient(fieldModifiers)) {
					continue;// Don't process transient fields if configured so
				}
				if (!field.isAccessible()) {
					field.setAccessible(true);// Enforces field access if needed
				}
				List<String> path = AnnotationUtils.getPath(field);
				Object value = config.get(path);
				Converter<Object, Object> converter = AnnotationUtils.getConverter(field);
				if (converter != null) {
					value = converter.convertToField(value);
				}
	
				Class<?> fieldType = field.getType();
	
				// --- Handle generic lists and arrays ---
				boolean valueContainsConfig = (value instanceof List
												&& !((List<?>)value).isEmpty()
												&& ((List<?>)value).get(0) instanceof UnmodifiableConfig);
				boolean isObjList = false;
				boolean isObjArray = false;
				Optional<Class<?>> genericParam = Optional.empty();
				if (valueContainsConfig && fieldType.isAssignableFrom(List.class)) {
					ParameterizedType parameterizedType = (ParameterizedType)field.getGenericType();
					Optional<Class<?>> listParam = Optional.ofNullable(parameterizedType)
														   .map(ParameterizedType::getActualTypeArguments)
														   .map(types -> types[0])
														   .filter(t -> (t instanceof Class))
														   .map(t -> (Class<?>)t);
					genericParam = listParam.filter(t -> !t.isAssignableFrom(UnmodifiableConfig.class));
					isObjList = genericParam.isPresent();
				} else if (valueContainsConfig && fieldType.isArray()) {
					Optional<Class<?>> arrayParam = Optional.of(fieldType.getComponentType());
					genericParam = arrayParam.filter(t -> !t.isAssignableFrom(UnmodifiableConfig.class));
					isObjArray = genericParam.isPresent();
				}
				// ------
				try {
					if (value instanceof UnmodifiableConfig && !(fieldType.isAssignableFrom(value.getClass()))) {
						// -- Read as a sub-object --
						final UnmodifiableConfig uConfig = (UnmodifiableConfig)value;
	
						// Get or create the field and convert it (if field is null OR not preserved):
						Object fieldValue = field.get(object);
						if (fieldValue == null) {
							fieldValue = createInstance(fieldType);
							field.set(object, fieldValue);
							convertToObject(uConfig, fieldValue, field.getType());
						} else if (!AnnotationUtils.mustPreserve(field, clazz)) {
							convertToObject(uConfig, fieldValue, field.getType());
						}
					} else if (isObjList || isObjArray) {
						final List<UnmodifiableConfig> configList = (List<UnmodifiableConfig>)value;
						final Class<?> fieldElementType = genericParam.get();
						if (isObjList) {
							// -- Read as a list of objects --
	
							// Get or create the field:
							List fieldValue = (List)field.get(object);
							if (fieldValue == null) {
								if (fieldType == List.class || fieldType == ArrayList.class
									|| fieldType == Collection.class) {
									// create a list of the correct size when possible
									fieldValue = new ArrayList<>(configList.size());
								} else {
									fieldValue = (List)createInstance(fieldType);
								}
								field.set(object, fieldValue);
							} else if (AnnotationUtils.mustPreserve(field, clazz)) {
								continue;
							}
	
							// Convert each value of the (config) list and add it to the (field) list:
							for (UnmodifiableConfig element : configList) {
								Object elementObj = createInstance(fieldElementType);
								convertToObject(element, elementObj, fieldElementType);
								fieldValue.add(elementObj);
							}
						} else { // isObjArray
							// -- Read as an array of objects --
	
							// Get or create the field:
							Object fieldValue = field.get(object);
							if (fieldValue == null) {
								fieldValue = Array.newInstance(genericParam.get(), configList.size());
							} else if (AnnotationUtils.mustPreserve(field, clazz)) {
								continue;
							}
	
							// Convert each value of the list and add it to the array:
							for (int i = 0; i < configList.size(); i++) {
								Object elementObj = createInstance(fieldElementType);
								convertToObject(configList.get(i), elementObj, fieldElementType);
								Array.set(fieldValue, i, elementObj);
							}
						}
						// ------
					} else {
						// Read as a plain value
						if (value == null && AnnotationUtils.mustPreserve(field, clazz)) {
							AnnotationUtils.checkField(field, field.get(object));
						} else {
							AnnotationUtils.checkField(field, value);
							field.set(object, value);
						}
					}
				} catch (ReflectiveOperationException ex) {
					throw new ReflectionException("Unable to work with field " + field, ex);
				}
			}
			clazz = clazz.getSuperclass();
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