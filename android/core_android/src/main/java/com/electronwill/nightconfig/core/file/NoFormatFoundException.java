package com.electronwill.nightconfig.core.file;

/**
 * @author TheElectronWill
 */
public class NoFormatFoundException extends RuntimeException {
	public NoFormatFoundException(String message) {
		super(message);
	}

	public NoFormatFoundException(String message, Throwable cause) {
		super(message, cause);
	}
}