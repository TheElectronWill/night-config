package com.electronwill.nightconfig.core.conversion;

/**
 * @author TheElectronWill
 */
public class ReflectionException extends RuntimeException {
	public ReflectionException(String message) {
		super(message);
	}

	public ReflectionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ReflectionException(Throwable cause) {
		super(cause);
	}
}