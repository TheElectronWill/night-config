package com.electronwill.nightconfig.core.impl;

public interface Tokenizer<Token> {
	Token next();

	CharSequence textValue();

	int intValue();
	long longValue();
	double doubleValue();

	int line();
	int column();
}
