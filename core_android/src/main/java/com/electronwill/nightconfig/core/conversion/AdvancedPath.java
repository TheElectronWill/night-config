package com.electronwill.nightconfig.core.conversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the path, in the config, of the annotated element. Unlike {@link Path}, AdvancedPath
 * accepts an array of String, so it's possible to use dots in key names.
 *
 * @author TheElectronWill
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface AdvancedPath {
	/**
	 * The path of the value in the configuration. Each key is given by an element of the array.
	 *
	 * @return the path in the config
	 */
	String[] value();
}
