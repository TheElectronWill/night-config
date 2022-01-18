package com.electronwill.nightconfig.toml;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class TomlFormatTest {

	@Test
	public void noNulls() {
		Assertions.assertFalse(TomlFormat.instance().supportsType(null));
	}
}