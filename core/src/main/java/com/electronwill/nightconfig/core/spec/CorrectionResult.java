package com.electronwill.nightconfig.core.spec;

@SuppressWarnings("unchecked")
public abstract class CorrectionResult<T> {
	CorrectionResult() {} // package-private to restrict the possible subclasses

	/** @return the new, corrected value, null if not {@link #isReplaced()} */
	public T replacementValue() {
		return null;
	}

	public boolean isCorrect() {
		return false;
	}

	public boolean isModified() {
		return false;
	}

	public boolean isReplaced() {
		return false;
	}

	public boolean isRemoved() {
		return false;
	}

	public boolean isFailed() {
		return false;
	}

	public Throwable failure() {
		return null;
	}

	public static <T> CorrectionResult<T> correct() {
		return (CorrectionResult<T>)Correct.INSTANCE;
	}

	public static <T> CorrectionResult<T> failed(Throwable failure) {
		return new Failed<>(failure);
	}

	public static <T> CorrectionResult<T> modified() {
		return (CorrectionResult<T>)Modified.INSTANCE;
	}

	public static <T> CorrectionResult<T> replacedBy(T value) {
		return new Replaced<>(value);
	}

	public static <T> CorrectionResult<T> removed() {
		return (CorrectionResult<T>)Removed.INSTANCE;
	}
}
