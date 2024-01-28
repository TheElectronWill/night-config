package com.electronwill.nightconfig.json;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Arrays;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.TestEnum;

public class Util {
    static void checkExample(Config config) {
        assertEquals("This is a string with a lot of characters to escape \n\r\t \\ \" ",
                config.get("string"));
        assertNull(config.get("null"));
        assertEquals(0.123456, config.<Double>get("double"));
        assertEquals(0.123456, config.<Number>get("float"));
        assertEquals(Arrays.asList(
                "a", "b", 3,
                null,
                true,
                false,
                17.5), config.get("list"));
        assertEquals(true, config.get("config.boolean"));
        assertEquals(false, config.get("config.false"));
        assertEquals("value", config.get(List.of("dots.in.key")));
        assertEquals(123456, config.getInt("int"));
        assertEquals(1234567890L, config.getLong("long"));
        assertEquals("A", config.get("enum"));
    }

    static void populateTest(Config config) {
        Config subConfig = config.createSubConfig();
        subConfig.set("string", "test");
        subConfig.set("null value", null);
        subConfig.set("sub", config.createSubConfig());

        List<Config> arrayOfTables = List.of(subConfig, subConfig, subConfig);

        config.set("string", "\"value\"");
        config.set("integer", 2);
        config.set("long", 123456789L);
        config.set("double", 3.1415926535);
        config.set("bool_array", List.of(true, false, true, false));
        config.set("config", subConfig);
        config.set("table_array", arrayOfTables);
        config.set("table_array2", arrayOfTables);
        config.set("enum", TestEnum.A);
    }

    static final String EXPECTED_SERIALIZED_FANCY = "{\n" + //
            "\t\"bool_array\": [\n" + //
            "\t\ttrue, \n" + //
            "\t\tfalse, \n" + //
            "\t\ttrue, \n" + //
            "\t\tfalse\n" + //
            "\t],\n" + //
            "\t\"table_array2\": [\n" + //
            "\t\t{\n" + //
            "\t\t\t\"sub\": {},\n" + //
            "\t\t\t\"string\": \"test\",\n" + //
            "\t\t\t\"null value\": null\n" + //
            "\t\t}, \n" + //
            "\t\t{\n" + //
            "\t\t\t\"sub\": {},\n" + //
            "\t\t\t\"string\": \"test\",\n" + //
            "\t\t\t\"null value\": null\n" + //
            "\t\t}, \n" + //
            "\t\t{\n" + //
            "\t\t\t\"sub\": {},\n" + //
            "\t\t\t\"string\": \"test\",\n" + //
            "\t\t\t\"null value\": null\n" + //
            "\t\t}\n" + //
            "\t],\n" + //
            "\t\"string\": \"\\\"value\\\"\",\n" + //
            "\t\"double\": 3.1415926535,\n" + //
            "\t\"integer\": 2,\n" + //
            "\t\"table_array\": [\n" + //
            "\t\t{\n" + //
            "\t\t\t\"sub\": {},\n" + //
            "\t\t\t\"string\": \"test\",\n" + //
            "\t\t\t\"null value\": null\n" + //
            "\t\t}, \n" + //
            "\t\t{\n" + //
            "\t\t\t\"sub\": {},\n" + //
            "\t\t\t\"string\": \"test\",\n" + //
            "\t\t\t\"null value\": null\n" + //
            "\t\t}, \n" + //
            "\t\t{\n" + //
            "\t\t\t\"sub\": {},\n" + //
            "\t\t\t\"string\": \"test\",\n" + //
            "\t\t\t\"null value\": null\n" + //
            "\t\t}\n" + //
            "\t],\n" + //
            "\t\"config\": {\n" + //
            "\t\t\"sub\": {},\n" + //
            "\t\t\"string\": \"test\",\n" + //
            "\t\t\"null value\": null\n" + //
            "\t},\n" + //
            "\t\"long\": 123456789,\n" + //
            "\t\"enum\": \"A\"\n" + //
            "}";
    static final String EXPECTED_SERIALIZED_MINIMAL = "{\"bool_array\":[true,false,true,false],\"table_array2\":[{\"sub\":{},\"string\":\"test\",\"null value\":null},{\"sub\":{},\"string\":\"test\",\"null value\":null},{\"sub\":{},\"string\":\"test\",\"null value\":null}],\"string\":\"\\\"value\\\"\",\"double\":3.1415926535,\"integer\":2,\"table_array\":[{\"sub\":{},\"string\":\"test\",\"null value\":null},{\"sub\":{},\"string\":\"test\",\"null value\":null},{\"sub\":{},\"string\":\"test\",\"null value\":null}],\"config\":{\"sub\":{},\"string\":\"test\",\"null value\":null},\"long\":123456789,\"enum\":\"A\"}";
}
