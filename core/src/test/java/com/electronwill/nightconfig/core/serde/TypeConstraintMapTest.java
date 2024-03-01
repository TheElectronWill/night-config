package com.electronwill.nightconfig.core.serde;

import static org.junit.jupiter.api.Assertions.*;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

public class TypeConstraintMapTest {
	@Test
	public void resolveMapArgumentsFromClassSimple() {
		List<TypeConstraint> t = extractMapValueType(new TypeConstraint(MyMap.class));
		assertEquals(List.of(CharSequence.class, Number.class), fullType(t));
	}

	@Test
	public void resolveMapArgumentsFromParentClass() {
		List<TypeConstraint> t = extractMapValueType(new TypeConstraint(MyMap1.class));
		assertEquals(List.of("interface java.lang.CharSequence", "V"), fullTypeString(t));
		assertEquals(List.of(Optional.of(CharSequence.class), Optional.of(Number.class)), rawType(t));

		t = extractMapValueType(new TypeConstraint(MyMap2.class));
		assertEquals(List.of(CharSequence.class, Number.class), fullType(t));
		t = extractMapValueType(new TypeConstraint(MyMap3.class));
		assertEquals(List.of(CharSequence.class, Number.class), fullType(t));

		t = extractMapValueType(new TypeConstraint(MyMap1_1.class));
		assertEquals(List.of("interface java.lang.CharSequence", "V"), fullTypeString(t));
		assertEquals(List.of(Optional.of(CharSequence.class), Optional.of(Number.class)), rawType(t));

		t = extractMapValueType(new TypeConstraint(MyMap1_2.class));
		assertEquals(List.of(CharSequence.class, Integer.class), fullType(t));
		assertEquals(List.of(Optional.of(CharSequence.class), Optional.of(Integer.class)), rawType(t));

		t = extractMapValueType(new TypeConstraint(MyMap2_1.class));
		assertEquals(List.of(CharSequence.class, Number.class), fullType(t));
		t = extractMapValueType(new TypeConstraint(MyMap2_2.class));
		assertEquals(List.of(CharSequence.class, Number.class), fullType(t));
		t = extractMapValueType(new TypeConstraint(MyMap3_1.class));
		assertEquals(List.of(CharSequence.class, Number.class), fullType(t));
	}

	@Test
	public void resolveMapArgumentsFromField() {
		for (Field f : ObjectA.class.getDeclaredFields()) {
			TypeConstraint t = new TypeConstraint(f.getGenericType());
			List<TypeConstraint> vt = extractMapValueType(t);
			List<Type> full = fullType(vt);
			assertEquals(CharSequence.class, full.get(0));
			assertTrue(Number.class.isAssignableFrom((Class<?>)full.get(1)));
			assertEquals(List.of(Optional.of(CharSequence.class), Optional.of(Number.class)), rawType(vt));
		}
		for (Field f : Params1.class.getDeclaredFields()) {
			TypeConstraint t = new TypeConstraint(f.getGenericType());
			List<TypeConstraint> vt = extractMapValueType(t);
			assertTrue(fullType(vt).get(0) instanceof TypeVariable);
			assertTrue(fullType(vt).get(1) instanceof TypeVariable);
			assertEquals(List.of(Optional.of(CharSequence.class), Optional.of(Number.class)), rawType(vt));
		}
		for (Field f : Params2.class.getDeclaredFields()) {
			TypeConstraint t = new TypeConstraint(f.getGenericType());
			List<TypeConstraint> vt = extractMapValueType(t);
			assertEquals(List.of(CharSequence.class, Number.class), fullType(vt));
			assertEquals(List.of(Optional.of(CharSequence.class), Optional.of(Number.class)), rawType(vt));
		}
	}

