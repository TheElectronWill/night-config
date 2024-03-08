package com.electronwill.nightconfig.core.serde;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import com.electronwill.nightconfig.core.UnmodifiableConfig;

final class Util {
	static boolean isEmpty(Object configValue) {
		if (configValue instanceof Collection) {
			return ((Collection<?>) configValue).isEmpty();
		}
		if (configValue instanceof Map) {
			return ((Map<?, ?>) configValue).isEmpty();
		}
		if (configValue instanceof UnmodifiableConfig) {
			return ((UnmodifiableConfig) configValue).isEmpty();
		}
		if (configValue instanceof CharSequence) {
			return ((CharSequence) configValue).length() == 0;
		}
		if (configValue instanceof Optional) {
			return !((Optional<?>) configValue).isPresent();
		}
		if (configValue instanceof java.nio.Buffer) {
			((java.nio.Buffer) configValue).hasRemaining();
		}
		Class<?> cls = configValue.getClass();
		if (cls.isArray()) {
			return Array.getLength(configValue) == 0;
		}
		return isEmptyWithReflection(cls, configValue);
	}

	private static boolean isEmptyWithReflection(Class<?> cls, Object configValue) {
		AdditionalEmptyables.EmptyableClass scalaIterableOnce = AdditionalEmptyables.scalaIterableOnce;
		if (scalaIterableOnce != null && scalaIterableOnce.isInstance(cls)) {
			return scalaIterableOnce.isEmpty(configValue);
		}

		AdditionalEmptyables.EmptyableClass kotlinCollection = AdditionalEmptyables.kotlinCollection;
		if (kotlinCollection != null && kotlinCollection.isInstance(cls)) {
			return kotlinCollection.isEmpty(configValue);
		}

		Method isEmptyMethod;
		try {
			isEmptyMethod = cls.getMethod("isEmpty");
		} catch (NoSuchMethodException | SecurityException e) {
			return false;
		}
		if (isEmptyMethod.getReturnType() != Boolean.TYPE) {
			return false;
		}
		// TODO add to cache?
		try {
			return (boolean) isEmptyMethod.invoke(configValue);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			throw new SerdeException("Exception during call to isEmpty() on " + configValue, e);
		}
	}

	private static final class AdditionalEmptyables {
		static final EmptyableClass scalaIterableOnce = classOrNull("scala.collection.IterableOnce");
		static final EmptyableClass kotlinCollection = classOrNull("kotlin.collections.Collection");
		// TODO add a cache for discovered emptyable classes?

		private static EmptyableClass classOrNull(String fullName) {
			Class<?> cls;
			try {
				cls = Class.forName(fullName);
			} catch (ClassNotFoundException e) {
				return null;
			}
			Method m;
			try {
				m = cls.getMethod("isEmpty");
			} catch (NoSuchMethodException | SecurityException e) {
				return null;
			}
			if (m.getReturnType() != Boolean.TYPE) {
				return null;
			}
			return new EmptyableClass(cls, m);
		}

		private static class EmptyableClass {
			final Class<?> cls;
			final Method isEmptyMethod;

			EmptyableClass(Class<?> cls, Method isEmptyMethod) {
				this.cls = cls;
				this.isEmptyMethod = isEmptyMethod;
			}

			boolean isInstance(Class<?> instanceClass) {
				return cls.isAssignableFrom(instanceClass);
			}

			boolean isEmpty(Object instance) {
				try {
					return (boolean) isEmptyMethod.invoke(instance);
				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
					throw new SerdeException("Exception during call to isEmpty() on " + instance, e);
				}
			}
		}
	}

	/**
	 * A better version of {@link Class#isAssignableFrom(Class)} that works with
	 * null,
	 * widening primitive conversions and conversions between primitive types and
	 * wrapper types.
	 *
	 * @param fieldType the type of the field
	 * @param valueType the type of the value
	 * @return true if a field of this type can receive a value of that type
	 */
	static boolean canAssign(Class<?> fieldType, Class<?> valueType) {
		if (valueType == null) {
			return !fieldType.isPrimitive();
		}
		if (fieldType.isPrimitive() || valueType.isPrimitive()) {
			TypeAndOrder a = fieldType.isPrimitive() ? PRIMITIVE_TO_WRAPPER.get(fieldType)
					: WRAPPER_TO_PRIMITIVE.get(fieldType);
			TypeAndOrder b = valueType.isPrimitive() ? PRIMITIVE_TO_WRAPPER.get(valueType)
					: WRAPPER_TO_PRIMITIVE.get(valueType);
			return a != null && b != null && a.canAssignValue(b);
		}
		return fieldType.isAssignableFrom(valueType);
	}

	static boolean isPrimitiveOrWrapper(Class<?> type) {
		return type.isPrimitive() || WRAPPER_TO_PRIMITIVE.get(type) != null;
	}

	private static final IdentityHashMap<Class<?>, TypeAndOrder> PRIMITIVE_TO_WRAPPER = new IdentityHashMap<>();
	private static final IdentityHashMap<Class<?>, TypeAndOrder> WRAPPER_TO_PRIMITIVE = new IdentityHashMap<>();

	static void addPrimitiveAndWrapper(Class<?> primitiveType, Class<?> wrapperType) {
		PRIMITIVE_TO_WRAPPER.put(primitiveType,
				new TypeAndOrder(PRIMITIVE_TO_WRAPPER.size(), wrapperType));
		WRAPPER_TO_PRIMITIVE.put(wrapperType,
				new TypeAndOrder(WRAPPER_TO_PRIMITIVE.size(), primitiveType));
	}

	static {
		addPrimitiveAndWrapper(Boolean.TYPE, Boolean.class);
		addPrimitiveAndWrapper(Byte.TYPE, Byte.class);
		addPrimitiveAndWrapper(Short.TYPE, Short.class);
		addPrimitiveAndWrapper(Character.TYPE, Character.class);
		addPrimitiveAndWrapper(Integer.TYPE, Integer.class);
		addPrimitiveAndWrapper(Long.TYPE, Long.class);
		addPrimitiveAndWrapper(Float.TYPE, Float.class);
		addPrimitiveAndWrapper(Double.TYPE, Double.class);
	}

	private static final class TypeAndOrder {
		final int order;
		final Class<?> type;

		TypeAndOrder(int order, Class<?> type) {
			this.order = order;
			this.type = type;
		}

		boolean canAssignValue(TypeAndOrder valueType) {
			// no widening conversion for boolean
			if (this.order == 0) {
				return valueType.order == 0;
			} else if (valueType.order == 0) {
				return false;
			}
			// widening conversions for numbers: int <- short, float <- int, ...
			return this.order >= valueType.order;
		}

		@Override
		public String toString() {
			return "TypeAndOrder [order=" + order + ", type=" + type + "]";
		}

	}
}
