package com.electronwill.nightconfig.core.spec;

import com.electronwill.nightconfig.core.utils.ListSupplier;

import java.lang.reflect.Array;
import java.util.List;
import java.util.ListIterator;

@SuppressWarnings("unchecked")
final class CorrectionUtils {
	private CorrectionUtils() {}

	static Iterable<Object> asIterable(Object value, ListSupplier ls) throws Exception {
		if (value instanceof Iterable) {
			return (Iterable<Object>)value;
		} else if (value != null && value.getClass().isArray()) {
			return convertArrayToList(value, ls);
		} else {
			String cls = value == null ? "" : " " + value.getClass().getCanonicalName();
			throw new IncorrectValueException("Expected an Iterable, got" + cls + " " + value);
		}
	}

	private static Iterable<Object> convertArrayToList(Object value, ListSupplier ls) {
		int len = Array.getLength(value);
		List<Object> list = ls.get(len);
		for (int i = 0; i < len; i++) {
			list.add(Array.get(value, i));
		}
		return list;
	}

	static <E> CorrectionResult<Iterable<E>> correctListElements(List<Object> list, ValueCorrecter<E> correcter, ListSupplier ls) {
		boolean modified = false;
		for (ListIterator<Object> it = list.listIterator(); it.hasNext();) {
			Object elem = it.next();
			CorrectionResult<E> result = correcter.correct(elem);
			if (result.isReplaced()) {
				try {
					it.set(result.replacementValue());
					modified = true;
				} catch (Exception ex) {
					// it.set might throw UnsupportedOperationException or
					// IllegalStateException (eg. for singleton lists), or
					// many other RuntimeExceptions. That's why I catch'em all here.
					List<E> replacement = ls.get(list.size());
					return CorrectionResult.replacedBy(correctWrongIterable(list, replacement, correcter));
				}
			} else if (result.isFailed()) {
				return (CorrectionResult<Iterable<E>>)result; // propagate the failure
				// it's ok to cast and return the result as is because there is no replacement value
			}
		}
		return modified ? CorrectionResult.modified() : CorrectionResult.correct();
	}

	static <E> CorrectionResult<Iterable<E>> correctIterableIfWrong(Iterable<Object> it, ValueCorrecter<E> correcter, ListSupplier ls) {
		for (Object o : it) {
			CorrectionResult<E> result = correcter.correct(o);
			if (result.isRemoved() || result.isReplaced()) {
				return CorrectionResult.replacedBy(correctWrongIterable(it, ls.get(), correcter));
			}
		}
		return CorrectionResult.correct();
	}

	static <E> List<E> correctWrongIterable(Iterable<Object> it, List<E> dst, ValueCorrecter<E> correcter) {
		for (Object o : it) {
			CorrectionResult<E> result = correcter.correct(o);
			dst.add(result.isCorrect() ? (E)o : result.replacementValue());
		}
		return dst;
	}

}
