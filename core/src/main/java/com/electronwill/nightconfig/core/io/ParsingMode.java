package com.electronwill.nightconfig.core.io;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.utils.StringUtils;
import java.util.List;
import java.util.function.Consumer;

/**
 * @author TheElectronWill
 */
public enum ParsingMode {
	/**
	 * Replaces the existing config by the parsed one.
	 */
	REPLACE(Config::clear, Config::set),

	/**
	 * Merges the parsed config with the existing one: the parsed values are prioritary.
	 */
	MERGE(c -> {}, Config::set),

	/**
	 * Adds the parsed values to the config: the existing values are prioritary and will not be
	 * replaced.
	 */
	ADD(c -> {}, Config::add);

	private final Consumer<? super Config> preparationAction;
	private final PutAction putAction;

	ParsingMode(Consumer<? super Config> preparationAction, PutAction putAction) {
		this.preparationAction = preparationAction;
		this.putAction = putAction;
	}

	public void prepareParsing(Config config) {
		preparationAction.accept(config);
	}

	public void put(Config config, List<String> key, Object value) {
		putAction.put(config, key, value);
	}

	public void put(Config config, String key, Object value) {
		putAction.put(config, key, value);
	}

	@FunctionalInterface
	public interface PutAction {
		void put(Config config, List<String> key, Object value);

		default void put(Config config, String key, Object value) {
			put(config, StringUtils.split(key, '.'), value);
		}
	}

}