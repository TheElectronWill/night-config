package com.electronwill.nightconfig.core.utils;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

/**
 * A TransformingSet applies "just in time" transformations to an {@code Set<InternalV>} in order
 * to make it like an {@code Set<ExternalV>}.
 * <p>
 * The transformations are applied "just in time", that is, the values are converted only when
 * they are used, not during the construction of the TransformingSet.
 *
 * @author TheElectronWill
 * @see TransformingMap
 */
public final class TransformingSet<InternalV, ExternalV>
		extends TransformingCollection<InternalV, ExternalV> implements Set<ExternalV> {
	public TransformingSet(Set<InternalV> internalCollection,
						   Function<InternalV, ExternalV> readTransformation,
						   Function<ExternalV, InternalV> writeTransformation,
						   Function<Object, Object> searchTransformation) {
		super(internalCollection, readTransformation, writeTransformation, searchTransformation);
	}
}