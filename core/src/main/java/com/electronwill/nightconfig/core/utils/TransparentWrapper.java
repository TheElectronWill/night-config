package com.electronwill.nightconfig.core.utils;

/**
 * @author TheElectronWill
 */

import java.util.Objects;

/**
 * A "transparent" wrapper that directly uses the equals, hashCode and toString methods of the object
 * its wrap.
 *
 * @param <T> the type of the wrapped object
 */
public abstract class TransparentWrapper<T> {
	protected final T wrapped;//the wrapped object, not null

	public TransparentWrapper(T wrapped) {this.wrapped = Objects.requireNonNull(wrapped);}

	@Override
	public boolean equals(Object obj) {
		return wrapped.equals(obj);
	}

	@Override
	public int hashCode() {
		return wrapped.hashCode();
	}

	@Override
	public String toString() {
		return wrapped.toString();
	}
}