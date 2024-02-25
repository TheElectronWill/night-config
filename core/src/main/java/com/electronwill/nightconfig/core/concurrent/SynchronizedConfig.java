package com.electronwill.nightconfig.core.concurrent;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.electronwill.nightconfig.core.AbstractCommentedConfig;
import com.electronwill.nightconfig.core.AbstractConfig;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.InMemoryCommentedFormat;
import com.electronwill.nightconfig.core.UnmodifiableCommentedConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import com.electronwill.nightconfig.core.utils.TransformingMap;

/**
 * A configuration that is synchronized, and therefore thread-safe (reads and
 * writes can happen in any order from any thread).
 * <p>
 * Only one read or write can happen at the same time. This includes every read
 * and write on sub-configurations, iterator operations, etc.
 */
public final class SynchronizedConfig implements ConcurrentCommentedConfig {

    public static SynchronizedConfig convert(Config c) {
        return convert(c, null);
    }

    private static SynchronizedConfig convert(Config c, SynchronizedConfig parent) {
        if (c instanceof SynchronizedConfig) {
            return (SynchronizedConfig) c;
        } else {
            SynchronizedConfig result = new SynchronizedConfig(c.configFormat(),
                    Config.getDefaultMapCreator(false), parent);

            CommentedConfig cc = CommentedConfig.fake(c);
            convertSubConfigs(cc, result);

            result.putAll(cc);
            result.putAllComments(cc);
            return result;
        }
    }

    /** Convert all sub-configurations to SynchronizedConfigs. */
    private static void convertSubConfigs(Config c, SynchronizedConfig parent) {
        if (c instanceof AbstractConfig) {
            AbstractConfig conf = (AbstractConfig) c;
            conf.valueMap().replaceAll((k, v) -> convertValue(v, parent));
        } else {
            for (Config.Entry entry : c.entrySet()) {
                Object value = entry.getRawValue();
                Object converted = convertValue(value, parent);
                if (value != converted) {
                    entry.setValue(converted);
                }
            }
        }
    }

    private static Object convertValue(Object v, SynchronizedConfig parent) {
        if (v instanceof Config) {
            SynchronizedConfig subConfig = convert((Config) v, parent);
            convertSubConfigs(subConfig, subConfig);
            return subConfig;
        } else if (v instanceof List) {
            List<?> l = (List<?>) v;
            List<Object> newList = new ArrayList<>(l);
            newList.replaceAll(elem -> convertValue(elem, parent));
            return newList;
        } else {
            return v;
        }
    }

    /** Underlying configuration. */
    private DataHolder dataHolder;

    /**
     * Root monitor: every operation on this config (including on
     * sub-configurations) is synchronized with this object.
     */
    final Object rootMonitor;

    public SynchronizedConfig() {
        this(InMemoryCommentedFormat.defaultInstance(), Config.getDefaultMapCreator(false));
    }

    public SynchronizedConfig(ConfigFormat<?> configFormat,
            Supplier<Map<String, Object>> mapSupplier) {
        this.rootMonitor = new Object();
        this.dataHolder = new DataHolder(this, configFormat, mapSupplier);
    }

    public SynchronizedConfig(ConfigFormat<?> configFormat,
            Supplier<Map<String, Object>> mapSupplier, SynchronizedConfig parent) {
        this.rootMonitor = (parent == null) ? new Object() : parent.rootMonitor;
        this.dataHolder = new DataHolder(parent == null ? this : parent, configFormat, mapSupplier);
    }

    // SynchronizedConfig(DataHolder subConfig, Object rootMonitor) {
    //     this.dataHolder = subConfig;
    //     this.rootMonitor = rootMonitor;
    // }

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
                this.dataHolder = newContent.dataHolder;
                newContent.dataHolder = null;
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
            CommentedConfig cc = CommentedConfig.fake(newContent);

