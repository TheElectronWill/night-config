import com.electronwill.nightconfig.core.file.FileConfig;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @author TheElectronWill
 */
public class AutoreloadExample {
	public static void main(String[] args) throws IOException, InterruptedException {
		// Creates an autoreloaded config:
		File configFile = new File("autoreload.json");
		FileConfig config = FileConfig.builder(configFile).autoreload().build();
		System.out.println("Config: " + config.valueMap());

		// Modifies the file:
		try (FileWriter writer = new FileWriter(configFile)) {
			writer.write("{ \"value\": 123 }");
		}
		Thread.sleep(1000);// The modifications take some (short) time to be detected
		System.out.println("Config: " + config);// You should see "Config: {value=123}"

		/* Don't forget to close the config! In that case the program terminates so it's not a
		big deal, but otherwise it's important to close the FileConfig in order to release the
		associated resources. For an autoreloaded config, the close() method tells the OS that it
		doesn't need to watch this file anymore. */
		config.close();
	}
}