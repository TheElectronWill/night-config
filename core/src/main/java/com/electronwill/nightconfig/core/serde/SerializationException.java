package com.electronwill.nightconfig.core.serde;

final class SerializationException extends RuntimeException {
    SerializationException(String message) {
        super(message);
    }

    SerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
