package com.electronwill.nightconfig.core.serde;

public final class SerdeException extends RuntimeException {
    public SerdeException(String message) {
        super(message);
    }

    public SerdeException(String message, Throwable cause) {
        super(message, cause);
    }
}
