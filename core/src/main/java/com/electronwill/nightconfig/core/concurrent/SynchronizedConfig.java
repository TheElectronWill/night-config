package com.electronwill.nightconfig.core.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;

/**
 * A configuration that is synchronized, and therefore thread-safe (reads and
 * writes can happen in any order from any thread).
 * <p>
 * Only one read or write can happen at the same time. This includes every read
 * and write on sub-configurations, iterator operations, etc.
 */
public final class SynchronizedConfig implements ConcurrentCommentedConfig {
    private static final class SynchronizedMap<K, V> implements Map<K, V> {
        private final Map<K, V> map;
        private final Object rootMonitor;

        SynchronizedMap(Map<K, V> map, Object monitor) {
            this.map = map;
            this.rootMonitor = monitor;
        }

        @Override
        public boolean equals(Object obj) {
            synchronized (rootMonitor) {
                return map.equals(obj);
            }
        }

        @Override
        public int hashCode() {
            synchronized (rootMonitor) {
                return map.hashCode();
            }
        }

        @Override
        public String toString() {
            synchronized (rootMonitor) {
                return map.toString();
            }
        }

        @Override
        public void clear() {
            synchronized (rootMonitor) {
                map.clear();
            }
        }

        @Override
        public boolean containsKey(Object key) {
            synchronized (rootMonitor) {
                return map.containsKey(key);
            }
        }

        @Override
        public boolean containsValue(Object value) {
            synchronized (rootMonitor) {
                return map.containsValue(value);
            }
        }

        @Override
        public Set<Entry<K, V>> entrySet() {
            synchronized (rootMonitor) {
                return new SynchronizedSet<Entry<K, V>>(map.entrySet(), rootMonitor);
            }
        }

        @Override
        public V get(Object key) {
            synchronized (rootMonitor) {
                return map.get(key);
            }
        }

        @Override
        public boolean isEmpty() {
            synchronized (rootMonitor) {
                return map.isEmpty();
            }
        }

        @Override
        public Set<K> keySet() {
            synchronized (rootMonitor) {
                return map.keySet();
            }
        }

        @Override
        public V put(K key, V value) {
            synchronized (rootMonitor) {
                return map.put(key, value);
            }
        }

        @Override
        public void putAll(Map<? extends K, ? extends V> m) {
            synchronized (rootMonitor) {
                map.putAll(m);
            }
        }

        @Override
        public V remove(Object key) {
            synchronized (rootMonitor) {
                return map.remove(key);
            }
        }

        @Override
        public int size() {
            synchronized (rootMonitor) {
                return map.size();
            }
        }

        @Override
        public Collection<V> values() {
            synchronized (rootMonitor) {
                return map.values();
            }
        }
    }

    private static class SynchronizedCollection<E> implements Collection<E> {
        private final Collection<E> coll;
        private final Object rootMonitor;

        SynchronizedCollection(Collection<E> coll, Object rootMonitor) {
            this.coll = coll;
            this.rootMonitor = rootMonitor;
        }

        @Override
        public boolean add(E e) {
            synchronized (rootMonitor) {
                return coll.add(e);
            }
        }

        @Override
        public boolean addAll(Collection<? extends E> c) {
            synchronized (rootMonitor) {
                return coll.addAll(c);
            }
        }

        @Override
        public void clear() {
            synchronized (rootMonitor) {
                coll.clear();
            }
        }

        @Override
        public boolean contains(Object o) {
            synchronized (rootMonitor) {
                return coll.contains(o);
            }
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            synchronized (rootMonitor) {
                return coll.containsAll(c);
            }
        }

        @Override
        public boolean isEmpty() {
            synchronized (rootMonitor) {
                return coll.isEmpty();
            }
        }

        @Override
        public Iterator<E> iterator() {
            synchronized (rootMonitor) {
                return new SynchronizedIterator<>(coll.iterator(), rootMonitor);
            }
        }

        @Override
        public boolean remove(Object o) {
            synchronized (rootMonitor) {
                return coll.remove(o);
            }
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            synchronized (rootMonitor) {
                return coll.removeAll(c);
            }
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            synchronized (rootMonitor) {
                return coll.retainAll(c);
            }
        }

        @Override
        public int size() {
            synchronized (rootMonitor) {
                return coll.size();
            }
        }

        @Override
        public Object[] toArray() {
            synchronized (rootMonitor) {
                return coll.toArray();
            }
        }

        @Override
        public <T> T[] toArray(T[] a) {
            synchronized (rootMonitor) {
                return coll.toArray(a);
            }
        }

        @Override
        public boolean removeIf(Predicate<? super E> filter) {
            synchronized (rootMonitor) {
                return coll.removeIf(filter);
            }
        }

