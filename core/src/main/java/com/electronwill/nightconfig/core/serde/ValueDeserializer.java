package com.electronwill.nightconfig.core.serde;

import java.util.Optional;

/** Turn a config value of type {@code T} into a Java object of type {@code R}. */
public interface ValueDeserializer<T, R> {
	R deserialize(T value, Optional<TypeConstraint> resultType, DeserializerContext ctx);
}
