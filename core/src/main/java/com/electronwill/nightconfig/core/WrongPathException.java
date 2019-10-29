package com.electronwill.nightconfig.core;

import com.electronwill.nightconfig.core.utils.StringUtils;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;

public final class WrongPathException extends RuntimeException {
	private final String[] path;

	public WrongPathException(String[] path, int len, int problemIdx, Object problemValue) {
		super(makeMessage(path, len, problemIdx, problemValue));
		this.path = path;
	}

	public String[] getPath() {
		return path;
	}

	private static String makeMessage(String[] path, int len, int problemIdx, Object problemValue) {
		if (problemValue == null) {
			return String.format(
				"Wrong path %s: intermediate level '%s' (#%d) doesn't exist",
				StringUtils.joinPath(path, len), path[problemIdx], problemIdx
			);
		} else if (problemValue == NULL_OBJECT) {
			return String.format(
				"Wrong path %s: intermediate level '%s' (#%d) shouldn't have a null value",
				StringUtils.joinPath(path, len), path[problemIdx], problemIdx
			);
		} else {
			return String.format(
				"Wrong path %s: intermediate level '%s' (#%d) shouldn't be of type %s",
				StringUtils.joinPath(path, len), path[problemIdx], problemIdx,
				problemValue.getClass()
			);
		}
	}
}
