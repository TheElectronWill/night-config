package com.electronwill.nightconfig.core.path;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.utils.CommentedConfigWrapper;

import java.nio.file.Path;

/**
 * @author TheElectronWill
 */
class SimpleCommentedPathConfig extends CommentedConfigWrapper<CommentedConfig>
		implements CommentedPathConfig {
	private final PathConfig pathConfig;

	SimpleCommentedPathConfig(CommentedConfig config, PathConfig pathConfig) {
		super(config);
		this.pathConfig = pathConfig;
	}

	@Override
	public Path getPath() {
		return pathConfig.getPath();
	}

	@Override
	public void save() {
		pathConfig.save();
	}

	@Override
	public void load() {
		pathConfig.load();
	}

	@Override
	public void close() {
		pathConfig.close();
	}
}