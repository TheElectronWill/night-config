package com.electronwill.nightconfig.core.serde;

public class SerdeException extends RuntimeException {
    SerdeException(String message) {
        super(message);
    }

    SerdeException(String message, Throwable cause) {
        super(message, cause);
    }
}
