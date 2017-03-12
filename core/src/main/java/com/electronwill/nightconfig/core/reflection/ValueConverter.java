package com.electronwill.nightconfig.core.reflection;

import java.util.Objects;

/**
 * Converts objects of type A to objects of type B.
 *
 * @param <A>
 * @param <B>
 * @author TheElectronWill
 */
final class ValueConverter<A, B> implements ConversionChecker<A>, ConversionApplier<A, B> {

	final ConversionChecker<A> checker;
	final ConversionApplier<A, B> applier;

	ValueConverter(ConversionChecker<A> checker, ConversionApplier<A, B> applier) {
		Objects.requireNonNull(checker, "The ConversionChecker must not be null.");
		Objects.requireNonNull(applier, "The ConversionApplier must not be null.");
		this.checker = checker;
		this.applier = applier;
	}

	public boolean canConvert(A a) {
		return checker.canConvert(a);
	}

	public B convert(A a) {
		return applier.convert(a);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		ValueConverter<?, ?> that = (ValueConverter<?, ?>)o;

		return checker.equals(that.checker) && applier.equals(that.applier);
	}

	@Override
	public int hashCode() {
		int result = checker.hashCode();
		result = 31 * result + applier.hashCode();
		return result;
	}
}
