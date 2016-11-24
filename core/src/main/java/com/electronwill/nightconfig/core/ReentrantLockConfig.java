package com.electronwill.nightconfig.core;

import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A thread-safe configuration that uses a {@link Map} to store its data and a {@link ReentrantReadWriteLock} to
 * ensure its thread-safety.
 *
 * @author TheElectronWill
 */
public final class ReentrantLockConfig extends MapConfig implements SafeConfig {
	/**
	 * The internal lock which is used to guarantee the thread-safety of the config.
	 */
	private final ReadWriteLock lock;

	/**
	 * Creates a new {@code RWLockConfig} backed by a new {@link java.util.HashMap} and a new <i>unfair</i>
	 * {@link ReentrantReadWriteLock}. This constructor is equivalent to {@code ReentrantLockConfig(false)}.
	 *
	 * @see #ReentrantLockConfig(boolean)
	 */
	public ReentrantLockConfig() {
		this(false);
	}

	/**
	 * Creates a new {@code RWLockConfig} backed by a new {@link java.util.HashMap} and a new
	 * {@link ReentrantReadWriteLock} with the specified fairnes policy.
	 *
	 * @see ReentrantReadWriteLock
	 */
	public ReentrantLockConfig(boolean fairLock) {
		this.lock = new ReentrantReadWriteLock(fairLock);
	}

	/**
	 * {@inheritDoc}
	 * The returned lock is reentrant.
	 */
	@Override
	public Lock getReadLock() {
		return lock.readLock();
	}

	/**
	 * {@inheritDoc}
	 * The returned lock is reentrant.
	 */
	@Override
	public Lock getWriteLock() {
		return lock.writeLock();
	}

	@Override
	public int size() {
		getReadLock().lock();//read lock
		try {
			return map.size();
		} finally {
			getReadLock().unlock();//read unlock
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * The returned map is <b>not</b> thread-safe. The caller is responsible for properly ensuring the
	 * thread-safety, by using the configuration's read and write locks.
	 * </p>
	 */
	@Override
	public Map<String, Object> asMap() {
		return map;
	}

	@Override
	public boolean containsValue(String path) {
		final List<String> keys = StringUtils.split(path, ',');
		final int lastIndex = keys.size() - 1;
		getReadLock().lock();// read lock
		try {
			return containsValue(keys, lastIndex);
		} finally {
			getReadLock().unlock();//read unlock
		}
	}

	@Override
	public Object getValue(String path) {
		final List<String> keys = StringUtils.split(path, ',');
		final int lastIndex = keys.size() - 1;
		getReadLock().lock();//read lock
		try {
			return getValue(keys, lastIndex);
		} finally {
			getReadLock().unlock();//read unlock
		}
	}

	@Override
	public void setValue(String path, Object value) {
		final List<String> keys = StringUtils.split(path, ',');
		final int lastIndex = keys.size() - 1;
		getWriteLock().lock();//write lock
		try {
			setValue(keys, lastIndex, value);
		} finally {
			getWriteLock().unlock();//write unlock
		}
	}
}
