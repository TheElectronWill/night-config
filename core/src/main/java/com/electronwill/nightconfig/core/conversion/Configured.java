package com.electronwill.nightconfig.core.conversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a class or field is designed to be read from a configuration.
 *
 * @author TheElectronWill
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.FIELD})
public @interface Configured {
	/**
	 * The path of the value in the configuration. If it's an array of size 0 (the default value)
	 * then the field's name (if it's a field) or the root path (if it's a class) is used.
	 */
	String[] path() default {};
}