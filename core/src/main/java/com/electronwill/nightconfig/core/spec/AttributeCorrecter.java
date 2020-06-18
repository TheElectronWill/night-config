package com.electronwill.nightconfig.core.spec;

import com.electronwill.nightconfig.core.AttributeType;

final class AttributeCorrecter<T> extends AttributeType<ValueCorrecter<T>> {
	AttributeCorrecter(AttributeType<T> attribute) {
		super("AttributeCorrecter(" + attribute.getName() + ")");
	}
}
