package com.electronwill.nightconfig.core.serde;

/**
 * Thrown when an error occurs during the serialization
 * or deserialization process.
 */
public class SerdeException extends RuntimeException {
    SerdeException(String message) {
        super(message);
    }

    SerdeException(String message, Throwable cause) {
        super(message, cause);
    }
}
