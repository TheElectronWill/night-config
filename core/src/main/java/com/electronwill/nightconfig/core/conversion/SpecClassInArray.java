package com.electronwill.nightconfig.core.conversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that the value of a field must have a specific class.
 *
 * @author TheElectronWill
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SpecClassInArray {
	/**
	 * @return the classes that are allowed
	 */
	Class<?>[] acceptableClasses();

	/**
	 * @return {@code true} to allow only the acceptable classes, {@code false} to allow their
	 * subclasses too.
	 */
	boolean strict() default false;
}
