package com.electronwill.nightconfig.core.utils;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A TransformingIterator applies "just in time" transformations to an {@code Interator<InternalV>}
 * in order to make it like an {@code Interator<ExternalV>}.
 * <p>
 * The transformations are applied "just in time", that is, the values are converted only when
 * they are used, not during the construction of the TransformingIterator.
 *
 * @author TheElectronWill
 * @see TransformingMap
 */
public class TransformingIterator<InternalV, ExternalV> implements Iterator<ExternalV> {
	protected final Function<? super InternalV, ? extends ExternalV> readTransformation;
	protected final Iterator<InternalV> internalIterator;

	public TransformingIterator(Iterator<InternalV> internalIterator,
								Function<? super InternalV, ? extends ExternalV> readTransformation) {
		this.readTransformation = readTransformation;
		this.internalIterator = internalIterator;
	}

	@Override
	public boolean hasNext() {
		return internalIterator.hasNext();
	}

	@Override
	public ExternalV next() {
		return readTransformation.apply(internalIterator.next());
	}

	@Override
	public void remove() {
		internalIterator.remove();
	}

	@Override
	public void forEachRemaining(Consumer<? super ExternalV> action) {
		internalIterator.forEachRemaining(
				internalV -> action.accept(readTransformation.apply(internalV)));
	}

	@Override
	public int hashCode() {
		return internalIterator.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return internalIterator.equals(obj);
	}

	@Override
	public String toString() {
		return internalIterator.toString();
	}
}