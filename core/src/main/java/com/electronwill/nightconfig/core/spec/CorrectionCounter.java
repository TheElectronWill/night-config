package com.electronwill.nightconfig.core.spec;

import com.electronwill.nightconfig.core.AttributeType;

/**
 * Counts the number of values and attributes corrected.
 */
public class CorrectionCounter implements CorrectionListener {
	protected int correctionCount;

	@Override
	public void onValueCorrection(String[] path, CorrectionResult<?> result) {
		if (result.isFailed()) {
			throw new CorrectionFailedException(result.failure());
		} else if (!result.isCorrect()) {
			correctionCount += 1;
		}
	}

	@Override
	public <T> void onAttributeCorrection(AttributeType<T> attribute, String[] path,
										  CorrectionResult<T> result) {
		if (result.isFailed()) {
			throw new CorrectionFailedException(result.failure());
		} else if (!result.isCorrect()) {
			correctionCount += 1;
		}
	}

	public int correctionCount() {
		return correctionCount;
	}
}
