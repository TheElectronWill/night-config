package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.AbstractCommentedConfig;
import com.electronwill.nightconfig.core.io.FileConfig;
import java.io.File;
import java.util.Map;

/**
 * A <a href=https://github.com/typesafehub/config/blob/master/HOCON.md>HOCON</a> configuration.
 *
 * @author TheElectronWill
 */
public final class HoconConfig extends AbstractCommentedConfig implements FileConfig {
	public HoconConfig() {}

	public HoconConfig(Map<String, Object> valuesMap) {
		super(valuesMap);
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