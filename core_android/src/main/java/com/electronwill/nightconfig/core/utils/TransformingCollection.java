package com.electronwill.nightconfig.core.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A TransformingCollection applies "just in time" transformations to a {@code
 * Collection<InternalV>} in order to make it like a {@code Collection<ExternalV>}.
 * <p>
 * The transformations are applied "just in time", that is, the values are converted only when
 * they are used, not during the construction of the TransformingCollection.
 *
 * @author TheElectronWill
 * @see TransformingMap
 */
@SuppressWarnings("unchecked")
public class TransformingCollection<InternalV, ExternalV> implements Collection<ExternalV> {
	private final Function<? super InternalV, ? extends ExternalV> readTransformation;
	private final Function<? super ExternalV, ? extends InternalV> writeTransformation;
	private final Function<Object, Object> searchTransformation;
	private final Collection<InternalV> internalCollection;

	public TransformingCollection(Collection<InternalV> internalCollection,
								  Function<? super InternalV, ? extends ExternalV> readTransformation,
								  Function<? super ExternalV, ? extends InternalV> writeTransformation,
								  Function<Object, Object> searchTransformation) {
		this.internalCollection = internalCollection;
		this.readTransformation = readTransformation;
		this.writeTransformation = writeTransformation;
		this.searchTransformation = searchTransformation;
	}

	@Override
	public int size() {
		return internalCollection.size();
	}

	@Override
	public boolean isEmpty() {
		return internalCollection.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return internalCollection.contains(searchTransformation.apply(o));
	}

	@Override
	public Iterator<ExternalV> iterator() {
		return new TransformingIterator<>(internalCollection.iterator(), readTransformation);
	}

	@Override
	public Object[] toArray() {
		Object[] array = internalCollection.toArray();
		for (int i = 0; i < array.length; i++) {
			array[i] = readTransformation.apply((InternalV)array[i]);
		}
		return array;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		T[] array = internalCollection.toArray(a);
		for (int i = 0; i < array.length; i++) {
			array[i] = (T)readTransformation.apply((InternalV)array[i]);
		}
		return array;
	}

	@Override
	public boolean add(ExternalV value) {
		return internalCollection.add(writeTransformation.apply(value));
	}

	@Override
	public boolean remove(Object o) {
		return internalCollection.remove(searchTransformation.apply(o));
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return internalCollection.containsAll(
				new TransformingCollection(c, searchTransformation, o -> o, searchTransformation));
	}

	@Override
	public boolean addAll(Collection<? extends ExternalV> c) {
		return internalCollection.addAll(
				new TransformingCollection(c, writeTransformation, readTransformation,
										   searchTransformation));
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return internalCollection.removeAll(
				new TransformingCollection(c, searchTransformation, o -> o, searchTransformation));
	}

	@Override
	public boolean removeIf(Predicate<? super ExternalV> filter) {
		return internalCollection.removeIf(
				internalV -> filter.test(readTransformation.apply(internalV)));
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return internalCollection.retainAll(
				new TransformingCollection(c, searchTransformation, o -> o, searchTransformation));
	}

	@Override
	public void clear() {
		internalCollection.clear();
	}

	@Override
	public Spliterator<ExternalV> spliterator() {
		return new TransformingSpliterator<>(internalCollection.spliterator(), readTransformation,
											 writeTransformation);
	}

	@Override
	public Stream<ExternalV> stream() {
		return internalCollection.stream().map(readTransformation);
	}

	@Override
	public Stream<ExternalV> parallelStream() {
		return internalCollection.parallelStream().map(readTransformation);
	}

	@Override
	public void forEach(Consumer<? super ExternalV> action) {
		internalCollection.forEach(internalV -> action.accept(readTransformation.apply(internalV)));
	}
}