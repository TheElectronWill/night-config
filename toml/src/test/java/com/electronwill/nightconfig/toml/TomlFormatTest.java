package com.electronwill.nightconfig.toml;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class TomlFormatTest {

	@Test
	public void noNulls() {
		assertFalse(TomlFormat.instance().supportsType(null));
	}
}