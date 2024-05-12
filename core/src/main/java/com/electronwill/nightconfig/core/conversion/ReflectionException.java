package com.electronwill.nightconfig.core.conversion;

/**
 * Thrown when an important reflective operation fails.
 *
 * @deprecated Use the new package
 *             {@link com.electronwill.nightconfig.core.serde} with
 *             {@code serde.annotations}.
 * @author TheElectronWill
 */
@Deprecated
public class ReflectionException extends RuntimeException {
	public ReflectionException(String message) {
		super(message);
	}

	public ReflectionException(String message, Throwable cause) {
		super(message, cause);
	}
}
