package com.electronwill.nightconfig.core.spec;

import com.electronwill.nightconfig.core.AttributeType;

public class CorrectionCounter implements CorrectionListener {
	protected int correctionCount;

	@Override
	public <T> void onCorrect(String[] path, AttributeType<T> attribute,
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
