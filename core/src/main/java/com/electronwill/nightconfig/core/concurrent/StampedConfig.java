package com.electronwill.nightconfig.core.concurrent;

import java.util.AbstractCollection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
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

    private final StampedLock valuesLock = new StampedLock();
    private final StampedLock commentsLock = new StampedLock();

    // BEWARE: StampedLock does not support reentrant locking

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public StampedConfig(ConfigFormat<?> configFormat, Supplier<Map<String, Object>> mapSupplier) {
        this.configFormat = configFormat;
        this.mapSupplier = mapSupplier;
        this.values = mapSupplier.get();
        this.comments = (Map) mapSupplier.get();
    }

    // ----- specific -----
    /**
     * Atomically replaces the content of this config by the content of the specified config.
     * The specified config cannot be used anymore after a call to this method.
     * 
     * @param newContent the new content (cannot be used anymore after this)
     */
    public void replaceContentBy(StampedConfig newContent) {
        long commentsStamp = commentsLock.writeLock();
        long valuesStamp = valuesLock.writeLock();
        try {
            long otherCS = newContent.commentsLock.writeLock();
            long otherVS = newContent.valuesLock.writeLock();
            try {
                this.values = newContent.values;
                this.comments = newContent.comments;
                newContent.values = null;
                newContent.comments = null;
            } finally {
                newContent.valuesLock.unlockWrite(otherVS);
                newContent.commentsLock.unlockWrite(otherCS);
            }
        } finally {
            valuesLock.unlockWrite(valuesStamp);
            commentsLock.unlockWrite(commentsStamp);
        }
    }

    // ----- internal -----
    private <V> V mapLockGet(Map<String, V> map, StampedLock lock, String key) {
        long stamp = lock.tryOptimisticRead();
        V value = map.get(key);
        if (!lock.validate(stamp)) {
            // optimistic read failed, use a full lock
            stamp = lock.readLock();
            try {
                value = map.get(key);
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return value;
    }

    private <V> boolean mapLockContains(Map<String, V> map, StampedLock lock, String key) {
        // try optimistic read first
        long stamp = lock.tryOptimisticRead();
        boolean contains = map.containsKey(key);
        if (!lock.validate(stamp)) {
            // optimistic read failed, use a full lock
            stamp = lock.readLock();
            try {
                contains = map.containsKey(key);
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return contains;
    }

    private <V> V mapLockRemove(Map<String, V> map, StampedLock lock, String key) {
        long stamp = lock.writeLock();
        try {
            return map.remove(key);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    private <V> V mapLockPut(Map<String, V> map, StampedLock lock, String key, V value) {
        long stamp = lock.writeLock();
        try {
            return map.put(key, value);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    private <V> V mapLockPutIfAbsent(Map<String, V> map, StampedLock lock, String key, V value) {
        long stamp = lock.writeLock();
        try {
            return map.putIfAbsent(key, value);
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    /** Finds an existing subconfig with the given path (for example "a.subconfig"). */
    private StampedConfig getExistingConfig(List<String> configPath) {
        // optimization: no recursion here
        StampedConfig current = this;
        for (String key : configPath) {
            Object level = mapLockGet(current.values, current.valuesLock, key);
            if (level == null) {
                // missing intermediary level: stop
                return null;
            } else if (level instanceof StampedConfig) {
                current = (StampedConfig) level;
            } else {
                // impossible to go further: what should have been a subconfig is another type of value
                throw new IllegalArgumentException("Cannot get entry with parent path " + configPath
                        + " because of an incompatible intermediary value of type: "
                        + level.getClass());
            }
        }
        return current;
    }

    /** Finds a subconfig with the given path (for example "a.subconfig"), creates it if it does not exist yet. */
    private StampedConfig getOrCreateConfig(List<String> configPath) {
        // optimization: no recursion here
        StampedConfig current = this;
        for (String key : configPath) {
            StampedLock lock = current.valuesLock;
            Map<String, Object> values = current.values;
            // try optimistic read once
            long stamp = lock.tryOptimisticRead();
            try {
                Object level = values.get(key);
                if (!lock.validate(stamp)) {
                    // Read has been invalidated, acquire the lock and read again.
                    stamp = lock.readLock();
                    level = values.get(key);
                }

                if (level == null) {
                    // Create missing intermediary level and continue one level down.
                    // We will insert the new level into the config, a write lock is necessary.
                    stamp = lock.tryConvertToWriteLock(stamp);
                    if (stamp == 0) {
                        // write lock not acquired, need to wait
                        stamp = lock.writeLock();
                    }
                    current = createSubConfig();
                    values.put(key, current);
                } else if (level instanceof StampedConfig) {
                    current = (StampedConfig) level;
                } else {
                    // Impossible to go further: what should have been a subconfig is another type of value.
                    throw new IllegalArgumentException("Cannot add/set entry with parent path "
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
        long stamp = valuesLock.tryOptimisticRead();
        int size = values.size();

        if (!valuesLock.validate(stamp)) {
            stamp = valuesLock.readLock();
            try {
                size = values.size();
            } finally {
                valuesLock.unlockRead(stamp);
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
        long stamp = valuesLock.writeLock();
        try {
            values.clear();
        } finally {
            valuesLock.unlockWrite(stamp);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getRaw(List<String> path) {
        switch (path.size()) {
            case 0:
                throw new IllegalArgumentException("empty entry path");
            case 1:
                return (T) mapLockGet(values, valuesLock, path.get(0));
            default:
                int lastIndex = path.size() - 1;
                List<String> parentPath = path.subList(0, lastIndex);
                StampedConfig parent = getOrCreateConfig(parentPath);
                return (T) mapLockGet(parent.values, parent.valuesLock, path.get(lastIndex));
        }
    }

    @Override
    public boolean contains(List<String> path) {
        switch (path.size()) {
            case 0:
                throw new IllegalArgumentException("empty entry path");
            case 1:
                return mapLockContains(values, valuesLock, path.get(0));
            default: {
                int lastIndex = path.size() - 1;
                List<String> parentPath = path.subList(0, lastIndex);
                StampedConfig parent = getExistingConfig(parentPath);
                return parent != null
                        && mapLockContains(parent.values, parent.valuesLock, path.get(lastIndex));
            }
        }
    }

    @Override
    public boolean add(List<String> path, Object value) {
        switch (path.size()) {
            case 0:
                throw new IllegalArgumentException("empty entry path");
            case 1:
                return mapLockPutIfAbsent(values, valuesLock, path.get(0), value) == null;
            default: {
                int lastIndex = path.size() - 1;
                List<String> parentPath = path.subList(0, lastIndex);
                StampedConfig parent = getOrCreateConfig(parentPath);
                return mapLockPutIfAbsent(parent.values, parent.valuesLock, path.get(lastIndex),
                        value) == null;
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
                return (T) mapLockRemove(values, valuesLock, path.get(0));
            default: {
                int lastIndex = path.size() - 1;
                List<String> parentPath = path.subList(0, lastIndex);
                StampedConfig parent = getExistingConfig(parentPath);
                if (parent == null) {
                    return null;
                }
                return (T) mapLockRemove(parent.values, parent.valuesLock, path.get(lastIndex));
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T set(List<String> path, Object value) {
        switch (path.size()) {
            case 0:
                throw new IllegalArgumentException("empty entry path");
            case 1:
                return (T) mapLockPut(values, valuesLock, path.get(0), value);
            default: {
                int lastIndex = path.size() - 1;
                List<String> parentPath = path.subList(0, lastIndex);
                StampedConfig parent = getOrCreateConfig(parentPath);
                return (T) mapLockPut(parent.values, parent.valuesLock, path.get(lastIndex), value);
            }
        }
    }

    @Override
    public void putAll(UnmodifiableConfig other) {
        long stamp = valuesLock.writeLock();
        try {
            if (other instanceof StampedConfig) {
                StampedConfig stamped = (StampedConfig) other;
                long stamp2 = stamped.valuesLock.readLock();
                try {
                    this.values.putAll(stamped.values);
                } finally {
                    stamped.valuesLock.unlockRead(stamp2);
                }
            } else {
                // TODO: danger, we may insert subconfigs that are not StampedConfig, should we convert them? Error?
                try {
                    Map<String, Object> values = other.valueMap();
                    this.values.putAll(values);
                } catch (UnsupportedOperationException ex) {
                    other.entrySet().forEach(entry -> {
                        values.put(entry.getKey(), entry.getValue());
                    });
                }
            }
        } finally {
            valuesLock.unlockWrite(stamp);
        }
    }

    @Override
    public void removeAll(UnmodifiableConfig other) {
        long stamp = valuesLock.writeLock();
        try {
            if (other instanceof StampedConfig) {
                StampedConfig stamped = (StampedConfig) other;
                long stamp2 = stamped.valuesLock.readLock();
                try {
                    this.values.keySet().removeAll(stamped.values.keySet());
                } finally {
                    stamped.valuesLock.unlockRead(stamp2);
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
        } finally {
            valuesLock.unlockWrite(stamp);
        }
    }

    // ----- CommentedConfig -----

    @Override
    public void clearComments() {
        long stamp = commentsLock.writeLock();
        try {
            comments.clear();
        } finally {
            commentsLock.unlockWrite(stamp);
        }
    }

    @Override
    public String removeComment(List<String> path) {
        switch (path.size()) {
            case 0:
                throw new IllegalArgumentException("empty entry path");
            case 1:
                return mapLockRemove(comments, commentsLock, path.get(0));
            default: {
                int lastIndex = path.size() - 1;
                List<String> parentPath = path.subList(0, lastIndex);
                StampedConfig parent = getExistingConfig(parentPath);
                if (parent == null) {
                    return null;
                }
                return mapLockRemove(parent.comments, parent.commentsLock, path.get(lastIndex));
            }
        }
    }

    @Override
    public String setComment(List<String> path, String value) {
        switch (path.size()) {
            case 0:
                throw new IllegalArgumentException("empty entry path");
            case 1:
                return mapLockPut(comments, commentsLock, path.get(0), value);
            default: {
                int lastIndex = path.size() - 1;
                List<String> parentPath = path.subList(0, lastIndex);
                StampedConfig parent = getOrCreateConfig(parentPath);
                return mapLockPut(parent.comments, parent.commentsLock, path.get(lastIndex), value);
            }
        }
    }

    @Override
    public boolean containsComment(List<String> path) {
        switch (path.size()) {
            case 0:
                throw new IllegalArgumentException("empty entry path");
            case 1:
                return mapLockContains(comments, commentsLock, path.get(0));
            default: {
                int lastIndex = path.size() - 1;
                List<String> parentPath = path.subList(0, lastIndex);
                StampedConfig parent = getExistingConfig(parentPath);
                return parent != null
                        && mapLockContains(parent.comments, parent.commentsLock,
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
                return mapLockGet(comments, commentsLock, path.get(0));
            default:
                int lastIndex = path.size() - 1;
                List<String> parentPath = path.subList(0, lastIndex);
                StampedConfig parent = getOrCreateConfig(parentPath);
                return mapLockGet(parent.comments, parent.commentsLock, path.get(lastIndex));
        }
    }

    @Override
    public Map<String, String> commentMap() {
        // TODO?
        throw new UnsupportedOperationException("StampedConfig does not support commentMap() yet.");
    }

    @Override
    public void putAllComments(UnmodifiableCommentedConfig other) {
        long stamp = commentsLock.writeLock();
        try {
            if (other instanceof StampedConfig) {
                StampedConfig stamped = (StampedConfig) other;
                long stamp2 = stamped.commentsLock.readLock();
                try {
                    this.comments.putAll(stamped.comments);
                } finally {
                    stamped.commentsLock.unlockRead(stamp2);
                }
            } else {
                try {
                    Map<String, String> comments = other.commentMap();
                    this.comments.putAll(comments);
                } catch (UnsupportedOperationException ex) {
                    other.entrySet().forEach(entry -> {
                        comments.put(entry.getKey(), entry.getValue());
                    });
                }
            }
        } finally {
            commentsLock.unlockWrite(stamp);
        }
    }

    @Override
    public void putAllComments(Map<String, CommentNode> comments) {
        long stamp = commentsLock.writeLock();
        try {
            comments.forEach((key, node) -> {
                this.comments.put(key, node.getComment());
                Map<String, CommentNode> children = node.getChildren();
                if (children != null) {
                    StampedConfig config = getRaw(Collections.singletonList(key));
                    config.putAllComments(children);
                }
            });
        } finally {
            commentsLock.unlockWrite(stamp);
        }
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
        // reasonable-effort deadlock prevention (no guarantee)
        private boolean lockedFlag = false;

        @Override
        public Iterator<LazyEntry> iterator() {
            // same lock order as bulk operations: lock comments, lock values, unlock values, unlock comments
            long commentsStamp = StampedConfig.this.commentsLock.readLock();
            long valuesStamp = StampedConfig.this.valuesLock.readLock();
            try {
                lockedFlag = true;

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
                lockedFlag = false;
                StampedConfig.this.valuesLock.unlockRead(valuesStamp);
                StampedConfig.this.commentsLock.unlockRead(commentsStamp);
            }
        }

        @Override
        public int size() {
            lockedFlag = true;
            try {
                return StampedConfig.this.size();
            } finally {
                lockedFlag = true;
            }
        }

        @Override
        public void forEach(Consumer<? super LazyEntry> action) {
            // "easy": lock, act, unlock - BUT be sure not to lock again in the LazyEntry (StampedLock is not reentrant).
            // To achieve this, LazyEntry has two subclasses. Here we use "InLockLazyEntry".

            // Write lock is used because the entry provides methods that modify the StampedConfig.
            long commentsStamp = StampedConfig.this.commentsLock.writeLock();
            long valuesStamp = StampedConfig.this.valuesLock.writeLock();
            lockedFlag = true;
            try {
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
                lockedFlag = false;
                StampedConfig.this.valuesLock.unlockWrite(valuesStamp);
                StampedConfig.this.commentsLock.unlockWrite(commentsStamp);
            }
        }

        @Override
        public boolean add(LazyEntry e) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean contains(Object o) {
            lockedFlag = true;
            try {
                if (o instanceof UnmodifiableConfig.Entry) {
                    UnmodifiableConfig.Entry entry = (UnmodifiableConfig.Entry) o;
                    Object value = StampedConfig.this
                            .getRaw(Collections.singletonList(entry.getKey()));
                    return value != null && value == entry.getRawValue();
                }
                return false;
            } finally {
                lockedFlag = false;
            }
        }

        @Override
        public boolean isEmpty() {
            lockedFlag = true;
            try {
                return StampedConfig.this.isEmpty();
            } finally {
                lockedFlag = false;
            }
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
            removed = true;
            LazyEntry entry = entries[nextPosition - 1];
            StampedConfig.this.remove(Collections.singletonList(entry.key));
        }

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

        /**
         * Ensures that the lock flag is currently off, to avoid deadlock (best effort to help the user, no
         * guarantee).
         */
        private void checkUnlocked() {
            if (set.lockedFlag) {
                throw new IllegalStateException(
                        "Entries provided by StampedConfig.entrySet().iterator().next() cannot be used during another operation on the config nor on its entrySet, for thread-safety reasons (and to avoid deadlocks).");
            }
        }

        @Override
        public String removeComment() {
            checkUnlocked();
            return mapLockRemove(StampedConfig.this.comments, StampedConfig.this.commentsLock, key);
        }

        @Override
        public String setComment(String comment) {
            checkUnlocked();
            return mapLockPut(StampedConfig.this.comments, StampedConfig.this.commentsLock, key,
                    comment);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T setValue(Object value) {
            checkUnlocked();
            return (T) mapLockPut(StampedConfig.this.values, StampedConfig.this.valuesLock, key,
                    value);
        }

        @Override
        public String getKey() {
            checkUnlocked();
            return key;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T getRawValue() {
            checkUnlocked();
            return (T) mapLockGet(StampedConfig.this.values, StampedConfig.this.valuesLock, key);
        }

        @Override
        public String getComment() {
            checkUnlocked();
            return mapLockGet(StampedConfig.this.comments, StampedConfig.this.commentsLock, key);
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
    }

    // ----- bulk operations -----

    @Override
    public <R> R bulkRead(Function<? super UnmodifiableConfig, R> action) {
        long stamp = valuesLock.readLock();
        ReadOnlyLockedView view = new ReadOnlyLockedView();
        try {
            return action.apply(view);
        } finally {
            view.invalidate();
            valuesLock.unlockRead(stamp);
        }
    }

    @Override
    public <R> R bulkUpdate(Function<? super Config, R> action) {
        long stamp = valuesLock.writeLock();
        WritableLockedView view = new WritableLockedView();
        try {
            return action.apply(view);
        } finally {
            view.invalidate();
            valuesLock.unlockWrite(stamp);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R bulkCommentedRead(Function<? super UnmodifiableCommentedConfig, R> action) {
        long stamp = commentsLock.readLock();
        try {
            return bulkRead((Function<? super UnmodifiableConfig, R>) action);
        } finally {
            commentsLock.unlockRead(stamp);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> R bulkCommentedUpdate(Function<? super CommentedConfig, R> action) {
        long stamp = commentsLock.writeLock();
        try {
            return bulkUpdate((Function<? super Config, R>) action);
        } finally {
            commentsLock.unlockWrite(stamp);
        }
    }

    /**
     * A read-only locked view of the configuration, used in the bulk methods.
     * <p>
     * It is assumed that the appropriate lock (valuesLock, commentsLock) is held during the use of the view.
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
                        return parent.containsComment(path.subList(1, path.size() - 1));
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
                        return parent.getComment(path.subList(1, path.size() - 1));
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
                        return parent.contains(path.subList(1, path.size() - 1));
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
                        return parent.getRaw(path.subList(1, path.size() - 1));
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
        public void clearComments() {
            checkValid();
            StampedConfig.this.comments.clear();
        }

        @Override
        public StampedConfig createSubConfig() {
            checkValid();
            return StampedConfig.this.createSubConfig();
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
                    return (T) values.put(key, value);
                }
                default: {
                    String key = path.get(0);
                    int lastIndex = path.size() - 1;
                    List<String> subPath = path.subList(0, lastIndex);
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
                        throw new IllegalArgumentException(
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
                    int lastIndex = path.size() - 1;
                    List<String> subPath = path.subList(0, lastIndex);
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
                        throw new IllegalArgumentException(
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
                    return values.putIfAbsent(key, value) == null;
                }
                default: {
                    String key = path.get(0);
                    int lastIndex = path.size() - 1;
                    List<String> subPath = path.subList(0, lastIndex);
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
                        throw new IllegalArgumentException(
                                "Cannot add an element to an intermediary value of type: "
                                        + currentParent.getClass());
                    }
                }
            }
        }

        @Override
        public void clear() {
            checkValid();
            StampedConfig.this.values.clear();
        }
    }
}
