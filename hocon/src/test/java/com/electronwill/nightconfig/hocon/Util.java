package com.electronwill.nightconfig.hocon;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import com.electronwill.nightconfig.core.BasicTestEnum;
import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.TestEnum;

public class Util {
    static void checkExample(CommentedConfig config) {
        assertEquals(" Comment associated to the boolean array\n With multiple lines",
                config.getComment("bool_array"));
        assertEquals(List.of(true, false, true, false), config.get("bool_array"));

        assertEquals(" Comment associated to the string", config.getComment("string"));
        assertEquals("\"value\"", config.get("string"));
        assertEquals(3.1415926535, config.<Double>get("double"));
        assertEquals(2, config.<Integer>get("integer"));

        List<? extends Config> configList = config.get("config_list");
        assertEquals(3, configList.size());
        assertEquals("test", configList.get(0).get("string"));
        assertTrue(configList.get(0).<Config>get("sub").isEmpty());

        assertEquals("test", config.get("config.string"));
        assertEquals(123456789, config.getLong("long"));
        assertEquals("test", config.get("nullSub.a"));
        assertNull(config.get("nullSub.n"));
    }

    static void populateTest(CommentedConfig config) {
        Config subConfig = config.createSubConfig();
        subConfig.set("string", "test");
        subConfig.set("enum", BasicTestEnum.C);
        subConfig.set("sub", config.createSubConfig());

        List<Config> configList = List.of(subConfig, subConfig, subConfig);

        config.set("string", "\"value\"");
        config.set("integer", 2);
        config.set("long", 123456789L);
        config.set("double", 3.1415926535);
        config.set("bool_array", List.of(true, false, true, false));
        config.set("config", subConfig);
        config.set("config_list", configList);
        config.setComment("string", " Comment 1\n Comment 2\n Comment 3");
        config.set("enum", TestEnum.A);
    }

    static final String EXPECTED_SERIALIZED =
        "bool_array: [\n" + //
        "\ttrue, \n" + //
        "\tfalse, \n" + //
        "\ttrue, \n" + //
        "\tfalse\n" + //
        "]\n" + //
        "# Comment 1\n" + //
        "# Comment 2\n" + //
        "# Comment 3\n" + //
        "string: \"\\\"value\\\"\"\n" + //
        "double: 3.1415926535\n" + //
        "integer: 2\n" + //
        "config_list: [\n" + //
        "\t{\n" + //
        "\t\tsub {}\n" + //
        "\t\tstring: test\n" + //
        "\t\tenum: C\n" + //
        "\t}, \n" + //
        "\t{\n" + //
        "\t\tsub {}\n" + //
        "\t\tstring: test\n" + //
        "\t\tenum: C\n" + //
        "\t}, \n" + //
        "\t{\n" + //
        "\t\tsub {}\n" + //
        "\t\tstring: test\n" + //
        "\t\tenum: C\n" + //
        "\t}\n" + //
        "]\n" + //
        "config {\n" + //
        "\tsub {}\n" + //
        "\tstring: test\n" + //
        "\tenum: C\n" + //
        "}\n" + //
        "long: 123456789\n" + //
        "enum: A\n";
}
