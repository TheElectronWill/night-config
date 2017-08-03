import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.conversion.ObjectConverter;

/**
 * @author TheElectronWill
 */
public class ObjectConverterExample {
	public static void main(String[] args) {
		// Creates a config with some values:
		Config config = Config.inMemory();
		config.set("value", "The value");
		config.set("number", 100);
		config.set("subData.str", "data string");
		config.set("ignored", true);
		config.set("notAField", "abc");
		System.out.println("Config: " + config);

		// Converts the config to an instance of ConfigData:
		ObjectConverter converter = new ObjectConverter();
		ConfigData object = converter.toObject(config, ConfigData::new);
		System.out.println("Object: " + object);

		/* Explanation of the result:
		   - The values of the config are mapped to the fields of ConfigData. Custom field types
		   (like SubData in our example) are automatically supported, provided they have a
		   constructor without arguments (the access level doesn't matter, it can be private).

		   - Notice that the value of "withDefaultValue" is replaced by null after the conversion.
		   That's because the content of the config overrides everything by default. To keep the
		   default value when the config doesn't provide one (ie gives null), add the
		   @PreserveNotNull annotation to the field.

		   You can also add @PreserveNotNull to the class to appy it to all the fields.

		   - The value of "ignored" didn't change, even if it was in the configuration. That's
		   because transient fields are ignored by default by the ObjectConverter. This behavior
		   can be changed by using the alternative constructor of ObjectConverter.

		   - There is no field "notAField" in ConfigData. And it's impossible to add a field to a
		   class at runtime, so the entry "notAField" is simply ignored by the ObjectConverter.
		   */

		// Modifies the config:
		config.set("value", "Another value");
		System.out.println("\nmodified Config: " + config);
		System.out.println("Object: " + object);
		/* See that the created object isn't linked to the config. They are independant, so you
		can modify the config without affecting the object and vice-versa. */

		// Of course it is possible to do the inverse operation: to convert an object to a config:
		Config configFromObject = converter.toConfig(object, Config::inMemory);
		System.out.println("\nConfig from object: " + configFromObject);
	}

	static class ConfigData {
		private int number;
		private transient Object ignored;
		private SubData subData;
		private String value;

		@Override
		public String toString() {
			return "ConfigData{"
				   + "number="
				   + number
				   + ", ignored="
				   + ignored
				   + ", subData="
				   + subData
				   + ", value='"
				   + value
				   + '\''
				   + '}';
		}
	}

	static class SubData {
		private String str;
		//@PreserveNotNull -- try adding this annotation to the field!
		private String withDefaultValue = "default value";

		@Override
		public String toString() {
			return "SubData{"
				   + "str='"
				   + str
				   + '\''
				   + ", withDefaultValue='"
				   + withDefaultValue
				   + '\''
				   + '}';
		}
	}
}