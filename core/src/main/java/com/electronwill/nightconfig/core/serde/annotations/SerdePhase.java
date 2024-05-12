package com.electronwill.nightconfig.core.serde.annotations;

/**
 * A serialization/deserialization phase.
 * <p>
 * Use this enum to specify when certain "serde" annotations apply.
 */
public enum SerdePhase {
	/** Only apply the annotation when serializing. */
	SERIALIZING,
	/** Only apply the annotation when deserializing. */
	DESERIALIZING,
	/** Apply the annotation when serializing and deserializing. */
	BOTH,
}
