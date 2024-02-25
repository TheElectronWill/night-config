package com.electronwill.nightconfig.core.concurrent;

import static com.electronwill.nightconfig.core.NullObject.NULL_OBJECT;

import java.util.AbstractCollection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.electronwill.nightconfig.core.AbstractCommentedConfig;
import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;
import com.electronwill.nightconfig.core.IncompatibleIntermediaryLevelException;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.utils.TransformingSet;

/**
 * A thread-safe configuration based on a {@link StampedLock}.
 * <p>
 * Unlike {@link SynchronizedConfig}, {@code StampedConfig} allows multiple concurrent reads.
 * It should also perform better under contention.
 */
public final class StampedConfig implements ConcurrentCommentedConfig {

    private final ConfigFormat<?> configFormat;
    private final Supplier<Map<String, Object>> mapSupplier;
    private Map<String, Object> values;
    private Map<String, String> comments;

    private final StampedLock lock = new StampedLock();

    /** current state for reasonable deadlock prevention */
    private final ThreadLocal<ThreadConfigState> state = ThreadLocal
            .withInitial(() -> ThreadConfigState.NORMAL);

    // BEWARE: StampedLock does not support reentrant locking

    public StampedConfig() {
        this(InMemoryCommentedFormat.defaultInstance(), Config.getDefaultMapCreator(false));
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public StampedConfig(ConfigFormat<?> configFormat, Supplier<Map<String, Object>> mapSupplier) {
        this.configFormat = configFormat;
        this.mapSupplier = mapSupplier;
        this.values = mapSupplier.get();
        this.comments = (Map) mapSupplier.get();
    }

    StampedConfig(ConfigFormat<?> configFormat, Supplier<Map<String, Object>> mapSupplier,
            Map<String, Object> values, Map<String, String> comments) {
        this.configFormat = configFormat;
        this.mapSupplier = mapSupplier;
        this.values = values;
        this.comments = comments;
    }

    // ----- specific -----
    /**
     * Atomically replaces the content of this config by the content of the specified config.
     * The specified config cannot be used anymore after this operation.
     *
     * @param newContent the new content (cannot be used anymore after this)
     */
    public void replaceContentBy(StampedConfig newContent) {
        checkStateForNormalOp();
        long stamp = lock.writeLock();
        try {
            long otherVS = newContent.lock.writeLock();
            try {
                this.values = newContent.values;
                this.comments = newContent.comments;
                newContent.values = null;
                newContent.comments = null;
            } finally {
                newContent.lock.unlockWrite(otherVS);
            }
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * Atomically replaces the content of this config by the content of the specified accumulator.
     * The accumulator cannot be used anymore after this operation.
     *
     * @param newContent the new content (cannot be used anymore after this)
     */
    public void replaceContentBy(Accumulator newContent) {
        checkStateForNormalOp();
        long stamp = lock.writeLock();
        try {
            newContent.prepareReplacement();
            this.values = newContent.values();
            this.comments = newContent.comments();
            newContent.invalidate();
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /**
     * Creates a new accumulator with the same {@code Supplier<Map>} and {@link ConfigFormat} as this config.
     * See {@link Accumulator} for more information.
     */
    public Accumulator newAccumulator() {
        return new Accumulator(configFormat, mapSupplier);
    }

    /**
     * Creates a deep copy of this config into an {@link Accumulator}.
     *
     * @return a deep copy
     */
    public Accumulator newAccumulatorCopy() {
        Accumulator acc = (Accumulator) copyValueInAccumulator(this);
        return acc;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private Object copyValueInAccumulator(Object v) {
        if (v instanceof StampedConfig) {
            StampedConfig stamped = (StampedConfig) v;
            stamped.checkStateForNormalOp();

            // lock the config and copy the values and comments
            long stamp = stamped.lock.readLock();
            try {
                Map<String, Object> valuesCopy = mapSupplier.get();
                valuesCopy.putAll(stamped.values);
                valuesCopy.replaceAll((k, w) -> copyValueInAccumulator(w));

                Map<String, String> commentsCopy = (Map) mapSupplier.get();
                commentsCopy.putAll(stamped.comments);
                return new Accumulator(valuesCopy, commentsCopy, mapSupplier, configFormat);
            } finally {
                stamped.lock.unlockRead(stamp);
            }

        } else if (v instanceof List) {
            List<Object> l = (List<Object>) v;
            List<Object> copy = new ArrayList<>(l);
            copy.replaceAll(elem -> copyValueInAccumulator(elem));
            return copy;
        } else {
            return v;
        }
    }

    /**
     * A CommentedConfig that allows to quickly accumulate values before a {@link #replaceContentBy(Accumulator)}.
     * It is NOT thread-safe.
     * <p>
     * Example:
     *
     * <pre>
     * {@code
     * StampedConfig config = new StampedConfig(configFormat, mapSupplier);
     * Accumulator acc = config.newAccumulator();
     *
     * // parse a file into the accumulator, the StampedConfig is not locked
     * configParser.parse(file, acc);
     *
     * // atomically replace the config's content with the accumulator's
     * config.replaceContentBy(acc);
     * }
     * </pre>
     */
    public static final class Accumulator extends AbstractCommentedConfig {
        // Seamlessly mirrors the values and comments. This StampedConfig is backed by the same maps
        // as the Accumulator, but access to the maps are unsynchronized to make the Accumulator fast.
        // When the Accumulator is done, the mirror is used to get the right structure
        // (all subconfigs of a StampedConfig must be StampedConfig too).
        private final StampedConfig mirror;
        private boolean valid = true;

        Accumulator(Map<String, Object> values, Map<String, String> comments,
                Supplier<Map<String, Object>> mapSupplier, ConfigFormat<?> configFormat) {
            super(values, comments);
            this.mirror = new StampedConfig(configFormat, mapSupplier, values, comments);
        }

        Accumulator(ConfigFormat<?> configFormat, Supplier<Map<String, Object>> mapSupplier) {
            super(mapSupplier);
            this.mirror = new StampedConfig(configFormat, mapSupplier, map, commentMap);
        }

        // public static Accumulator inMemoryUniversal() {
        // return new Accumulator(InMemoryCommentedFormat.defaultInstance(), Config.getDefaultMapCreator(false));
        // }

        private void checkValid() {
            if (!valid) {
                throw new IllegalStateException(
                        "This StampedConfig.Accumulator is no longer valid after a call to replaceContentBy().");
            }
        }

        void invalidate() {
            valid = false;
        }

        Map<String, Object> values() {
            return map;
        }

        Map<String, String> comments() {
            return commentMap;
        }

        Supplier<Map<String, Object>> mapSupplier() {
            return mapCreator;
        }

        /** Replaces all sub-configurations by their StampedConfig mirrors. */
        void prepareReplacement() {
            checkValid();
            map.replaceAll((k, v) -> replaceValue(v));
        }

        private Object replaceValue(Object v) {
            if (v instanceof Accumulator) {
                Accumulator acc = (Accumulator) v;
                acc.prepareReplacement();
                return acc.mirror;
            } else if (v instanceof UnmodifiableConfig) {
                throw new IllegalStateException("Invalid sub-configuration of type "
                        + v.getClass().getSimpleName()
                        + " in the Accumulator. Sub-configurations must always be created with createSubConfig().");
            } else if (v instanceof List) {
                List<?> l = (List<?>) v;
                List<Object> newList = new ArrayList<>(l);
                newList.replaceAll(elem -> replaceValue(elem));
                return newList;
            } else {
                return v;
            }
        }

        @Override
        public AbstractCommentedConfig clone() {
            Accumulator copy = new Accumulator(configFormat(), mapCreator);
            copy.map.putAll(this.map);
            copy.commentMap.putAll(this.commentMap);
            return copy;
        }

        @Override
        public CommentedConfig createSubConfig() {
            return new Accumulator(configFormat(), mapCreator);
        }

        @Override
        public ConfigFormat<?> configFormat() {
            checkValid();
            return mirror.configFormat();
        }
    }

    // ----- internal -----
    private <V> V mapLockGet(Map<String, V> map, StampedLock lock, String key) {
        long stamp = lock.tryOptimisticRead();
        V value = map.get(key);
        if (!lock.validate(stamp)) {
            // optimistic read failed, use a full lock
            checkStateForNormalOp(); // if in bulk, that's a mistake from the library user
            stamp = lock.readLock();
            try {
                value = map.get(key);
            } finally {
                lock.unlockRead(stamp);
            }
        } else {
            assert state.get() == ThreadConfigState.NORMAL : "invalid state " + state.get()
                    + " are you using bulk operations / iterators properly?";
        }
        return value;
    }

    private <V> boolean mapLockContains(Map<String, V> map, StampedLock lock, String key) {
        // try optimistic read first
        long stamp = lock.tryOptimisticRead();
        boolean contains = map.containsKey(key);
        if (!lock.validate(stamp)) {
            // optimistic read failed, use a full lock
            checkStateForNormalOp(); // if in bulk, that's a mistake from the library user
            stamp = lock.readLock();
            try {
                contains = map.containsKey(key);
            } finally {
                lock.unlockRead(stamp);
            }
        }
        assert state.get() == ThreadConfigState.NORMAL : "invalid state " + state.get()
                + " are you using bulk operations / iterators properly?";

        return contains;
    }

    private <V> V mapLockRemove(Map<String, V> map, StampedLock lock, String key) {
        long stamp = lock.tryWriteLock();
        if (stamp == 0) {
            checkStateForNormalOp();
            stamp = lock.writeLock();
        }
        assert state.get() == ThreadConfigState.NORMAL : "invalid state " + state.get()
                + " are you using bulk operations / iterators properly?";

        try {
            return map.remove(key);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    private <V> V mapLockPut(Map<String, V> map, StampedLock lock, String key, V value) {
        long stamp = lock.tryWriteLock();
        if (stamp == 0) {
            checkStateForNormalOp();
            stamp = lock.writeLock();
        }
        assert state.get() == ThreadConfigState.NORMAL : "invalid state " + state.get()
                + " are you using bulk operations / iterators properly?";

        try {
            return map.put(key, value);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    private <V> V mapLockPutIfAbsent(Map<String, V> map, StampedLock lock, String key, V value) {
        long stamp = lock.tryWriteLock();
        if (stamp == 0) {
            checkStateForNormalOp();
            stamp = lock.writeLock();
        }
        assert state.get() == ThreadConfigState.NORMAL : "invalid state " + state.get()
                + " are you using bulk operations / iterators properly?";

        try {
            return map.putIfAbsent(key, value);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /** Finds an existing subconfig with the given path (for example "a.subconfig"). */
    private StampedConfig getExistingConfig(List<String> configPath,
            boolean failIfIncompatibleLevel) {
        // optimization: no recursion here
        StampedConfig current = this;
        for (String key : configPath) {
            Object level = mapLockGet(current.values, current.lock, key);
            if (level == null) {
                // missing intermediary level: stop
                return null;
            } else if (level instanceof StampedConfig) {
                current = (StampedConfig) level;
            } else {
                // impossible to go further: what should have been a subconfig is another type of value
                if (failIfIncompatibleLevel) {
                    throw new IncompatibleIntermediaryLevelException(
                            "Cannot get entry with parent path " + configPath
                                    + " because of an incompatible intermediary value of type: "
                                    + level.getClass());
                } else {
                    return null;
                }
            }
        }
        return current;
    }

    /** Finds a subconfig with the given path (for example "a.subconfig"), creates it if it does not exist yet. */
    private StampedConfig getOrCreateConfig(List<String> configPath) {
        assert state.get() == ThreadConfigState.NORMAL : "invalid state " + state.get()
                + " are you using bulk operations / iterators properly?";

        // optimization: no recursion here
        StampedConfig current = this;
        for (String key : configPath) {
            StampedLock lock = current.lock;
            Map<String, Object> values = current.values;
            // try optimistic read once
            long stamp = lock.tryOptimisticRead();
            try {
                Object level = values.get(key);
                if (!lock.validate(stamp)) {
                    // Read has been invalidated, acquire the lock and read again.
                    checkStateForNormalOp();
                    stamp = lock.readLock();
                    level = values.get(key);
                }

                if (level == null) {
                    // Create missing intermediary level and continue one level down.
                    // We will insert the new level into the config, a write lock is necessary.
                    stamp = lock.tryConvertToWriteLock(stamp);
                    if (stamp == 0) {
                        // write lock not acquired, need to wait
                        checkStateForNormalOp();
                        stamp = lock.writeLock();
                    }
                    current = createSubConfig();
                    values.put(key, current);
                } else if (level instanceof StampedConfig) {
                    current = (StampedConfig) level;
                } else {
                    // Impossible to go further: what should have been a subconfig is another type of value.
                    throw new IncompatibleIntermediaryLevelException(
                            "Cannot get/create entry with parent path "
                                    + configPath
                                    + " because of an incompatible intermediary value of type: "
                                    + level.getClass());
                }
            } finally {
                if (StampedLock.isLockStamp(stamp)) {
                    lock.unlock(stamp);
                } // else: optimistic read succeeded, nothing to unlock
            }
        }
        return current;
    }

    // ----- Config -----

    @Override
    public int size() {
        long stamp = lock.tryOptimisticRead();
        int size = values.size();

        if (!lock.validate(stamp)) {
            checkStateForNormalOp();
            stamp = lock.readLock();
            try {
                size = values.size();
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return size;
    }

    @Override
    public StampedConfig createSubConfig() {
        return new StampedConfig(configFormat, mapSupplier);
    }

    @Override
    public ConfigFormat<?> configFormat() {
        return configFormat;
    }

    @Override
    public Map<String, Object> valueMap() {
        // TODO?
        throw new UnsupportedOperationException("StampedConfig does not support valueMap() yet.");
    }

    @Override
    public void clear() {
        long stamp = lock.tryWriteLock();
        if (stamp == 0) {
            checkStateForNormalOp();
            stamp = lock.writeLock();
        }
        try {
            values.clear();
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getRaw(List<String> path) {
        switch (path.size()) {
            case 0:
                throw new IllegalArgumentException("empty entry path");
            case 1:
                return (T) mapLockGet(values, lock, path.get(0));
            default:
                int lastIndex = path.size() - 1;
                List<String> parentPath = path.subList(0, lastIndex);
                StampedConfig parent = getExistingConfig(parentPath, false);
                if (parent == null) {
                    return null;
                }
                return (T) mapLockGet(parent.values, parent.lock, path.get(lastIndex));
        }
    }

    @Override
    public boolean contains(List<String> path) {
        switch (path.size()) {
            case 0:
                throw new IllegalArgumentException("empty entry path");
            case 1:
                return mapLockContains(values, lock, path.get(0));
            default: {
                int lastIndex = path.size() - 1;
                List<String> parentPath = path.subList(0, lastIndex);
                StampedConfig parent = getExistingConfig(parentPath, false);
                return parent != null
                        && mapLockContains(parent.values, parent.lock, path.get(lastIndex));
            }
        }
    }

    @Override
    public boolean add(List<String> path, Object value) {
        Object nnValue = (value == null) ? NULL_OBJECT : value;
        switch (path.size()) {
            case 0:
                throw new IllegalArgumentException("empty entry path");
            case 1:
                return mapLockPutIfAbsent(values, lock, path.get(0), nnValue) == null;
            default: {
                int lastIndex = path.size() - 1;
                List<String> parentPath = path.subList(0, lastIndex);
                StampedConfig parent = getOrCreateConfig(parentPath);
                Object prev = mapLockPutIfAbsent(parent.values, parent.lock,
                        path.get(lastIndex), nnValue);
                return prev == null;
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T remove(List<String> path) {
        switch (path.size()) {
            case 0:
                throw new IllegalArgumentException("empty entry path");
            case 1:
                return (T) mapLockRemove(values, lock, path.get(0));
            default: {
                int lastIndex = path.size() - 1;
                List<String> parentPath = path.subList(0, lastIndex);
                StampedConfig parent = getExistingConfig(parentPath, false);
                if (parent == null) {
                    return null;
                }
                return (T) mapLockRemove(parent.values, parent.lock, path.get(lastIndex));
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T set(List<String> path, Object value) {
        Object nnValue = (value == null) ? NULL_OBJECT : value;
        switch (path.size()) {
            case 0:
                throw new IllegalArgumentException("empty entry path");
            case 1:
                return (T) mapLockPut(values, lock, path.get(0), nnValue);
            default: {
                int lastIndex = path.size() - 1;
                List<String> parentPath = path.subList(0, lastIndex);
                StampedConfig parent = getOrCreateConfig(parentPath);
                return (T) mapLockPut(parent.values, parent.lock, path.get(lastIndex),
                        nnValue);
            }
        }
    }

    /** Convert all sub-configurations to StampedConfigs. */
    private void convertSubConfigs(Config c) {
        if (c instanceof AbstractConfig) {
            AbstractConfig conf = (AbstractConfig) c;
            conf.valueMap().replaceAll((k, v) -> convertValue(v));
        } else {
            for (Config.Entry entry : c.entrySet()) {
                Object value = entry.getRawValue();
                Object converted = convertValue(value);
                if (value != converted) {
                    entry.setValue(converted);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Object convertValue(Object v) {
        if (v instanceof StampedConfig) {
            return v;
        } else if (v instanceof Config) {
            Config c = (Config) v;
            Config converted = createSubConfig();
            convertSubConfigs(c);
            converted.putAll(c);
            return converted;
        } else if (v instanceof List) {
            List<Object> l = (List<Object>) v;
            l.replaceAll(elem -> convertValue(elem));
            return l;
        } else {
            return v;
        }
    }

    @Override
    public void putAll(UnmodifiableConfig other) {
        long stamp = lock.tryWriteLock();
        if (stamp == 0) {
            checkStateForNormalOp();
            stamp = lock.writeLock();
        }
        try {
            unsafePutAll(other);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /** Performs {@code this.putAll(other)}, assuming that the proper lock is held. */
    private void unsafePutAll(UnmodifiableConfig other) {
        if (other == this) {
            throw new IllegalArgumentException("I cannot putAll() into myself.");
        }
        if (other instanceof StampedConfig) {
            StampedConfig stamped = (StampedConfig) other;
            long stamp = stamped.lock.tryReadLock();
            if (stamp == 0) {
                stamped.checkStateForNormalOp(); // catch misuse, especially for other == this
                stamp = stamped.lock.readLock();
            }
            try {
                this.values.putAll(stamped.values);
            } finally {
                stamped.lock.unlockRead(stamp);
            }
        } else {
            // Danger: we may insert subconfigs that are not StampedConfig! convert them
            convertSubConfigs((Config) other);
            try {
                Map<String, Object> values = other.valueMap();
                this.values.putAll(values);
            } catch (UnsupportedOperationException ex) {
                // valueMap() is not supported, use entrySet() instead
                other.entrySet().forEach(entry -> {
                    values.put(entry.getKey(), entry.getRawValue());
                });
            }
        }
    }

    /** Performs {@code this.removeAll(other)}, assuming that the proper lock is held. */
    private void unsafeRemoveAll(UnmodifiableConfig other) {
        if (other == this) {
            throw new IllegalArgumentException("I cannot removeAll() from myself.");
        }
        if (other instanceof StampedConfig) {
            StampedConfig stamped = (StampedConfig) other;
            long stamp = stamped.lock.tryReadLock();
            if (stamp == 0) {
                stamped.checkStateForNormalOp(); // catch misuse, especially for other == this
                stamp = stamped.lock.readLock();
            }
            try {
                this.values.keySet().removeAll(stamped.values.keySet());
            } finally {
                stamped.lock.unlockRead(stamp);
            }
        } else {
            try {
                Set<String> values = other.valueMap().keySet();
                this.values.keySet().removeAll(values);
            } catch (UnsupportedOperationException ex) {
                other.entrySet().forEach(entry -> {
                    values.remove(entry.getKey());
                });
            }
        }
    }

    @Override
    public void removeAll(UnmodifiableConfig other) {
        long stamp = lock.tryWriteLock();
        if (stamp == 0) {
            checkStateForNormalOp();
            stamp = lock.writeLock();
        }
        try {
            unsafeRemoveAll(other);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    // ----- CommentedConfig -----

    @Override
    public void clearComments() {
        checkStateForNormalOp();
        bulkCommentedUpdate(view -> {
            view.clearComments();
        });
    }

    @Override
    public String removeComment(List<String> path) {
        switch (path.size()) {
            case 0:
                throw new IllegalArgumentException("empty entry path");
            case 1:
                return mapLockRemove(comments, lock, path.get(0));
            default: {
                int lastIndex = path.size() - 1;
                List<String> parentPath = path.subList(0, lastIndex);
                StampedConfig parent = getExistingConfig(parentPath, false);
                if (parent == null) {
                    return null;
                }
                return mapLockRemove(parent.comments, parent.lock, path.get(lastIndex));
            }
        }
    }

    @Override
    public String setComment(List<String> path, String value) {
        switch (path.size()) {
            case 0:
                throw new IllegalArgumentException("empty entry path");
            case 1:
                return mapLockPut(comments, lock, path.get(0), value);
            default: {
                int lastIndex = path.size() - 1;
                List<String> parentPath = path.subList(0, lastIndex);
                StampedConfig parent = getOrCreateConfig(parentPath);
                return mapLockPut(parent.comments, parent.lock, path.get(lastIndex), value);
            }
        }
    }

    @Override
    public boolean containsComment(List<String> path) {
        switch (path.size()) {
            case 0:
                throw new IllegalArgumentException("empty entry path");
            case 1:
                return mapLockContains(comments, lock, path.get(0));
            default: {
                int lastIndex = path.size() - 1;
                List<String> parentPath = path.subList(0, lastIndex);
                StampedConfig parent = getExistingConfig(parentPath, false);
                return parent != null
                        && mapLockContains(parent.comments, parent.lock,
                                path.get(lastIndex));
            }
        }
    }

    @Override
    public String getComment(List<String> path) {
        switch (path.size()) {
            case 0:
                throw new IllegalArgumentException("empty entry path");
            case 1:
                return mapLockGet(comments, lock, path.get(0));
            default:
                int lastIndex = path.size() - 1;
                List<String> parentPath = path.subList(0, lastIndex);
                StampedConfig parent = getExistingConfig(parentPath, false);
                if (parent == null) {
                    return null;
                }
                return mapLockGet(parent.comments, parent.lock, path.get(lastIndex));
        }
    }

    @Override
    public Map<String, String> commentMap() {
        // TODO?
        throw new UnsupportedOperationException("StampedConfig does not support commentMap() yet.");
    }

    @Override
    public void putAllComments(UnmodifiableCommentedConfig other) {
        if (other == this) {
            throw new IllegalArgumentException("I cannot putAllComments() into myself.");
        }

        bulkUpdate(view -> {
            if (other instanceof StampedConfig) {
                StampedConfig otherStamped = (StampedConfig) other;
                long otherStamp = otherStamped.lock.tryReadLock();
                if (otherStamp == 0) {
                    otherStamped.checkStateForNormalOp();
                    otherStamp = otherStamped.lock.readLock();
                }
                try {
                    // put all top-level comments
                    this.comments.putAll(otherStamped.comments);

                    // recursively copies the comments of the subconfigs
                    for (CommentedConfig.Entry entry : otherStamped.entrySet()) {
                        Object value = entry.getRawValue();
                        if (value instanceof StampedConfig) {
                            // all subconfigs are StampedConfig
                            Object config = values.get(entry.getKey());
                            if (config instanceof StampedConfig) {
                                ((StampedConfig) config).putAllComments((StampedConfig) value);
                            }
                        }
                    }
                } finally {
                    otherStamped.lock.unlockRead(otherStamp);
                }
            } else {
                try {
                    // put all top-level comments
                    Map<String, String> comments = other.commentMap();
                    this.comments.putAll(comments);

                    // recursively copies the comments of the subconfigs
                    for (UnmodifiableCommentedConfig.Entry entry : other.entrySet()) {
                        Object value = entry.getRawValue();
                        if (value instanceof UnmodifiableCommentedConfig) {
                            Object config = values.get(entry.getKey());
                            if (config instanceof StampedConfig) {
                                ((StampedConfig) config)
                                        .putAllComments((UnmodifiableCommentedConfig) value);
                            }
                        }
                    }
                } catch (UnsupportedOperationException ex) {
                    other.entrySet().forEach(entry -> {
                        comments.put(entry.getKey(), entry.getComment());
                        Object value = entry.getRawValue();
                        // copy comments recursively if the value is a config
                        if (value instanceof UnmodifiableCommentedConfig) {
                            Object config = values.get(entry.getKey());
                            if (config instanceof StampedConfig) {
                                ((StampedConfig) config)
                                        .putAllComments((UnmodifiableCommentedConfig) value);
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void putAllComments(Map<String, CommentNode> comments) {
        long stamp = lock.tryWriteLock();
        if (stamp == 0) {
            checkStateForNormalOp();
            stamp = lock.writeLock();
        }
        try {
            comments.forEach((key, node) -> {
                this.comments.put(key, node.getComment());
                Map<String, CommentNode> children = node.getChildren();
                if (children != null) {
                    Object config = values.get(key);
                    if (config instanceof StampedConfig) {
                        ((StampedConfig)config).putAllComments(children);
                    }
                }
            });
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof StampedConfig) {
            return bulkCommentedRead(view -> {
                return ((StampedConfig) obj).bulkCommentedRead(objView -> {
                    return view.equals(objView);
                });
            });
        } else if (obj instanceof UnmodifiableConfig) {
            return bulkRead(view -> {
                return view.equals(obj);
            });
        } else {
            return false;
        }
    }

    @Override
    public String toString() {
        return bulkRead(view -> {
            StringBuilder builder = new StringBuilder();
            builder.append("StampedConfig{");
            for (UnmodifiableConfig.Entry entry : view.entrySet()) {
                builder.append(entry.getKey());
                builder.append('=');
                builder.append(String.valueOf((Object) entry.getRawValue()));
                builder.append(", ");
            }
            builder.append('}');
            return builder.toString();
        });
    }

    // ----- entrySet -----
    /**
     * Returns a view of the entries contained in the configuration.
     * <p>
     * The returned Set provides iterators that do not reflect entries that are removed or added during iteration.
     * Each iterator obtained from the Set provides a list of entries that existed at the time of its creation,
     * and allow to read their latest value.
     */
    @Override
    public Set<? extends CommentedConfig.Entry> entrySet() {
        return new EntrySet();
    }

    private class EntrySet extends AbstractCollection<LazyEntry> implements Set<LazyEntry> {
        @Override
        public Iterator<LazyEntry> iterator() {
            // Check the state because there can be bad interactions between iterators and bulk operations (when misused),
            // which sometimes leads to a deadlock.
            StampedConfig.this.checkStateForNormalOp();

            long stamp = StampedConfig.this.lock.readLock();

            try {
                StampedConfig.this.state.set(ThreadConfigState.IN_ITER_OP);

                // Take a snapshot of the list of entries in the config, to guarantee thread-safety.
                // Unfortunately we cannot lock here and automatically unlock once the iterator
                // goes out of scope...
                // (that would also require, ideally, to limit the lifetime of the iterator, but Java isn't Rust).
                LazyEntry[] snapshot = new LazyEntry[StampedConfig.this.values.size()];
                int i = 0;
                for (Map.Entry<String, Object> entry : StampedConfig.this.values.entrySet()) {
                    snapshot[i++] = new LockingLazyEntry(entry.getKey(), this);
                }
                return new EntryIterator(snapshot);
            } finally {
                StampedConfig.this.state.set(ThreadConfigState.NORMAL);
                StampedConfig.this.lock.unlockRead(stamp);
            }
        }

        @Override
        public int size() {
            return StampedConfig.this.size();
        }

        @Override
        public void forEach(Consumer<? super LazyEntry> action) {
            // "easy": lock, act, unlock - BUT be sure not to lock again in the LazyEntry (StampedLock is not reentrant).
            // To achieve this, LazyEntry has two subclasses. Here we use "InLockLazyEntry".

            // Write lock is used because the entry provides methods that modify the StampedConfig.
            long stamp = StampedConfig.this.lock.tryWriteLock();
            if (stamp == 0) {
                StampedConfig.this.checkStateForNormalOp();
                stamp = StampedConfig.this.lock.writeLock();
            }

            try {
                StampedConfig.this.state.set(ThreadConfigState.IN_ITER_OP);
                StampedConfig.this.values.forEach((key, value) -> {
                    InLockLazyEntry entry = new InLockLazyEntry(key);
                    try {
                        action.accept(entry);
                    } finally {
                        // Prevent further use of the entry, which would be dangerous.
                        // (technically we could wait until after the entire forEach, but it's easier this way)
                        entry.invalidate();
                    }
                });
            } finally {
                StampedConfig.this.state.set(ThreadConfigState.NORMAL);
                StampedConfig.this.lock.unlockWrite(stamp);
            }
        }

        @Override
        public boolean add(LazyEntry e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            StampedConfig.this.bulkCommentedUpdate(view -> {
                view.clear();
                view.clearComments();
            });
        }

        @Override
        public boolean contains(Object o) {
            if (o instanceof UnmodifiableConfig.Entry) {
                UnmodifiableConfig.Entry entry = (UnmodifiableConfig.Entry) o;
                Object entryValue = entry.getRawValue();
                Object value = StampedConfig.this
                        .getRaw(Collections.singletonList(entry.getKey()));
                return entryValue == null ? (value == null) : (entryValue.equals(value));
            }
            return false;
        }

        @Override
        public boolean isEmpty() {
            return StampedConfig.this.isEmpty();
        }

        @Override
        public boolean remove(Object o) {
            throw new UnsupportedOperationException();
        }
    }

    private class EntryIterator implements Iterator<LazyEntry> {
        private final LazyEntry[] entries;
        private int nextPosition;
        private boolean removed;

        EntryIterator(LazyEntry[] entries) {
            this.entries = entries;
        }

        @Override
        public boolean hasNext() {
            return nextPosition < entries.length;
        }

        @Override
        public LazyEntry next() {
            removed = false;
            return entries[nextPosition++];
        }

        @Override
        public void remove() {
            if (removed) {
                throw new IllegalStateException(
                        "remove() can be called only once per call to next()");
            }
            if (nextPosition == 0) {
                throw new IllegalStateException("next() must be called before remove()");
            }
            if (nextPosition - 1 >= entries.length) {
                throw new IllegalStateException("No more elements in this iterator");
            }
            removed = true;
            LazyEntry entry = entries[nextPosition - 1];
            StampedConfig.this.remove(Collections.singletonList(entry.key));
        }

        public void forEachRemaining(java.util.function.Consumer<? super LazyEntry> action) {
            // Write lock is used because the entry provides methods that modify the StampedConfig.
            long stamp = StampedConfig.this.lock.tryWriteLock();
            if (stamp == 0) {
                StampedConfig.this.checkStateForNormalOp();
                stamp = StampedConfig.this.lock.writeLock();
            }

            try {
                StampedConfig.this.state.set(ThreadConfigState.IN_ITER_OP);

                for (int i = nextPosition; i < entries.length; i++) {
                    LazyEntry entry = entries[i];
                    InLockLazyEntry inLockEntry;
                    if (entry instanceof InLockLazyEntry) {
                        inLockEntry = (InLockLazyEntry) entry;
                    } else {
                        inLockEntry = new InLockLazyEntry(entry.key);
                    }
                    try {
                        action.accept(inLockEntry);
                    } finally {
                        inLockEntry.invalidate();
                    }
                }
            } finally {
                StampedConfig.this.state.set(ThreadConfigState.NORMAL);
                StampedConfig.this.lock.unlockWrite(stamp);
            }
        };
    }

    /** A "lazy" entry: its value is always determined on demand by querying the StampedConfig. */
    private abstract class LazyEntry implements CommentedConfig.Entry {
        protected final String key;

        protected LazyEntry(String key) {
            this.key = key;
        }
    }

    /** A lazy entry that locks the values/comments lock(s) on demand. */
    private final class LockingLazyEntry extends LazyEntry {
        private final EntrySet set;

        protected LockingLazyEntry(String key, EntrySet set) {
            super(key);
            this.set = set;
        }

        @Override
        public String removeComment() {
            return mapLockRemove(StampedConfig.this.comments, StampedConfig.this.lock, key);
        }

        @Override
        public String setComment(String comment) {
            return mapLockPut(StampedConfig.this.comments, StampedConfig.this.lock, key,
                    comment);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T setValue(Object value) {
            return (T) mapLockPut(StampedConfig.this.values, StampedConfig.this.lock, key,
                    value);
        }

        @Override
        public String getKey() {
            checkStateForNormalOp();
            return key;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getRawValue() {
            return (T) mapLockGet(StampedConfig.this.values, StampedConfig.this.lock, key);
        }

        @Override
        public String getComment() {
            return mapLockGet(StampedConfig.this.comments, StampedConfig.this.lock, key);
        }

        @Override
        public String toString() {
            return "StampedConfig.LockingLazyEntry{key=\"" + key + "\"}";
        }
    }

    /** A lazy entry that does NOT lock. To use it, the necessary locks must have been acquired. */
    private final class InLockLazyEntry extends LazyEntry {
        private volatile boolean valid = true;

        private void checkValid() {
            if (!valid) {
                throw new IllegalStateException(
                        "Entries provided by StampedConfig.entrySet().forEach() are only valid in the scope of the forEach call, for thread-safety reasons (and to avoid deadlocks).");
            }
        }

        void invalidate() {
            valid = false;
        }

        protected InLockLazyEntry(String key) {
            super(key);
        }

        @Override
        public String removeComment() {
            checkValid();
            return StampedConfig.this.comments.remove(key);
        }

        @Override
        public String setComment(String comment) {
            checkValid();
            return StampedConfig.this.comments.put(key, comment);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T setValue(Object value) {
            checkValid();
            return (T) StampedConfig.this.values.put(key, value);
        }

        @Override
        public String getKey() {
            checkValid();
            return key;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getRawValue() {
            checkValid();
            return (T) StampedConfig.this.values.get(key);
        }

        @Override
        public String getComment() {
            checkValid();
            return StampedConfig.this.comments.get(key);
        }

        @Override
        public String toString() {
            checkValid();
            return "StampedConfig.InLockLazyEntry{key=\"" + key + "\"}";
        }
    }

    // ----- bulk operations -----
    private void checkStateForBulkOp() {
        switch (state.get()) {
            case IN_BULK_OP:
                throw new IllegalStateException(
                        "StampedConfig.{bulkRead, bulkUpdate, bulkCommentedRead, bulkCommentedUpdate} cannot be nested.");
            case IN_ITER_OP:
                throw new IllegalStateException(
                        "Entries provided by StampedConfig.entrySet() cannot be used during another operation on the config nor on its entrySet, for thread-safety reasons (and to avoid deadlocks).");
            case CONSUMED:
                throw new IllegalStateException(
                        "This StampedConfig has been given to otherConfig.replaceContentBy() and cannot be used anymore.");
            case NORMAL: {
                // ok
            }
        }
    }

    private void checkStateForNormalOp() {
        switch (state.get()) {
            case IN_BULK_OP:
                throw new IllegalStateException(
                        "StampedConfig cannot be used inside of bulk operations, you must use the argument provided to your function by bulk, for example: bulkUpdate(bulkedConf -> {/* use bulkedConf here*/}).");
            case IN_ITER_OP:
                throw new IllegalStateException(
                        "Entries provided by StampedConfig.entrySet() cannot be used during another operation on the config nor on its entrySet, for thread-safety reasons (and to avoid deadlocks).");
            case CONSUMED:
                throw new IllegalStateException(
                        "This StampedConfig has been given to otherConfig.replaceContentBy() and cannot be used anymore.");
            case NORMAL: {
                // ok
            }
        }
    }

    @Override
    public <R> R bulkRead(Function<? super UnmodifiableConfig, R> action) {
        long stamp = lock.tryReadLock();
        if (stamp == 0) {
            checkStateForBulkOp();
            stamp = lock.readLock();
        }

        // Even if we acquired the read lock, we want to prevent some bad uses like
        // nested bulks, entrySet iteration inside of bulks (which can cause deadlocks on forEachRemaining), etc.
        try {
            checkStateForBulkOp();
        } catch (IllegalStateException ex) {
            lock.unlockRead(stamp);
            throw ex;
        }

        state.set(ThreadConfigState.IN_BULK_OP);
        ReadOnlyLockedView view = new ReadOnlyLockedView();
        try {
            return action.apply(view);
        } finally {
            view.invalidate();
            state.set(ThreadConfigState.NORMAL);
            lock.unlockRead(stamp);
        }
    }

    @Override
    public <R> R bulkUpdate(Function<? super Config, R> action) {
        long stamp = lock.tryWriteLock();
        if (stamp == 0) {
            checkStateForBulkOp();
            stamp = lock.writeLock();
        }

        try {
            checkStateForBulkOp();
        } catch (IllegalStateException ex) {
            lock.unlockWrite(stamp);
            throw ex;
        }

        state.set(ThreadConfigState.IN_BULK_OP);
        WritableLockedView view = new WritableLockedView();
        try {
            return action.apply(view);
        } finally {
            view.invalidate();
            state.set(ThreadConfigState.NORMAL);
            lock.unlockWrite(stamp);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R bulkCommentedRead(Function<? super UnmodifiableCommentedConfig, R> action) {
        return bulkRead((Function<? super UnmodifiableConfig, R>) action);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R bulkCommentedUpdate(Function<? super CommentedConfig, R> action) {
        return bulkUpdate((Function<? super Config, R>) action);
    }

    /**
     * A read-only locked view of the configuration, used in the bulk methods.
     * <p>
     * It is assumed that the appropriate lock is held during the use of the view.
     * <p>
     * Since StampedLock is NOT reentrant, the view cannot use the config locks,
     * nor can it call config methods that use the locks.
     */
    private class ReadOnlyLockedView implements UnmodifiableCommentedConfig {
        protected class Entry implements UnmodifiableCommentedConfig.Entry {
            protected final Map.Entry<String, Object> mapEntry;

            Entry(Map.Entry<String, Object> entry) {
                this.mapEntry = entry;
                checkValid();
            }

            @Override
            public String getComment() {
                checkValid();
                return StampedConfig.this.comments.get(mapEntry.getKey());
            }

            @Override
            public String getKey() {
                checkValid();
                return mapEntry.getKey();
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> T getRawValue() {
                checkValid();
                return (T) mapEntry.getValue();
            }
        }

        // True if this view is still valid. When the bulk operation ends, the view
        // is marked as invalid in order to prevent its use outside of the locking region
        // (which would be unsafe).
        private boolean valid = true;

        /** Prevents further use of the view (reasonable effort but not perfect since the field is not volatile). */
        void invalidate() {
            valid = false;
        }

        protected void checkValid() {
            if (!valid) {
                throw new IllegalStateException(
                        "View provided by bulk operations are only valid in the scope of the bulkRead or bulkWrite method."
                                + "To use the config elsewhere, use the actual config variable (not the one provided to your bulk action).");
            }
        }

        @Override
        public Map<String, String> commentMap() {
            throw new UnsupportedOperationException(
                    "The view provided by bulk operations on StampedConfig does not support commentMap()");
        }

        @Override
        public Set<? extends UnmodifiableCommentedConfig.Entry> entrySet() {
            checkValid();
            return new TransformingSet<>(StampedConfig.this.values.entrySet(), Entry::new,
                    o -> null, o -> {
                        checkValid();
                        return o;
                    });
        }

        @Override
        public boolean containsComment(List<String> path) {
            checkValid();
            switch (path.size()) {
                case 0:
                    throw new IllegalArgumentException("empty entry path");
                case 1: {
                    String key = path.get(0);
                    return comments.containsKey(key);
                }
                default: {
                    Object maybeParent = values.get(path.get(0));
                    if (maybeParent instanceof StampedConfig) {
                        StampedConfig parent = (StampedConfig) maybeParent;
                        return parent.containsComment(path.subList(1, path.size()));
                        // it is OK to acquire a read lock on sub-configurations
                    } else {
                        return false;
                    }
                }
            }
        }

        @Override
        public String getComment(List<String> path) {
            checkValid();
            switch (path.size()) {
                case 0:
                    throw new IllegalArgumentException("empty entry path");
                case 1: {
                    String key = path.get(0);
                    return comments.get(key);
                }
                default: {
                    Object maybeParent = values.get(path.get(0));
                    if (maybeParent instanceof StampedConfig) {
                        StampedConfig parent = (StampedConfig) maybeParent;
                        return parent.getComment(path.subList(1, path.size()));
                    } else {
                        return null;
                    }
                }
            }
        }

        @Override
        public ConfigFormat<?> configFormat() {
            checkValid();
            return StampedConfig.this.configFormat;
        }

        @Override
        public boolean contains(List<String> path) {
            checkValid();
            switch (path.size()) {
                case 0:
                    throw new IllegalArgumentException("empty entry path");
                case 1: {
                    String key = path.get(0);
                    return values.containsKey(key);
                }
                default: {
                    Object maybeParent = values.get(path.get(0));
                    if (maybeParent instanceof StampedConfig) {
                        StampedConfig parent = (StampedConfig) maybeParent;
                        return parent.contains(path.subList(1, path.size()));
                    } else {
                        return false;
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T getRaw(List<String> path) {
            checkValid();
            switch (path.size()) {
                case 0:
                    throw new IllegalArgumentException("empty entry path");
                case 1: {
                    String key = path.get(0);
                    return (T) values.get(key);
                }
                default: {
                    Object maybeParent = values.get(path.get(0));
                    if (maybeParent instanceof StampedConfig) {
                        StampedConfig parent = (StampedConfig) maybeParent;
                        return parent.getRaw(path.subList(1, path.size()));
                    } else {
                        return null;
                    }
                }
            }
        }

        @Override
        public int size() {
            checkValid();
            return StampedConfig.this.values.size();
        }

        @Override
        public Map<String, Object> valueMap() {
            throw new UnsupportedOperationException(
                    "The view provided by bulk operations on StampedConfig does not support valueMap()");
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("StampedConfig#LockedView{");
            for (UnmodifiableCommentedConfig.Entry entry : entrySet()) {
                builder.append(entry.getKey());
                builder.append('=');
                builder.append(String.valueOf((Object) entry.getRawValue()));
                builder.append(", ");
            }
            builder.append("}");
            return builder.toString();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            } else if (!(obj instanceof UnmodifiableConfig)) {
                return false;
            } else {
                UnmodifiableConfig conf = (UnmodifiableConfig) obj;
                if (conf.size() != size()) {
                    return false;
                }
                for (UnmodifiableConfig.Entry entry : entrySet()) {
                    Object value = entry.getValue();
                    Object otherEntry = conf.get(Collections.singletonList(entry.getKey()));
                    if (value == null) {
                        if (otherEntry != null) {
                            return false;
                        }
                    } else {
                        return value.equals(otherEntry);
                    }
                }
                return true;
            }
        }
    }

    /**
     * A read+write view of the configuration, used in bulk method.
     */
    private final class WritableLockedView extends ReadOnlyLockedView implements CommentedConfig {
        protected class Entry extends ReadOnlyLockedView.Entry implements CommentedConfig.Entry {

            Entry(java.util.Map.Entry<String, Object> entry) {
                super(entry);
                checkValid();
            }

            @Override
            public String removeComment() {
                checkValid();
                return StampedConfig.this.comments.remove(mapEntry.getKey());
            }

            @Override
            public String setComment(String comment) {
                checkValid();
                return StampedConfig.this.comments.put(mapEntry.getKey(), comment);
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> T setValue(Object value) {
                checkValid();
                return (T) mapEntry.setValue(value);
            }
        }

        @Override
        public void clear() {
            checkValid();
            StampedConfig.this.values.clear();
        }

        @Override
        public void removeAll(UnmodifiableConfig config) {
            checkValid();
            StampedConfig.this.unsafeRemoveAll(config);
        }

        @Override
        public void putAll(UnmodifiableConfig other) {
            checkValid();
            StampedConfig.this.unsafePutAll(other);
        }

        @Override
        public void clearComments() {
            checkValid();
            StampedConfig.this.comments.clear();
            for (Object o : values.values()) {
                if (o instanceof StampedConfig) {
                    ((StampedConfig) o).clearComments();
                }
            }
        }

        @Override
        public StampedConfig createSubConfig() {
            checkValid();
            return new StampedConfig(configFormat, mapSupplier);
        }

        @Override
        public Set<? extends CommentedConfig.Entry> entrySet() {
            checkValid();
            return new TransformingSet<>(StampedConfig.this.values.entrySet(), Entry::new,
                    o -> null, o -> {
                        checkValid();
                        return o;
                    });
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T remove(List<String> path) {
            checkValid();
            switch (path.size()) {
                case 0:
                    throw new IllegalArgumentException("empty entry path");
                case 1: {
                    String key = path.get(0);
                    return (T) values.remove(key);
                }
                default: {
                    int lastIndex = path.size() - 1;
                    Object maybeParent = getRaw(path.subList(0, lastIndex));
                    if (maybeParent instanceof StampedConfig) {
                        StampedConfig parent = (StampedConfig) maybeParent;
                        String key = path.get(lastIndex);
                        return (T) parent.values.remove(key);
                    } else {
                        return null;
                    }
                }
            }
        }

        @Override
        public String removeComment(List<String> path) {
            checkValid();
            switch (path.size()) {
                case 0:
                    throw new IllegalArgumentException("empty entry path");
                case 1: {
                    String key = path.get(0);
                    return comments.remove(key);
                }
                default: {
                    int lastIndex = path.size() - 1;
                    Object maybeParent = getRaw(path.subList(0, lastIndex));
                    if (maybeParent instanceof StampedConfig) {
                        StampedConfig parent = (StampedConfig) maybeParent;
                        String key = path.get(lastIndex);
                        return parent.comments.remove(key);
                    } else {
                        return null;
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        @Override
        public <T> T set(List<String> path, Object value) {
            checkValid();
            switch (path.size()) {
                case 0:
                    throw new IllegalArgumentException("empty entry path");
                case 1: {
                    String key = path.get(0);
                    Object nnValue = (value == null) ? NULL_OBJECT : value;
                    return (T) values.put(key, nnValue);
                }
                default: {
                    String key = path.get(0);
                    List<String> subPath = path.subList(1, path.size());
                    Object currentParent = values.get(key);
                    if (currentParent == null) {
                        // create intermediary config
                        StampedConfig subConfig = createSubConfig();
                        values.put(key, subConfig);
                        // set value in intermediary level
                        return subConfig.set(subPath, value);
                    } else if (currentParent instanceof StampedConfig) {
                        // intermediary config exists, use it
                        return ((StampedConfig) currentParent).set(subPath, value);
                    } else {
                        throw new IncompatibleIntermediaryLevelException(
                                "Cannot add an element to an intermediary value of type: "
                                        + currentParent.getClass());
                    }
                }
            }
        }

        @Override
        public String setComment(List<String> path, String value) {
            checkValid();
            switch (path.size()) {
                case 0:
                    throw new IllegalArgumentException("empty entry path");
                case 1: {
                    String key = path.get(0);
                    return comments.put(key, value);
                }
                default: {
                    String key = path.get(0);
                    List<String> subPath = path.subList(1, path.size());
                    Object currentParent = values.get(key);
                    if (currentParent == null) {
                        // create intermediary config
                        StampedConfig subConfig = createSubConfig();
                        values.put(key, subConfig);
                        // set value in intermediary level
                        return subConfig.setComment(subPath, value);
                    } else if (currentParent instanceof StampedConfig) {
                        // intermediary config exists, use it
                        return ((StampedConfig) currentParent).setComment(subPath, value);
                    } else {
                        throw new IncompatibleIntermediaryLevelException(
                                "Cannot add a comment to an intermediary value of type: "
                                        + currentParent.getClass());
                    }
                }
            }
        }

        @Override
        public boolean add(List<String> path, Object value) {
            checkValid();
            switch (path.size()) {
                case 0:
                    throw new IllegalArgumentException("empty entry path");
                case 1: {
                    String key = path.get(0);
                    Object nnValue = (value == null) ? NULL_OBJECT : value;
                    return values.putIfAbsent(key, nnValue) == null;
                }
                default: {
                    String key = path.get(0);
                    List<String> subPath = path.subList(1, path.size());
                    Object currentParent = values.get(key);
                    if (currentParent == null) {
                        // create intermediary config
                        StampedConfig subConfig = createSubConfig();
                        values.put(key, subConfig);
                        // set value in intermediary level
                        return subConfig.add(subPath, value);
                    } else if (currentParent instanceof StampedConfig) {
                        // intermediary config exists, use it
                        return ((StampedConfig) currentParent).add(subPath, value);
                    } else {
                        throw new IncompatibleIntermediaryLevelException(
                                "Cannot add an element to an intermediary value of type: "
                                        + currentParent.getClass());
                    }
                }
            }
        }
    }

    private static enum ThreadConfigState {
        /** normal state */
        NORMAL,
        /** currently in bulkRead/bulkWrite, the StampedConfig must not be used, only the view */
        IN_BULK_OP,
        /** currently in iterator, the StampedConfig must not be used, only the iterator */
        IN_ITER_OP,
        /** passed to otherConfig.replaceContentBy(this), cannot be used anymore */
        CONSUMED;
    }
}
