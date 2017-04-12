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
		MyObjectFinal subObject = new MyObjectFinal(0, 3.14159265358, "", null, null, null);
		MyObjectFinal object = new MyObjectFinal(123, 1.23, "initialV", list2, config2, subObject);
		ObjectBinder binder = new ObjectBinder();
		Config config = binder.bind(object);
		config.setValue("integer", 1234568790);
		config.setValue("decimal", Math.PI);
		config.setValue("string", "value");
		config.setValue("stringList", list1);
		config.setValue("config", config1);
		config.setValue("subObject.integer", -1);
		config.setValue("subObject.decimal", 0.5);
		config.setValue("subObject.string", "Hey!");
		config.setValue("subObject.stringList", list2);
		config.setValue("subObject.config", config2);
		config.setValue("subObject.subObject", null);

		assert object.integer == config.<Integer>getValue("integer");
		assert object.decimal == config.<Double>getValue("decimal");
		assert object.string == config.<String>getValue("string");
		assert object.stringList == config.getValue("stringList");
		assert object.config == config.getValue("config");
		assert config.getValue("subObject") instanceof Config;
		assert object.subObject.integer == config.<Integer>getValue("subObject.integer");
		assert object.subObject.decimal == config.<Double>getValue("subObject.decimal");
		assert object.subObject.string == config.<String>getValue("subObject.string");
		assert object.subObject.stringList == config.getValue("subObject.stringList");
		assert object.subObject.config == config.getValue("subObject.config");
		assert object.subObject.subObject == config.getValue("subObject.subObject");

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