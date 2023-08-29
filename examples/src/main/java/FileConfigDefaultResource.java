import java.io.File;

import com.electronwill.nightconfig.core.file.FileConfig;

public class FileConfigDefaultResource {
	public static void main(String[] args) {
		File localFile = new File("local-file.json");
		localFile.delete();

		FileConfig conf = FileConfig.builder(localFile).defaultResource("some-resource.json").build();
		conf.load();

		System.out.println(conf);

		conf.set("value", "added to the local file");
		conf.save();
	}
}
