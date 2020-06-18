package com.electronwill.nightconfig.core.spec;

import com.electronwill.nightconfig.core.check.ValueChecker;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Supplier;

import static com.electronwill.nightconfig.core.spec.CorrectionResult.*;

/**
 * Corrects configuration values, ie provides correct values from incorrect ones.
 * @param <T> type of value
 */
@FunctionalInterface
public interface ValueCorrecter<T> {
	/**
	 * Fix a config value by providing a new value or by modifying the existing value.
	 *
	 * @param value the value to correct
	 * @return the result of the correction
	 */
	CorrectionResult<T> correct(Object value);

	// --- STATIC METHODS ---
	static <T> ValueCorrecter<T> failIfNot(ValueChecker checker) {
		return value -> {
			if (checker.check(value)) {
				return CorrectionResult.correct();
			}
			return failed(new IncorrectValueException(value));
		};
	}

	static <T> ValueCorrecter<T> replaceIfNot(T replace, ValueChecker checker) {
		return value -> checker.check(value) ? CorrectionResult.correct() : replacedBy(replace);
	}

	static <T> ValueCorrecter<T> replaceIfNot(Supplier<T> replace, ValueChecker checker) {
		return value -> checker.check(value) ? CorrectionResult.correct() : replacedBy(replace.get());
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	static <E, C extends Collection<E>> ValueCorrecter<C> correctElements(
			Supplier<C> replacementSupplier, ValueCorrecter<E> elementCorrecter) {
		return value -> {
			if (!(value instanceof Iterable)) {
				return replacedBy(replacementSupplier.get());
			}
			// Correct the collection in-place if possible
			if (value instanceof List) {
				List<Object> l = (List)value;
				int state = 0; // 0: all elements are correct, 1: some replaced, -1: exception
				for (ListIterator<Object> it = l.listIterator(); it.hasNext(); ) {
					Object elem = it.next();
					CorrectionResult<E> result = elementCorrecter.correct(elem);
					if (result.isReplaced()) {
						try {
							it.set(result.replacementValue());
							state = 1;
						} catch (Exception ex) {
							// it.set might throw UnsupportedOperationException or
							// IllegalStateException (eg. for singleton lists), or
							// many other RuntimeExceptions. That's why I catch'em all here.
							state = -1;
							break;
						}
					} else if (result.isFailed()) {
						return (CorrectionResult<C>)result; // propagate the failure
						// it's ok to cast and return the result as is because there is no replacement value
					}
				}
				if (state == 0) {
					return CorrectionResult.correct();
				} else if (state == 1) {
					return modified();
				} // else: continue below
			}
			// Otherwise, build a new collection
			C newCollection = null;
			for (Object elem : (Iterable)value) {
				CorrectionResult<E> result = elementCorrecter.correct(elem);
				if (newCollection == null && !result.isCorrect()) {
					newCollection = replacementSupplier.get();
				}
				if (newCollection != null) {
					E okElement = result.isReplaced() ? result.replacementValue() : (E)elem;
					newCollection.add(okElement);
				}
			}
			return (newCollection == null) ? CorrectionResult.correct() : replacedBy(newCollection);
		};
	}
}
