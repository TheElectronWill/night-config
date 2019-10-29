package com.electronwill.nightconfig.core.spec;

public final class Removed<T> extends CorrectionResult<T> {
	static final Removed INSTANCE = new Removed();

	private Removed() {}

	@Override
	public boolean isRemoved() {
		return true;
	}
}
