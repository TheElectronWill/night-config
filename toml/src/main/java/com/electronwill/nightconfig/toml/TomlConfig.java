package com.electronwill.nightconfig.toml;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.FileConfig;
import java.io.File;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.Map;

/**
 * @author TheElectronWill
 */
public class TomlConfig extends AbstractConfig implements FileConfig {
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
	public void write(File file, boolean append) {
		new TomlWriter().write(this, file, append);
	}

	@Override
	public void parse(File file, boolean merge) {
		if (!merge) { asMap().clear(); }
		new TomlParser().parse(file, this);
	}
}