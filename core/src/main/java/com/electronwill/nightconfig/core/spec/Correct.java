package com.electronwill.nightconfig.core.spec;

public final class Correct<T> extends CorrectionResult<T> {
	static final Correct INSTANCE = new Correct();

	private Correct() {}

	@Override
	boolean isCorrect() {
		return true;
	}
}
