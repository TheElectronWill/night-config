package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import java.io.File;
import org.junit.jupiter.api.Test;

/**
 * @author TheElectronWill
 */
public class CommentedFileConfigTest {
	public static void main(String[] args) {
		new CommentedFileConfigTest().test();
	}
	@Test
	public void test() {
		File file = new File("test.conf");
		CommentedFileConfig config = CommentedFileConfig.of(file);
		System.out.println(config.size());
	}
}