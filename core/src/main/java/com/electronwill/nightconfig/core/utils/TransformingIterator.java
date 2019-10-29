package com.electronwill.nightconfig.core.utils;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A TransformingIterator applies "just in time" transformations to an {@code Interator<I>}
 * in order to make it like an {@code Interator<E>}.
 * <p>
 * The transformations are applied "just in time", that is, the values are converted only when
 * they are used, not during the construction of the TransformingIterator.
 *
 * @author TheElectronWill
 * @see TransformingMap
 */
public final class TransformingIterator<I, E> extends TransformingBase<I, E> implements Iterator<E> {
	private final Iterator<I> internal;

	public TransformingIterator(Iterator<I> iterator,
								Function<? super I, ? extends E> readTransform) {
		super(readTransform, null, null);
		this.internal = iterator;
	}

	@Override
	public boolean hasNext() {
		return internal.hasNext();
	}

	@Override
	public E next() {
		return read(internal.next());
	}

	@Override
	public void remove() {
		internal.remove();
	}

	@Override
	public void forEachRemaining(Consumer<? super E> action) {
		internal.forEachRemaining(v -> action.accept(read(v)));
	}
}