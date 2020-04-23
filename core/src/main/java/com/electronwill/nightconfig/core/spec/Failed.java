package com.electronwill.nightconfig.core.spec;

public final class Failed<T> extends CorrectionResult<T> {
	private final Throwable failure;

	Failed(Throwable failure) {
		this.failure = failure;
	}

	@Override
	public boolean isFailed() {
		return true;
	}

	@Override
	public Throwable failure() {
		return failure;
	}
}
