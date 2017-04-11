package com.electronwill.nightconfig.yaml;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.utils.TransparentWrapper;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.*;
import java.util.stream.*;

/**
 * @author TheElectronWill
 */
final class ConfigUnwrapper {
	private ConfigUnwrapper() {}

	/**
	 * Unwraps a configuration. Each subconfig is unwrapped into a plain java Map, using the
	 * {@link Config#asMap()} method.
	 *
	 * @param config the config to unwrap
	 * @return the unwrapped config
	 */
	static Map<String, Object> unwrap(Config config) {
		return new UnwrappedMap(config.asMap());
	}

	/**
	 * Unwraps a value: a Config is unwrapped into its map and an Entry is converted into an
	 * UnwrappedEntry. Any other value is returned as it is.
	 *
	 * @param value the value to unwrap
	 * @param <T>   the value's type
	 * @return the unwrapped value
	 */
	private static <T> T unwrap(T value) {
		if (value instanceof Config) { return (T)unwrap((Config)value); }
		if (value instanceof Entry) { return (T)new UnwrappedEntry((Entry)value); }
		return value;
	}

	/**
	 * Unwraps an array of values. Each value is unwrapped if necessary.
	 *
	 * @param array the array to unwrap
	 * @param <T>   the array's type
	 * @return the array, after its values have been unwrapped
	 */
	private static <T> T[] unwrap(T[] array) {
		for (int i = 0; i < array.length; i++) {
			Object element = array[i];
			if (element instanceof Config) {
				array[i] = (T)((Config)element).asMap();
			} else if (element instanceof Entry) {
				array[i] = (T)new UnwrappedEntry((Entry)element);
			}
		}
		return array;
	}

	/**
	 * Unwrap an optional value.
	 *
	 * @param optional the Optional to unwrap
	 * @param <T>      the optional's type
	 * @return a new Optional containing the unwrapped value, or {@code Optional.empty()} if the
	 * given optional is empty
	 */
	private static <T> Optional<T> unwrap(Optional<T> optional) {
		if (optional.isPresent()) {
			return Optional.of(unwrap(optional.get()));
		}
		return Optional.empty();
	}

	private static final class UnwrappedMap extends TransparentWrapper<Map<String, Object>>
			implements Map<String, Object> {

		public UnwrappedMap(Map<String, Object> wrapped) {super(wrapped);}

		@Override
		public int size() {
			return wrapped.size();
		}

		@Override
		public boolean isEmpty() {
			return wrapped.isEmpty();
		}

		@Override
		public boolean containsKey(Object key) {
			return wrapped.containsKey(key);
		}

		@Override
		public boolean containsValue(Object value) {
			return wrapped.containsValue(value);
		}

		@Override
		public Object get(Object key) {
			return unwrap(wrapped.get(key));
		}

		@Override
		public Object put(String key, Object value) {
			return unwrap(wrapped.put(key, value));
		}

		@Override
		public Object remove(Object key) {
			return unwrap(wrapped.remove(key));
		}

		@Override
		public void putAll(Map<? extends String, ?> m) {
			wrapped.putAll(m);
		}

		@Override
		public void clear() {
			wrapped.clear();
		}

		@Override
		public Set<String> keySet() {
			return wrapped.keySet();
		}

		@Override
		public Collection<Object> values() {
			return new UnwrappedValueCollection(wrapped.values());
		}

		@Override
		public Set<Entry<String, Object>> entrySet() {
			return new UnwrappedEntrySet(wrapped.entrySet());
		}

		@Override
		public Object getOrDefault(Object key, Object defaultValue) {
			return unwrap(wrapped.getOrDefault(key, defaultValue));
		}

		@Override
		public void forEach(BiConsumer<? super String, ? super Object> action) {
			wrapped.forEach((s, o) -> action.accept(s, unwrap(o)));
		}

		@Override
		public void replaceAll(BiFunction<? super String, ? super Object, ?> function) {
			wrapped.replaceAll((s, o) -> function.apply(s, unwrap(o)));
		}

		@Override
		public Object putIfAbsent(String key, Object value) {
			return unwrap(wrapped.putIfAbsent(key, value));
		}

		@Override
		public boolean remove(Object key, Object value) {
			return wrapped.remove(key, value);//TODO wrap the value so that the comparison work
		}

		@Override
		public boolean replace(String key, Object oldValue, Object newValue) {
			return wrapped.replace(key, oldValue, newValue);
		}

		@Override
		public Object replace(String key, Object value) {
			return unwrap(wrapped.replace(key, value));
		}

		@Override
		public Object computeIfAbsent(String key, Function<? super String, ?> mappingFunction) {
			return unwrap(wrapped.computeIfAbsent(key, mappingFunction));
		}

		@Override
		public Object computeIfPresent(String key,
									   BiFunction<? super String, ? super Object, ?> remappingFunction) {
			return unwrap(
					wrapped.computeIfPresent(key, (s, o) -> remappingFunction.apply(s, unwrap(o))));
		}

		@Override
		public Object compute(String key,
							  BiFunction<? super String, ? super Object, ?> remappingFunction) {
			return unwrap(wrapped.compute(key, (s, o) -> remappingFunction.apply(s, unwrap(o))));
		}

		@Override
		public Object merge(String key, Object value,
							BiFunction<? super Object, ? super Object, ?> remappingFunction) {
			return unwrap(wrapped.merge(key, unwrap(value),
										(o, o2) -> remappingFunction.apply(unwrap(o), unwrap(o2))));
		}
	}

