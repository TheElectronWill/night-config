package com.electronwill.nightconfig.core.serde;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.TypeVariable;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.junit.jupiter.api.Test;

public class TypeConstraintCollectionTest {
	@Test
	public void resolveCollectionArgumentsFromClassSimple() {
		TypeConstraint t = extractCollectionValueType(new TypeConstraint(MyCollection.class));
		assertEquals(String.class, t.getFullType());
	}

	@Test
	public void resolveCollectionArgumentsFromParentClass() {
		TypeConstraint t = extractCollectionValueType(new TypeConstraint(MyList1.class));
		assertEquals(String.class, t.getFullType());
		t = extractCollectionValueType(new TypeConstraint(MyList2.class));
		assertEquals(String.class, t.getFullType());
		t = extractCollectionValueType(new TypeConstraint(MyList3.class));
		assertEquals(String.class, t.getFullType());
		t = extractCollectionValueType(new TypeConstraint(MyList1_1.class));
		assertEquals(String.class, t.getFullType());
		t = extractCollectionValueType(new TypeConstraint(MyList1_2.class));
		assertEquals(String.class, t.getFullType());
		t = extractCollectionValueType(new TypeConstraint(MyList2_1.class));
		assertEquals(String.class, t.getFullType());
		t = extractCollectionValueType(new TypeConstraint(MyList2_2.class));
		assertEquals(String.class, t.getFullType());
		t = extractCollectionValueType(new TypeConstraint(MyList3_1.class));
		assertEquals(String.class, t.getFullType());
	}

	@Test
	public void resolveCollectionArgumentsFromField() {
		for (Field f : ObjectA.class.getDeclaredFields()) {
			TypeConstraint t = new TypeConstraint(f.getGenericType());
			TypeConstraint valueType = extractCollectionValueType(t);
			assertEquals(String.class, valueType.getFullType());
			assertEquals(Optional.of(String.class), valueType.getSatisfyingRawType());
		}
		for (Field f : Params1.class.getDeclaredFields()) {
			TypeConstraint t = new TypeConstraint(f.getGenericType());
			TypeConstraint valueType = extractCollectionValueType(t);
			assertTrue(valueType.getFullType() instanceof TypeVariable);
			assertEquals(Optional.of(String.class), valueType.getSatisfyingRawType());
		}
		for (Field f : Params2.class.getDeclaredFields()) {
			TypeConstraint t = new TypeConstraint(f.getGenericType());
			TypeConstraint valueType = extractCollectionValueType(t);
			assertEquals(String.class, valueType.getFullType());
			assertEquals(Optional.of(String.class), valueType.getSatisfyingRawType());
		}
	}

	@Test
	public void resolveCollectionArgumentsFromFieldWildcards()
			throws NoSuchFieldException, SecurityException {
		{
			Field superString = Wildcards.class.getDeclaredField("superString");
			TypeConstraint t = new TypeConstraint(superString.getGenericType());
			TypeConstraint vt = extractCollectionValueType(t);
			assertEquals("? super java.lang.String", vt.getFullType().toString());
			assertEquals(Optional.of(String.class), vt.getSatisfyingRawType());

			Field extendString = Wildcards.class.getDeclaredField("extendString");
			t = new TypeConstraint(extendString.getGenericType());
			vt = extractCollectionValueType(t);
			assertEquals("? extends java.lang.String", vt.getFullType().toString());
			assertEquals(Optional.of(String.class), vt.getSatisfyingRawType());
		}

		{
			{
				Field superString = WildcardsWithParams.class.getDeclaredField("superString");
				TypeConstraint t = new TypeConstraint(superString.getGenericType());
				TypeConstraint vt = extractCollectionValueType(t);
				assertEquals("? super T", vt.getFullType().toString());
				assertEquals(Optional.of(String.class), vt.getSatisfyingRawType());

				Field extendString = WildcardsWithParams.class.getDeclaredField("extendString");
				t = new TypeConstraint(extendString.getGenericType());
				vt = extractCollectionValueType(t);
				assertEquals("? extends T", vt.getFullType().toString());
				assertEquals(Optional.of(String.class), vt.getSatisfyingRawType());
			}
		}
	}

	private TypeConstraint extractCollectionValueType(TypeConstraint t) {
		return t.resolveTypeArgumentsFor(Collection.class).map(c -> c[0]).orElse(null);
	}

	static class ObjectA {
		Collection<String> field1;
		List<String> field2;
		ArrayList<String> field3;
		ConcurrentLinkedQueue<String> field4;
		MyList1 field5;
		MyList2 field6;
		MyList1_1 field7;
		MyList1_2 field8;
		MyList2_1 field9;
		MyList2_2 field10;
		MyList3 field11;
		MyList3_1 field12;
	}

