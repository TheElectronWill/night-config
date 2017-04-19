package com.electronwill.nightconfig.core.conversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author TheElectronWill
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE})
public @interface Path {
	/**
	 * The path of the value in the configuration. Each key is separated by a dot.
	 * <p>
	 * Use {@link AdvancedPath} if you have a key that contains dots.
	 *
	 * @return the path in the config
	 */
	String value();
}
