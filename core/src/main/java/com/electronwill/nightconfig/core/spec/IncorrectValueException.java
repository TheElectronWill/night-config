package com.electronwill.nightconfig.core.spec;

/**
 * Thrown when a value is incorrect and cannot be corrected.
 */
public final class IncorrectValueException extends RuntimeException {
	private final Object value;

	public IncorrectValueException(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}
}
