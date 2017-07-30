package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.ConfigFormat;
import java.io.File;

/**
 * @author TheElectronWill
 */
public interface CommentedFileConfig extends CommentedConfig, FileConfig {
	@Override
	default CommentedFileConfig checked() {
		return new CheckedCommentedFileConfig(this);
	}

	static CommentedFileConfig of(File file,
								  ConfigFormat<? extends CommentedConfig, ? super CommentedConfig, ? super CommentedConfig> format) {
		return builder(file, format).build();
	}

	static CommentedFileConfigBuilder builder(File file,
											  ConfigFormat<? extends CommentedConfig, ? super CommentedConfig, ? super CommentedConfig> format) {
		return new CommentedFileConfigBuilder(file, format);
	}
}