import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.spec.ConfigSpec;
import java.util.Arrays;

/**
 * @author TheElectronWill
 */
public class ConfigSpecExample {
	public static void main(String[] args) {
		// Creates the ConfigSpec and defines how the config must be:
		ConfigSpec spec = new ConfigSpec();
		// defines an entry "key" of type String with a default value
		spec.define("key", "defaultValue");

		// defines an integer in range [-1;1], default 0
		spec.defineInRange("number", 0, -1, 1);

		// defines an entry "letter" that must be either 'a', 'b' or 'c'; default 'a'
		spec.defineInList("letter", 'a', Arrays.asList('a', 'b', 'c'));

		// You can also use defineOfClass with an enum:
		// This defines an entry "letter" that must be Letter.A, Letter.B or Letter.C, default A
		spec.defineOfClass("letter", Letter.A, Letter.class);
		// Redefining an entry overrides the previous definition.

		// Let's create a Config to use the ConfigSpec:
		Config config = Config.inMemory();
		config.set("key", "the value");// this is a valid entry of type String
		config.set("number", 1234);// number isn't in range [-1;1]
		config.set("unwanted_key", "abcdefg");// an entry that isn't in the specification
		// -- no "letter" entry

		// Checks if the config respects the configuration:
		boolean correct = spec.isCorrect(config);
		System.out.println("Correct: " + correct);

		// Corrects the configuration by using the default values where necessary:
		spec.correct(config);
		System.out.println("Config after correction: " + config);
		/*
		Explanation of the output:
		 - The "key" entry wasn't valid: the spec required it to be a String, it is the case.
		 - The "number" value wasn't in the specified range, therefore it has been replaced by the
		 default value 0.
		 - The "unwanted_key" entry wasn't in the spec, therefore it has been deleted.
		 - The "letter" entry was missing, therefore it has been added with the default value A.
		*/
	}

	private enum Letter {
		A, B, C
	}

}