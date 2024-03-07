package com.electronwill.nightconfig.core.conversion;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.electronwill.nightconfig.core.Config;

public class ObjectConverterTestPreserveNotNull {

    @PreserveNotNull
    static class Foo {
        String fooValue = "FooDefault";

        Bar bar = new Bar();

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
	}

    // OUTPUT > Object: Foo{fooValue='FooValue', bar=Bar{barValue='BarDefault'}}
}
