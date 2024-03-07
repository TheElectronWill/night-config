package com.electronwill.nightconfig.core;

/**
 * Singleton for representing null values in Maps that don't support them.
 */
public final class NullObject {
	/**
	 * Represents a "null" value, without being null. It has a different meaning than "no value".
	 */
	public static final NullObject NULL_OBJECT = new NullObject();

	private NullObject() {}

	@Override
	public String toString() {
		return "NULL_OBJECT";
	}

	@Override
	public boolean equals(Object o) {
		return o == this;
	}

	@Override
	public int hashCode() {
		return 0;
	}
}
