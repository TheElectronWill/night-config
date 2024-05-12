package com.electronwill.nightconfig.core.serde.annotations;

import java.lang.annotation.*;

/**
 * A container for multiple {@link SerdeAssert} annotations, because
 * that's how {@link Repeatable} annotations are implemented in Java.
 * <p>
 * You probably do NOT want to use this annotation in your code:
 * you should simply repeat the {@code @SerdeAssert} annotation multiple times.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SerdeAssertsContainer {
	SerdeAssert[] value();
}
