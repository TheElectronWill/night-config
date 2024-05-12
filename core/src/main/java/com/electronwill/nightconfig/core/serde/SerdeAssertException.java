package com.electronwill.nightconfig.core.serde;

public final class SerdeAssertException extends SerdeException {
    SerdeAssertException(String message) {
        super(message);
    }

    SerdeAssertException(String message, Throwable cause) {
        super(message, cause);
    }
}
