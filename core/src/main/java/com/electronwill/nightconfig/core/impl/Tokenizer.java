package com.electronwill.nightconfig.core.impl;

public interface Tokenizer<Token> {
	Token next();

	<T> T genericValue();
	CharSequence textValue();

	int intValue();
	long longValue();
	double doubleValue();
	boolean booleanValue();

	int line();
	int column();
}
