package com.electronwill.nightconfig.json;

import com.electronwill.nightconfig.core.impl.Charray;
import org.junit.jupiter.api.Test;

import static com.electronwill.nightconfig.json.JsonToken.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class JsonTokenizerTest {
	@Test
	void test() {
		Charray cha = new Charray("{\"key\": \"value\\n\\t\\u0021\", \"l\": [1, 2.0\n,\rtrue  ,false,\t{\n\t\"n\"\n:null\n}\n,\n-1\n] }");
		JsonTokenizer jt = new JsonTokenizer(cha.asInput());
		assertEquals(OBJECT_START, jt.next()); // { object start (0)
		assertEquals(VALUE_STRING, jt.next());
		assertEquals("key", jt.stringValue());
		assertEquals(KV_SEPARATOR, jt.next());
		assertEquals(VALUE_STRING, jt.next());
		assertEquals("value\n\t!", jt.stringValue());
		assertEquals(ELEMENT_SEPARATOR, jt.next());
		assertEquals(VALUE_STRING, jt.next());
		assertEquals("l", jt.stringValue());
		assertEquals(KV_SEPARATOR, jt.next());

		assertEquals(ARRAY_START, jt.next()); // [ array start (A)
		assertEquals(VALUE_INTEGER, jt.next());
		assertEquals(1, jt.intValue());
		assertEquals(ELEMENT_SEPARATOR, jt.next());

		assertEquals(VALUE_FLOATING, jt.next());
		assertEquals(2.0, jt.doubleValue());
		assertEquals(2, jt.intValue());
		assertEquals(ELEMENT_SEPARATOR, jt.next());

		assertEquals(VALUE_TRUE, jt.next());
		assertEquals(ELEMENT_SEPARATOR, jt.next());

		assertEquals(VALUE_FALSE, jt.next());
		assertEquals(ELEMENT_SEPARATOR, jt.next());

		assertEquals(OBJECT_START, jt.next()); // { object start (1)
		assertEquals(VALUE_STRING, jt.next());
		assertEquals("n", jt.stringValue());
		assertEquals(KV_SEPARATOR, jt.next());
		assertEquals(VALUE_NULL, jt.next());
		assertEquals(OBJECT_END, jt.next()); // } object end (1)

		assertEquals(ELEMENT_SEPARATOR, jt.next());
		assertEquals(VALUE_INTEGER, jt.next());
		assertEquals(-1, jt.intValue());
		assertEquals(ARRAY_END, jt.next()); // ] array end (A)

		assertEquals(OBJECT_END, jt.next()); // } object end (0)

		assertEquals(END_OF_DATA, jt.next());
		assertEquals(END_OF_DATA, jt.next());
		assertEquals(END_OF_DATA, jt.next());
	}
}