package com.electronwill.nightconfig.core.check;

import com.electronwill.nightconfig.core.AttributeType;
import com.electronwill.nightconfig.core.spec.ConfigSpec;
import com.electronwill.nightconfig.core.spec.CorrectionResult;

/**
 * Checks configurations updates.
 */
public interface UpdateChecker {

	/**
	 * Checks that an update is correct. Throws a RuntimeException if it's not.
	 * This method is not limited to {@link IncorrectUpdateException}, it can use other exceptions
	 * to indicate different failures, like {@link UnmodifiableConfigException}.
	 *
	 * @param oldValue the old value of the attribute, can be null
	 * @param newValue the new value of the attribute, can be null
	 */
	void checkValueUpdate(String[] path, Object oldValue, Object newValue);

	/**
	 * Checks that an attribute modification is correct. Throws a RuntimeException if it's not.
	 * This method is not limited to {@link IncorrectUpdateException}, it can use other exceptions
	 * to indicate different failures, like {@link UnmodifiableConfigException}.
	 *
	 * @param attribute the attribute to modify
	 * @param oldValue the old value of the attribute, can be null
	 * @param newValue the new value of the attribute, can be null
	 */
	void checkAttributeUpdate(AttributeType<?> attribute, String[] path, Object oldValue, Object newValue);


	/** @return a ConfigChecker that rejects any modification. */
	static UpdateChecker freeze() {
		return new UpdateChecker() {
			@Override
			public void checkValueUpdate(String[] path, Object oldValue, Object newValue) {
				throw new UnmodifiableConfigException(path);
			}

			@Override
			public void checkAttributeUpdate(AttributeType<?> attribute, String[] path, Object oldValue, Object newValue) {
				throw new UnmodifiableConfigException(path);
			}
		};
	}

	/** @return a ConfigChecker that ensures that all the updates conform to the specification. */
	static UpdateChecker conform(ConfigSpec spec) {
		return new UpdateChecker() {
			@Override
			public void checkValueUpdate(String[] path, Object oldValue, Object newValue) {
				if (!spec.correct(path, newValue).isCorrect()) {
					throw new IncorrectUpdateException(path, newValue);
				}
			}

			@Override
			public void checkAttributeUpdate(AttributeType<?> attribute, String[] path, Object oldValue, Object newValue) {
				if (!spec.correct(attribute, path, newValue).isCorrect()) {
					throw new IncorrectUpdateException(attribute, path, newValue);
				}
			}
		};
	}
}
