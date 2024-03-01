package com.electronwill.nightconfig.core.serde;

import com.electronwill.nightconfig.core.ConfigFormat;

final class SerializationException extends RuntimeException {
    SerializationException(String message) {
        super(message);
    }

    SerializationException(String message, Throwable cause) {
        super(message, cause);
    }

    static SerializationException noSerializerFound(Object value, Class<?> valueClass,
            SerializerContext ctx) {
        ConfigFormat<?> format = ctx.configFormat();
        String supportedStr;
        if (format == null) {
            supportedStr = "The current SerializerContext has no ConfigFormat. Is there a bug in the implementation of the chosen Config type?";
        } else if (format.supportsType(valueClass)) {
            supportedStr = "The value's type is supported by the ConfigFormat of the current SerializerContext.";
        } else {
            supportedStr = "The value's type is NOT supported by the ConfigFormat of the current SerializerContext.";
        }
        String ofTypeStr = valueClass == null ? "" : " of type " + valueClass;
        return new SerializationException(
                "No suitable serializer found for value" + ofTypeStr + ": " + value + ". "
                        + supportedStr);
    }
}
