package com.electronwill.nightconfig.core.utils;

/**
 * @author TheElectronWill
 */

import java.util.Objects;

/**
 * A "transparent" wrapper that directly uses the equals, hashCode and toString methods of the
 * object its wrap.
 *
 * @param <T> the type of the wrapped object
 */
public abstract class TransparentWrapper<T> {
	/**
	 * The wrapped object, not null.
	 */
	protected final T wrapped;

	public TransparentWrapper(T wrapped) {
		this.wrapped = Objects.requireNonNull(wrapped, "The wrapped object may not be null.");
	}

	@Override
	public final boolean equals(Object obj) {
		return wrapped.equals(obj);
	}

	@Override
	public final int hashCode() {
		return wrapped.hashCode();
	}

	@Override
	public final String toString() {
		return wrapped.toString();
	}
}