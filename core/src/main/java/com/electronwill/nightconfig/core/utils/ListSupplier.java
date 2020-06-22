package com.electronwill.nightconfig.core.utils;

import java.util.List;

@FunctionalInterface
public interface ListSupplier {
	default <T> List<T> get() {
		return get(8);
	}

	<T> List<T> get(int sizeHint);
}
