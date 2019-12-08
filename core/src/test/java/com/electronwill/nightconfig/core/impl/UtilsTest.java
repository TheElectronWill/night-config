package com.electronwill.nightconfig.core.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UtilsTest {

	@Test
	void parseLong() {
		assertEquals(123456789L, Utils.parseLong(new Charray("123456789"), 10));
		assertEquals(0, Utils.parseLong(new Charray("0"), 10));
		assertEquals(-123456789L, Utils.parseLong(new Charray("-123456789"), 10));
		assertEquals(0xff, Utils.parseLong(new Charray("ff"), 16));
	}
}