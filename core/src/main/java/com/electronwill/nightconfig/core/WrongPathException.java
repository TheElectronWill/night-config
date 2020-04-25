package com.electronwill.nightconfig.core;

public final class WrongPathException extends RuntimeException {

	public WrongPathException(String message) {
		super(message);
	}

	static WrongPathException missingIntermediateLevel(String path, String level) {
		return new WrongPathException(String.format(
			"Wrong path %s: intermediate level %s doesn't exist",
			path, level));
	}

	static WrongPathException nullIntermediateLevel(String path, String level) {
		return new WrongPathException(String.format(
			"Wrong path %s: intermediate level %s has a null value",
			path, level));
	}

	static WrongPathException incompatibleIntermediateLevel(String path, String level, Object problemValue) {
		return new WrongPathException(String.format(
			"Wrong path %s: intermediate level %s is not a config, but an instance of %s",
			path, level, problemValue.getClass().getCanonicalName()));
	}
}
