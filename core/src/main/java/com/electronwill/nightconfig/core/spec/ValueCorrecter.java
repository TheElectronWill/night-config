package com.electronwill.nightconfig.core.spec;

import com.electronwill.nightconfig.core.NightConfig;
import com.electronwill.nightconfig.core.check.ValueChecker;
import com.electronwill.nightconfig.core.utils.ListSupplier;

import java.util.*;
import java.util.function.Supplier;

import static com.electronwill.nightconfig.core.check.ValueChecker.not;
import static com.electronwill.nightconfig.core.spec.CorrectionResult.*;

/**
 * Corrects configuration values, ie provides correct values from incorrect ones.
 * @param <T> type of value
 */
@FunctionalInterface
public interface ValueCorrecter<T> {
	/**
	 * Fixes a config value by providing a new value or by modifying the existing value.
	 *
	 * @param value the value to correct
	 * @return the result of the correction
	 */
	CorrectionResult<T> correct(Object value);

	/**
	 * If the correction fails, recovers with another result.
	 *
	 * @param recovery supplies the result to return in case of failure
	 * @return a ValueCorrecter that returns the given result instead of {@link CorrectionResult#failed(Throwable)}.
	 */
	default ValueCorrecter<T> onFailure(Supplier<CorrectionResult<T>> recovery) {
		return value -> {
			CorrectionResult<T> res = correct(value);
			return res.isFailed() ? recovery.get() : res;
		};
	}

	/**
	 * If the correction fails, replaces the value with a default one.
	 *
	 * @param recoveryValue the value to use in case of failure
	 * @return a ValueCorrecter that returns {@code replacedBy(recoveryValue)} instead of {@code failed}.
	 */
	default ValueCorrecter<T> replaceFailure(T recoveryValue) {
		return this.onFailure(() -> CorrectionResult.replacedBy(recoveryValue));
	}

	/**
	 * If the correction fails, replaces the value with a default one.
	 *
	 * @param recoveryValue supplies the value to use in case of failure
	 * @return a ValueCorrecter that returns {@code replacedBy(recoveryValue)} instead of {@code failed}.
	 */
	default ValueCorrecter<T> replaceFailure(Supplier<T> recoveryValue) {
		return this.onFailure(() -> CorrectionResult.replacedBy(recoveryValue.get()));
	}

	/**
	 * If the correction fails, removes the value.
	 *
	 * @return a ValueCorrecter that returns {@link CorrectionResult#removed()} instead of {@code failed}.
	 */
	default ValueCorrecter<T> removeFailure() {
		return this.onFailure(CorrectionResult::removed);
	}

	// --- STATIC METHODS ---

	// ------ CORRECTION OF SIMPLE VALUES ------

	/**
	 * Fails if the value doesn't fulfill some criteria.
	 */
	static <T> ValueCorrecter<T> failIfNot(ValueChecker checker) {
		return value -> {
			try {
				if (checker.check(value)) {
					return CorrectionResult.correct();
				}
			} catch (Exception ex) {
				return CorrectionResult.failed(ex);
			}
			return failed(new IncorrectValueException(value));
		};
	}

	static <T> ValueCorrecter<T> replaceIfNot(T replacement, ValueChecker checker) {
		return value -> {
			try {
				return checker.check(value) ? CorrectionResult.correct() : replacedBy(replacement);
			} catch (Exception ex) {
				return CorrectionResult.failed(ex);
			}
		};
	}

	static <T> ValueCorrecter<T> replaceIfNot(Supplier<T> replacement, ValueChecker checker) {
		return value -> {
			try {
				return checker.check(value) ? CorrectionResult.correct() : replacedBy(replacement.get());
			} catch (Exception ex) {
				return CorrectionResult.failed(ex);
			}
		};
	}

	static <T> ValueCorrecter<T> failIf(ValueChecker checker) {
		return failIfNot(not(checker));
	}

	static <T> ValueCorrecter<T> replaceIf(T replacement, ValueChecker checker) {
		return replaceIfNot(replacement, not(checker));
	}

	static <T> ValueCorrecter<T> replaceIf(Supplier<T> replacement, ValueChecker checker) {
		return replaceIfNot(replacement, not(checker));
	}

	// ------ CORRECTION OF ITERABLES ------

	static <E> ValueCorrecter<Iterable<E>> replaceElementIfNot(E replacement, ValueChecker checker) {
		return correctElements(NightConfig.getDefaultListSupplier(), replaceIfNot(replacement, checker));
	}

	static <E> ValueCorrecter<Iterable<E>> replaceElementIfNot(Supplier<E> replacement, ValueChecker checker) {
		return correctElements(NightConfig.getDefaultListSupplier(), replaceIfNot(replacement, checker));
	}

	static <E> ValueCorrecter<Iterable<E>> removeElementIfNot(ValueChecker checker) {
		ValueCorrecter<E> elementCorrecter = e -> checker.check(e) ? CorrectionResult.correct() : removed();
		return correctElements(NightConfig.getDefaultListSupplier(), elementCorrecter);
	}

	static <E> ValueCorrecter<Iterable<E>> replaceElementIf(E replacement, ValueChecker checker) {
		return replaceElementIfNot(replacement, not(checker));
	}

	static <E> ValueCorrecter<Iterable<E>> replaceElementIf(Supplier<E> replacement, ValueChecker checker) {
		return replaceElementIfNot(replacement, not(checker));
	}

	static <E> ValueCorrecter<Iterable<E>> removeElementIf(ValueChecker checker) {
		return removeElementIfNot(not(checker));
	}

	static <E> ValueCorrecter<Iterable<E>> correctElements(ValueCorrecter<E> elementCorrecter) {
		return correctElements(NightConfig.getDefaultListSupplier(), elementCorrecter);
	}

	static <E> ValueCorrecter<Iterable<E>> correctElements(ListSupplier ls, ValueCorrecter<E> elementCorrecter) {
		return value -> {
			if (value instanceof List) {
				return CorrectionUtils.correctListElements((List<Object>)value, elementCorrecter, ls);
			} else {
				try {
					Iterable<Object> it = CorrectionUtils.asIterable(value, ls);
					return CorrectionUtils.correctIterableIfWrong(it, elementCorrecter, ls);
				} catch (Exception ex) {
					return CorrectionResult.failed(ex);
				}
			}
		};
	}
}
