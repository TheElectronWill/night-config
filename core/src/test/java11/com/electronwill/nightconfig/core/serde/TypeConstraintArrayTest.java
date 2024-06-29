package com.electronwill.nightconfig.core.serde;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.AbstractList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class TypeConstraintArrayTest {
    private void checkField(Field f, Type fullType, Optional<Class<?>> rawType) {
        var t = new TypeConstraint(f.getGenericType());
        assertEquals(fullType, t.getFullType());
        assertEquals(rawType, t.getSatisfyingRawType());
    }

    private void checkField(Field f, String fullTypeString, Optional<Class<?>> rawType) {
        var t = new TypeConstraint(f.getGenericType());
        assertEquals(fullTypeString, t.getFullType().toString());
        assertEquals(rawType, t.getSatisfyingRawType());
    }

    @Test
    public void resolveArrayArgumentsFromArraySimple() {
        TypeConstraint t = new TypeConstraint(Object[].class);
        assertEquals(Object[].class, t.getFullType());
        assertEquals(Optional.of(Object[].class), t.getSatisfyingRawType());

        t = new TypeConstraint(CharSequence[].class);
        assertEquals(CharSequence[].class, t.getFullType());
        assertEquals(Optional.of(CharSequence[].class), t.getSatisfyingRawType());

        t = new TypeConstraint(CharSequence[][].class);
        assertEquals(CharSequence[][].class, t.getFullType());
        assertEquals(Optional.of(CharSequence[][].class), t.getSatisfyingRawType());

        t = new TypeConstraint(int[].class);
        assertEquals(int[].class, t.getFullType());
        assertEquals(Optional.of(int[].class), t.getSatisfyingRawType());

        t = new TypeConstraint(int[][].class);
        assertEquals(int[][].class, t.getFullType());
        assertEquals(Optional.of(int[][].class), t.getSatisfyingRawType());
    }

    @Test
    public void resolveArrayArgumentsFromClassTypeArg() throws Exception {
        Class<?> cls = Generic1.class;
        checkField(cls.getDeclaredField("a"), "A[]", Optional.of(Object[].class));
        checkField(cls.getDeclaredField("aa"), "A[][]", Optional.of(Object[][].class));
        checkField(cls.getDeclaredField("aaa"), "A[][][]", Optional.of(Object[][][].class));
    }

    @Test
    public void resolveArrayArgumentsFromGenericParent() throws Exception {
        // TODO add a way to say "I want field a declared in Generic1.class but in Generic1_1.class, so take this
        // class into account to"?
        // checkField(Generic1_1.class.getField("a"), "A[]", Optional.of(CharSequence[].class));
        // checkField(Generic1_1.class.getField("aa"), "A[][]", Optional.of(CharSequence[][].class));
        // checkField(Generic1_1.class.getField("aaa"), "A[][][]", Optional.of(CharSequence[][][].class));
        checkField(Generic1_1.class.getDeclaredField("more"), CharSequence[].class,
                Optional.of(CharSequence[].class));
        checkField(Generic1_2.class.getDeclaredField("more"), "A[]", Optional.of(Object[].class));
        checkField(Generic1_3.class.getDeclaredField("more"), "A[]", Optional.of(CharSequence[].class));
        checkField(Generic1_4.class.getDeclaredField("more"), "A[]", Optional.empty());
        checkField(Generic2.class.getDeclaredField("more"), "A[]", Optional.of(Serializable[].class));
    }

    @Test
    public void resolveMixedArrayAndGenericTypes() throws Exception {
        Class<?> cls = Mixed.class;
        checkField(cls.getDeclaredField("nested"), "java.util.List<X[]>", Optional.of(List.class));
        checkField(cls.getDeclaredField("nestedExtend"), "java.util.List<? extends X[]>", Optional.of(List.class));
        checkField(cls.getDeclaredField("nestedSuper"), "java.util.List<? super X[]>", Optional.of(List.class));

        var custom = cls.getDeclaredField("custom");
        checkField(custom, "com.electronwill.nightconfig.core.serde.TypeConstraintArrayTest$MyListOfString<X>", Optional.of(MyListOfString.class));
        var vt = TypeConstraintCollectionTest.extractCollectionValueType(new TypeConstraint(custom.getGenericType()));
        assertEquals(String[].class, vt.getFullType());
        assertEquals(Optional.of(String[].class), vt.getSatisfyingRawType());
    }

    static class Generic1<A> {
        A[] a;
        A[][] aa;
        A[][][] aaa;
    }

    static final class Generic1_1 extends Generic1<CharSequence> {
        CharSequence[] more;
    }

    static final class Generic1_2<A> extends Generic1<A> {
        A[] more;
    }

    static final class Generic1_3<A extends CharSequence> extends Generic1<A> {
        A[] more;
    }

    static final class Generic1_4<A extends Serializable & Cloneable> extends Generic1<A> {
        A[] more;
    }

    static class Generic2<A extends Serializable> {
        A[] more;
    }

    static final class Generic2_1<T extends Cloneable & Serializable> extends Generic2<T> {
    }

    static final class Mixed<X extends CharSequence> {
        List<X[]> nested;
        List<? extends X[]> nestedExtend;
        List<? super X[]> nestedSuper;
        MyListOfString<X> custom;
    }

    static final class MyListOfString<T> extends AbstractList<String[]> {

        @Override
        public String[] get(int index) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int size() {
            // TODO Auto-generated method stub
            return 0;
        }

    }
}
