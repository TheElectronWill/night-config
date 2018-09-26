package com.electronwill.nightconfig.core.conversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.function.Predicate;

/**
 * Indicates that the value of the field must be validated by the specified validator class.
 *
 * @author TheElectronWill
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SpecValidator {
	/**
	 * The validator class used to check that the value is correct.
	 *
	 * @return the validator class
	 */
	Class<? extends Predicate<Object>> value();
}