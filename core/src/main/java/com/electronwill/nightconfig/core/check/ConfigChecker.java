package com.electronwill.nightconfig.core.check;

import com.electronwill.nightconfig.core.AttributeType;
import com.electronwill.nightconfig.core.spec.ConfigSpec;
import com.electronwill.nightconfig.core.spec.CorrectionResult;

public interface ConfigChecker {
	/**
	 * Checks that an attribute modification is correct. Throws an exception if it's not.
	 * This method is not limited to {@link IncorrectUpdateException}, it can use other exceptions
	 * to indicate different failures, like {@link UnmodifiableConfigException}.
	 *
	 * @param attribute the attribute to modify, can be
	 *                  {@link com.electronwill.nightconfig.core.StandardAttributes#VALUE}
	 * @param oldValue the old value of the attribute, can be null
	 * @param newValue the new value of the attribute, can be null
	 * @throws IncorrectUpdateException if the new value is incorrect
	 */
	void checkUpdate(AttributeType<?> attribute, String[] path, Object oldValue, Object newValue);

	// --- STATIC METHODS ---
	/** @return a ConfigChecker that rejects any modification. */
	static ConfigChecker freeze() {
		return (attribute, path, oldValue, newValue) -> {
			if (oldValue != newValue) {
				throw new UnmodifiableConfigException(path, attribute);
			}
		};
	}

	/** @return a ConfigChecker that ensures that all the updates conform to the specification. */
	static ConfigChecker conform(ConfigSpec spec) {
		return (attribute, path, oldValue, newValue) -> {
			CorrectionResult<?> corrected = spec.correct(attribute, path, newValue);
			if (!corrected.isCorrect()) {
				throw new IncorrectUpdateException(path, attribute, newValue);
			}
		};
	}
	// TODO Config wrap(Config)
}
