package com.electronwill.nightconfig.core.reflection;

/**
 * Checks if a conversion is possible.
 *
 * @author TheElectronWill
 * @see ConversionApplier
 */
@FunctionalInterface
public interface ConversionChecker<A> {
	boolean canConvert(A a);
}
