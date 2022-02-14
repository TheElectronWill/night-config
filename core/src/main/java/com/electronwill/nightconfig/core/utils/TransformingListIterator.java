package com.electronwill.nightconfig.core.utils;

import java.util.ListIterator;
import java.util.function.Function;

public final class TransformingListIterator<InternalV, ExternalV>
	    extends TransformingIterator<InternalV, ExternalV> implements ListIterator<ExternalV> {
	private final Function<? super ExternalV, ? extends InternalV> writeTransformation;

	public TransformingListIterator(ListIterator<InternalV> internalIterator,
									Function<? super InternalV, ? extends ExternalV> readTransformation,
									Function<? super ExternalV, ? extends InternalV> writeTransformation) {
		super(internalIterator, readTransformation);
		this.writeTransformation = writeTransformation;
	}

	@Override
	public boolean hasPrevious() {
		return ((ListIterator<InternalV>) internalIterator).hasPrevious();
	}

	@Override
	public ExternalV previous() {
		return readTransformation.apply(((ListIterator<InternalV>) internalIterator).previous());
	}

	@Override
	public int nextIndex() {
		return ((ListIterator<InternalV>) internalIterator).nextIndex();
	}

	@Override
	public int previousIndex() {
		return ((ListIterator<InternalV>) internalIterator).previousIndex();
	}

	@Override
	public void set(ExternalV externalV) {
		((ListIterator<InternalV>) internalIterator).set(writeTransformation.apply(externalV));
	}

	@Override
	public void add(ExternalV externalV) {
		((ListIterator<InternalV>) internalIterator).add(writeTransformation.apply(externalV));
	}
}
