package com.electronwill.nightconfig.core.serialization;

import com.electronwill.nightconfig.core.Config;

/**
 * Interface for writing Configs to CharacterOutputs.
 *
 * @author TheElectronWill
 */
@FunctionalInterface
public interface ConfigWriter {
	/**
	 * Writes a configuration.
	 *
	 * @param config the config to write
	 * @param output the output to write it to
	 */
	void writeConfig(Config config, CharacterOutput output);
}
