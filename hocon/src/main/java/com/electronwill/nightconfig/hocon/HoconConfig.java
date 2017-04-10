package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.io.FileConfig;
import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * A <a href=https://github.com/typesafehub/config/blob/master/HOCON.md>HOCON</a> configuration.
 *
 * @author TheElectronWill
 */
public final class HoconConfig extends AbstractConfig implements FileConfig {
	public HoconConfig() {}

	public HoconConfig(Map<String, Object> map) {
		super(map);
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return type == Integer.class
			   || type == Long.class
			   || type == Float.class
			   || type == Double.class
			   || type == Boolean.class
			   || type == String.class
			   || List.class.isAssignableFrom(type)
			   || Config.class.isAssignableFrom(type);
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
			asMap().clear();
		}
		new HoconParser().parse(file, this);
	}
}