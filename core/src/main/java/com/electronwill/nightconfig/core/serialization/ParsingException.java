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

	public static ParsingException readFailed(Throwable cause) {
		return new ParsingException("Failed to read data", cause);
	}

	public static ParsingException notEnoughData() {
		return new ParsingException("Not enough data available");
	}
}
