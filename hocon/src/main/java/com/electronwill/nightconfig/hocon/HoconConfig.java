package com.electronwill.nightconfig.hocon;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.MapConfig;
import com.electronwill.nightconfig.core.io.FileConfig;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author TheElectronWill
 */
public final class HoconConfig extends MapConfig implements FileConfig {
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
	public void writeTo(File file, boolean append) throws IOException {
		new HoconWriter().writeConfig(this, file, append);
	}

	@Override
	public void readFrom(File file, boolean merge) throws IOException {
		if (!merge) asMap().clear();
		new HoconParser().parseConfig(file, this);
	}
}
