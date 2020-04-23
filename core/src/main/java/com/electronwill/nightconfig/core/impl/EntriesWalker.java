package com.electronwill.nightconfig.core.impl;

public interface EntriesWalker {
	String nextKey();
	Object nextValue();
	Class<?> nextClass();
	boolean hasNext();
	EntriesWalker breakdown();
}
