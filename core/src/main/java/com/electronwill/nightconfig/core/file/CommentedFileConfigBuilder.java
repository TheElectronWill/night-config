package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.io.ConfigFormat;
import com.electronwill.nightconfig.core.io.ConfigParser;
import com.electronwill.nightconfig.core.io.ConfigWriter;
import java.io.File;

/**
 * @author TheElectronWill
 */
public final class CommentedFileConfigBuilder extends FileConfigBuilder<CommentedConfig> {
	CommentedFileConfigBuilder(File file, CommentedConfig config,
							   ConfigWriter<? super CommentedConfig> writer,
							   ConfigParser<?, ? super CommentedConfig> parser) {
		super(file, config, writer, parser);
	}

	CommentedFileConfigBuilder(File file,
									  ConfigFormat<? extends CommentedConfig, ? super CommentedConfig, ? super CommentedConfig> format) {
		super(file, format);
	}

	public CommentedFileConfig build() {
		return new SimpleCommentedFileConfig(super.config, super.build());
	}
}