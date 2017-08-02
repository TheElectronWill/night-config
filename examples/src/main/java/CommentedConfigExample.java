import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import com.electronwill.nightconfig.toml.TomlWriter;
import java.io.File;

/**
 * @author TheElectronWill
 */
public class CommentedConfigExample {
	public static void main(String[] args) {
		// Creates a CommentedConfig in memory (ie not linked to a file)
		CommentedConfig config = CommentedConfig.inMemory();
		config.set("key", "value");
		config.setComment("key", "This is a comment.");

		String comment = config.getComment("key");
		System.out.println("Comment of the key: " + comment);
		System.out.println("Config: " + config);

		/* An non-FileConfig cannot be saved with a save() method, but any config can be saved by
		 an appropriate ConfigWriter. Here we'll use a TomlWriter to write the config in the TOML
		 format. */
		File configFile = new File("commentedConfig.toml");
		TomlWriter writer = new TomlWriter();
		writer.write(config, configFile, WritingMode.REPLACE);
		/* That's it! The config has been written to the file. Note that there is no need to close
		 the writer nor to catch any exception. */
	}
}