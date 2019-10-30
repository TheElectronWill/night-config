package com.electronwill.nightconfig.core.impl;

import java.io.IOException;

@FunctionalInterface
public interface Tokenizer<I> {
	void tokenize(I in, TokenHandler out) throws IOException;
}