	private static final class UnwrappedValueCollection
			extends TransparentWrapper<Collection<Object>> implements Collection<Object> {

		public UnwrappedValueCollection(Collection<Object> wrapped) {super(wrapped);}

		@Override
		public int size() {
			return wrapped.size();
		}

		@Override
		public boolean isEmpty() {
			return wrapped.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return wrapped.contains(o);
		}

		@Override
		public Iterator<Object> iterator() {
			return new UnwrappedIterator<>(wrapped.iterator());
		}

		@Override
		public Object[] toArray() {
			return unwrap(wrapped.toArray());
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return unwrap(wrapped.toArray(a));
		}

		@Override
		public boolean add(Object o) {
			return wrapped.add(o);
		}

		@Override
		public boolean remove(Object o) {
			return wrapped.remove(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return wrapped.contains(c);
		}

		@Override
		public boolean addAll(Collection<?> c) {
			return wrapped.addAll(c);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return wrapped.removeAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return wrapped.retainAll(c);
		}

		@Override
		public void clear() {
			wrapped.clear();
		}

		@Override
		public Spliterator<Object> spliterator() {
			return new UnwrappedSpliterator<>(wrapped.spliterator());
		}

		@Override
		public void forEach(Consumer<? super Object> action) {
			wrapped.forEach(new UnwrappedAction<>(action));
		}

		@Override
		public boolean removeIf(Predicate<? super Object> filter) {
			return wrapped.removeIf(value -> filter.test(unwrap(value)));
		}

		@Override
		public Stream<Object> stream() {
			return new UnwrappedStream<>(wrapped.stream());
		}

		@Override
		public Stream<Object> parallelStream() {
			return new UnwrappedStream<>(wrapped.parallelStream());
		}
	}

	private static final class UnwrappedEntrySet
			extends TransparentWrapper<Set<Entry<String, Object>>>
			implements Set<Entry<String, Object>> {

		public UnwrappedEntrySet(Set<Entry<String, Object>> wrapped) {super(wrapped);}

		@Override
		public int size() {
			return wrapped.size();
		}

		@Override
		public boolean isEmpty() {
			return wrapped.isEmpty();
		}

		@Override
		public boolean contains(Object o) {
			return wrapped.contains(o);
		}

		@Override
		public Iterator<Entry<String, Object>> iterator() {
			return new UnwrappedIterator(wrapped.iterator());
		}

		@Override
		public Object[] toArray() {
			return unwrap(wrapped.toArray());
		}

		@Override
		public <T> T[] toArray(T[] a) {
			return unwrap(wrapped.toArray(a));
		}

		@Override
		public boolean add(Entry<String, Object> entry) {
			return wrapped.add(entry);
		}

		@Override
		public boolean remove(Object o) {
			return wrapped.remove(o);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return wrapped.containsAll(c);
		}

		@Override
		public boolean addAll(Collection<? extends Entry<String, Object>> c) {
			return wrapped.addAll(c);
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			return wrapped.retainAll(c);
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			return wrapped.removeAll(c);
		}

		@Override
		public void clear() {
			wrapped.clear();
		}

		@Override
		public Spliterator<Entry<String, Object>> spliterator() {
			return new UnwrappedSpliterator(wrapped.spliterator());
		}

		@Override
		public void forEach(Consumer<? super Entry<String, Object>> action) {
			wrapped.forEach(new UnwrappedAction(action));
		}

		@Override
		public boolean removeIf(Predicate<? super Entry<String, Object>> filter) {
			return wrapped.removeIf(entry -> filter.test(new UnwrappedEntry(entry)));
		}

		@Override
		public Stream<Entry<String, Object>> stream() {
			return new UnwrappedStream<>(wrapped.stream());
		}

		@Override
		public Stream<Entry<String, Object>> parallelStream() {
			return new UnwrappedStream<>(wrapped.parallelStream());
		}
	}

	private static final class UnwrappedStream<E> extends TransparentWrapper<Stream<E>>
			implements Stream<E> {

		public UnwrappedStream(Stream<E> wrapped) {super(wrapped);}

		@Override
		public Stream<E> filter(Predicate<? super E> predicate) {
			return wrapped.filter(e -> predicate.test(unwrap(e)));
		}

		@Override
		public <R> Stream<R> map(Function<? super E, ? extends R> mapper) {
			return wrapped.map(e -> mapper.apply(unwrap(e)));
		}

		@Override
		public IntStream mapToInt(ToIntFunction<? super E> mapper) {
			return wrapped.mapToInt(e -> mapper.applyAsInt(unwrap(e)));
		}

		@Override
		public LongStream mapToLong(ToLongFunction<? super E> mapper) {
			return wrapped.mapToLong(e -> mapper.applyAsLong(unwrap(e)));
		}

		@Override
		public DoubleStream mapToDouble(ToDoubleFunction<? super E> mapper) {
			return wrapped.mapToDouble(e -> mapper.applyAsDouble(unwrap(e)));
		}

		@Override
		public <R> Stream<R> flatMap(Function<? super E, ? extends Stream<? extends R>> mapper) {
			return wrapped.flatMap(e -> mapper.apply(unwrap(e)));
		}

		@Override
		public IntStream flatMapToInt(Function<? super E, ? extends IntStream> mapper) {
			return wrapped.flatMapToInt(e -> mapper.apply(unwrap(e)));
		}

		@Override
		public LongStream flatMapToLong(Function<? super E, ? extends LongStream> mapper) {
			return wrapped.flatMapToLong(e -> mapper.apply(unwrap(e)));
		}

		@Override
		public DoubleStream flatMapToDouble(Function<? super E, ? extends DoubleStream> mapper) {
			return wrapped.flatMapToDouble(e -> mapper.apply(unwrap(e)));
		}

		@Override
		public Stream<E> distinct() {
			return new UnwrappedStream<E>(wrapped.distinct());
		}

		@Override
		public Stream<E> sorted() {
			return new UnwrappedStream<E>(wrapped.sorted());
		}

		@Override
		public Stream<E> sorted(Comparator<? super E> comparator) {
			return new UnwrappedStream<E>(wrapped.sorted(comparator));
		}

		@Override
		public Stream<E> peek(Consumer<? super E> action) {
			return new UnwrappedStream<E>(wrapped.peek(new UnwrappedAction<E>(action)));
		}

		@Override
		public Stream<E> limit(long maxSize) {
			return new UnwrappedStream<E>(wrapped.limit(maxSize));
		}

		@Override
		public Stream<E> skip(long n) {
			return new UnwrappedStream<E>(wrapped.skip(n));
		}

		@Override
		public void forEach(Consumer<? super E> action) {
			wrapped.forEach(new UnwrappedAction<E>(action));
		}

		@Override
		public void forEachOrdered(Consumer<? super E> action) {
			wrapped.forEachOrdered(new UnwrappedAction<E>(action));
		}

		@Override
		public Object[] toArray() {
			return unwrap(wrapped.toArray());
		}

		@Override
		public <A> A[] toArray(IntFunction<A[]> generator) {
			return unwrap(wrapped.toArray(generator));
		}

		@Override
		public E reduce(E identity, BinaryOperator<E> accumulator) {
			E e = wrapped.reduce(unwrap(identity),
								 (t, u) -> accumulator.apply(unwrap(t), unwrap(u)));
			return unwrap(e);
		}

		@Override
		public Optional<E> reduce(BinaryOperator<E> accumulator) {
			Optional<E> opt = wrapped.reduce((t, u) -> accumulator.apply(unwrap(t), unwrap(u)));
			return unwrap(opt);
		}

		@Override
		public <U> U reduce(U identity, BiFunction<U, ? super E, U> accumulator,
							BinaryOperator<U> combiner) {
			return wrapped.reduce(unwrap(identity), (u, e) -> accumulator.apply(u, unwrap(e)),
								  combiner);
		}

		@Override
		public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super E> accumulator,
							 BiConsumer<R, R> combiner) {
			return wrapped.collect(supplier, (r, e) -> accumulator.accept(r, unwrap(e)), combiner);
		}

		@Override
		public <R, A> R collect(Collector<? super E, A, R> collector) {
			return wrapped.collect(new UnwrappedCollector<>(collector));
		}

		@Override
		public Optional<E> min(Comparator<? super E> comparator) {
			Optional<E> opt = wrapped.min((o1, o2) -> comparator.compare(unwrap(o1), unwrap(o2)));
			return unwrap(opt);
		}

		@Override
		public Optional<E> max(Comparator<? super E> comparator) {
			Optional<E> opt = wrapped.max((o1, o2) -> comparator.compare(unwrap(o1), unwrap(o2)));
			return unwrap(opt);
		}

		@Override
		public long count() {
			return wrapped.count();
		}

		@Override
		public boolean anyMatch(Predicate<? super E> predicate) {
			return wrapped.anyMatch(e -> predicate.test(unwrap(e)));
		}

		@Override
		public boolean allMatch(Predicate<? super E> predicate) {
			return wrapped.allMatch(e -> predicate.test(unwrap(e)));
		}

		@Override
		public boolean noneMatch(Predicate<? super E> predicate) {
			return wrapped.noneMatch(e -> predicate.test(unwrap(e)));
		}

		@Override
		public Optional<E> findFirst() {
			return unwrap(wrapped.findFirst());
		}

		@Override
		public Optional<E> findAny() {
			return unwrap(wrapped.findAny());
		}

		@Override
		public Iterator<E> iterator() {
			return new UnwrappedIterator<E>(wrapped.iterator());
		}

		@Override
		public Spliterator<E> spliterator() {
			return new UnwrappedSpliterator<E>(wrapped.spliterator());
		}

		@Override
		public boolean isParallel() {
			return wrapped.isParallel();
		}

		@Override
		public Stream<E> sequential() {
			return new UnwrappedStream<E>(wrapped.sequential());
		}

		@Override
		public Stream<E> parallel() {
			return new UnwrappedStream<E>(wrapped.parallel());
		}

		@Override
		public Stream<E> unordered() {
			return new UnwrappedStream<E>(wrapped.unordered());
		}

		@Override
		public Stream<E> onClose(Runnable closeHandler) {
			return new UnwrappedStream<E>(wrapped.onClose(closeHandler));
		}

		@Override
		public void close() {
			wrapped.close();
		}
	}

