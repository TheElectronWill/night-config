package com.electronwill.nightconfig.core.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * A TransformingCollection applies "just in time" transformations to a {@code
 * Collection<I>} in order to make it like a {@code Collection<E>}.
 * <p>
 * The transformations are applied "just in time", that is, the values are converted only when
 * they are used, not during the construction of the TransformingCollection.
 *
 * @author TheElectronWill
 * @see TransformingMap
 */
@SuppressWarnings("unchecked")
public class TransformingCollection<I, E> extends TransformingBase<I, E> implements Collection<E> {
	private final Collection<I> internal;

	public TransformingCollection(Collection<I> collection,
								  Function<? super I, ? extends E> readTransform,
								  Function<? super E, ? extends I> writeTransform,
								  Function<Object, ? extends I> searchTransform) {
		super(readTransform, writeTransform, searchTransform);
		this.internal = collection;
	}

	private Collection<I> searchIn(Collection<?> elements) {
		Collection<Object> c = (Collection<Object>)elements;
		return new TransformingCollection<Object, I>(c, searchTransform, null, null);
	}

	@Override
	public int size() {
		return internal.size();
	}

	@Override
	public boolean isEmpty() {
		return internal.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return internal.contains(search(o));
	}

	@Override
	public Iterator<E> iterator() {
		return new TransformingIterator<>(internal.iterator(), readTransform);
	}

	@Override
	public Object[] toArray() {
		Object[] array = internal.toArray();
		for (int i = 0; i < array.length; i++) {
			array[i] = read((I)array[i]);
		}
		return array;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		T[] array = internal.toArray(a);
		for (int i = 0; i < array.length; i++) {
			array[i] = (T)read((I)array[i]);
		}
		return array;
	}

	@Override
	public boolean add(E value) {
		return internal.add(write(value));
	}

	@Override
	public boolean remove(Object o) {
		return internal.remove(search(o));
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return internal.containsAll(searchIn(c));
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return internal.addAll(
				new TransformingCollection(c, writeTransform, readTransform, searchTransform));
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return internal.removeAll(searchIn(c));
	}


	@Override
	public boolean removeIf(Predicate<? super E> filter) {
		return internal.removeIf(v -> filter.test(read(v)));
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return internal.retainAll(searchIn(c));
	}

	@Override
	public void clear() {
		internal.clear();
	}

	@Override
	public Spliterator<E> spliterator() {
		return new TransformingSpliterator<>(internal.spliterator(), readTransform, searchTransform);
	}

	@Override
	public Stream<E> stream() {
		return internal.stream().map(readTransform);
	}

	@Override
	public Stream<E> parallelStream() {
		return internal.parallelStream().map(readTransform);
	}

	@Override
	public void forEach(Consumer<? super E> action) {
		internal.forEach(i -> action.accept(read(i)));
	}
}