package com.electronwill.nightconfig.core.serde;

final class DeserializationException extends RuntimeException {
    DeserializationException(String message) {
        super(message);
    }

    DeserializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
