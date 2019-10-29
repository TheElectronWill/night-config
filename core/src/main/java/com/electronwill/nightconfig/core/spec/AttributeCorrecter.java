package com.electronwill.nightconfig.core.spec;

import com.electronwill.nightconfig.core.AttributeType;

final class AttributeCorrecter<T> implements AttributeType<ValueCorrecter<T>> {
	private final String name;

	AttributeCorrecter(AttributeType<T> attribute) {
		this.name = "AttributeCorrecter" + attribute.getName() + ")";
	}

	@Override
	public String getName() {
		return name;
	}
}
