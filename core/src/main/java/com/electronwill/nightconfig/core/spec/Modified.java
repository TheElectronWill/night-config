package com.electronwill.nightconfig.core.spec;

public final class Modified<T> extends CorrectionResult<T> {
	static final Modified INSTANCE = new Modified();

	private Modified() {}

	@Override
	public boolean isModified() {
		return true;
	}
}
