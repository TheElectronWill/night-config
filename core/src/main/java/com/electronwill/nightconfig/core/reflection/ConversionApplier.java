package com.electronwill.nightconfig.core.reflection;

/**
 * Converts objects of type A to objects of type B.
 *
 * @author TheElectronWill
 * @see ConversionChecker
 */
@FunctionalInterface
public interface ConversionApplier<A, B> {

	/**
	 * Converts an object of type A to an object to type B.
	 *
	 * @param a the object to convert.
	 * @return a new object of type B.
	 */
	B convert(A a);
}
