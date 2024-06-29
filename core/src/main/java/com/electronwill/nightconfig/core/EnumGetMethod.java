package com.electronwill.nightconfig.core;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;

/**
 * Specifies how to treat non-enum values when using
 * {@link UnmodifiableConfig#getEnum(String, Class, EnumGetMethod)}.
 */
public enum EnumGetMethod {

	/**
	 * If the config value is an enum constant, returns it as it is.
	 * If it's a String, returns the enum constant whose name exactly matches the string (string
	 * case-sensitive equality).
	 * If it's a value of another type, throws an error. In particular, throws an error if it's an
	 * Integer.
	 */
	NAME,

	/**
	 * If the config value is an enum constant, returns it as it is.
	 * If it's a String, returns the enum constant whose name matches the string, ignoring the case.
	 * If it's a value of another type, throws an error. In particular, throws an error if it's an
	 * Integer.
     *
     * This is the default method used by {@link UnmodifiableConfig} (and other classes) when
     * not explicitely specified.
	 */
	NAME_IGNORECASE,

	/**
	 * If the config value is an enum constant, returns it as it is.
	 * If it's a String, returns the enum constant whose name exactly matches the string (string
	 * case-sensitive equality).
	 * If it's an Integer, returns the enum constant whose {@code ordinal()} is equal to the value.
	 * If it's a value of another type, throws an error.
	 */
	ORDINAL_OR_NAME,

	/**
	 * If the config value is an enum constant, returns it as it is.
	 * If it's a String, returns the enum constant whose name exactly matches the string.
	 * If it's an Integer, returns the enum constant whose {@code ordinal()} is equal to the value.
	 * If it's a value of another type, throws an error.
	 */
	ORDINAL_OR_NAME_IGNORECASE;

	public boolean isCaseSensitive() {
		return this == NAME || this == ORDINAL_OR_NAME;
	}

	public boolean isOrdinalOk() {
		return this == ORDINAL_OR_NAME || this == ORDINAL_OR_NAME_IGNORECASE;
	}

	@SuppressWarnings("unchecked")
	public <T extends Enum<T>> T get(Object value, Class<T> enumType) {
		if (value == null || value == NULL_OBJECT) {
			return null;
		} else {
			final Class<?> cls = value.getClass();
			if (enumType.isAssignableFrom(cls)) {
				return (T)value;
			} else if (cls == String.class) {
				final String name = (String)value;
				if (isCaseSensitive()) {
					return Enum.valueOf(enumType, name);
				} else {
					for (T item : enumType.getEnumConstants()) {
						if (item.name().equalsIgnoreCase(name)) {
							return item;
						}
					}
					String enumName = enumType.getCanonicalName();
					throw new IllegalArgumentException("No enum constant " + enumName + "." + name);
				}
			} else if (cls == Integer.class || cls == Short.class || cls == Byte.class) {
				if (isOrdinalOk()) {
					return enumType.getEnumConstants()[(int)value];
				} else {
					throw new ClassCastException("Cannot convert an Integer to an Enum: disallowed by EnumGetMethod." + this);
				}
			} else {
				String name = cls.getCanonicalName();
				throw new ClassCastException("Cannot convert a value of type " + name + " to an Enum");
			}
		}
	}

	public <T extends Enum<T>> boolean validate(Object value, Class<T> enumType) {
		if (value == null || value == NULL_OBJECT) {
			return true;
		}
		final Class<?> cls = value.getClass();
		if (enumType.isAssignableFrom(cls)) {
			return true;
		} else if (cls == String.class) {
			final String name = (String)value;
			if (isCaseSensitive()) {
				for (T item : enumType.getEnumConstants()) {
					if (item.name().equals(name)) return true;
				}
			} else {
				for (T item : enumType.getEnumConstants()) {
					if (item.name().equalsIgnoreCase(name)) return true;
				}
			}
		} else if (cls == Integer.class && isOrdinalOk()) {
			int idx = (int)value;
			return idx >= 0 && idx < enumType.getEnumConstants().length;
		}
		return false;
	}
}