            // try to use the same Map supplier as the new content
            Supplier<Map<String, Object>> mapSupplier = null;
            if (newContent instanceof StampedConfig.Accumulator) {
                mapSupplier = ((StampedConfig.Accumulator)newContent).mapSupplier();
            } else if (newContent instanceof AbstractConfig) {
                try {
                    Map<String,Object> map = ((AbstractConfig)newContent).valueMap();
                    if (map instanceof HashMap) {
                        mapSupplier = HashMap::new;
                    } else if (map instanceof LinkedHashMap) {
                        mapSupplier = LinkedHashMap::new;
                    }
                } catch (UnsupportedOperationException ex) {
                    mapSupplier = null;
                }
            }
            if (mapSupplier == null) {
                mapSupplier = Config.getDefaultMapCreator(false);
            }
            synchronized (rootMonitor) {
                DataHolder dataHolder = new DataHolder(this, newContent.configFormat(), mapSupplier);
                dataHolder.putAll(cc);
                dataHolder.putAllComments(cc);
                convertSubConfigs(dataHolder, this);
                this.dataHolder = dataHolder;
            }
        }
    }

    // ----- ConcurrentConfig ----

    @Override
    public <R> R bulkCommentedRead(Function<? super UnmodifiableCommentedConfig, R> action) {
        synchronized (rootMonitor) {
            return action.apply(this.dataHolder);
        }
    }

    @Override
    public <R> R bulkCommentedUpdate(Function<? super CommentedConfig, R> action) {
        synchronized (rootMonitor) {
            return action.apply(this.dataHolder);
        }
    }

    // ----- Config -----

    @Override
    public boolean add(List<String> path, Object value) {
        synchronized (rootMonitor) {
            return dataHolder.add(path, value);
        }
    }

    @Override
    public void clearComments() {
        synchronized (rootMonitor) {
            dataHolder.clearComments();
        }

    }

    @Override
    public Map<String, String> commentMap() {
        synchronized (rootMonitor) {
            return new SynchronizedMap<>(dataHolder.commentMap(), rootMonitor);
        }
    }

    @Override
    public String removeComment(List<String> path) {
        synchronized (rootMonitor) {
            return dataHolder.removeComment(path);
        }
    }

    @Override
    public String setComment(List<String> path, String comment) {
        synchronized (rootMonitor) {
            return dataHolder.setComment(path, comment);
        }
    }

    @Override
    public boolean containsComment(List<String> path) {
        synchronized (rootMonitor) {
            return dataHolder.containsComment(path);
        }
    }

    @Override
    public String getComment(List<String> path) {
        synchronized (rootMonitor) {
            return dataHolder.getComment(path);
        }
    }

    @Override
    public ConfigFormat<?> configFormat() {
        synchronized (rootMonitor) {
            return dataHolder.configFormat();
        }
    }

    @Override
    public void clear() {
        synchronized (rootMonitor) {
            dataHolder.clear();
        }
    }

    @Override
    public SynchronizedConfig createSubConfig() {
        return dataHolder.createSubConfig();
    }

    @Override
    public Set<? extends CommentedConfig.Entry> entrySet() {
        synchronized (rootMonitor) {
            return new SynchronizedSet<>(dataHolder.entrySet(), rootMonitor);
        }
    }

    @Override
    public <T> T remove(List<String> path) {
        synchronized (rootMonitor) {
            return dataHolder.remove(path);
        }
    }

    @Override
    public <T> T set(List<String> path, Object value) {
        synchronized (rootMonitor) {
            return dataHolder.set(path, value);
        }
    }

    @Override
    public String toString() {
        synchronized (rootMonitor) {
            return "SynchronizedConfig{" + dataHolder.toString() + "}";
        }
    }

    @Override
    public boolean contains(List<String> path) {
        synchronized (rootMonitor) {
            return dataHolder.contains(path);
        }
    }

    @Override
    public boolean equals(Object obj) {
        synchronized (rootMonitor) {
            return dataHolder.equals(obj);
        }
    }

    @Override
    public <T> T getRaw(List<String> path) {
        synchronized (rootMonitor) {
            return dataHolder.getRaw(path);
        }
    }

    @Override
    public int hashCode() {
        synchronized (rootMonitor) {
            return dataHolder.hashCode();
        }
    }

    @Override
    public boolean isEmpty() {
        synchronized (rootMonitor) {
            return dataHolder.isEmpty();
        }
    }

    @Override
    public int size() {
        synchronized (rootMonitor) {
            return dataHolder.size();
        }
    }

    @Override
    public Map<String, Object> valueMap() {
        synchronized (rootMonitor) {
            Map<String, Object> transformingMap = new TransformingMap<>(dataHolder.valueMap(),
                    o -> o,
                    toWrite -> convertValue(toWrite, this), o -> o);
            return new SynchronizedMap<>(transformingMap, rootMonitor);
        }
    }

    @Override
    public boolean add(String path, Object value) {
        synchronized (rootMonitor) {
            return dataHolder.add(path, value);
        }
    }

    @Override
    public void addAll(UnmodifiableConfig other) {
        synchronized (rootMonitor) {
            dataHolder.addAll(other);
        }
    }

    @Override
    public void putAll(UnmodifiableConfig other) {
        synchronized (rootMonitor) {
            dataHolder.putAll(other);
        }
    }

    @Override
    public <T> T remove(String path) {
        synchronized (rootMonitor) {
            return dataHolder.remove(path);
        }
    }

    @Override
    public void removeAll(UnmodifiableConfig toRemove) {
        synchronized (rootMonitor) {
            dataHolder.removeAll(toRemove);
        }
    }

    @Override
    public <T> T set(String path, Object value) {
        synchronized (rootMonitor) {
            return dataHolder.set(path, value);
        }
    }

    @Override
    public UnmodifiableCommentedConfig unmodifiable() {
        return dataHolder.unmodifiable();
    }

    @Override
    public void update(List<String> path, Object value) {
        synchronized (rootMonitor) {
            dataHolder.update(path, value);
        }
    }

    @Override
    public <T> T apply(List<String> path) {
        synchronized (rootMonitor) {
            return dataHolder.apply(path);
        }
    }

    @Override
    public <T> T get(List<String> path) {
        synchronized (rootMonitor) {
            return dataHolder.get(path);
        }
    }

    @Override
    public boolean isNull(List<String> path) {
        synchronized (rootMonitor) {
            return dataHolder.isNull(path);
        }
    }

    @Override
    public Map<String, CommentNode> getComments() {
        synchronized (rootMonitor) {
            return dataHolder.getComments(); // this is a deep copy, no need for synchronizedmap
        }
    }

    @Override
    public void getComments(Map<String, CommentNode> destination) {
        synchronized (rootMonitor) {
            dataHolder.getComments(destination);
        }
    }

    @Override
    public void putAllComments(Map<String, CommentNode> comments) {
        synchronized (rootMonitor) {
            dataHolder.putAllComments(comments);
        }
    }

    @Override
    public void putAllComments(UnmodifiableCommentedConfig commentedConfig) {
        synchronized (rootMonitor) {
            dataHolder.putAllComments(commentedConfig);
        }
    }

    private static final class DataHolder extends AbstractCommentedConfig {

        private SynchronizedConfig syncConfig;
        private final ConfigFormat<?> format;

        DataHolder(SynchronizedConfig parent) {
            super(parent.dataHolder.mapCreator);
            this.format = parent.configFormat();
            this.syncConfig = parent;
        }

        DataHolder(SynchronizedConfig syncConfig, ConfigFormat<?> configFormat,
                Supplier<Map<String, Object>> mapCreator) {
            super(mapCreator);
            this.format = configFormat;
            this.syncConfig = syncConfig;
        }

        @Override
        public AbstractCommentedConfig clone() {
            throw new UnsupportedOperationException();
        }

        @Override
        public SynchronizedConfig createSubConfig() {
            synchronized (syncConfig.rootMonitor) {
                return new SynchronizedConfig(format, mapCreator, syncConfig);
            }
        }

        @Override
        public ConfigFormat<?> configFormat() {
            return format;
        }

    }

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

}
