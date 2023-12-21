package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class MultiSpecTest {

	@SuppressWarnings("unused")
	private static class TestClass {
		@SpecNotNull
		@SpecIntInRange(min = 1, max = 3)
		private int integer;
	}

	@Test
	public void multiSpecTest() {
		ObjectConverter converter = new ObjectConverter();

		Config config = Config.inMemory();

		assertThrows(InvalidValueException.class, () -> converter.toObject(config, TestClass::new));

		config.set("integer", 1);
		assertDoesNotThrow(() -> converter.toObject(config, TestClass::new));

		config.set("integer", 0);
		assertThrows(InvalidValueException.class, () -> converter.toObject(config, TestClass::new));
	}

}
