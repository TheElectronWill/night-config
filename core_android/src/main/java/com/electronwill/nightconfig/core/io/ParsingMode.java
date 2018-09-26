package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.utils.Consumer;
import com.electronwill.nightconfig.core.utils.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * @author TheElectronWill
 */
public enum ParsingMode {
	/**
	 * Replaces the existing config by the parsed one.
	 */
	REPLACE(Config::clear, Config::set, Map::put),

	/**
	 * Merges the parsed config with the existing one: the parsed values are prioritary.
	 * This mode performs a shallow merge, not a deep one.
	 */
	MERGE(c -> {}, Config::set, Map::put),

	/**
	 * Adds the parsed values to the config: the existing values are prioritary and will not be
	 * replaced.
	 */
	ADD(c -> {}, (cfg, path, value) -> {
		cfg.add(path, value);
		return null;
	}, Map::putIfAbsent);

	private final Consumer<? super Config> preparationAction;
	private final PutAction putAction;
	private final MapPutAction mapPutAction;

	ParsingMode(Consumer<? super Config> preparationAction, PutAction putAction,
				MapPutAction mapPutAction) {
		this.preparationAction = preparationAction;
		this.putAction = putAction;
		this.mapPutAction = mapPutAction;
	}

	/**
	 * Prepare the config to be parsed with this mode. This method is called before the parsed
	 * data is put into the config.
	 *
	 * @param config the config that will be parsed
	 */
	public void prepareParsing(Config config) {
		preparationAction.accept(config);
	}

	/**
	 * Puts (set or add) a value into the config
	 *
	 * @return the previous value if any, or null if none
	 */
	public Object put(Config config, List<String> key, Object value) {
		return putAction.put(config, key, value);
	}

	/**
	 * Puts (set or add) a value into the config
	 *
	 * @return the previous value if any, or null if none
	 */
	public Object put(Config config, String key, Object value) {
		return putAction.put(config, key, value);
	}

	/**
	 * Puts (set or add) a value into the config
	 *
	 * @return the previous value if any, or null if none
	 */
	public Object put(Map<String, Object> map, String key, Object value) {
		return mapPutAction.put(map, key, value);
	}

	@FunctionalInterface
	private interface PutAction {
		Object put(Config config, List<String> key, Object value);

		default Object put(Config config, String key, Object value) {
			return put(config, StringUtils.split(key, '.'), value);
		}
	}

	@FunctionalInterface
	private interface MapPutAction {
		Object put(Map<String, Object> map, String key, Object value);
	}
}