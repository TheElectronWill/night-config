package com.electronwill.nightconfig.core.serde;

/** Java object -> Config value. */
public interface ValueSerializer<T, R> {
    R serialize(T value, SerializerContext ctx);
}
