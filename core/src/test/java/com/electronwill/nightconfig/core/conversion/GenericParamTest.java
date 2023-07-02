package com.electronwill.nightconfig.core.conversion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import com.electronwill.nightconfig.core.Config;

public class GenericParamTest {

	static class GenericClass<T> {
		List<T> list = new ArrayList<>();
	}

	@Test
	public void genericConversion() {
		ObjectConverter converter = new ObjectConverter();

		Config conf = Config.inMemory();
		conf.set("list", Arrays.asList("a", "b"));

		// expected to fail: config -> object with unknown generic parameter
		assertThrows(IndexOutOfBoundsException.class, () -> {
			converter.toObject(conf, GenericClass<String>::new);
		});

		// expected to succeed: object -> config
		GenericClass<String> object = new GenericClass<>();
		object.list = Arrays.asList("a", "b");
		assertEquals(conf, converter.toConfig(object, Config::inMemory));
	}

}
