package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.AbstractCommentedConfig;
import com.electronwill.nightconfig.core.SimpleConfig;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.FileConfig;
import java.io.File;
import java.util.Map;

/**
 * A TOML configuration.
 *
 * @author TheElectronWill
 */
public class TomlConfig extends AbstractCommentedConfig implements FileConfig {
	/**
	 * Creates an empty TomlConfig.
	 */
	public TomlConfig() {}

	/**
	 * Creates a TomlConfig backed by the given Map.
	 *
	 * @param valueMap the map containing the values
	 */
	public TomlConfig(Map<String, Object> valueMap) {
		super(valueMap);
	}

	/**
	 * Creates a TomlConfig that is a copy of the specified config.
	 *
	 * @param toCopy the config to copy
	 */
	public TomlConfig(UnmodifiableConfig toCopy) {
		super(toCopy);
	}

	/**
	 * Creates a TomlConfig that is a copy of the specified config.
	 *
	 * @param toCopy the config to copy
	 */
	public TomlConfig(UnmodifiableCommentedConfig toCopy) {
		super(toCopy);
	}

	@Override
	public TomlConfig clone() {
		return new TomlConfig(this);
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return SimpleConfig.BASIC_SUPPORT_PREDICATE.test(type);
	}

	@Override
	protected TomlConfig createSubConfig() {
		return new TomlConfig();
	}

	@Override
	public void write(File file, boolean append) {
		new TomlWriter().write(this, file, append);
	}

	@Override
	public void parse(File file, boolean merge) {
		if (!merge) {
			clear();
		}
		new TomlParser().parse(file, this);
	}
}