package com.electronwill.nightconfig.core.utils;

import java.util.Set;
import java.util.function.Function;

/**
 * A TransformingSet applies "just in time" transformations to an {@code Set<I>} in order
 * to make it like an {@code Set<E>}.
 * <p>
 * The transformations are applied "just in time", that is, the values are converted only when
 * they are used, not during the construction of the TransformingSet.
 *
 * @author TheElectronWill
 * @see TransformingMap
 */
public final class TransformingSet<I, E> extends TransformingCollection<I, E> implements Set<E> {
	public TransformingSet(Set<I> set,
						   Function<? super I, ? extends E> readTransform,
						   Function<? super E, ? extends I> writeTransform,
						   Function<Object, ? extends I> searchTransform) {
		super(set, readTransform, writeTransform, searchTransform);
	}
}