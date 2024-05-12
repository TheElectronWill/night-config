package com.electronwill.nightconfig.core.serde;

/** Turns a Java object of type {@code T} into a Config value of type {@code R}. */
public interface ValueSerializer<T, R> {
    R serialize(T value, SerializerContext ctx);
}
