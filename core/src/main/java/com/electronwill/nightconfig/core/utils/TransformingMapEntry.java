package com.electronwill.nightconfig.core.utils;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A TransformingMapEntry applies "just in time" transformations to a {@code Map.Entry<K, I>}
 * in order to make it look like a {@code Map.Entry<K, E>}.
 * <p>
 * The transformations are applied "just in time", that is, the values are converted only when
 * they are used, not during the construction of the TransformingMapEntry.
 *
 * @author TheElectronWill
 * @see TransformingMap
 */
final class TransformingMapEntry<K, I, E> extends TransformingBase<I, E> implements Map.Entry<K, E> {
	static <K, I, E> TransformingMapEntry<K, I, E> from(
			Map.Entry<K, I> entry,
			BiFunction<K, ? super I, ? extends E> readTransform,
			BiFunction<K, ? super E, ? extends I> writeTransform) {
		return new TransformingMapEntry<>(
			entry,
			v -> readTransform.apply(entry.getKey(), v),
			v -> writeTransform.apply(entry.getKey(), v)
		);
	}

	private final Map.Entry<K, I> internal;

	TransformingMapEntry(Map.Entry<K, I> entry,
						 Function<? super I, ? extends E> readTransform,
						 Function<? super E, ? extends I> writeTransform) {
		super(readTransform, writeTransform, null);
		this.internal = entry;
	}

	@Override
	public K getKey() {
		return internal.getKey();
	}

	@Override
	public E getValue() {
		return read(internal.getValue());
	}

	@Override
	public E setValue(E value) {
		return read(internal.setValue(write(value)));
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (!(obj instanceof Map.Entry)) {
			return false;
		}
		Map.Entry<?, ?> entry = (Map.Entry<?, ?>)obj;
		return Objects.equals(getKey(), entry.getKey())
			&& Objects.equals(getValue(), entry.getValue());
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(getKey()) ^ Objects.hashCode(getValue());
	}
}