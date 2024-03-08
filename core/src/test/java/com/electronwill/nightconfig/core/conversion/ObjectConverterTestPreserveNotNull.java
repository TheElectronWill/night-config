package com.electronwill.nightconfig.core.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.Config;

public class ObjectConverterTestPreserveNotNull {

    @PreserveNotNull
    static class Foo {
        String fooValue = "FooDefault";

        Bar bar = new Bar();

		transient String ignore = "ignore me";
		static String ignore2 = "ignore me too";

        @Override
        public String toString() {
            return "Foo{" + "fooValue='" + fooValue + '\'' + ", bar=" + bar + '}';
        }

        @PreserveNotNull
        static class Bar {

            String barValue = "BarDefault";

            @Override
            public String toString() {
                return "Bar{" + "barValue='" + barValue + '\'' + '}';
            }

        }
    }

	@Test
	public void testPreserveNotNull() {
		Config config = Config.inMemory();
        config.set("fooValue", "FooValue");
        config.set("bar.barValue", "BarValue");

        Foo object = new ObjectConverter().toObject(config, Foo::new);
		assertEquals("FooValue", object.fooValue);
		assertEquals("BarValue", object.bar.barValue);

		config.remove("fooValue");
		object = new ObjectConverter().toObject(config, Foo::new);
		assertEquals("FooDefault", object.fooValue);
		assertEquals("BarValue", object.bar.barValue);

		config.remove("bar");
		object = new ObjectConverter().toObject(config, Foo::new);
		assertEquals("FooDefault", object.fooValue);
		assertEquals("BarDefault", object.bar.barValue);

		config.set("bar.barValue", null);
		object = new ObjectConverter().toObject(config, Foo::new);
		assertEquals("FooDefault", object.fooValue);
		assertEquals("BarDefault", object.bar.barValue);

		config.set("fooValue", "FooValue");
		object = new ObjectConverter().toObject(config, Foo::new);
		assertEquals("FooValue", object.fooValue);
		assertEquals("BarDefault", object.bar.barValue);

		Config conf2 = new ObjectConverter().toConfig(object, Config::inMemory);
		Config expected = Config.inMemory();
		expected.set("fooValue", "FooValue");
		expected.set("bar.barValue", "BarDefault");
		assertEquals(expected, conf2);
	}

    static class Foo2 {
		@PreserveNotNull
        String fooValue = "FooDefault";

		@PreserveNotNull
        Bar2 bar = new Bar2();

        @Override
        public String toString() {
            return "Foo{" + "fooValue='" + fooValue + '\'' + ", bar=" + bar + '}';
        }

        static class Bar2 {
			@PreserveNotNull
            String barValue = "BarDefault";

            @Override
            public String toString() {
                return "Bar{" + "barValue='" + barValue + '\'' + '}';
            }

        }
    }

	@Test
	public void testPreserveNotNull2() {
		Config config = Config.inMemory();
        config.set("fooValue", "FooValue");
        config.set("bar.barValue", "BarValue");

        Foo2 object = new ObjectConverter().toObject(config, Foo2::new);
		assertEquals("FooValue", object.fooValue);
		assertEquals("BarValue", object.bar.barValue);

		config.remove("fooValue");
		object = new ObjectConverter().toObject(config, Foo2::new);
		assertEquals("FooDefault", object.fooValue);
		assertEquals("BarValue", object.bar.barValue);

		config.remove("bar");
		object = new ObjectConverter().toObject(config, Foo2::new);
		assertEquals("FooDefault", object.fooValue);
		assertEquals("BarDefault", object.bar.barValue);

		config.set("bar.barValue", null);
		object = new ObjectConverter().toObject(config, Foo2::new);
		assertEquals("FooDefault", object.fooValue);
		assertEquals("BarDefault", object.bar.barValue);

		config.set("fooValue", "FooValue");
		object = new ObjectConverter().toObject(config, Foo2::new);
		assertEquals("FooValue", object.fooValue);
		assertEquals("BarDefault", object.bar.barValue);
	}
}
