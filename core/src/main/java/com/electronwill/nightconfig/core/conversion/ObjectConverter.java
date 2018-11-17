package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
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
				ConfigFormat<?> format = destination.configFormat();

				if (value == null) {
					destination.set(path, null);
				} else {
					Class<?> valueType = value.getClass();
					if (field.isAnnotationPresent(ForceBreakdown.class) || !format.supportsType(valueType)) {
						// We have to convert the value
						destination.set(path, value);
						Config converted = destination.createSubConfig();
						convertToConfig(value, valueType, converted);
						destination.set(path, converted);
					} else if (value instanceof List) {
						// Check that the ConfigFormat supports the type of the elements of the list
						List<?> src = (List<?>)value;
						Class<?> bottomType = bottomElementType(src);
						if (format.supportsType(bottomType)) {
							// Everything is supported, no conversion needed
							destination.set(path, value);
						} else {
							// List of complex objects, the bottom elements need conversion
							List<Object> dst = new ArrayList<>(src.size());
							convertListToConfigs(src, bottomType, dst, destination);
							destination.set(path, dst);
						}
					} else {
						// Simple value writing
						destination.set(path, value);
					}
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

				// ------
				try {
					if (value instanceof UnmodifiableConfig && !(fieldType.isAssignableFrom(value.getClass()))) {
						// -- Read as a sub-object --
						final UnmodifiableConfig cfg = (UnmodifiableConfig)value;
	
						// Get or create the field and convert it (if field is null OR not preserved):
						Object fieldValue = field.get(object);
						if (fieldValue == null) {
							fieldValue = createInstance(fieldType);
							field.set(object, fieldValue);
							convertToObject(cfg, fieldValue, field.getType());
						} else if (!AnnotationUtils.mustPreserve(field, clazz)) {
							convertToObject(cfg, fieldValue, field.getType());
						}
					} else if (value instanceof List && fieldType.isAssignableFrom(List.class)) {
						// --- Reads as a list, maybe a list of objects with conversion ---
						final List<?> src = (List<?>)value;
						Class<?> srcBottomType = bottomElementType(src);
						Class<?> dstBottomType = bottomElementType((ParameterizedType)field.getGenericType());

						if (srcBottomType == null
							|| dstBottomType == null
							|| dstBottomType.isAssignableFrom(srcBottomType)) {

							// Simple list, no conversion needed
							AnnotationUtils.checkField(field, value);
							field.set(object, value);
						} else {
							// List of objects, the bottom elements need conversion

							// Use the current field value if there is one, or create a new list
							List dst = (List)field.get(object);
							if (dst == null) {
								if (fieldType == List.class
									|| fieldType == ArrayList.class
									|| fieldType == Collection.class) {
									dst = new ArrayList(src.size());// allocates the right size
								} else {
									dst = (List)createInstance(fieldType);
								}
								field.set(object, dst);
							}

							// Convert the elements of the list
							convertListToObjects(src, dst, dstBottomType);

							// Apply the checks
							AnnotationUtils.checkField(field, dst);
						}
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
	 * Checks if the given generic type represents a list of configs, or a list of lists of configs,
	 * or a list of lists of lists of ... of configs.
	 */
	private boolean isListOfConfig(ParameterizedType genericType) {
		if (genericType != null && genericType.getActualTypeArguments().length > 0) {
			Type parameter = genericType.getActualTypeArguments()[0];
			if ((parameter instanceof Class)) {
				return ((Class<?>)parameter).isAssignableFrom(UnmodifiableConfig.class);
			}
			if (parameter instanceof ParameterizedType) {
				ParameterizedType genericParameter = (ParameterizedType)parameter;
				Class<?> paramClass = (Class<?>)genericParameter.getRawType();
				return paramClass.isAssignableFrom(List.class) && isListOfConfig(genericParameter);
			}
		}
		return false;
	}

	/**
	 *
	 * @param genericType
	 * @return
	 */
	private Class<?> bottomElementType(ParameterizedType genericType) {
		if (genericType != null && genericType.getActualTypeArguments().length > 0) {
			Type parameter = genericType.getActualTypeArguments()[0];
			if (parameter instanceof ParameterizedType) {
				ParameterizedType genericParameter = (ParameterizedType)parameter;
				Class<?> paramClass = (Class<?>)genericParameter.getRawType();
				if (paramClass.isAssignableFrom(List.class)) {
					return bottomElementType(genericParameter);
				} else {
					return paramClass;
				}
			}
			if ((parameter instanceof Class)) {
				return (Class<?>)parameter;
			}
		}
		return null;
	}

	/**
	 *
	 * @param list
	 * @return
	 */
	private Class<?> bottomElementType(List<?> list) {
		for (Object elem : list) {
			if (elem instanceof List) {
				return bottomElementType((List<?>)elem);
			} else if (elem != null) {
				return elem.getClass();
			}
		}
		return null;
	}

	private void convertListToObjects(List<?> src, List<Object> dst, Class<?> dstBottomType) {
		for (Object elem : src) {
			if (elem == null) {
				dst.add(null);
			}
			if (elem instanceof List) {
				ArrayList<Object> subList = new ArrayList<>();
				convertListToObjects((List<?>)elem, subList, dstBottomType);
				subList.trimToSize();
				dst.add(subList);
			} else if (elem instanceof UnmodifiableConfig) {
				Object elementObj = createInstance(dstBottomType);
				convertToObject((UnmodifiableConfig)elem, elementObj, dstBottomType);
				dst.add(elementObj);
			} else {
				String elemType = elem.getClass().toString();
				throw new InvalidValueException("Unexpected element of type " + elemType + " in list of objects");
							}
		}
	}

	private void convertListToConfigs(List<?> src,
									  Class<?> srcBottomType,
									  List<Object> dst,
									  Config parentConfig) {
		for (Object elem : src) {
			if (elem == null) {
				dst.add(null);
			} else if (srcBottomType.isAssignableFrom(elem.getClass())) {
				Config elementConfig = parentConfig.createSubConfig();
				convertToConfig(elem, elem.getClass(), elementConfig);
				dst.add(elementConfig);
			} else if (elem instanceof List) {
				ArrayList<Object> subList = new ArrayList<>();
				convertListToConfigs((List<?>)elem, srcBottomType, subList, parentConfig);
				subList.trimToSize();
				dst.add(subList);
			} else {
				String elemType = elem.getClass().toString();
				throw new InvalidValueException("Unexpected element of type " + elemType + " in (maybe nested) list of " + srcBottomType);
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