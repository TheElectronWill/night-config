package com.electronwill.nightconfig.core.conversion;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies that the value of a field must be a String in a certain range (inclusive, comparison
 * done lexicographically).
 *
 * @author TheElectronWill
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface SpecStringInRange {
	/** @return the minimum possible value, inclusive */
	String min();

	/** @return the maximum possible value, inclusive */
	String max();
}