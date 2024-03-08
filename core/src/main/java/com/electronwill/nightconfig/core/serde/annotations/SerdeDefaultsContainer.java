package com.electronwill.nightconfig.core.serde.annotations;

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A container for multiple {@link SerdeDefault} annotations, because
 * that's how {@link Repeatable} annotations are implemented in Java.
 * <p>
 * You probably do NOT want to use this annotation in your code:
 * you should simply repeat the {@code @SerdeDefault} annotation multiple times.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface SerdeDefaultsContainer {
	SerdeDefault[] value();
}