	@Test
	public void resolveMapArgumentsFromFieldWildcards()
			throws NoSuchFieldException, SecurityException {
		{
			TypeConstraint t;
			List<TypeConstraint> vt;

			Field superMap = Wildcards.class.getDeclaredField("superMap");
			t = new TypeConstraint(superMap.getGenericType());
			vt = extractMapValueType(t);
			assertEquals(List.of("? super java.lang.CharSequence", "? super java.lang.Number"), fullTypeString(vt));
			assertEquals(List.of(Optional.of(CharSequence.class), Optional.of(Number.class)), rawType(vt));

			Field extendMap = Wildcards.class.getDeclaredField("extendMap");
			t = new TypeConstraint(extendMap.getGenericType());
			vt = extractMapValueType(t);
			assertEquals(List.of("? extends java.lang.CharSequence", "? extends java.lang.Number"), fullTypeString(vt));
			assertEquals(List.of(Optional.of(CharSequence.class), Optional.of(Number.class)), rawType(vt));

			Field rawClass = Wildcards.class.getDeclaredField("rawClass");
			t = new TypeConstraint(rawClass.getGenericType());
			vt = extractMapValueType(t);
			assertEquals(List.of("interface java.lang.CharSequence", "V"), fullTypeString(vt));
			assertEquals(List.of(Optional.of(CharSequence.class), Optional.of(Number.class)), rawType(vt));

			// here we have no information on the generic arguments...
			Field rawInterface = Wildcards.class.getDeclaredField("rawInterface");
			t = new TypeConstraint(rawInterface.getGenericType());
			vt = extractMapValueType(t);
			assertNull(vt); // ... therefore vt is null

			Field unspecifiedClass = Wildcards.class.getDeclaredField("unspecifiedClass");
			t = new TypeConstraint(unspecifiedClass.getGenericType());
			vt = extractMapValueType(t);
			assertEquals(List.of("interface java.lang.CharSequence", "? <: [class java.lang.Number]"), fullTypeString(vt));
			assertEquals(List.of(Optional.of(CharSequence.class), Optional.of(Number.class)), rawType(vt));

			Field unspecifiedInterface = Wildcards.class.getDeclaredField("unspecifiedInterface");
			t = new TypeConstraint(unspecifiedInterface.getGenericType());
			vt = extractMapValueType(t);
			assertEquals(List.of("?", "?"), fullTypeString(vt));
			assertEquals(List.of(Optional.of(Object.class), Optional.of(Object.class)), rawType(vt));

			Field subExtend = Wildcards.class.getDeclaredField("subExtend");
			t = new TypeConstraint(subExtend.getGenericType());
			vt = extractMapValueType(t);
			assertEquals(List.of("interface java.lang.CharSequence", "? extends java.lang.Number"), fullTypeString(vt));
			assertEquals(List.of(Optional.of(CharSequence.class), Optional.of(Number.class)), rawType(vt));

			Field subSuper = Wildcards.class.getDeclaredField("subSuper");
			t = new TypeConstraint(subSuper.getGenericType());
			vt = extractMapValueType(t);
			assertEquals(List.of("interface java.lang.CharSequence", "class java.lang.Number"), fullTypeString(vt));
			assertEquals(List.of(Optional.of(CharSequence.class), Optional.of(Number.class)), rawType(vt));
		}

		{
			{
				Field superMap = WildcardsWithParams.class.getDeclaredField("superMap");
				TypeConstraint t = new TypeConstraint(superMap.getGenericType());
				List<TypeConstraint> vt = extractMapValueType(t);
				assertEquals(List.of("? super K", "? super V"), fullTypeString(vt));
				assertEquals(List.of(Optional.of(CharSequence.class), Optional.of(Number.class)), rawType(vt));

				Field extendMap = WildcardsWithParams.class.getDeclaredField("extendMap");
				t = new TypeConstraint(extendMap.getGenericType());
				vt = extractMapValueType(t);
				assertEquals(List.of("? extends K", "? extends V"), fullTypeString(vt));
				assertEquals(List.of(Optional.of(CharSequence.class), Optional.of(Number.class)), rawType(vt));
			}
		}
	}

	private List<TypeConstraint> extractMapValueType(TypeConstraint t) {
		return t.resolveTypeArgumentsFor(Map.class).map(c -> Arrays.asList(c)).orElse(null);
	}

	private List<Type> fullType(List<TypeConstraint> t) {
		return t.stream().map(c -> c.getFullType()).collect(Collectors.toList());
	}

