package com.electronwill.nightconfig.core.utils;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Set;
import java.util.function.Function;

/**
 * @author TheElectronWill
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