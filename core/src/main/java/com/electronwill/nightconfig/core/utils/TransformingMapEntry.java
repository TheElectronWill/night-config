package com.electronwill.nightconfig.core.utils;

import java.util.Map;
import java.util.function.Function;

/**
 * A TransformingMapEntry applies "just in time" transformations to a {@code Map.Entry<K,
 * InternalV>} in order to make it like a {@code Map.Entry<K, ExternalV>}.
 * <p>
 * The transformations are applied "just in time", that is, the values are converted only when
 * they are used, not during the construction of the TransformingMapEntry.
 *
 * @author TheElectronWill
 * @see TransformingMap
 */
final class TransformingMapEntry<K, InternalV, ExternalV> implements Map.Entry<K, ExternalV> {
	private final Function<InternalV, ExternalV> readTransformation;
	private final Function<ExternalV, InternalV> writeTransformation;
	private final Map.Entry<K, InternalV> internalEntry;

	TransformingMapEntry(Map.Entry<K, InternalV> internalEntry,
						 Function<InternalV, ExternalV> readTransformation,
						 Function<ExternalV, InternalV> writeTransformation) {
		this.readTransformation = readTransformation;
		this.writeTransformation = writeTransformation;
		this.internalEntry = internalEntry;
	}

	@Override
	public K getKey() {
		return internalEntry.getKey();
	}

	@Override
	public ExternalV getValue() {
		return readTransformation.apply(internalEntry.getValue());
	}

	@Override
	public ExternalV setValue(ExternalV value) {
		return readTransformation.apply(internalEntry.setValue(writeTransformation.apply(value)));
	}
}