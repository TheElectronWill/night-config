package com.electronwill.nightconfig.core.conversion;

import com.electronwill.nightconfig.core.Config;
import org.junit.jupiter.api.Test;

import static com.electronwill.nightconfig.core.conversion.ObjectConverterTest.*;

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

		assert object.integer == config.<Integer>get("integer");
		assert object.decimal == config.<Double>get("decimal");
		assert object.string == config.<String>get("string");
		assert object.stringList == config.get("stringList");
		assert object.config == config.get("config");
		assert config.get("subObject") instanceof Config;
		assert object.subObject.integer == config.<Integer>get("subObject.integer");
		assert object.subObject.decimal == config.<Double>get("subObject.decimal");
		assert object.subObject.string == config.<String>get("subObject.string");
		assert object.subObject.stringList == config.get("subObject.stringList");
		assert object.subObject.config == config.get("subObject.config");
		assert object.subObject.subObject == config.get("subObject.subObject");

		assert object.integer == 1234568790;
		assert object.decimal == Math.PI;
		assert object.string.equals("value");
		assert object.stringList == list1;
		assert object.config == config1;
		assert object.subObject != null;
		assert object.subObject.integer == -1;
		assert object.subObject.decimal == 0.5;
		assert object.subObject.string.equals("Hey!");
		assert object.subObject.stringList == list2;
		assert object.subObject.config == config2;
		assert object.subObject.subObject == null;
	}
}