	private List<String> fullTypeString(List<TypeConstraint> t) {
		return t.stream().map(c -> c.getFullType().toString()).collect(Collectors.toList());
	}

	private List<Optional<? extends Type>> rawType(List<TypeConstraint> t) {
		return t.stream().map(c -> c.getSatisfyingRawType()).collect(Collectors.toList());
	}

	static class ObjectA {
		Map<CharSequence, Number> field1;
		NavigableMap<CharSequence, Number> field2;
		HashMap<CharSequence, Number> field3;
		ConcurrentHashMap<CharSequence, Number> field4;
		MyMap1<Number> field5;
		MyMap2 field6;
		MyMap1_1<Number> field7;
		// MyMap1_2 field8;
		MyMap2_1 field9;
		MyMap2_2 field10;
		MyMap3 field11;
		MyMap3_1 field12;
	}

	static class Wildcards {
		Map<? super CharSequence, ? super Number> superMap;
		Map<? extends CharSequence, ? extends Number> extendMap;
		Map rawInterface;
		MyMap1 rawClass;
		Map<?,?> unspecifiedInterface;
		MyMap1<?> unspecifiedClass;
		MyMap1<? extends Number> subExtend;
		MyMap1<? super Number> subSuper;
	}

	static class WildcardsWithParams<K extends CharSequence, V extends Number> {
		Map<? super K, ? super V> superMap;
		Map<? extends K, ? extends V> extendMap;
	}

	static class Params1<A extends Number, B extends CharSequence, C> {
		Map<B, A> map;
	}

	static class Params2<C extends Map<CharSequence, Number>> {
		C map;
	}

	static class MyMap implements Map<CharSequence, Number> {

		@Override
		public void clear() {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean containsKey(Object key) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean containsValue(Object value) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Set<Entry<CharSequence, Number>> entrySet() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Number get(Object key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isEmpty() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Set<CharSequence> keySet() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Number put(CharSequence key, Number value) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void putAll(Map<? extends CharSequence, ? extends Number> m) {
			// TODO Auto-generated method stub

		}

		@Override
		public Number remove(Object key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Collection<Number> values() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	static class MyMap1<V extends Number> implements Map<CharSequence, V> {

		@Override
		public void clear() {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean containsKey(Object key) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean containsValue(Object value) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Set<Entry<CharSequence, V>> entrySet() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public V get(Object key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isEmpty() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Set<CharSequence> keySet() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public V put(CharSequence key, V value) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void putAll(Map<? extends CharSequence, ? extends V> m) {
			// TODO Auto-generated method stub

		}

		@Override
		public V remove(Object key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Collection<V> values() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	static class MyMap2 extends AbstractMap<CharSequence, Number> {

		@Override
		public Set<Entry<CharSequence, Number>> entrySet() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	static class MyMap1_1<V extends Number> extends MyMap1<V> {
	}

	static class MyMap1_2 extends MyMap1_1<Integer> {
	}

	static class MyMap2_1 extends MyMap2 {
	}

	static class MyMap2_2 extends MyMap2_1 {
	}

	static class NonMap {
	}

	static class MyMap3 extends NonMap
			implements Cloneable, Serializable, Map<CharSequence, Number> {

		@Override
		protected Object clone() throws CloneNotSupportedException {
			// TODO Auto-generated method stub
			return super.clone();
		}

		@Override
		public void clear() {
			// TODO Auto-generated method stub

		}

		@Override
		public boolean containsKey(Object key) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public boolean containsValue(Object value) {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Set<Entry<CharSequence, Number>> entrySet() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Number get(Object key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public boolean isEmpty() {
			// TODO Auto-generated method stub
			return false;
		}

		@Override
		public Set<CharSequence> keySet() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Number put(CharSequence key, Number value) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void putAll(Map<? extends CharSequence, ? extends Number> m) {
			// TODO Auto-generated method stub

		}

		@Override
		public Number remove(Object key) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int size() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public Collection<Number> values() {
			// TODO Auto-generated method stub
			return null;
		}

	}

	static class MyMap3_1 extends MyMap3 {
	}
}
