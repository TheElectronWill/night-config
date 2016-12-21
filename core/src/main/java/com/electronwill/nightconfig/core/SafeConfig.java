package com.electronwill.nightconfig.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;

/**
 * A thread-safe configuration. With a {@code SafeConfig}, each operation defined in the {@link Config}
 * interface is thread-safe. Furthermore, a {@code SafeConfig} gives access to a read lock and a write
 * lock. They are very useful when, for instance, the map returned by {@link #asMap()} is used; because
 * this map is <b>not</b> thread-safe.
 *
 * @author TheElectronWill
 */
public interface SafeConfig extends Config {
	/**
	 * Gets the lock used for reading.
	 * <p>
	 * Important things:
	 * <li>The returned lock is NOT necessarily reentrant.</li>
	 * <li>This method does NOT necessarily return the same value every time it is called.</li>
	 * </p>
	 *
	 * @return the config's read lock.
	 */
	Lock getReadLock();

	/**
	 * Gets the lock used for writing.
	 * <p>
	 * Important things:
	 * <li>The returned lock is NOT necessarily reentrant.</li>
	 * <li>This method does NOT necessarily return the same value every time it is called.</li>
	 * </p>
	 *
	 * @return the config's write lock.
	 */
	Lock getWriteLock();

	/**
	 * {@inheritDoc}
	 * This method returns a SafeConfig, which means that it is guaranteed to be thread-safe and to have a
	 * pair of locks.
	 */
	@Override
	default SafeConfig getConfig(List<String> path) {
		final Object value = getValue(path);
		if (value == null)
			return null;
		return (SafeConfig)value;
	}

	/**
	 * {@inheritDoc}
	 * This method returns a SafeConfig, which means that it is guaranteed to be thread-safe and to have a
	 * pair of locks.
	 */
	@Override
	default SafeConfig getConfig(String path) {
		final Object value = getValue(path);
		if (value == null)
			return null;
		return (SafeConfig)value;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * To ensure thread-safety, the caller is responsible for using the configuration's read and write locks.
	 * In particular, when the caller iterates on the map's entrySet, without modifying it, the read lock must
	 * be used like this:
	 * <pre>
	 *     final Lock readLock = config.getReadLock();
	 *     readLock.lock();
	 *     try {
	 *         for(Map.Entry&lt;String, Object&gt; entry : config.asMap().entrySet()) {
	 *             //Do something BUT DO NOT MODIFY the entry or the map!
	 *         }
	 *     } finally {
	 *         readLock.unlock();
	 *     }
	 * </pre>
	 * If the caller modifies the map then it must use the write lock instead of the read lock, like this:
	 * <pre>
	 *     final Lock writeLock = config.getWriteLock();
	 *     writeLock.lock();
	 *     try {
	 *         for(Map.Entry&lt;String, Object&gt; entry : config.asMap().entrySet()) {
	 *             //Here you can modify the values, because you have used the write lock
	 *         }
	 *     } finally {
	 *         writeLock.unlock();
	 *     }
	 * </pre>
	 * If you apply a function to the map (or to an entry of the map, or anything else that may modify the
	 * map's values) and you don't know (or you aren't absolutely confident) whether it modifies it or not,
	 * use the write lock. <b>Put it simply: use the read lock only if you are absolutely confident that you
	 * are just reading values and not writing/adding/modifying ones.</b>
	 * </p>
	 * <p>
	 * The caller is also responsible for ensuring the thread-safety of the map's values, and especially
	 * the sub configurations which are NOT protected by the configuration's read and write locks.
	 * </p>
	 */
	@Override
	Map<String, Object> asMap();

	@Override
	SafeConfig createEmptyConfig();
}
