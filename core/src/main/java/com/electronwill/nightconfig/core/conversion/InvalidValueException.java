package com.electronwill.nightconfig.core.conversion;

/**
 * @author TheElectronWill
 */
public final class InvalidValueException extends RuntimeException {
	public InvalidValueException(String message) {
		super(message);
	}

	public InvalidValueException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidValueException(String messageFormat, Object... args) {
		super(String.format(messageFormat, args));
	}
}
