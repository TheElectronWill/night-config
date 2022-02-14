package com.electronwill.nightconfig.core.utils;

import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import java.util.function.Function;

public final class TransformingList<InternalV, ExternalV>
	    extends TransformingCollection<InternalV, ExternalV> implements List<ExternalV> {
	public TransformingList(List<InternalV> internalList,
							Function<? super InternalV, ? extends ExternalV> readTransformation,
							Function<? super ExternalV, ? extends InternalV> writeTransformation,
							Function<Object, Object> searchTransformation) {
		super(internalList, readTransformation, writeTransformation, searchTransformation);
	}

	@Override
	public boolean addAll(int index, Collection<? extends ExternalV> c) {
		return ((List<InternalV>) internalCollection).addAll(index,
			new TransformingCollection(c, writeTransformation, readTransformation, searchTransformation));
	}

	@Override
	public ExternalV get(int index) {
		return readTransformation.apply(((List<InternalV>) internalCollection).get(index));
	}

	@Override
	public ExternalV set(int index, ExternalV element) {
		return readTransformation.apply(((List<InternalV>) internalCollection).set(index, writeTransformation.apply(element)));
	}

	@Override
	public void add(int index, ExternalV element) {
		((List<InternalV>) internalCollection).add(index, writeTransformation.apply(element));
	}

	@Override
	public ExternalV remove(int index) {
		return readTransformation.apply(((List<InternalV>) internalCollection).remove(index));
	}

	@Override
	public int indexOf(Object o) {
		return ((List<InternalV>) internalCollection).indexOf(searchTransformation.apply(o));
	}

	@Override
	public int lastIndexOf(Object o) {
		return ((List<InternalV>) internalCollection).lastIndexOf(searchTransformation.apply(o));
	}

	@Override
	public ListIterator<ExternalV> listIterator() {
		return new TransformingListIterator<>(((List<InternalV>) internalCollection).listIterator(), readTransformation, writeTransformation);
	}

	@Override
	public ListIterator<ExternalV> listIterator(int index) {
		return new TransformingListIterator<>(((List<InternalV>) internalCollection).listIterator(index), readTransformation, writeTransformation);
	}

	@Override
	public List<ExternalV> subList(int fromIndex, int toIndex) {
		return new TransformingList<>(((List<InternalV>) internalCollection).subList(fromIndex, toIndex),
			readTransformation, writeTransformation, searchTransformation);
	}
}
