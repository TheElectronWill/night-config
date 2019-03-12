package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.EnumGetMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that the value of a field must correspond to an enum and
 * that the value must be read using the given {@link EnumGetMethod}.
 *
 * @author TheElectronWill
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SpecEnum {
	/**
	 * How to interpret the config value. For instance, should we match the name and the ordinal()
	 * or just the name? Should we ignore the case of the string value or not?
	 */
	EnumGetMethod method();
}