        @Override
        public void forEach(Consumer<? super E> action) {
            synchronized (rootMonitor) {
                coll.forEach(action);
            }
        }

    }

    private static final class SynchronizedIterator<E> implements Iterator<E> {
        private final Iterator<E> iter;
        private final Object rootMonitor;

        SynchronizedIterator(Iterator<E> iter, Object rootMonitor) {
            this.iter = iter;
            this.rootMonitor = rootMonitor;
        }

        @Override
        public void forEachRemaining(Consumer<? super E> action) {
            synchronized (rootMonitor) {
                iter.forEachRemaining(action);
            }
        }

        @Override
        public boolean hasNext() {
            synchronized (rootMonitor) {
                return iter.hasNext();
            }
        }

        @Override
        public E next() {
            synchronized (rootMonitor) {
                return iter.next();
            }
        }

        @Override
        public void remove() {
            synchronized (rootMonitor) {
                iter.remove();
            }
        }
    }

    private static final class SynchronizedSet<E> extends SynchronizedCollection<E>
            implements Set<E> {
        SynchronizedSet(Set<E> coll, Object rootMonitor) {
            super(coll, rootMonitor);
        }
    }

    public static SynchronizedConfig wrap(Config c) {
        if (c instanceof SynchronizedConfig) {
            return (SynchronizedConfig) c;
        } else {
            CommentedConfig cc = CommentedConfig.fake(c);
            convertSubConfigs(cc);
            return new SynchronizedConfig(cc);
        }
    }

