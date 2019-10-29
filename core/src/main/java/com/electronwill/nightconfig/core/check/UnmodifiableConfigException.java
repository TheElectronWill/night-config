package com.electronwill.nightconfig.core.check;

import com.electronwill.nightconfig.core.AttributeType;
import com.electronwill.nightconfig.core.utils.StringUtils;

import java.util.List;

/**
 * Thrown when someone tries to modify an unmodifiable and checked config.
 */
public final class UnmodifiableConfigException extends RuntimeException {
	private final String[] path;
	private final AttributeType<?> attribute;

	public UnmodifiableConfigException(String[] path, AttributeType<?> attribute) {
		super("Cannot modify " + attribute + " at " + StringUtils.joinPath(path)
				  + " because this config is unmodifiable.");
		this.path = path;
		this.attribute = attribute;
	}

	public String[] getPath() {
		return path;
	}

	public AttributeType<?> getAttribute() {
		return attribute;
	}
}
