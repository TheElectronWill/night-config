import com.electronwill.nightconfig.core.file.FileConfig;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

/**
 * @author TheElectronWill
 */
public class AutosaveExample {
	public static void main(String[] args) throws IOException, InterruptedException {
		// Creates an autosaved config:
		File configFile = new File("autosave.json");
		FileConfig config = FileConfig.builder(configFile).autosave().build();

		// Reads the file:
		printFile(configFile);

		// Modifies the config:
		config.set("value", 123);

		Thread.sleep(500);
		/* The config we built is in "asynchronous writing" mode, which means that the writing
		operations occurs in the background, without blocking the config.set method. That's why
		to see the modifications in our program we have to wait a little bit.

		Try adding sync() to the build chain and removing the Thread.sleep instruction!
		*/
		printFile(configFile);

		// Modifies the config again:
		config.set("value", 1234);
		Thread.sleep(500);
		printFile(configFile);

		/* Don't forget to close the config! In that case the program terminates so it's not a
		big deal, but otherwise it's important to close the FileConfig in order to release the
		associated resources. For an autosaved config, the close() method discards the
		channel/stream used to save the config. */
		config.close();
	}

	private static void printFile(File file) throws IOException {
		System.out.println("--- config file ---");
		if (file.exists()) {
			for (String line : Files.readAllLines(file.toPath())) {
				System.out.println(line);
			}
		} else {
			System.out.println("The file doesn't exist.");
		}
	}
}