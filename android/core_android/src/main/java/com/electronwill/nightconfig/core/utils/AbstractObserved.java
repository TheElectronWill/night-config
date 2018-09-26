package com.electronwill.nightconfig.core.utils;

/**
 * @author TheElectronWill
 */
abstract class AbstractObserved {
	protected final Runnable callback;

	protected AbstractObserved(Runnable callback) {
		this.callback = callback;
	}
}