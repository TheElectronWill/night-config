package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.MapConfig;
import com.electronwill.nightconfig.core.io.FileConfig;
import java.io.File;
import java.io.IOException;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Map;

/**
 * @author TheElectronWill
 */
public class TomlConfig extends MapConfig implements FileConfig {
	public TomlConfig() {}

	public TomlConfig(Map<String, Object> map) {
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
			   || Temporal.class.isAssignableFrom(type)
			   || List.class.isAssignableFrom(type)
			   || Config.class.isAssignableFrom(type);
	}

	@Override
	protected TomlConfig createSubConfig() {
		return new TomlConfig();
	}

	@Override
	public void writeTo(File file, boolean append) throws IOException {
		new TomlWriter().writeConfig(this, file, append);
	}

	@Override
	public void readFrom(File file, boolean merge) throws IOException {
		if (!merge) { asMap().clear(); }
		new TomlParser().parseConfig(file, this);
	}
}