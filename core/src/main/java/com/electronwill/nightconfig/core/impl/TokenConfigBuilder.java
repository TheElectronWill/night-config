package com.electronwill.nightconfig.core.impl;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.MemoryConfig;

import java.util.function.Supplier;

/**
 * Builds {@link Config} objects from tokens.
 */
public class TokenConfigBuilder implements TokenHandler {
	private final Supplier<Config> configSupplier;
	private Config config;

	public TokenConfigBuilder() {
		this(MemoryConfig::new);
	}

	public TokenConfigBuilder(Supplier<Config> configSupplier) {
		this.configSupplier = configSupplier;
	}

	@Override
	public void beginDocument() {
		config = configSupplier.get();
	}

	@Override
	public void endDocument() {
		// TODO
	}

	@Override
	public void beginConfig() {

	}

	@Override
	public void endConfig() {

	}

	@Override
	public void beginEntry(CharSequence key) {

	}

	@Override
	public void endEntry() {

	}

	@Override
	public void beginList() {

	}

	@Override
	public void endList() {

	}

	@Override
	public void numberValue(int n) {

	}

	@Override
	public void numberValue(long n) {

	}

	@Override
	public void numberValue(float f) {

	}

	@Override
	public void numberValue(double d) {

	}

	@Override
	public void booleanValue(boolean b) {

	}

	@Override
	public void stringValue(CharSequence str) {

	}

	@Override
	public void nullValue() {

	}

	@Override
	public void comment(CharSequence comment) {

	}
}
