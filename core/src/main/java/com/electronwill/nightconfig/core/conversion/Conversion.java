package com.electronwill.nightconfig.core.conversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that the value of the field must be converted with the specified converter class.
 *
 * @author TheElectronWill
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Conversion {
	/**
	 * The conversion class used to convert the field's value when it is put into/read from a
	 * configuration
	 *
	 * @return the conversion class
	 */
	Class<? extends Converter<?, ?>> value();
}
