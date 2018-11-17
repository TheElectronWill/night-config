package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import org.junit.jupiter.api.Test;

import static com.electronwill.nightconfig.core.conversion.ObjectConverterTest.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author TheElectronWill
 */
class ObjectBinderTest {

	@Test
	public void writeToObjectTest() {
		MyObjectFinal subObject = new MyObjectFinal(0, 3.14159265358, "", null, null, null, null, null, null);
		MyObjectFinal object = new MyObjectFinal(123, 1.23, "initialV", list2, null, null, null, config2, subObject);
		ObjectBinder binder = new ObjectBinder();
		Config config = binder.bind(object);
		config.set("integer", 1234568790);
		config.set("decimal", Math.PI);
		config.set("string", "value");
		config.set("stringList", list1);
		config.set("config", config1);
		config.set("subObject.integer", -1);
		config.set("subObject.decimal", 0.5);
		config.set("subObject.string", "Hey!");
		config.set("subObject.stringList", list2);
		config.set("subObject.config", config2);
		config.set("subObject.subObject", null);

		// Checks that the object has been modified according to the config
		assertEquals(object.integer, (int)config.get("integer"));
		assertEquals(object.decimal, (double)config.get("decimal"));
		assertEquals(object.string, config.get("string"));
		assertEquals(object.stringList, config.get("stringList"));
		assertNull(object.objList);
		assertEquals(object.config, config.get("config"));
		assertTrue(config.get("subObject") instanceof Config);
		Config sub = config.get("subObject");
		assertEquals(object.subObject.integer, (int)sub.get("integer"));
		assertEquals(object.subObject.decimal, (double)sub.get("decimal"));
		assertEquals(object.subObject.string, sub.get("string"));
		assertEquals(object.subObject.stringList, sub.get("stringList"));
		assertNull(object.subObject.objList);
		assertNull(sub.get("subObject"));
	}
}