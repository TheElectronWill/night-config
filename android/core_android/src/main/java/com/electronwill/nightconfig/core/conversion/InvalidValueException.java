package com.electronwill.nightconfig.core.conversion;

/**
 * Thrown when a value that is associated to a field, or that should become associated to a
 * field, doesn't conform to the @Spec(something) annotation of that field.
 *
 * @author TheElectronWill
 */
public final class InvalidValueException extends RuntimeException {
	public InvalidValueException(String message) {
		super(message);
	}

	public InvalidValueException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Creates a new InvalidException with a formatted message.
	 *
	 * @param messageFormat a format string
	 * @param args          the arguments
	 * @see String#format(String, Object...)
	 */
	public InvalidValueException(String messageFormat, Object... args) {
		super(String.format(messageFormat, args));
	}
}