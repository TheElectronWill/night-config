package com.electronwill.nightconfig.core.serde;

/** Java object -> Config value. */
public interface ValueSerializer<T> {
    Object serialize(T value, SerializerContext ctx);
}
