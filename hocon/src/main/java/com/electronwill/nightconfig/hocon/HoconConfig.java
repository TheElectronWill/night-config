package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.AbstractCommentedConfig;
import com.electronwill.nightconfig.core.SimpleConfig;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.FileConfig;
import java.io.File;
import java.util.Map;

/**
 * A <a href=https://github.com/typesafehub/config/blob/master/HOCON.md>HOCON</a> configuration.
 *
 * @author TheElectronWill
 */
public final class HoconConfig extends AbstractCommentedConfig implements FileConfig {
	/**
	 * Creates an empty HoconConfig.
	 */
	public HoconConfig() {}

	/**
	 * Creates an HoconConfig backed by the specified Map.
	 *
	 * @param valuesMap the map containing the values
	 */
	public HoconConfig(Map<String, Object> valuesMap) {
		super(valuesMap);
	}

	/**
	 * Creates an HoconConfig that is a copy of the given config.
	 *
	 * @param toCopy the config to copy
	 */
	public HoconConfig(UnmodifiableConfig toCopy) {
		super(toCopy);
	}

	/**
	 * Creates an HoconConfig that is a copy of the given config.
	 *
	 * @param toCopy the config to copy
	 */
	public HoconConfig(UnmodifiableCommentedConfig toCopy) {
		super(toCopy);
	}

	/**
	 * Creates an HoconConfig that is a copy of the given config.
	 *
	 * @param toCopy the config to copy
	 */
	public HoconConfig(AbstractCommentedConfig toCopy) {
		super(toCopy);
	}

	@Override
	public HoconConfig clone() {
		return new HoconConfig(this);
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return SimpleConfig.BASIC_SUPPORT_PREDICATE.test(type);
	}

	@Override
	protected HoconConfig createSubConfig() {
		return new HoconConfig();
	}

	@Override
	public void write(File file, boolean append) {
		new HoconWriter().write(this, file, append);
	}

	@Override
	public void parse(File file, boolean merge) {
		if (!merge) {
			clear();
		}
		new HoconParser().parse(file, this);
	}
}