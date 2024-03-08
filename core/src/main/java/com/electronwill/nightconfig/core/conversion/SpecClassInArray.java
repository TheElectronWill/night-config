package com.electronwill.nightconfig.core.conversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that the value of a field must have a specific class.
 *
 * @deprecated Use the new package {@link com.electronwill.nightconfig.core.serde} with {@code serde.annotations}.
 * @author TheElectronWill
 */
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SpecClassInArray {
	/**
	 * @return the classes that are allowed
	 */
	Class<?>[] value();

	/**
	 * @return {@code true} to allow only the acceptable classes, {@code false} to allow their
	 * subclasses too. Default is {@code false}.
	 */
	boolean strict() default false;
}