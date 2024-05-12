package com.electronwill.nightconfig.core.serde;

/**
 * Thrown when an assertion, specified on a field by an annotation of type
 * {@link com.electronwill.nightconfig.core.serde.annotations.SerdeAssert},
 * is not verified during (de)serialization.
 */
public final class SerdeAssertException extends SerdeException {
    SerdeAssertException(String message) {
        super(message);
    }

    SerdeAssertException(String message, Throwable cause) {
        super(message, cause);
    }
}
