package com.electronwill.nightconfig.core.spec;

import com.electronwill.nightconfig.core.AttributeType;

@FunctionalInterface
public interface CorrectionListener {
	<T> void onCorrect(String[] path, AttributeType<T> attribute, CorrectionResult<T> result);

	/** @return a CorrectionListener that does nothing. */
	static CorrectionListener noop() {
		return new CorrectionListener() {
			@Override
			public <T> void onCorrect(String[] path, AttributeType<T> attribute,
									  CorrectionResult<T> result) {
				// does nothing
			}
		};
	}
}
