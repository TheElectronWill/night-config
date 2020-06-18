package com.electronwill.nightconfig.core.check;

import com.electronwill.nightconfig.core.AttributeType;
import com.electronwill.nightconfig.core.StandardAttributes;
import com.electronwill.nightconfig.core.utils.StringUtils;

import java.util.List;

/**
 * Thrown when someone attempts to assign an invalid value to a config entry.
 */
public final class IncorrectUpdateException extends RuntimeException {
	private final String[] path;
	private final AttributeType<?> attribute;
	private final Object incorrectValue;

	public IncorrectUpdateException(String[] path, Object incorrectValue) {
		super("Incorrect update " + StringUtils.joinPath(path) + " = " + incorrectValue);
		this.path = path;
		this.attribute = null;
		this.incorrectValue = incorrectValue;
	}

	public IncorrectUpdateException(AttributeType<?> attribute, String[] path, Object incorrectValue) {
		super("Incorrect " + attribute.getName() + " update " + StringUtils.joinPath(path) + " = " + incorrectValue);
		this.path = path;
		this.attribute = attribute;
		this.incorrectValue = incorrectValue;
	}

	public String[] getPath() {
		return path;
	}

	public AttributeType<?> getAttribute() {
		return attribute;
	}

	public Object getValue() {
		return incorrectValue;
	}
}
