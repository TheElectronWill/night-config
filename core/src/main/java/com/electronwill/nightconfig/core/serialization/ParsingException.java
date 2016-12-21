package com.electronwill.nightconfig.core.serialization;

/**
 * @author TheElectronWill
 */
public class ParsingException extends RuntimeException {
	public ParsingException(String message) {
		super(message);
	}

	public ParsingException(String message, Throwable cause) {
		super(message, cause);
	}

	public ParsingException(Throwable cause) {
		super(cause);
	}
}
