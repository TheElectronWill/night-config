package com.electronwill.nightconfig.core.io;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

	@Test
	void parseLong() {
		assertEquals(123456789L, Utils.parseLong(new CharsWrapper("123456789"), 10, null));
		assertEquals(0, Utils.parseLong(new CharsWrapper("0"), 10, null));
		assertEquals(-123456789L, Utils.parseLong(new CharsWrapper("-123456789"), 10, null));
		assertEquals(0xff, Utils.parseLong(new CharsWrapper("ff"), 16, null));
	}
}