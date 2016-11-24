package com.electronwill.nightconfig.core;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;

/**
 * A thread-safe configuration. With a {@code SafeConfig}, each operation defined in the {@link Config}
 * interface is thread-safe. Furthermore, a {@code SafeConfig} gives access to a read lock and a write
 * lock.
 *
 * @author TheElectronWill
 */
public interface SafeConfig extends Config {

	/**
	 * Gets the lock used for reading.
	 *
	 * @return the config's read lock.
	 * @see ReadWriteLock#readLock()
	 * @see ReadWriteLock
	 */
	Lock getReadLock();

	/**
	 * Gets the lock used for writing.
	 *
	 * @return the config's write lock.
	 * @see ReadWriteLock#writeLock()
	 * @see ReadWriteLock
	 */
	Lock getWriteLock();
}