	static class Wildcards {
		Collection<? super String> superString;
		Collection<? extends String> extendString;
	}

	static class WildcardsWithParams<T extends String> {
		List<? super T> superString;
		List<? extends T> extendString;
	}

	static class Params1<A, B extends String, C> {
		Collection<B> coll;
	}

	static class Params2<C extends Collection<String>> {
		C coll;
	}

	static class MyCollection implements Collection<String> {

		@Override
		public boolean add(String e) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public boolean addAll(Collection<? extends String> c) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public void clear() {
			// XXX Auto-generated method stub

		}

		@Override
		public boolean contains(Object o) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public boolean isEmpty() {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public Iterator<String> iterator() {
			// XXX Auto-generated method stub
			return null;
		}

		@Override
		public boolean remove(Object o) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public boolean removeAll(Collection<?> c) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public boolean retainAll(Collection<?> c) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public int size() {
			// XXX Auto-generated method stub
			return 0;
		}

		@Override
		public Object[] toArray() {
			// XXX Auto-generated method stub
			return null;
		}

		@Override
		public <T> T[] toArray(T[] a) {
			// XXX Auto-generated method stub
			return null;
		}

	}

	static class MyList1 implements List<String> {

		@Override
		public boolean add(String arg0) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public void add(int arg0, String arg1) {
			// XXX Auto-generated method stub

		}

		@Override
		public boolean addAll(Collection<? extends String> arg0) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public boolean addAll(int arg0, Collection<? extends String> arg1) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public void clear() {
			// XXX Auto-generated method stub

		}

		@Override
		public boolean contains(Object arg0) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> arg0) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public String get(int arg0) {
			// XXX Auto-generated method stub
			return null;
		}

		@Override
		public int indexOf(Object arg0) {
			// XXX Auto-generated method stub
			return 0;
		}

		@Override
		public boolean isEmpty() {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public Iterator<String> iterator() {
			// XXX Auto-generated method stub
			return null;
		}

		@Override
		public int lastIndexOf(Object arg0) {
			// XXX Auto-generated method stub
			return 0;
		}

		@Override
		public ListIterator<String> listIterator() {
			// XXX Auto-generated method stub
			return null;
		}

		@Override
		public ListIterator<String> listIterator(int arg0) {
			// XXX Auto-generated method stub
			return null;
		}

		@Override
		public boolean remove(Object arg0) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public String remove(int arg0) {
			// XXX Auto-generated method stub
			return null;
		}

		@Override
		public boolean removeAll(Collection<?> arg0) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public boolean retainAll(Collection<?> arg0) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public String set(int arg0, String arg1) {
			// XXX Auto-generated method stub
			return null;
		}

		@Override
		public int size() {
			// XXX Auto-generated method stub
			return 0;
		}

		@Override
		public List<String> subList(int arg0, int arg1) {
			// XXX Auto-generated method stub
			return null;
		}

		@Override
		public Object[] toArray() {
			// XXX Auto-generated method stub
			return null;
		}

		@Override
		public <T> T[] toArray(T[] arg0) {
			// XXX Auto-generated method stub
			return null;
		}

	}

	static class MyList2 extends AbstractList<String> {

		@Override
		public String get(int arg0) {
			// XXX Auto-generated method stub
			return null;
		}

		@Override
		public int size() {
			// XXX Auto-generated method stub
			return 0;
		}

	}

	static class MyList1_1 extends MyList1 {
	}

	static class MyList1_2 extends MyList1_1 {
	}

	static class MyList2_1 extends MyList2 {
	}

	static class MyList2_2 extends MyList2_1 {
	}

	static class NonCollection {
	}

	static class MyList3 extends NonCollection
			implements Cloneable, Serializable, Collection<String> {

		@Override
		public boolean add(String arg0) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public boolean addAll(Collection<? extends String> arg0) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public void clear() {
			// XXX Auto-generated method stub

		}

		@Override
		public boolean contains(Object arg0) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public boolean containsAll(Collection<?> arg0) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public boolean isEmpty() {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public Iterator<String> iterator() {
			// XXX Auto-generated method stub
			return null;
		}

		@Override
		public boolean remove(Object arg0) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public boolean removeAll(Collection<?> arg0) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public boolean retainAll(Collection<?> arg0) {
			// XXX Auto-generated method stub
			return false;
		}

		@Override
		public int size() {
			// XXX Auto-generated method stub
			return 0;
		}

		@Override
		public Object[] toArray() {
			// XXX Auto-generated method stub
			return null;
		}

		@Override
		public <T> T[] toArray(T[] arg0) {
			// XXX Auto-generated method stub
			return null;
		}

	}

	static class MyList3_1 extends MyList3 {
	}
}
