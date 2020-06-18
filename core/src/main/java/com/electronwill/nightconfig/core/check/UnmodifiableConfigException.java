package com.electronwill.nightconfig.core.check;

import com.electronwill.nightconfig.core.utils.StringUtils;

/**
 * Thrown when someone tries to modify an unmodifiable and checked config.
 */
public final class UnmodifiableConfigException extends RuntimeException {
	private final String[] path;

	public UnmodifiableConfigException(String[] path) {
		super("Cannot upadte " + StringUtils.joinPath(path) + " because this config is unmodifiable.");
		this.path = path;
	}

	public String[] getPath() {
		return path;
	}
}
