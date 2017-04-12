package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.io.FileConfig;
import java.io.File;
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
public final class JsonConfig extends AbstractConfig implements FileConfig {
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
				|| Config.class.isAssignableFrom(type)
				|| type.isArray();
	}

	@Override
	protected JsonConfig createSubConfig() {
		return new JsonConfig();
	}

	@Override
	public void write(File file, boolean append) {
		new MinimalJsonWriter().write(this, file, append);
	}

	@Override
	public void parse(File file, boolean merge) {
		if (!merge) {
			this.valueMap().clear();// clears the config
		}
		new JsonParser().parse(file, this);
	}
}