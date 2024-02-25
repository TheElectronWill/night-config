package com.electronwill.nightconfig.core;

import java.util.concurrent.ConcurrentMap;

/**
 * @author TheElectronWill
 * @deprecated A concurrent HashMap is not enough to make the whole spec robust to multi-threaded use.
 */
@Deprecated
public class ConcurrentConfigSpec extends ConfigSpec {
	/** Creates a new ConfigSpec backed by a {@link ConcurrentMap}. */
	public ConcurrentConfigSpec() {
		super(Config.inMemoryUniversalConcurrent());
	}
}