    /** Convert all sub-configurations to SynchronizedConfigs. */
    private static void convertSubConfigs(Config c) {
        if (c instanceof AbstractConfig) {
            AbstractConfig conf = (AbstractConfig)c;
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
    private static Object convertValue(Object v) {
        if (v instanceof Config) {
            Config subConfig = wrap((Config) v);
            convertSubConfigs(subConfig);
            return subConfig;
        } else if (v instanceof List) {
            List<Object> l = (List<Object>) v;
            l.replaceAll(elem -> convertValue(elem));
            return l;
        } else {
            return v;
        }
    }

    /** Underlying configuration. */
    private CommentedConfig config;

    /**
     * Root monitor: every operation on this config (including on
     * sub-configurations) is synchronized with this object.
     */
    final Object rootMonitor;

    SynchronizedConfig(CommentedConfig rootConfig) {
        this.config = rootConfig;
        this.rootMonitor = this;
    }

    SynchronizedConfig(CommentedConfig dataHolder, Object rootMonitor) {
        this.config = dataHolder;
        this.rootMonitor = rootMonitor;
    }

    // ----- specific -----

    /**
     * Atomically replaces the content of this config by the content of the specified config.
     * The specified config cannot be used anymore after a call to this method.
     * 
     * @param newContent the new content (cannot be used anymore after this)
     */
    public void replaceContentBy(SynchronizedConfig newContent) {
        synchronized (rootMonitor) {
            synchronized (newContent.rootMonitor) {
                this.config = newContent.config;
                newContent.config = null;
            }
        }
    }

    /**
     * Atomically replaces the content of this config by the content of the specified config.
     * The specified config cannot be used anymore after a call to this method.
     * 
     * @param newContent the new content (cannot be used anymore after this)
     */
    public void replaceContentBy(Config newContent) {
        if (newContent instanceof SynchronizedConfig) {
            replaceContentBy((SynchronizedConfig) newContent);
        } else if (newContent instanceof StampedConfig) {
            throw new UnsupportedOperationException(
                    "SynchronizedConfig.replaceContentBy(StampedConfig) is illegal (and useless anyway).");
        } else {
            synchronized (rootMonitor) {
                CommentedConfig newConfig = CommentedConfig.fake(newContent);
                convertSubConfigs(newConfig);
                this.config = newConfig;
            }
        }
    }

    // ----- ConcurrentConfig ----

    @Override
    public <R> R bulkCommentedRead(Function<? super UnmodifiableCommentedConfig, R> action) {
        synchronized (rootMonitor) {
            return action.apply(this.config);
        }
    }

    @Override
    public <R> R bulkCommentedUpdate(Function<? super CommentedConfig, R> action) {
        synchronized (rootMonitor) {
            return action.apply(this.config);
        }
    }

    // ----- Config -----

    @Override
    public boolean add(List<String> path, Object value) {
        synchronized (rootMonitor) {
            return config.add(path, value);
        }
    }

    @Override
    public void clearComments() {
        synchronized (rootMonitor) {
            config.clearComments();
        }

    }

    @Override
    public Map<String, String> commentMap() {
        synchronized (rootMonitor) {
            return new SynchronizedMap<>(config.commentMap(), rootMonitor);
        }
    }

    @Override
    public String removeComment(List<String> path) {
        synchronized (rootMonitor) {
            return config.removeComment(path);
        }
    }

    @Override
    public String setComment(List<String> path, String comment) {
        synchronized (rootMonitor) {
            return config.setComment(path, comment);
        }
    }

    @Override
    public boolean containsComment(List<String> path) {
        synchronized (rootMonitor) {
            return config.containsComment(path);
        }
    }

    @Override
    public String getComment(List<String> path) {
        synchronized (rootMonitor) {
            return config.getComment(path);
        }
    }

    @Override
    public ConfigFormat<?> configFormat() {
        synchronized (rootMonitor) {
            return config.configFormat();
        }
    }

    @Override
    public void clear() {
        synchronized (rootMonitor) {
            config.clear();
        }
    }

    @Override
    public SynchronizedConfig createSubConfig() {
        CommentedConfig subConfig;
        synchronized (rootMonitor) {
            subConfig = config.createSubConfig();
        }
        // every read or write to a subconfig must be synchronized too
        return new SynchronizedConfig(subConfig, rootMonitor);
    }

    @Override
    public Set<? extends CommentedConfig.Entry> entrySet() {
        synchronized (rootMonitor) {
            return new SynchronizedSet<>(config.entrySet(), rootMonitor);
        }
    }

    @Override
    public <T> T remove(List<String> path) {
        synchronized (rootMonitor) {
            return config.remove(path);
        }
    }

    @Override
    public <T> T set(List<String> path, Object value) {
        synchronized (rootMonitor) {
            return config.set(path, value);
        }
    }

    @Override
    public String toString() {
        synchronized (rootMonitor) {
            return config.toString();
        }
    }

    @Override
    public boolean contains(List<String> path) {
        synchronized (rootMonitor) {
            return config.contains(path);
        }
    }

    @Override
    public boolean equals(Object obj) {
        synchronized (rootMonitor) {
            return config.equals(obj);
        }
    }

    @Override
    public <T> T getRaw(List<String> path) {
        synchronized (rootMonitor) {
            return config.getRaw(path);
        }
    }

    @Override
    public int hashCode() {
        synchronized (rootMonitor) {
            return config.hashCode();
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (rootMonitor) {
            return config.isEmpty();
        }
    }

    @Override
    public int size() {
        synchronized (rootMonitor) {
            return config.size();
        }
    }

    @Override
    public Map<String, Object> valueMap() {
        synchronized (rootMonitor) {
            return new SynchronizedMap<>(config.valueMap(), rootMonitor);
        }
    }

    @Override
    public boolean add(String path, Object value) {
        synchronized (rootMonitor) {
            return config.add(path, value);
        }
    }

    @Override
    public void addAll(UnmodifiableConfig other) {
        synchronized (rootMonitor) {
            config.addAll(other);
        }
    }

    @Override
    public void putAll(UnmodifiableConfig other) {
        synchronized (rootMonitor) {
            config.putAll(other);
        }
    }

    @Override
    public <T> T remove(String path) {
        synchronized (rootMonitor) {
            return config.remove(path);
        }
    }

    @Override
    public void removeAll(UnmodifiableConfig toRemove) {
        synchronized (rootMonitor) {
            config.removeAll(toRemove);
        }
    }

    @Override
    public <T> T set(String path, Object value) {
        synchronized (rootMonitor) {
            return config.set(path, value);
        }
    }

    @Override
    public UnmodifiableCommentedConfig unmodifiable() {
        return config.unmodifiable();
    }

    @Override
    public void update(List<String> path, Object value) {
        synchronized (rootMonitor) {
            config.update(path, value);
        }
    }

    @Override
    public <T> T apply(List<String> path) {
        synchronized (rootMonitor) {
            return config.apply(path);
        }
    }

    @Override
    public <T> T get(List<String> path) {
        synchronized (rootMonitor) {
            return config.get(path);
        }
    }

    @Override
    public boolean isNull(List<String> path) {
        synchronized (rootMonitor) {
            return config.isNull(path);
        }
    }

    @Override
    public Map<String, CommentNode> getComments() {
        synchronized (rootMonitor) {
            return config.getComments(); // this is a deep copy, no need for synchronizedmap
        }
    }

    @Override
    public void getComments(Map<String, CommentNode> destination) {
        synchronized (rootMonitor) {
            config.getComments(destination);
        }
    }

    @Override
    public void putAllComments(Map<String, CommentNode> comments) {
        synchronized (rootMonitor) {
            config.putAllComments(comments);
        }
    }

    @Override
    public void putAllComments(UnmodifiableCommentedConfig commentedConfig) {
        synchronized (rootMonitor) {
            config.putAllComments(commentedConfig);
        }
    }

}
