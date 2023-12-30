import com.electronwill.nightconfig.core.file.FileConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author TheElectronWill
 */
public class AutoreloadExample {
	public static void main(String[] args) throws IOException, InterruptedException {
		// Create an autoreloaded config
		File configFile = new File("autoreload.json");
		FileConfig config = FileConfig.builder(configFile).autoreload().build();
		System.out.println("Config: " + config.valueMap());

		// Wait a bit for the file monitoring to start
		Thread.sleep(250);

		// Modify the file
		try (FileWriter writer = new FileWriter(configFile)) {
			writer.write("{ \"value\": 123 }");
		}
		Thread.sleep(100); // The modifications take some time to be detected (usually < 1 second)
		System.out.println("Config: " + config.valueMap());// Config: {value=123}

		// Modify again
		// The reloading of configurations is throttled: a minimum delay is ensured between two reloading.
		try (FileWriter writer = new FileWriter(configFile)) {
			writer.write("{ \"value\": -1, \"it_works\": \"yes!\" }");
		}
		// throttling, the config is not reloaded immediately
		System.out.println("Config: " + config.valueMap());// Config: {value=123}

		Thread.sleep(500); // wait until the throttling delay expires, and then look at the config again
		System.out.println("Config: " + config.valueMap());// Config: {it_works=yes!, value=-1}

		/* Don't forget to close the config! In that case the program terminates so it's not a
		big deal, but otherwise it's important to close the FileConfig in order to release the
		associated resources. For an autoreloaded config, the close() method tells the OS that it
		doesn't need to watch this file anymore. */
		config.close();
	}
}
