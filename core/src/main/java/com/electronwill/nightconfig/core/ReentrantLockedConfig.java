package com.electronwill.nightconfig.core;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A thread-safe wrapper around a configuration. It uses a reentrant read lock and a reentrant write lock.
 * <p>
 * Any sub-configuration added to a LockedConfig is wrapped in a LockedConfig. Therefore, a config returned
 * by a {@code getConfig(path)} method is guaranteed to be a SafeConfig.
 * </p>
 *
 * @author TheElectronWill
 */
public final class ReentrantLockedConfig implements SafeConfig {
	/**
	 * The internal lock which is used to guarantee the thread-safety of the config.
	 */
	private final ReadWriteLock lock;

	/**
	 * The internal config that contains the value. It should be accessed only through this locked config.
	 */
	private final Config config;

	/**
	 * Creates a new {@code ReentrantLockedConfig} around a given configuration, and with a new <i>unfair</i>
	 * {@link ReentrantReadWriteLock}. This constructor is equivalent to {@code LockedConfig(config, false)}.
	 *
	 * @param config the configuration to wrap
	 * @see #ReentrantLockedConfig(Config, boolean)
	 */
	public ReentrantLockedConfig(Config config) {
		this(config, false);
	}

	/**
	 * Creates a new {@code ReentrantLockedConfig} around a given configuration, and with a new
	 * {@link ReentrantReadWriteLock} with the specified fairnes policy.
	 *
	 * @param fairLock true to use a fair lock, false to use an unfair lock
	 * @see ReentrantReadWriteLock
	 */
	public ReentrantLockedConfig(Config config, boolean fairLock) {
		this.config = config;
		this.lock = new ReentrantReadWriteLock(fairLock);
	}

	/**
	 * {@inheritDoc}
	 * The returned lock is reentrant. It will always be the same, that is, it will not change between
	 * several method calls.
	 */
	@Override
	public Lock getReadLock() {
		return lock.readLock();
	}

	/**
	 * {@inheritDoc}
	 * The returned lock is reentrant. It will always be the same, that is, it will not change between
	 * several method calls.
	 */
	@Override
	public Lock getWriteLock() {
		return lock.writeLock();
	}

	@Override
	public int size() {
		getReadLock().lock();//read lock
		try {
			return config.size();
		} finally {
			getReadLock().unlock();//read unlock
		}
	}

	@Override
	public Map<String, Object> asMap() {
		return config.asMap();
	}

	@Override
	public SafeConfig createEmptyConfig() {
		return new ReentrantLockedConfig(config.createEmptyConfig());
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return config.supportsType(type);
	}

	@Override
	public boolean containsValue(List<String> path) {
		/*
		For every config, lock it (with a read lock) to safely get the next level (that is, the sub-config).
		Eventually, when we are at the last sub-config, we check if the value we are looking for exists, by
		 using the containsValue(List) method. At this point we don't need to lock it explicitely.
		 */
		SafeConfig currentConfig = this;
		for (String key : path) {
			final Object value;
			final List<String> keyPath = Collections.singletonList(key);//optimization for getValue
			final Lock lock = currentConfig.getReadLock();
			lock.lock();
			try {
				value = currentConfig.getValue(keyPath);
				if (!(value instanceof Config)) {//missing or incompatible intermediary level
					return false;
				}
			} finally {
				lock.unlock();
			}
			currentConfig = ((SafeConfig)value);
		}
		final List<String> lastElement = path.subList(path.size() - 1, path.size());//contains the last element of the path
		return currentConfig.containsValue(lastElement);
	}

	@Override
	public Object getValue(List<String> path) {
		/*
		For every config, lock it (with a read lock) to safely get the next level (that is, the sub-config).
		Eventually, when we are at the last sub-config, we get the value we want by using the getValue
		(List) method. At this point we don't need to lock it explicitely.
		 */
		SafeConfig currentConfig = this;
		for (String key : path) {
			final SafeConfig value;
			final List<String> keyPath = Collections.singletonList(key);//optimization for getConfig
			final Lock lock = currentConfig.getReadLock();
			lock.lock();
			try {
				value = currentConfig.getConfig(keyPath);
				if (value == null) {//missing or incompatible intermediary level
					return null;
				}
			} finally {
				lock.unlock();
			}
			currentConfig = value;
		}
		final List<String> lastElement = path.subList(path.size() - 1, path.size());//contains the last element of the path
		return currentConfig.getValue(lastElement);
	}

	@Override
	public void setValue(List<String> path, Object value) {
		/*
		Ensure that all sub configs are instances of SafeConfig.
		 */
		if (value instanceof Config) {
			value = new ReentrantLockedConfig((Config)value);
		}
		/*
		For every config, lock it (with a write lock) to safely get the next level (that is, the sub-config).
		Here we have to use a write lock even if no new intermediary is created. We can't use a read lock
		and, while it is locked, use the write lock: this is not possible with a ReentrantReadWriteLock
		(read the documentation of ReentrantReadWriteLock for more information).
		 */
		SafeConfig currentConfig = this;
		for (String currentKey : path) {
			final SafeConfig config;
			final List<String> keyPath = Collections.singletonList(currentKey);//optimization: use a list for
			// the currentConfig's methods to avoid any useless parsing of the key
			final Lock lock = currentConfig.getWriteLock();
			lock.lock();
			try {
				final Object currentValue = currentConfig.getValue(keyPath);
				if (currentValue == null) {//missing intermediary level => create it
					config = currentConfig.createEmptyConfig();
					currentConfig.setValue(keyPath, config);
				} else {//existing intermediary level.
					config = (SafeConfig)currentValue;//If currentValue is not a SafeConfig, throws a ClassCastException
				}
			} finally {
				lock.unlock();
			}
			currentConfig = config;
		}
		final List<String> lastElement = path.subList(path.size() - 1, path.size());//contains the last element of the path
		currentConfig.setValue(lastElement, value);//no need to lock here: currentConfig is a SafeConfig
		// and will do the necessary locking by itself
	}

	@Override
	public boolean equals(Object obj) {
		return config.equals(obj);
	}

	@Override
	public int hashCode() {
		return config.hashCode();
	}
}
