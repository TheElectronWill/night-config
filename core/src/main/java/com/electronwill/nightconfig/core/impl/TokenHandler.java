package com.electronwill.nightconfig.core.impl;

public interface TokenHandler {
	void beginDocument();
	void endDocument();

	void beginConfig();
	void endConfig();

	void beginEntry(CharSequence key);
	void endEntry();

	void beginList();
	void endList();

	void numberValue(int n);
	void numberValue(long n);
	void numberValue(float f);
	void numberValue(double d);

	void booleanValue(boolean b);
	void stringValue(CharSequence str);
	void nullValue();

	void comment(CharSequence comment);
}
