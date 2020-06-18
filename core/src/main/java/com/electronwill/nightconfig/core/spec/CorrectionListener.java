package com.electronwill.nightconfig.core.spec;

import com.electronwill.nightconfig.core.AttributeType;

public interface CorrectionListener {
	void onValueCorrection(String[] path, CorrectionResult<?> result);

	<T> void onAttributeCorrection(AttributeType<T> attribute, String[] path, CorrectionResult<T> result);

	/** @return a CorrectionListener that does nothing. */
	static CorrectionListener noop() {
		return new CorrectionListener() {
			@Override
			public void onValueCorrection(String[] path, CorrectionResult<?> result) {
				// does nothing
			}

			@Override
			public <T> void onAttributeCorrection(AttributeType<T> attribute, String[] path,
												  CorrectionResult<T> result) {
				// does nothing
			}
		};
	}
}
