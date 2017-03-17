package com.electronwill.nightconfig.core.utils;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * @author TheElectronWill
 */
public final class TransformingIterator<InternalV, ExternalV> implements Iterator<ExternalV> {
	private final Function<InternalV, ExternalV> readTransformation;
	private final Iterator<InternalV> internalIterator;

	public TransformingIterator(Iterator<InternalV> internalIterator,
								Function<InternalV, ExternalV> readTransformation) {
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
}