package com.electronwill.nightconfig.core.spec;

public final class Replaced<T> extends CorrectionResult<T> {
	private final T value;

	public Replaced(T value) {
		this.value = value;
	}

	@Override
	public T replacementValue() {
		return value;
	}

	@Override
	public boolean isReplaced() {
		return true;
	}
}
