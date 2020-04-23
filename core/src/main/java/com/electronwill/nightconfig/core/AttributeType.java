package com.electronwill.nightconfig.core;

/**
 * Represents a type of attribute attached to a configuration entry.
 *
 * @param <T> the type of the attribute's value
 */
public class AttributeType<T> {
	private final String name;

	public AttributeType(String name) {
		this.name = name;
	}

	public String getName() {
		return this.name;
	}
}
