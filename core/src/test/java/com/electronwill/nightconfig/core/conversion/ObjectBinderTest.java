package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.sharedtests.TestEnum;
import org.junit.jupiter.api.Test;

import static com.electronwill.nightconfig.core.conversion.ObjectConverterTest.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * @author TheElectronWill
 */
class ObjectBinderTest {

	@Test
	public void writeToObjectTest() {
		MyObjectFinal subObject = new MyObjectFinal(0, 3.14159265358, "", null, null, null, null, null, null, null);
		MyObjectFinal object = new MyObjectFinal(123, 1.23, "initialV", list2, null, null, null, config2, subObject, TestEnum.C);
		ObjectBinder binder = new ObjectBinder();
		Config config = binder.bind(object);
		config.set("integer", 1234568790);
		config.set("decimal", Math.PI);
		config.set("string", "value");
		config.set("stringList", list1);
		config.set("config", config1);
		config.set("enumValue", "A");
		config.set("subObject.integer", -1);
		config.set("subObject.decimal", 0.5);
		config.set("subObject.string", "Hey!");
		config.set("subObject.stringList", list2);
		config.set("subObject.config", config2);
		config.set("subObject.subObject", null);
		config.set("subObject.enumValue", "B");

		// Checks that the object has been modified according to the config
		assertEquals((int)config.get("integer"), object.integer);
		assertEquals((double)config.get("decimal"), object.decimal);
		assertEquals(config.get("string"), object.string);
		assertEquals(config.get("stringList"), object.stringList);
		assertNull(object.objList);
		assertEquals(config.get("config"), object.config);
		assertEquals(config.getEnum("enumValue", TestEnum.class), object.enumValue);
		assertTrue(config.get("subObject") instanceof Config);

		Config sub = config.get("subObject");
		assertEquals((int)sub.get("integer"), object.subObject.integer);
		assertEquals((double)sub.get("decimal"), object.subObject.decimal);
		assertEquals(sub.get("string"), object.subObject.string);
		assertEquals(sub.get("stringList"), object.subObject.stringList);
		assertEquals(sub.getEnum("enumValue", TestEnum.class), object.subObject.enumValue);
		assertNull(object.subObject.objList);
		assertNull(sub.get("subObject"));
	}
}