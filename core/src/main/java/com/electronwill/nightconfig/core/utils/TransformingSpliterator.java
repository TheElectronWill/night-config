package com.electronwill.nightconfig.core.utils;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A TransformingSpliterator applies "just in time" transformations to an {@code
 * Spliterator<I>} in order to make it like an {@code Spliterator<E>}.
 * <p>
 * The transformations are applied "just in time", that is, the values are converted only when
 * they are used, not during the construction of the TransformingSpliterator.
 *
 * @author TheElectronWill
 * @see TransformingMap
 */
public final class TransformingSpliterator<I, E> extends TransformingBase<I, E>
	implements Spliterator<E> {

	private final Spliterator<I> internal;

	public TransformingSpliterator(Spliterator<I> spliterator,
								   Function<? super I, ? extends E> readTransform,
								   Function<Object, ? extends I> searchTransform) {
		super(readTransform, null, searchTransform);
		this.internal = spliterator;
	}

	@Override
	public boolean tryAdvance(Consumer<? super E> action) {
		return internal.tryAdvance(v -> action.accept(read(v)));
	}

	@Override
	public void forEachRemaining(Consumer<? super E> action) {
		internal.forEachRemaining(v -> action.accept(read(v)));
	}

	@Override
	public Spliterator<E> trySplit() {
		return new TransformingSpliterator<>(internal.trySplit(), readTransform, searchTransform);
	}

	@Override
	public long estimateSize() {
		return internal.estimateSize();
	}

	@Override
	public long getExactSizeIfKnown() {
		return internal.getExactSizeIfKnown();
	}

	@Override
	public int characteristics() {
		return internal.characteristics();
	}

	@Override
	public boolean hasCharacteristics(int characteristics) {
		return internal.hasCharacteristics(characteristics);
	}

	@Override
	public Comparator<? super E> getComparator() {
		return (o1, o2) -> internal.getComparator().compare(search(o1), search(o2));
	}
}