package com.electronwill.nightconfig.core.utils;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A TransformingSpliterator applies "just in time" transformations to an {@code
 * Spliterator<InternalV>} in order to make it like an {@code Spliterator<ExternalV>}.
 * <p>
 * The transformations are applied "just in time", that is, the values are converted only when
 * they are used, not during the construction of the TransformingSpliterator.
 *
 * @author TheElectronWill
 * @see TransformingMap
 */
public final class TransformingSpliterator<InternalV, ExternalV> implements Spliterator<ExternalV> {
	private final Function<InternalV, ExternalV> readTransformation;
	private final Function<ExternalV, InternalV> writeTransformation;
	private final Spliterator<InternalV> internalSpliterator;

	public TransformingSpliterator(Spliterator<InternalV> internalSpliterator,
								   Function<InternalV, ExternalV> readTransformation,
								   Function<ExternalV, InternalV> writeTransformation) {
		this.readTransformation = readTransformation;
		this.writeTransformation = writeTransformation;
		this.internalSpliterator = internalSpliterator;
	}

	@Override
	public boolean tryAdvance(Consumer<? super ExternalV> action) {
		return internalSpliterator.tryAdvance(
				internalV -> action.accept(readTransformation.apply(internalV)));
	}

	@Override
	public void forEachRemaining(Consumer<? super ExternalV> action) {
		internalSpliterator.forEachRemaining(
				internalV -> action.accept(readTransformation.apply(internalV)));
	}

	@Override
	public Spliterator<ExternalV> trySplit() {
		return new TransformingSpliterator<>(internalSpliterator.trySplit(), readTransformation,
											 writeTransformation);
	}

	@Override
	public long estimateSize() {
		return internalSpliterator.estimateSize();
	}

	@Override
	public long getExactSizeIfKnown() {
		return internalSpliterator.getExactSizeIfKnown();
	}

	@Override
	public int characteristics() {
		return internalSpliterator.characteristics();
	}

	@Override
	public boolean hasCharacteristics(int characteristics) {
		return internalSpliterator.hasCharacteristics(characteristics);
	}

	@Override
	public Comparator<? super ExternalV> getComparator() {
		return (o1, o2) -> internalSpliterator.getComparator()
											  .compare(writeTransformation.apply(o1),
													   writeTransformation.apply(o2));
	}
}