	private static final class UnwrappedCollector<E, A, R>
			extends TransparentWrapper<Collector<? super E, A, R>> implements Collector<E, A, R> {

		public UnwrappedCollector(Collector<? super E, A, R> wrapped) {super(wrapped);}

		@Override
		public Supplier<A> supplier() {
			return wrapped.supplier();
		}

		@Override
		public BiConsumer<A, E> accumulator() {
			return (a, e) -> wrapped.accumulator().accept(a, unwrap(e));
		}

		@Override
		public BinaryOperator<A> combiner() {
			return wrapped.combiner();
		}

		@Override
		public Function<A, R> finisher() {
			return wrapped.finisher();
		}

		@Override
		public Set<Characteristics> characteristics() {
			return wrapped.characteristics();
		}
	}

	private static final class UnwrappedSpliterator<E> extends TransparentWrapper<Spliterator<E>>
			implements Spliterator<E> {

		public UnwrappedSpliterator(Spliterator<E> wrapped) {super(wrapped);}

		@Override
		public boolean tryAdvance(Consumer<? super E> action) {
			return wrapped.tryAdvance(new UnwrappedAction(action));
		}

		@Override
		public void forEachRemaining(Consumer<? super E> action) {
			wrapped.forEachRemaining(new UnwrappedAction(action));
		}

		@Override
		public Spliterator<E> trySplit() {
			return new UnwrappedSpliterator(wrapped.trySplit());
		}

		@Override
		public long estimateSize() {
			return wrapped.estimateSize();
		}

		@Override
		public long getExactSizeIfKnown() {
			return wrapped.getExactSizeIfKnown();
		}

		@Override
		public int characteristics() {
			return wrapped.characteristics();
		}

		@Override
		public boolean hasCharacteristics(int characteristics) {
			return wrapped.hasCharacteristics(characteristics);
		}

		@Override
		public Comparator<? super E> getComparator() {
			return wrapped.getComparator();
		}
	}

