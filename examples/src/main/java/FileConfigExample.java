import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.FileConfig;
import java.util.Arrays;

/**
 * @author TheElectronWill
 */
public class FileConfigExample {
	public static void main(String[] args) {
		// Creates the FileConfig:
		FileConfig config = FileConfig.of("config.toml");

		// Loads the config (reads the file):
		config.load();
		// Note: the load() call is always blocking: it returns when the reading operation terminates.
		// load() is also used to reload the configuration.

		System.out.println("Config: " + config);

		// Modifies the config:
		config.set("key", "value");
		config.set("number", 123456);
		config.set("floatingPoint", 3.1415926535);

		// You can also use sub configs!
		Config subConfig = Config.inMemory();
		subConfig.set("subKey", "value");
		config.set("subConfig", subConfig);

		// NightConfig supports dot-separated paths:
		config.set("subConfig.subKey", "newValue");

		// If you want to use a key that contains a dot in its name, use a list:
		config.set(Arrays.asList("subConfig", "127.0.0.1"),
				   "test");// the key "127.0.0.1" is in subConfig

		System.out.println("Config: " + config);

		// Saves the config:
		config.save();
		// Note: by default, the save operation is done in the background, and config.save()
		// returns immediately without waiting for the operation to terminates.

		/* Once you don't need the FileConfig anymore, remember to close it, in order to release
		the associated resources. There aren't always such resources, but it's a good practise to
		call the close() method anyway.
		Closing the FileConfig also ensures that all the data has been written, in particular it
		waits for the background saving operations to complete. */
		config.close();
	}
}