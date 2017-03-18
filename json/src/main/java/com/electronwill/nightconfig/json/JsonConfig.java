package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.MapConfig;
import com.electronwill.nightconfig.core.io.FileConfig;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * A JSON configuration. It supports the following types:
 * <ul>
 * <li>Integer</li>
 * <li>Long</li>
 * <li>Float</li>
 * <li>Double</li>
 * <li>Boolean</li>
 * <li>String</li>
 * <li>Collection and its subclasses</li>
 * <li>Config and its subclasses</li>
 * </ul>
 *
 * @author TheElectronWill
 */
public final class JsonConfig extends MapConfig implements FileConfig {
	public JsonConfig() {}

	public JsonConfig(Map<String, Object> map) {
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
				|| Collection.class.isAssignableFrom(type)
				|| Config.class.isAssignableFrom(type);
	}

	@Override
	protected JsonConfig createSubConfig() {
		return new JsonConfig();
	}

	@Override
	public void writeTo(File file, boolean append) throws IOException {
		new MinimalJsonWriter().writeConfig(this, file, append);
	}

	@Override
	public void readFrom(File file, boolean merge) throws IOException {
		if (!merge) {
			this.asMap().clear();// clears the config
		}
		new JsonParser().parseConfig(file, this);
	}
}