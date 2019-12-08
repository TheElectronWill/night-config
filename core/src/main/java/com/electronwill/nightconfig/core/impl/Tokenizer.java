package com.electronwill.nightconfig.core.impl;

public interface Tokenizer<Token> {
	Token next();

	CharSequence textValue();
	default String stringValue() {
		return textValue().toString();
	}

	int intValue();
	long longValue();
	double doubleValue();

	int line();
	int column();
}
