package com.electronwill.nightconfig.core.conversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates the path, in the config, of the annotated element. Unlike {@link AdvancedPath}, Path
 * accepts a simple String, so it's not possible to use dots in key names (because dots are
 * interpreted as a separator between each key). If you have a complicated path with dots in key
 * names, use {@link AdvancedPath} instead of Path.
 *
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
