package com.electronwill.nightconfig.core.utils;

import java.util.function.Function;

/** Abstract class to handle {@code External<=>Internal } transformations more easily. */
abstract class TransformingBase<Internal, External> {
	protected final Function<? super Internal, ? extends External> readTransform;
	protected final Function<? super External, ? extends Internal> writeTransform;
	protected final Function<Object, ? extends Internal> searchTransform;

	protected TransformingBase(Function<? super Internal, ? extends External> readTransform,
							   Function<? super External, ? extends Internal> writeTransform,
							   Function<Object, ? extends Internal> searchTransform) {
		this.readTransform = readTransform;
		this.writeTransform = writeTransform;
		this.searchTransform = searchTransform;
	}

	protected final External read(Internal v) {
		return readTransform.apply(v);
	}

	protected final Internal write(External v) {
		return writeTransform.apply(v);
	}

	protected final Internal search(Object o) {
		return searchTransform.apply(o);
	}
}