	private static final class UnwrappedIterator<E> extends TransparentWrapper<Iterator<E>>
			implements Iterator<E> {

		public UnwrappedIterator(Iterator<E> wrapped) {super(wrapped);}

		@Override
		public boolean hasNext() {
			return wrapped.hasNext();
		}

		@Override
		public E next() {
			return unwrap(wrapped.next());
		}

		@Override
		public void forEachRemaining(Consumer<? super E> action) {
			wrapped.forEachRemaining(new UnwrappedAction(action));
		}
	}

	private static final class UnwrappedAction<T> extends TransparentWrapper<Consumer<? super T>>
			implements Consumer<T> {

		private UnwrappedAction(Consumer<? super T> consumer) {super(consumer);}

		@Override
		public void accept(T t) {
			wrapped.accept(unwrap(t));
		}
	}

	private static final class UnwrappedEntry extends TransparentWrapper<Entry<String, Object>>
			implements Entry<String, Object> {

		private UnwrappedEntry(Entry<String, Object> entry) {super(entry);}

		@Override
		public String getKey() {
			return wrapped.getKey();
		}

		@Override
		public Object getValue() {
			return unwrap(wrapped.getValue());
		}

		@Override
		public Object setValue(Object value) {
			return unwrap(wrapped.setValue(value));
		}
	}
}
