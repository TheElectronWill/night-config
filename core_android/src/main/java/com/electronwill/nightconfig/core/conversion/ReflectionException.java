package com.electronwill.nightconfig.core.conversion;

/**
 * Thrown when an important reflective operation fails.
 *
 * @author TheElectronWill
 */
public class ReflectionException extends RuntimeException {
	public ReflectionException(String message) {
		super(message);
	}

	public ReflectionException(String message, Throwable cause) {
		super(message, cause);
	}
}