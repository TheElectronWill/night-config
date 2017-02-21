package com.electronwill.nightconfig.core.serialization;

/**
 * @author TheElectronWill
 */
public class SerializationException extends RuntimeException {
	public SerializationException(String message) {
		super(message);
	}

	public SerializationException(String message, Throwable cause) {
		super(message, cause);
	}

	public SerializationException(Throwable cause) {
		super(cause);
	}

	public static SerializationException writeFailed(Throwable cause) {
		return new SerializationException("Failed to write data", cause);
	}
}
