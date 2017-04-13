package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.SimpleConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.io.FileConfig;
import java.io.File;
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

	public JsonConfig(UnmodifiableConfig toCopy) {
		super(toCopy);
	}

	public JsonConfig(Map<String, Object> map) {
		super(map);
	}

	@Override
	public JsonConfig clone() {
		return new JsonConfig(this);
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return SimpleConfig.BASIC_SUPPORT_PREDICATE.test(type);
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
			clear();
		}
		new JsonParser().parse(file, this);
	}
}